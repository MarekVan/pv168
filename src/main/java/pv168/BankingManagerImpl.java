package pv168;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

/**
 * Created by xvancik on 3/8/16.
 */
public class BankingManagerImpl implements BankingManager {

    private final DataSource dataSource;
    private AccountManager accManager;
    private PaymentManager payManager;

    public BankingManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        accManager = new AccountManagerImpl(dataSource);
        payManager = new PaymentManagerImpl(dataSource);
    }

    @Override
    public void executePayment(Payment payment) {

        if (payment == null){
            throw new IllegalArgumentException("Payment is null!");
        }
        if (payment.getId() != null) {
            throw new IllegalArgumentException("Payment cannot have set id before execution!");
        }
        if (payment.getFrom() == null) {
            throw new IllegalArgumentException("Sender must be specified to execute a payment!");
        }
        if (payment.getTo() == null) {
            throw new IllegalArgumentException("Reciever must be specified to execute a payment!");
        }
        if (payment.getAmount() == null) {
            throw new IllegalArgumentException("Ammount of money must be specified for payment!");
        }
        if (payment.getSent() != null) {
            throw new IllegalArgumentException("Timestamp of payment is assigned during this method!");
        }
        if (payment.getFrom() == payment.getTo()){
            throw new IllegalArgumentException("Sender and receiver are the same accounts!");
        }
        if (payment.getFrom().getBalance().compareTo(payment.getAmount()) < 0) {
            throw new InsufficientBalanceException("The sending account does not have enough money for the payment!");
        }


        try (Connection connection = dataSource.getConnection();
        ) {
            connection.setAutoCommit(false);

            payment.getFrom().setBalance(payment.getFrom().getBalance().subtract(payment.getAmount()));
            payment.getTo().setBalance(payment.getTo().getBalance().add(payment.getAmount()));

            payment.setSent(new Date());

            try {

                accManager.updateAccount(payment.getFrom(), connection);
                accManager.updateAccount(payment.getTo(), connection);

                payManager.createPayment(payment, connection);

                connection.commit();
                connection.setAutoCommit(true);

            } catch (Exception ex) {
                try {
                    connection.rollback();
                } catch (SQLException ex1) {
                    ex1.addSuppressed(ex);
                }
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex2) {
                    ex2.addSuppressed(ex);
                }
                
                payment.setSent(null);
                payment.setId(null);

                payment.getFrom().setBalance(payment.getFrom().getBalance().add(payment.getAmount()));
                payment.getTo().setBalance(payment.getTo().getBalance().subtract(payment.getAmount()));
                throw ex;
            }


        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to execute payment " + payment, ex);
        }

    }


    @Override
    public List<Payment> findAllIncomingPaymentsToAccount(Account account) {

        validateAccount(account);
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement incomingPayments = connection.prepareStatement("SELECT * FROM payment WHERE toAcc = ?");
        ) {

            return processStatementToList(incomingPayments, account, connection);

        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to retrieve incoming payments to account " + account, ex);
        }

    }

    @Override
    public List<Payment> findOutgoingPaymentsToAccount(Account account) {

        validateAccount(account);
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement incomingPayments = connection.prepareStatement("SELECT * FROM payment WHERE fromAcc = ?");
        ) {

            return processStatementToList(incomingPayments, account, connection);

        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to retrieve outgoing payments from account " + account, ex);
        }
    }

    private List<Payment> processStatementToList(PreparedStatement stmt, Account account, Connection con) throws SQLException {
        if(accManager.findAccountById(account.getId()) == null){
        throw new EntityNotFoundException("Specified account does not exist!");
        }
        
        stmt.setLong(1, account.getId());

        ResultSet result = stmt.executeQuery();

        List<Payment> resultList = new ArrayList<>();

        while (result.next()) {
            resultList.add(PaymentManagerImpl.resultSetToPayment(result, con));
        }

        return resultList;
    }
    
    private void validateAccount(Account account){
         if(account == null){
            throw new IllegalArgumentException("Passed account is null!");
        }
        if(account.getId() == null){
            throw new IllegalArgumentException("Passed account.id is null!");
        }
        if(account.getOwner() == null){
            throw new IllegalArgumentException("Passed account.owner is null!");
        }
        if(account.getBalance() == null){
            throw new IllegalArgumentException("Passed account.balance is null!");
        }   
    }
}
