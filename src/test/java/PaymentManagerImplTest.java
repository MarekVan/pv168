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

/**
 * Created by Marek Van��k on 12. 3. 2016.
 */
public class PaymentManagerImplTest {

    // koment git pull test

    private PaymentManagerImpl manager;
    private DataSource dataSource;
    private static Long count = 0L;

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection())
             /*PreparedStatement prepStatement = connection.prepareStatement("CREATE TABLE account ("
                     + "accountId BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                     + "owner VARCHAR(200) ,"
                     + "balance DECIMAL)");
             PreparedStatement prepStatement2 = connection.prepareStatement("CREATE TABLE payment ("
                     + "id BIGINT NOT NULL primary key generated always as identity,"
                     + "amount DECIMAL ,"
                     + "CONSTRAINT fromAcc FOREIGN KEY (accountId) REFERENCES account (accountId),"
                     + "CONSTRAINT toAcc FOREIGN KEY (accountId) REFERENCES account (accountId),"
                     + "dateSent DATE )")) */ {
            
            
            connection.prepareStatement("CREATE TABLE account ("
                    + "accountId BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "owner VARCHAR(200) ,"
                    + "balance DECIMAL)").executeUpdate();

            connection.prepareStatement("CREATE TABLE payment (\n" +
"id BIGINT NOT NULL primary key generated always as identity,\n" +
"amount DECIMAL ," +
"fromAcc BIGINT," +
"toAcc BIGINT," +
//"CONSTRAINT fromAcc FOREIGN KEY (\"fromAcc\") REFERENCES account (\"accountId\")," +
//"CONSTRAINT toAcc FOREIGN KEY (\"toAcc\") REFERENCES account (\"accountId\")," +
"dateSent DATE)").executeUpdate();
            


            //prepStatement.executeUpdate();
            //prepStatement2.executeUpdate();
        }
        manager = new PaymentManagerImpl(dataSource);
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
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        manager.createPayment(payment);
        Long paymentId = payment.getId();

        assertThat("saved payment has null id", payment.getId(), is(not(equalTo(null))));
        Payment result = manager.findPaymentById(paymentId);
        assertThat("loaded payment differs from the saved one", result, is(equalTo(payment)));
        assertThat("loaded payment is the same instance", result, is(not(sameInstance(payment))));
        assertDeepEquals(payment, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNull() throws Exception {
        manager.createPayment(null);
    }

    @Test
    public void createPaymentWithExistingId() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        payment.setId(1L);
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void createPaymentWithNegativeAmount() {
        BigDecimal amount = new BigDecimal(-2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void createPaymentWithNullFromAccount() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, null, to1, cal.getTime());
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void createPaymentWithNullToAccount() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Payment payment = newPayment(amount, from1, null, cal.getTime());
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void createPaymentWithNullDate() {
        BigDecimal amount = new BigDecimal(2000);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, null);
        expectedException.expect(IllegalArgumentException.class);
        manager.createPayment(payment);
    }

    @Test
    public void testDeletePayment() throws Exception {
        BigDecimal amount = new BigDecimal(2000);
        BigDecimal amount2 = new BigDecimal(20000);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Account from2 = newAccount("robo", amount);
        Account to2 = newAccount("laco", amount);
        Calendar cal = newCalendar(2016, 3, 12);
        Calendar ca2 = newCalendar(2015, 3, 12);
        Payment p1 = newPayment(amount, from1, to1, cal.getTime());
        Payment p2 = newPayment(amount2, from2, to2, ca2.getTime());
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
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        payment.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.deletePayment(payment);
    }

    @Test
    public void deletePaymentThatDoesNotExist() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        payment.setId(12L);
        expectedException.expect(EntityNotFoundException.class);
        manager.deletePayment(payment);
    }

    @Test
    public void testUpdatePayment() throws Exception {
        Calendar cal = newCalendar(2016, 3, 12);
        Calendar ca2 = newCalendar(2015, 4, 1);
        BigDecimal amount = new BigDecimal(2000);
        BigDecimal amount2 = new BigDecimal(20000);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Account from2 = newAccount("robo", amount);
        Account to2 = newAccount("laco", amount);
        Payment p1 = newPayment(amount, from1, to1, cal.getTime());
        Payment p2 = newPayment(amount, from1, to1, cal.getTime());
        manager.createPayment(p1);
        manager.createPayment(p2);
        Long paymentId = p1.getId();

        p1.setAmount(amount2);
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat(p1.getAmount(), is(equalTo(amount2)));
        assertThat(p1.getFrom(), is(equalTo(from1)));
        assertThat(p1.getTo(), is(equalTo(to1)));
        assertThat(p1.getSent(), is(equalTo(cal.getTime())));

        p1.setFrom(from2);
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat("amount was changed when changing from account", p1.getAmount(), is(equalTo(amount2)));
        assertThat("account from was not changed", p1.getFrom(), is(equalTo(from2)));
        assertThat("account to was changed when changing from account", p1.getTo(), is(equalTo(to1)));
        assertThat("date was changed when changing from account", p1.getSent(), is(equalTo(cal.getTime())));

        p1.setTo(to2);
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat("amount was changed when changing to accoun", p1.getAmount(), is(equalTo(amount2)));
        assertThat("account from was changed when changing to accoun", p1.getFrom(), is(equalTo(from2)));
        assertThat("account to was not changed", p1.getTo(), is(equalTo(to2)));
        assertThat("date was changed when changing to accoun", p1.getSent(), is(equalTo(cal.getTime())));

        p1.setSent(ca2.getTime());
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat("amount was changed when changing date", p1.getAmount(), is(equalTo(amount2)));
        assertThat("account from was changed when changing date", p1.getFrom(), is(equalTo(from2)));
        assertThat("account to was changed when changing date", p1.getTo(), is(equalTo(to2)));
        assertThat("date was not changed", p1.getSent(), is(equalTo(ca2.getTime())));

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
        manager.createPayment(payment);
        payment.setId(payment.getId() + 1000);
        expectedException.expect(EntityNotFoundException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNullAmount() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        manager.createPayment(payment);
        payment.setAmount(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNegativeAmount() {
        BigDecimal amount = new BigDecimal(2000);
        BigDecimal amount2 = new BigDecimal(-2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        manager.createPayment(payment);
        payment.setAmount(amount2);
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNullFromAccount() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        manager.createPayment(payment);
        payment.setFrom(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNullToAccount() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
        manager.createPayment(payment);
        payment.setTo(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updatePayment(payment);
    }

    @Test
    public void updatePaymentWithNullDate() {
        BigDecimal amount = new BigDecimal(2000);
        Calendar cal = newCalendar(2016, 3, 12);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Payment payment = newPayment(amount, from1, to1, cal.getTime());
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
        BigDecimal amount = new BigDecimal(2000);
        BigDecimal amount2 = new BigDecimal(20000);
        Account from1 = newAccount("jano", amount);
        Account to1 = newAccount("marian", amount);
        Account from2 = newAccount("robo", amount);
        Account to2 = newAccount("laco", amount);
        Payment p1 = newPayment(amount, from1, to1, cal.getTime());
        Payment p2 = newPayment(amount2, from2, to2, ca2.getTime());
        manager.createPayment(p1);
        manager.createPayment(p2);

        List<Payment> expected = Arrays.asList(p1, p2);
        List<Payment> actual = manager.findAllPayments();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals("saved and retrieved payments differ", expected, actual);
        assertDeepEquals(expected, actual);

    }

    public static Payment newPayment(BigDecimal amount, Account from, Account to, Date sent) {
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setFrom(from);
        payment.setTo(to);
        payment.setSent(sent);
        return payment;
    }

    public static Account newAccount(String owner, BigDecimal balance) {
        Account account = new Account();
        account.setOwner(owner);
        account.setBalance(balance);
        account.setId(count++);
        return account;
    }

    private void assertDeepEquals(Payment expected, Payment actual) {
        assertEquals("id value is not equal", expected.getId(), actual.getId());
        assertEquals("amount value is not equal", expected.getAmount(), actual.getAmount());
        assertEquals("from value is not equal", expected.getFrom(), actual.getFrom());
        assertEquals("to value is not equal", expected.getTo(), actual.getTo());
        assertEquals("date value is not equal", expected.getSent(), actual.getSent());
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