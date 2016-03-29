import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import pv168.*;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static pv168.Account.*;
import static pv168.Payment.*;

/**
 * Created by Marek Van��k on 12. 3. 2016.
 */
public class PaymentManagerImplTest {

    private PaymentManagerImpl manager;
    private AccountManagerImpl manager2;
    private DataSource dataSource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStatement = connection.prepareStatement("CREATE TABLE account ("
                     + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                     + "owner VARCHAR(200) ,"
                     + "balance DECIMAL)")) {

            prepStatement.executeUpdate();

            try (PreparedStatement prepStatement2 = connection.prepareStatement("CREATE TABLE payment (" +
                    "id BIGINT NOT NULL primary key generated always as identity, " +
                    "amount DECIMAL, " +
                    "fromAcc BIGINT NOT NULL, " +
                    "toAcc BIGINT NOT NULL, " +
                    "FOREIGN KEY (fromAcc) REFERENCES account (id)," +
                    "FOREIGN KEY (toAcc) REFERENCES account (id)," +
                    "dateSent TIMESTAMP )")) {

                prepStatement2.executeUpdate();

            }
        }
        manager = new PaymentManagerImpl(dataSource);
        manager2 = new AccountManagerImpl(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE PAYMENT").executeUpdate();
            connection.prepareStatement("DROP TABLE ACCOUNT").executeUpdate();
        }

    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:paymentmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Test
    public void testCreatePayment() throws Exception {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(45000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager.createPayment(payment);
        Long paymentId = payment.getId();

        assertThat(payment.getId(), is(not(equalTo(null))));
        Payment result = manager.findPaymentById(paymentId);
        assertThat(result, is(equalTo(payment)));
        assertThat(result, is(not(sameInstance(payment))));
        assertDeepEquals(payment, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNull() throws Exception {
        manager.createPayment(null);
    }

    @Test
    public void createPaymentWithExistingId() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        payment.setId(1L);
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void createPaymentWithNegativeAmount() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(-2000), from1, to1, cal.getTime());
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void createPaymentWithNullFromAccount() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), null, to1, cal.getTime());
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void createPaymentWithNullToAccount() {;
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, null, cal.getTime());
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void createPaymentWithNullDate() {
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, null);
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void testDeletePayment() throws Exception {
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(3000));
        Account from2 = newAccount("robo", new BigDecimal(4000));
        Account to2 = newAccount("laco", new BigDecimal(5000));
        Calendar cal = newCalendar(2016, 3, 12);
        Calendar ca2 = newCalendar(2015, 3, 12);
        Payment p1 = newPayment(new BigDecimal(6000), from1, to1, cal.getTime());
        Payment p2 = newPayment(new BigDecimal(70000), from2, to2, ca2.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager2.createAccount(from2);
        manager2.createAccount(to2);
        manager.createPayment(p1);
        manager.createPayment(p2);

        assertNotNull(manager.findPaymentById(p1.getId()));
        assertNotNull(manager.findPaymentById(p2.getId()));

        manager.deletePayment(p1);

        assertNull(manager.findPaymentById(p1.getId()));
        assertNotNull(manager.findPaymentById(p2.getId()));
    }

    @Test
    public void deleteNullPayment() {
        expectedException.expect(IllegalArgumentException.class);
        manager.deletePayment(null);
    }

    @Test
    public void deletePaymentWithNullId() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        payment.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.deletePayment(payment);
    }

    @Test
    public void deletePaymentThatDoesNotExist() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        payment.setId(12L);
        expectedException.expect(EntityNotFoundException.class);
        manager.deletePayment(payment);
    }

    @Test
    public void testUpdatePayment() throws Exception {
        Calendar cal = newCalendar(2016, 3, 12);
        Calendar ca2 = newCalendar(2015, 4, 1);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Account from2 = newAccount("robo", new BigDecimal(2000));
        Account to2 = newAccount("laco", new BigDecimal(2000));
        Payment p1 = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        Payment p2 = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager2.createAccount(from2);
        manager2.createAccount(to2);
        manager.createPayment(p1);
        manager.createPayment(p2);
        Long paymentId = p1.getId();

        p1.setAmount(new BigDecimal(3000));
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat(p1.getAmount(), is(equalTo(new BigDecimal(3000))));
        assertThat(p1.getFrom(), is(equalTo(from1)));
        assertThat(p1.getTo(), is(equalTo(to1)));
        assertThat(p1.getSent(), is(equalTo(cal.getTime())));

        p1.setFrom(from2);
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat(p1.getAmount(), is(equalTo(new BigDecimal(3000))));
        assertThat(p1.getFrom(), is(equalTo(from2)));
        assertThat(p1.getTo(), is(equalTo(to1)));
        assertThat(p1.getSent(), is(equalTo(cal.getTime())));

        p1.setTo(to2);
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat(p1.getAmount(), is(equalTo(new BigDecimal(3000))));
        assertThat(p1.getFrom(), is(equalTo(from2)));
        assertThat(p1.getTo(), is(equalTo(to2)));
        assertThat(p1.getSent(), is(equalTo(cal.getTime())));

        p1.setSent(ca2.getTime());
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat(p1.getAmount(), is(equalTo(new BigDecimal(3000))));
        assertThat(p1.getFrom(), is(equalTo(from2)));
        assertThat(p1.getTo(), is(equalTo(to2)));
        assertThat(p1.getSent(), is(equalTo(ca2.getTime())));

        // Check if updates didn't affected other records
        assertDeepEquals(p2, manager.findPaymentById(p2.getId()));
    }

    @Test
    public void updatePaymentWithNull() {
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(null);
    }

    @Test
    public void updatePaymentThatDoesNotExist() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager.createPayment(payment);
        payment.setId(payment.getId() + 1000);
        expectedException.expect(EntityNotFoundException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNullAmount() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager.createPayment(payment);
        payment.setAmount(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNegativeAmount() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager.createPayment(payment);
        payment.setAmount(new BigDecimal(-2000));
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNullFromAccount() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager.createPayment(payment);
        payment.setFrom(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNullToAccount() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager.createPayment(payment);
        payment.setTo(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNullDate() {
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Payment payment = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager.createPayment(payment);
        payment.setSent(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void testFindAllPayments() throws Exception {
        assertTrue(manager.findAllPayments().isEmpty());

        Calendar cal = newCalendar(2016, 3, 12);
        Calendar ca2 = newCalendar(2015, 4, 1);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(2000));
        Account from2 = newAccount("robo", new BigDecimal(2000));
        Account to2 = newAccount("laco", new BigDecimal(2000));
        Payment p1 = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        Payment p2 = newPayment(new BigDecimal(30000), from2, to2, ca2.getTime());

        manager2.createAccount(from1);
        manager2.createAccount(to1);
        manager2.createAccount(from2);
        manager2.createAccount(to2);
        manager.createPayment(p1);
        manager.createPayment(p2);

        List<Payment> expected = Arrays.asList(p1, p2);
        List<Payment> actual = manager.findAllPayments();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);

    }

    private void assertDeepEquals(Payment expected, Payment actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAmount(), actual.getAmount());
        assertEquals(expected.getFrom(), actual.getFrom());
        assertEquals(expected.getTo(), actual.getTo());
        assertEquals(expected.getSent(), actual.getSent());
    }

    public static Calendar newCalendar(int year, int month, int day) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }

    private void assertDeepEquals(List<Payment> expectedList, List<Payment> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Payment expected = expectedList.get(i);
            Payment actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private static Comparator<Payment> idComparator = new Comparator<Payment>() {
        @Override
        public int compare(Payment o1, Payment o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}

