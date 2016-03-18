package pv168;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
            throw new IllegalArgumentException("grave id is already set");
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "INSERT INTO PAYMENT (amount, fromAcc, toAcc, dateSent) VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            st.setBigDecimal(1, payment.getAmount());
            st.setLong(2, payment.getFrom().getId());
            st.setLong(3, payment.getTo().getId());
            st.setDate(4, new java.sql.Date(payment.getSent().getTime()));
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert grave " + payment);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            payment.setId(getKey(keyRS, payment));

        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting grave " + payment, ex);
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
            st.setDate(4, new java.sql.Date(payment.getSent().getTime()));
            st.setLong(5, payment.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Payment " + payment + " was not found in database!");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid updated rows count detected (one row should be updated): " + count);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when updating grave " + payment, ex);
        }
    }

    @Override
    public Payment findPaymentById(Long id) throws ServiceFailureException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,amount,fromAcc,toAcc,dateSent FROM payment WHERE id = ?")) {

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Payment payment = resultSetToPayment(rs);

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
                result.add(resultSetToPayment(rs));
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
                        + "retrieving failed when trying to insert grave " + payment
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert grave " + payment
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert grave " + payment
                    + " - no key found");
        }
    }

    private Payment resultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        //payment.setFrom(rs.getLong("fromAcc"));
        //payment.setTo(rs.getLong("toAcc"));
        payment.setSent(rs.getDate("dateSent"));
        return payment;
    }
}
