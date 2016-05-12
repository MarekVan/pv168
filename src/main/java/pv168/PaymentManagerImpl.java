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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pv168.AccountManagerImpl.*;

/**
 * Created by xvancik on 3/8/16.
 */
public class PaymentManagerImpl implements PaymentManager {

    private final DataSource dataSource;
    final static Logger log = LoggerFactory.getLogger(PaymentManagerImpl.class);

    public PaymentManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createPayment(Payment payment) throws ServiceFailureException {

        checkPaymentForCreatePayment(payment);

        try (Connection connection = dataSource.getConnection();) {
            createPaymentInnerProcess(connection, payment);
            log.info("payment {} was created", payment);
        } catch (SQLException ex) {
            log.error("payment {} coudld not be created", payment);
            throw new ServiceFailureException("Error when inserting payment " + payment, ex);
        }
    }

    @Override
    public void createPayment(Payment payment, Connection con) throws ServiceFailureException {
        checkPaymentForCreatePayment(payment);
        createPaymentInnerProcess(con, payment);
        log.info("payment {} was created", payment);
    }

    @Override
    public void deletePayment(Payment payment) throws ServiceFailureException {

        checkPaymentForDeletePayment(payment);

        try (Connection connection = dataSource.getConnection();) {
            deletePaymentInnerProcess(connection, payment);
            log.info("payment {} was deleted", payment);
        } catch (SQLException ex) {
            log.info("payment {} coudld not be deleted", payment);
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }

    @Override
    public void deletePayment(Payment payment, Connection con) throws ServiceFailureException {
        checkPaymentForDeletePayment(payment);
        deletePaymentInnerProcess(con, payment);
        log.info("payment {} was deleted", payment);
    }

    @Override
    public void updatePayment(Payment payment) throws ServiceFailureException {

        checkPaymentForUpdatePayment(payment);

        try (Connection connection = dataSource.getConnection();) {
            updatePaymentInnerProcess(connection, payment);
            log.info("payment {} was updated", payment);
        } catch (SQLException ex) {
            log.info("payment {} coudld not updated", payment);
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }

    @Override
    public void updatePayment(Payment payment, Connection con) throws ServiceFailureException {
        checkPaymentForUpdatePayment(payment);
        updatePaymentInnerProcess(con, payment);
        log.info("payment {} was updated", payment);
    }

    @Override
    public Payment findPaymentById(Long id) throws ServiceFailureException {

        checkIdForFindPaymentById(id);

        try (Connection connection = dataSource.getConnection();) {
            return findPaymentByIdInnerProcess(id, connection);
        } catch (SQLException ex) {
            log.error("payment with id {} could not be find", id);
            throw new ServiceFailureException(
                    "Error when retrieving payment with id " + id, ex);
        }
    }

    @Override
    public Payment findPaymentById(Long id, Connection con) throws ServiceFailureException {
        checkIdForFindPaymentById(id);
        return findPaymentByIdInnerProcess(id, con);
    }

    @Override
    public List<Payment> findAllPayments() throws ServiceFailureException {
        try (Connection connection = dataSource.getConnection()) {
            return findAllPaymentsInnerProcess(connection);
        } catch (SQLException ex) {
            log.error("Failed to retrieve all payments record");
            throw new ServiceFailureException(
                    "Error when retrieving all payments", ex);
        }
    }

    @Override
    public List<Payment> findAllPayments(Connection con) throws ServiceFailureException {
        return findAllPaymentsInnerProcess(con);
    }

//------------------------------------------------------------------------------
    public static Long getKey(ResultSet keyRS, Payment payment) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                log.warn("Internal Error: generated keys does not contain one collumn. Occured after inserting payment {}", payment);
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert payment " + payment
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                log.warn("Internal Error: More than one key was retrieved. Occured after inserting payment {}", payment);
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert payment " + payment
                        + " - more keys found");
            }
            return result;
        } else {
            log.warn("Internal Error: No key retrieved. Occured after inserting payment {}", payment);
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert payment " + payment
                    + " - no key found");
        }
    }

    public static Payment resultSetToPayment(ResultSet rs, Connection con) throws SQLException {
        Payment payment = new Payment();
        Date date = new Date(rs.getTimestamp("dateSent").getTime());
        payment.setId(rs.getLong("id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setSent(date);

        Long fromId = rs.getLong("fromAcc");
        Long toId = rs.getLong("toAcc");

        findAccountsToPayment(payment, con, fromId, toId);
        return payment;
    }

    private static void findAccountsToPayment(Payment payment, Connection connection, Long fromAcc, Long toAcc) {

        try (PreparedStatement stFrom = connection.prepareStatement(""
                + "SELECT * FROM account WHERE id = ?");
                PreparedStatement stTo = connection.prepareStatement(""
                        + "SELECT * FROM account WHERE id = ?");) {
            stFrom.setLong(1, fromAcc);
            ResultSet rsFrom = stFrom.executeQuery();
            stTo.setLong(1, toAcc);
            ResultSet rsTo = stTo.executeQuery();

            if (rsFrom.next()) {
                Account from = resultSetToAccount(rsFrom);
                payment.setFrom(from);
                if (rsFrom.next()) {
                    log.error("Internal error: More accounts with same id found");
                    throw new ServiceFailureException(
                            "Internal error: More accounts with same id found");
                }
            }
            if (rsTo.next()) {
                Account to = resultSetToAccount(rsTo);
                payment.setTo(to);
                if (rsTo.next()) {
                    log.error("Internal error: More accounts with same id found");
                    throw new ServiceFailureException(
                            "Internal error: More accounts with same id found");
                }
            }
        } catch (SQLException ex) {
            log.error("rror when retrieving accounts to payment with id {}", payment.getId());
            throw new ServiceFailureException(
                    "Error when retrieving accounts to payment with id " + payment.getId(), ex);
        }

    }

//------------------------------------------------------------------------------
    private void validate(Payment payment) throws IllegalArgumentException {
        if (payment == null) {
            log.warn("Operation failed: passed payment is null!");
            throw new IllegalArgumentException("payment is null");
        }
        if (payment.getAmount() == null) {
            log.warn("Operation failed: amount is null!");
            throw new IllegalArgumentException("amount is null");
        }
        if (payment.getAmount().compareTo(new BigDecimal(0)) < 0) {
            log.warn("Operation failed: amount is negative!");
            throw new IllegalArgumentException("amount is negative number");
        }
        if (payment.getFrom() == null) {
            log.warn("Operation failed: from account is null!");
            throw new IllegalArgumentException("account from is null");
        }
        if (payment.getTo() == null) {
            log.warn("Operation failed: to account is null!");
            throw new IllegalArgumentException("account to is null");
        }
        if (payment.getSent() == null) {
            log.warn("Operation failed: date is null!");
            throw new IllegalArgumentException("date is null");
        }
    }

    private void checkPaymentForCreatePayment(Payment payment) {
        validate(payment);
        if (payment.getId() != null) {
            log.warn("Operation failed: passed id is null!");
            throw new IllegalArgumentException("payment id is already set");
        }
    }

    private void createPaymentInnerProcess(Connection con, Payment payment) {

        try (PreparedStatement prepStatement = con.prepareStatement(
                "INSERT INTO PAYMENT (amount, fromAcc, toAcc, dateSent) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {

            prepStatement.setBigDecimal(1, payment.getAmount());
            prepStatement.setLong(2, payment.getFrom().getId());
            prepStatement.setLong(3, payment.getTo().getId());
            prepStatement.setTimestamp(4, new java.sql.Timestamp(payment.getSent().getTime()));
            int addedRows = prepStatement.executeUpdate();
            if (addedRows != 1) {
                log.error(addedRows + " have been affected!");
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert payment " + payment);
            }

            ResultSet keyRS = prepStatement.getGeneratedKeys();
            payment.setId(getKey(keyRS, payment));

        } catch (SQLException ex) {
            log.error("payment {} coudl not be created", payment);
            throw new ServiceFailureException("Error when inserting payment " + payment, ex);
        }

    }

    private void checkPaymentForDeletePayment(Payment payment) {

        if (payment == null) {
            log.warn("Operation failed: passed account is null!");
            throw new IllegalArgumentException("payment is null");
        }
        if (payment.getId() == null) {
            log.warn("Operation failed: passed id is null!");
            throw new IllegalArgumentException("payment id is null");
        }
    }

    private void deletePaymentInnerProcess(Connection con, Payment payment) {

        try (PreparedStatement prepStatement = con.prepareStatement(
                "DELETE FROM payment WHERE id = ?")) {

            prepStatement.setLong(1, payment.getId());

            int count = prepStatement.executeUpdate();
            if (count == 0) {
                log.error("Entity {} not found", payment);
                throw new EntityNotFoundException("Payment " + payment + " was not found in database!");
            } else if (count != 1) {
                log.error(count + " rows have been affected!");
                throw new ServiceFailureException("Invalid deleted rows count detected (one row should be updated): " + count);
            }

        } catch (SQLException ex) {
            log.error("payment {} coudl not be deleted", payment);
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }

    private void checkPaymentForUpdatePayment(Payment payment) {
        validate(payment);
        if (payment.getId() == null) {
            log.error("Operation failed: Id is null!");
            throw new IllegalArgumentException("payment id is null");
        }
    }

    private void updatePaymentInnerProcess(Connection con, Payment payment) {

        try (PreparedStatement prepStatement = con.prepareStatement(
                "UPDATE Payment SET amount = ?, fromAcc = ?, toAcc = ?, dateSent = ? WHERE id = ?")) {
            prepStatement.setBigDecimal(1, payment.getAmount());
            prepStatement.setLong(2, payment.getFrom().getId());
            prepStatement.setLong(3, payment.getTo().getId());
            prepStatement.setTimestamp(4, new java.sql.Timestamp(payment.getSent().getTime()));
            prepStatement.setLong(5, payment.getId());

            int count = prepStatement.executeUpdate();
            if (count == 0) {
                log.error("Entity {} not found", payment);
                throw new EntityNotFoundException("Payment " + payment + " was not found in database!");
            } else if (count != 1) {
                log.error(count + " rows have been affected!");
                throw new ServiceFailureException("Invalid updated rows count detected (one row should be updated): " + count);
            }

        } catch (SQLException ex) {
            log.error("payment {} could not be updated", payment);
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }

    private void checkIdForFindPaymentById(Long id) {
        if (id == null) {
            log.error("Operation failed: Id is null!");
            throw new IllegalArgumentException("Id must not be null!");
        }
    }

    private Payment findPaymentByIdInnerProcess(Long id, Connection con) {

        try (PreparedStatement prepStatement = con.prepareStatement(
                "SELECT * FROM payment WHERE id = ?")) {
            prepStatement.setLong(1, id);
            ResultSet rs = prepStatement.executeQuery();

            if (rs.next()) {
                Payment payment = resultSetToPayment(rs, con);
                findAccountsToPayment(payment, con, rs.getLong("fromAcc"), rs.getLong("toAcc"));
                if (rs.next()) {
                    log.error("found more than one entity with id {}", id);
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                            + "(source id: " + id + ", found " + payment + " and " + resultSetToPayment(rs, con));
                }
                log.info("payment {} was found", payment);
                return payment;
            } else {
                log.info("payment with id {} was not found", id);
                return null;
            }
        } catch (SQLException ex) {
            log.error("Failed to retrieve payment with id ()", id);
            throw new ServiceFailureException(
                    "Error when retrieving payment with id " + id, ex);
        }
    }

    private List<Payment> findAllPaymentsInnerProcess(Connection con) {

        try (PreparedStatement prepStatement = con.prepareStatement(
                "SELECT * FROM payment")) {

            ResultSet rs = prepStatement.executeQuery();

            List<Payment> result = new ArrayList<>();
            while (rs.next()) {
                Payment current = resultSetToPayment(rs, con);
                findAccountsToPayment(current, con, rs.getLong("fromAcc"), rs.getLong("toAcc"));
                result.add(current);
            }
            log.info("all payments was successfully retrieved", result);
            return result;

        } catch (SQLException ex) {
            log.error("Listing of all payments failed");
            throw new ServiceFailureException(
                    "Error when retrieving all payments", ex);
        }
    }
}
