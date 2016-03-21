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

        validate(payment);
        if (payment.getId() != null) {
            throw new IllegalArgumentException("payment id is already set");
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "INSERT INTO PAYMENT (amount, fromAcc, toAcc, dateSent) VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            st.setBigDecimal(1, payment.getAmount());
            st.setLong(2, payment.getFrom().getId());
            st.setLong(3, payment.getTo().getId());
            st.setTimestamp(4, new java.sql.Timestamp(payment.getSent().getTime()));
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert payment " + payment);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            payment.setId(getKey(keyRS, payment));

        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting payment " + payment, ex);
        }
    }

    @Override
    public void deletePayment(Payment payment) throws ServiceFailureException {
        if (payment == null) {
            throw new IllegalArgumentException("payment is null");
        }
        if (payment.getId() == null) {
            throw new IllegalArgumentException("payment id is null");
        }
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "DELETE FROM payment WHERE id = ?")) {

            st.setLong(1, payment.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Payment " + payment + " was not found in database!");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid deleted rows count detected (one row should be updated): " + count);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }

    @Override
    public void updatePayment(Payment payment) throws ServiceFailureException {
        validate(payment);
        if (payment.getId() == null) {
            throw new IllegalArgumentException("payment id is null");
        }
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "UPDATE Payment SET amount = ?, fromAcc = ?, toAcc = ?, dateSent = ? WHERE id = ?")) {

            st.setBigDecimal(1, payment.getAmount());
            st.setLong(2, payment.getFrom().getId());
            st.setLong(3, payment.getTo().getId());
            st.setTimestamp(4, new java.sql.Timestamp(payment.getSent().getTime()));
            st.setLong(5, payment.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Payment " + payment + " was not found in database!");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid updated rows count detected (one row should be updated): " + count);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when updating payment " + payment, ex);
        }
    }

    @Override
    public Payment findPaymentById(Long id) throws ServiceFailureException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,amount,fromAcc,toAcc,dateSent FROM payment WHERE id = ?");
                PreparedStatement stFrom = connection.prepareStatement(""
                        + "SELECT * FROM account WHERE id = ?");
                PreparedStatement stTo = connection.prepareStatement(""
                        + "SELECT * FROM account WHERE id = ?")
        ) {

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Payment payment = resultSetToPayment(rs);
                findAccountsToPayment(payment, connection, rs.getLong("fromAcc"), rs.getLong("toAcc"));
                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + payment + " and " + resultSetToPayment(rs));
                }
                return payment;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving payment with id " + id, ex);
        }
    }

    @Override
    public List<Payment> findAllPayments() throws ServiceFailureException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,amount,fromAcc,toAcc,dateSent FROM payment")) {

            ResultSet rs = st.executeQuery();

            List<Payment> result = new ArrayList<>();
            while (rs.next()) {
                Payment current = resultSetToPayment(rs);
                findAccountsToPayment(current, connection, rs.getLong("fromAcc"), rs.getLong("toAcc"));
                result.add(current);
            }
            return result;

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all payments", ex);
        }

    }

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

    private Long getKey(ResultSet keyRS, Payment payment) throws ServiceFailureException, SQLException {
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

    private Payment resultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        Date date = new Date(rs.getTimestamp("dateSent").getTime());         //new Date(rs.getDate("dateSent").getTime());
        payment.setId(rs.getLong("id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setSent(date);
        return payment;
    }

    private void findAccountsToPayment(Payment payment, Connection connection, Long fromAcc, Long toAcc) {

        try (PreparedStatement stFrom = connection.prepareStatement(""
                + "SELECT * FROM account WHERE id = ?");
             PreparedStatement stTo = connection.prepareStatement(""
                     + "SELECT * FROM account WHERE id = ?")
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
                    "Error when retrieving payment with id " + payment.getId(), ex);
        }

    }
}
