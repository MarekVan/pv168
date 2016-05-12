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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xvancik on 3/8/16.
 */
public class BankingManagerImpl implements BankingManager {

    private final DataSource dataSource;
    private AccountManager accManager;
    private PaymentManager payManager;
    final static Logger log = LoggerFactory.getLogger(PaymentManagerImpl.class);

    public BankingManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        accManager = new AccountManagerImpl(dataSource);
        payManager = new PaymentManagerImpl(dataSource);
    }

    @Override
    public void executePayment(Payment payment) {

        if (payment == null) {
            log.warn("Operation failed: payment is null!");
            throw new IllegalArgumentException("Payment is null!");
        }
        if (payment.getId() != null) {
            log.warn("Operation failed: id is null!");
            throw new IllegalArgumentException("Payment cannot have set id before execution!");
        }
        if (payment.getFrom() == null) {
            log.warn("Operation failed: from account is null!");
            throw new IllegalArgumentException("Sender must be specified to execute a payment!");
        }
        if (payment.getTo() == null) {
            log.warn("Operation failed: to account is null!");
            throw new IllegalArgumentException("Reciever must be specified to execute a payment!");
        }
        if (payment.getAmount() == null) {
            log.warn("Operation failed: amount is null!");
            throw new IllegalArgumentException("Ammount of money must be specified for payment!");
        }
        if (payment.getSent() != null) {
            log.warn("Operation failed: date is null!");
            throw new IllegalArgumentException("Timestamp of payment is assigned during this method!");
        }
        if (payment.getFrom().equals(payment.getTo())) {
            log.warn("Operation failed: sender and receiver are the same accounts!");
            throw new IllegalArgumentException("Sender and receiver are the same accounts!");
        }

        if (payment.getFrom().getBalance().compareTo(payment.getAmount()) < 0) {
            log.warn("Operation failed: sending account does not have enough money for the payment {} !", payment);
            throw new InsufficientBalanceException("The sending account does not have enough money for the payment!");
        }

        try (Connection connection = dataSource.getConnection();) {
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
                    log.error("Error during rollback!");
                    ex1.addSuppressed(ex);
                }
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex2) {
                    log.error("Error while enabling autocommit!");
                    ex2.addSuppressed(ex);
                }

                payment.setSent(null);
                payment.setId(null);

                payment.getFrom().setBalance(payment.getFrom().getBalance().add(payment.getAmount()));
                payment.getTo().setBalance(payment.getTo().getBalance().subtract(payment.getAmount()));
                throw ex;
            }

        } catch (SQLException ex) {
            log.error("Failed to execute payment {}", payment);
            throw new ServiceFailureException("Failed to execute payment " + payment, ex);
        }

    }

    @Override
    public List<Payment> findAllIncomingPaymentsToAccount(Account account) {

        validateAccount(account);

        try (Connection connection = dataSource.getConnection();
                PreparedStatement incomingPayments = connection.prepareStatement("SELECT * FROM payment WHERE toAcc = ?");) {

            return processStatementToList(incomingPayments, account, connection);

        } catch (SQLException ex) {
            log.error("Failed to retrieve incoming payments to account {}", account);
            throw new ServiceFailureException("Failed to retrieve incoming payments to account " + account, ex);
        }

    }

    @Override
    public List<Payment> findOutgoingPaymentsToAccount(Account account) {

        validateAccount(account);

        try (Connection connection = dataSource.getConnection();
                PreparedStatement incomingPayments = connection.prepareStatement("SELECT * FROM payment WHERE fromAcc = ?");) {

            return processStatementToList(incomingPayments, account, connection);

        } catch (SQLException ex) {
            log.error("Failed to retrieve incoming payments from account {}", account);
            throw new ServiceFailureException("Failed to retrieve outgoing payments from account " + account, ex);
        }
    }

    private List<Payment> processStatementToList(PreparedStatement stmt, Account account, Connection con) throws SQLException {
        if (accManager.findAccountById(account.getId()) == null) {
            log.error("Entity not found {}", account);
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

    private void validateAccount(Account account) {
        if (account == null) {
            log.warn("Operation failed: account is null!");
            throw new IllegalArgumentException("Passed account is null!");
        }
        if (account.getId() == null) {
            log.warn("Operation failed: id is null!");
            throw new IllegalArgumentException("Passed account.id is null!");
        }
        if (account.getOwner() == null) {
            log.warn("Operation failed: owner is null!");
            throw new IllegalArgumentException("Passed account.owner is null!");
        }
        if (account.getBalance() == null) {
             log.warn("Operation failed: balance is null!");
            throw new IllegalArgumentException("Passed account.balance is null!");
        }
    }
}
