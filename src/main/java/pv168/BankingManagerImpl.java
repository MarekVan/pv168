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
    
    public BankingManagerImpl(DataSource dataSource){
    this.dataSource = dataSource;
    }

    @Override
    public void executePayment(Payment payment){
        
        if(payment.getId() != null){
            throw new IllegalArgumentException("Payment cannot have set id before execution!");
        }
        if(payment.getFrom() == null){
            throw new IllegalArgumentException("Sender must be specified to execute a payment!");
        }
        if(payment.getTo() == null){
            throw new IllegalArgumentException("Reciever must be specified to execute a payment!");
        }
        if(payment.getAmount() == null){
            throw new IllegalArgumentException("Ammount of money must be specified for payment!");
        }
        if(payment.getSent() != null){
            throw new IllegalArgumentException("Timestamp of payment is assigned during this method!");
        }       
        if( payment.getFrom().getBalance().compareTo(payment.getAmount()) < 0 ) {
            throw new InsufficientBalanceException("The sending account does not have enough money for the payment!");
        }
        
        
        
        try(Connection connection = dataSource.getConnection();
        ){
           connection.setAutoCommit(false);
            
           payment.getFrom().setBalance(payment.getFrom().getBalance().subtract(payment.getAmount()));
           payment.getTo().setBalance(payment.getTo().getBalance().add(payment.getAmount()));
            
           payment.setSent(new Date());
           
           try{
           
           accManager.updateAccount(payment.getFrom(), connection);
           accManager.updateAccount(payment.getTo(), connection);
           
           payManager.createPayment(payment, connection);
           
           connection.commit();
           connection.setAutoCommit(true);
           
           } catch(ServiceFailureException ex){
           connection.rollback();
           
           payment.setSent(null);
           payment.setId(null);
           
           payment.getFrom().setBalance(payment.getFrom().getBalance().add(payment.getAmount()));
           payment.getTo().setBalance(payment.getTo().getBalance().subtract(payment.getAmount()));         
           }
//            try(PreparedStatement updateAccount = connection.prepareStatement("UPDATE account SET balance = ? WHERE id = ?");
//                PreparedStatement createPayment = connection.prepareStatement("INSERT INTO payment VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
//            ){
//            
//            
//            
//            
//            updateAccount.setBigDecimal(1, payment.getFrom().getBalance().subtract(payment.getAmount()));
//            updateAccount.setLong(2, payment.getFrom().getId());
//            
//            int updated = updateAccount.executeUpdate();
//            
//            if(updated == 0){
//                connection.rollback();
//                throw new ServiceFailureException("No affected account, account with specified id probably doues not exist!");
//            }
//            if(updated != 1){
//                connection.rollback();
//                throw new ServiceFailureException("Update affected more than one row!");
//            }
//            
//            updateAccount.setBigDecimal(1, payment.getTo().getBalance().add(payment.getAmount()));
//            updateAccount.setLong(2, payment.getTo().getId());
//            
//            updated = updateAccount.executeUpdate();
//            
//            if(updated == 0){
//                connection.rollback();
//                throw new ServiceFailureException("No affected account, account with specified id probably doues not exist!");
//            }
//            if(updated != 1){
//                connection.rollback();
//                throw new ServiceFailureException("Update affected more than one row!");
//            }
//            
//            
//            createPayment.setBigDecimal(1, payment.getAmount());
//            createPayment.setLong(2, payment.getFrom().getId());
//            createPayment.setLong(3, payment.getTo().getId());
//            
//            Date date = new Date();
//            createPayment.setTimestamp(4, new java.sql.Timestamp(date.getTime()));
//            
//            int added = createPayment.executeUpdate();
//            
//            if(added != 1){
//                connection.rollback();
//                throw new ServiceFailureException("Insertion affected more than one row, " + added + " rows affected!");
//            }
//            
//            ResultSet keys = createPayment.getGeneratedKeys();
//            
//            connection.commit();
//            connection.setAutoCommit(true);
//            
//            
//            payment.setId(PaymentManagerImpl.getKey(keys, payment));
//            payment.setSent(date);
//            
//            payment.getFrom().setBalance(payment.getFrom().getBalance().subtract(payment.getAmount()));
//            payment.getTo().setBalance(payment.getTo().getBalance().add(payment.getAmount()));
//    
////I had to use nested try-with-resources block to be able to call rollback in case of a fail
//            
//            } catch(SQLException ex) {
//                connection.rollback();
//                throw new ServiceFailureException("Failed to execute payment " + payment, ex);
//            }
        
        
        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to execute payment " + payment, ex);
        }
        
       }
    
    
    @Override
    public List<Payment> findAllIncomingPaymentsToAccount(Account account){
        
        try(Connection connection = dataSource.getConnection();
            PreparedStatement incomingPayments = connection.prepareStatement("SELECT * FROM payment WHERE toAcc = ?");
        ){
            
            return processStatementToList(incomingPayments, account);   
               
        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to retrieve incoming payments to account " + account, ex);
        }
        
    }
    @Override
    public List<Payment> findOutgoingPaymentsToAccount(Account account){
        
        try(Connection connection = dataSource.getConnection();
            PreparedStatement incomingPayments = connection.prepareStatement("SELECT * FROM payment WHERE fromAcc = ?");
        ){
            
           return processStatementToList(incomingPayments, account);
               
        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to retrieve outgoing payments from account " + account, ex);
        }
    }
    
    private static List<Payment> processStatementToList(PreparedStatement stmt, Account account) throws SQLException{
            stmt.setLong(1, account.getId());
            
            ResultSet result = stmt.executeQuery();
            
            List<Payment> resultList = new ArrayList<>();
            
            while(result.next()){
            resultList.add(PaymentManagerImpl.resultSetToPayment(result));
            }
            
            return resultList;    
    }
}
