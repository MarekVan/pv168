package pv168;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static pv168.AccountManagerImpl.*;

/**
 * Created by xvancik on 3/8/16.
 */
public class PaymentManagerImpl implements PaymentManager {

    private final DataSource dataSource;

    public PaymentManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createPayment(Payment payment) {

        checkPaymentForCreatePayment(payment);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement(
            "INSERT INTO PAYMENT (amount, fromAcc, toAcc, dateSent) VALUES (?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)
        ) {

        createPaymentInnerProcess(st, payment);
        
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting payment " + payment, ex);
        }
    }
    
    @Override
    public void createPayment(Payment payment, Connection con){
    
        checkPaymentForCreatePayment(payment);

        try (PreparedStatement st = con.prepareStatement(
             "INSERT INTO PAYMENT (amount, fromAcc, toAcc, dateSent) VALUES (?,?,?,?)",
             Statement.RETURN_GENERATED_KEYS)
        ) {

        createPaymentInnerProcess(st, payment);

        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting payment " + payment, ex);
        }   
    
    }

    @Override
    public void deletePayment(Payment payment) throws ServiceFailureException {
        
        checkPaymentForDeletePayment(payment);
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
             "DELETE FROM payment WHERE id = ?")
        ) {

        deletePaymentInnerProcess(st, payment);
        
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }
    
    @Override
    public void deletePayment(Payment payment, Connection con) throws ServiceFailureException {
        
        checkPaymentForDeletePayment(payment);
        
        try (PreparedStatement st = con.prepareStatement(
             "DELETE FROM payment WHERE id = ?")
        ) {

        deletePaymentInnerProcess(st, payment);
        
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }    

    @Override
    public void updatePayment(Payment payment) throws ServiceFailureException {

        checkPaymentForUpdatePayment(payment);
        
        try (Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement(
            "UPDATE Payment SET amount = ?, fromAcc = ?, toAcc = ?, dateSent = ? WHERE id = ?")
        ) {

        updatePaymentInnerProcess(st, payment);
        
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }
    
    @Override
    public void updatePayment(Payment payment, Connection con) throws ServiceFailureException {

        checkPaymentForUpdatePayment(payment);
        
        try (PreparedStatement st = con.prepareStatement(
            "UPDATE Payment SET amount = ?, fromAcc = ?, toAcc = ?, dateSent = ? WHERE id = ?")
        ) {

        updatePaymentInnerProcess(st, payment);
        
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }    

    @Override
    public Payment findPaymentById(Long id) throws ServiceFailureException {
        
        checkIdForFindPaymentById(id);
        
        try (Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement(
            "SELECT * FROM payment WHERE id = ?")
        ) {

        return findPaymentByIdInnerProcess(st, id, connection);
        
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving payment with id " + id, ex);
        }
    }
    
    @Override
    public Payment findPaymentById(Long id, Connection con) throws ServiceFailureException {
        
        checkIdForFindPaymentById(id);
        
        try (PreparedStatement st = con.prepareStatement(
            "SELECT * FROM payment WHERE id = ?")
        ) {

        return findPaymentByIdInnerProcess(st, id, con);
        
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving payment with id " + id, ex);
        }
    }    

    @Override
    public List<Payment> findAllPayments() throws ServiceFailureException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement(
            "SELECT * FROM payment")) {

        return findAllPaymentsInnerProcess(st, connection);

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all payments", ex);
        }

    }
    
    @Override
    public List<Payment> findAllPayments(Connection con) throws ServiceFailureException {
        try (PreparedStatement st = con.prepareStatement(
            "SELECT * FROM payment")) {

        return findAllPaymentsInnerProcess(st, con);

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all payments", ex);
        }

    }    
    
//------------------------------------------------------------------------------

    public static Long getKey(ResultSet keyRS, Payment payment) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert payment " + payment
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert payment " + payment
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert payment " + payment
                    + " - no key found");
        }
    }

    public static Payment resultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        Date date = new Date(rs.getTimestamp("dateSent").getTime());
        payment.setId(rs.getLong("id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setSent(date);
        return payment;
    }
    
//------------------------------------------------------------------------------
    
    private void validate(Payment payment) throws IllegalArgumentException {
        if (payment == null) {
            throw new IllegalArgumentException("payment is null");
        }
        if (payment.getAmount() == null) {
            throw new IllegalArgumentException("amount is null");
        }
        if (payment.getAmount().compareTo(new BigDecimal(0)) < 0) {
            throw new IllegalArgumentException("amount is negative number");
        }
        if (payment.getFrom() == null) {
            throw new IllegalArgumentException("account from is null");
        }
        if (payment.getTo() == null) {
            throw new IllegalArgumentException("account to is null");
        }
        if (payment.getSent() == null) {
            throw new IllegalArgumentException("date is null");
        }
    }
    
    private void findAccountsToPayment(Payment payment, Connection connection, Long fromAcc, Long toAcc) {

        try (PreparedStatement stFrom = connection.prepareStatement(""
                + "SELECT * FROM account WHERE id = ?");
             PreparedStatement stTo = connection.prepareStatement(""
                     + "SELECT * FROM account WHERE id = ?");
                

        ) {
            stFrom.setLong(1, fromAcc);
            ResultSet rsFrom = stFrom.executeQuery();
            stTo.setLong(1, toAcc);
            ResultSet rsTo = stTo.executeQuery();

            if (rsFrom.next()) {
                Account from = resultSetToAccount(rsFrom);
                payment.setFrom(from);
                if (rsFrom.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More accounts with same id found");
                }
            }
            if (rsTo.next()) {
                Account to = resultSetToAccount(rsTo);
                payment.setTo(to);
                if (rsTo.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More accounts with same id found");
                }
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving accounts to payment with id " + payment.getId(), ex);
        }

    }
    
    private void checkPaymentForCreatePayment(Payment payment){
        validate(payment);
        if (payment.getId() != null) {
            throw new IllegalArgumentException("payment id is already set");
        }   
    }
    
    private void createPaymentInnerProcess(PreparedStatement prepStatement, Payment payment) throws SQLException{
            
            prepStatement.setBigDecimal(1, payment.getAmount());
            prepStatement.setLong(2, payment.getFrom().getId());
            prepStatement.setLong(3, payment.getTo().getId());
            prepStatement.setTimestamp(4, new java.sql.Timestamp(payment.getSent().getTime()));
            int addedRows = prepStatement.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert payment " + payment);
            }

            ResultSet keyRS = prepStatement.getGeneratedKeys();
            payment.setId(getKey(keyRS, payment));    
    
    }

    private void checkPaymentForDeletePayment(Payment payment){

        if (payment == null) {
            throw new IllegalArgumentException("payment is null");
        }
        if (payment.getId() == null) {
            throw new IllegalArgumentException("payment id is null");
        }
    }

    private void deletePaymentInnerProcess(PreparedStatement prepStatement, Payment payment) throws SQLException{
            prepStatement.setLong(1, payment.getId());

            int count = prepStatement.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Payment " + payment + " was not found in database!");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid deleted rows count detected (one row should be updated): " + count);
            }    
    }
    
    private void checkPaymentForUpdatePayment(Payment payment){
        validate(payment);
        if (payment.getId() == null) {
            throw new IllegalArgumentException("payment id is null");
        }    
    }
    
    private void updatePaymentInnerProcess(PreparedStatement prepStatement, Payment payment) throws SQLException{
            prepStatement.setBigDecimal(1, payment.getAmount());
            prepStatement.setLong(2, payment.getFrom().getId());
            prepStatement.setLong(3, payment.getTo().getId());
            prepStatement.setTimestamp(4, new java.sql.Timestamp(payment.getSent().getTime()));
            prepStatement.setLong(5, payment.getId());

            int count = prepStatement.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Payment " + payment + " was not found in database!");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid updated rows count detected (one row should be updated): " + count);
            }    
    }
    
    private void checkIdForFindPaymentById(Long id){
        if(id == null){
            throw new IllegalArgumentException("Id must not be null!");
        }
    }
    
    private Payment findPaymentByIdInnerProcess(PreparedStatement prepStatement, Long id, Connection con) throws SQLException{
            prepStatement.setLong(1, id);
            ResultSet rs = prepStatement.executeQuery();

            if (rs.next()) {
                Payment payment = resultSetToPayment(rs);
                findAccountsToPayment(payment, con, rs.getLong("fromAcc"), rs.getLong("toAcc"));
                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + payment + " and " + resultSetToPayment(rs));
                }
                return payment;
            } else {
                return null;
            }    
    }
    
    private List<Payment> findAllPaymentsInnerProcess(PreparedStatement prepStatement, Connection con) throws SQLException{
            
        ResultSet rs = prepStatement.executeQuery();

        List<Payment> result = new ArrayList<>();
        while (rs.next()) {
            Payment current = resultSetToPayment(rs);
            findAccountsToPayment(current, con, rs.getLong("fromAcc"), rs.getLong("toAcc"));
            result.add(current);
        }
            return result;    
    }
}
