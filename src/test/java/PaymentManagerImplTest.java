

import pv168.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by Marek Van��k on 12. 3. 2016.
 */
public class PaymentManagerImplTest {

    private PaymentManagerImpl manager;

    @Before
    public void setUp() throws Exception {
        manager = new PaymentManagerImpl();
    }

    @Test
    public void testCreatePayment() throws Exception {

        Calendar cal = newCalendar(2016, 3, 12);
        Payment payment = newPayment(new BigDecimal(2000), new Account(), new Account(), cal.getTime());
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
    public void createPaymentWithWrongValues() {
        Calendar cal = newCalendar(2016, 3, 12);
        Payment payment = newPayment(new BigDecimal(2000), new Account(), new Account(), cal.getTime());
        payment.setId(1L);
        try {
            manager.createPayment(payment);
            fail("should refuse assigned id");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        payment = newPayment(new BigDecimal(-2000), new Account(), new Account(), cal.getTime());
        try {
            manager.createPayment(payment);
            fail("negative balance not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        payment = newPayment(new BigDecimal(2000), null, new Account(), cal.getTime());
        try {
            manager.createPayment(payment);
            fail("null account from not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        payment = newPayment(new BigDecimal(2000), new Account(), null, cal.getTime());
        try {
            manager.createPayment(payment);
            fail("null account to not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        payment = newPayment(new BigDecimal(2000), new Account(), new Account(), null);
        try {
            manager.createPayment(payment);
            fail("null date not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

    }

    @Test
    public void testDeletePayment() throws Exception {
        Calendar cal = newCalendar(2016, 3, 12);
        Payment p1 = newPayment(new BigDecimal(2000), new Account(), new Account(), cal.getTime());
        Payment p2 = newPayment(new BigDecimal(2000), new Account(), new Account(), cal.getTime());
        manager.createPayment(p1);
        manager.createPayment(p2);

        assertNotNull(manager.findPaymentById(p1.getId()));
        assertNotNull(manager.findPaymentById(p2.getId()));

        manager.deletePayment(p1);

        assertNull(manager.findPaymentById(p1.getId()));
        assertNotNull(manager.findPaymentById(p2.getId()));
    }

    @Test
    public void deletePaymentWithWrongAttributes() {
        Calendar cal = newCalendar(2016, 3, 12);
        Payment payment = newPayment(new BigDecimal(2000), new Account(), new Account(), cal.getTime());

        try {
            manager.deletePayment(null);
            fail("null payment not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment.setId(null);
            manager.deletePayment(payment);
            fail("null id not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment.setId(1L);
            manager.deletePayment(payment);
            fail("non-existing payment not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

    }

    @Test
    public void testUpdatePayment() throws Exception {
        Calendar cal = newCalendar(2016, 3, 12);
        Calendar ca2 = newCalendar(2015, 4, 1);
        Account from1 = newAccount("jano", new BigDecimal(2000));
        Account to1 = newAccount("marian", new BigDecimal(3000));
        Account from2 = newAccount("robo", new BigDecimal(4000));
        Account to2 = newAccount("laco", new BigDecimal(5000));
        Payment p1 = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        Payment p2 = newPayment(new BigDecimal(2000), from1, to1, cal.getTime());
        manager.createPayment(p1);
        manager.createPayment(p2);
        Long paymentId = p1.getId();

        p1.setAmount(new BigDecimal(555));
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat("amount was not changed", p1.getAmount(), is(equalTo(new BigDecimal(555))));
        assertThat("account from was changed when changing amount", p1.getFrom(), is(equalTo(from1)));
        assertThat("account to was changed when changing amount", p1.getTo(), is(equalTo(to1)));
        assertThat("date was changed when changing amount", p1.getSent(), is(equalTo(cal.getTime())));

        p1.setFrom(from2);
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat("amount was changed when changing from account", p1.getAmount(), is(equalTo(new BigDecimal(555))));
        assertThat("account from was not changed", p1.getFrom(), is(equalTo(from2)));
        assertThat("account to was changed when changing from account", p1.getTo(), is(equalTo(to1)));
        assertThat("date was changed when changing from account", p1.getSent(), is(equalTo(cal.getTime())));

        p1.setTo(to2);
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat("amount was changed when changing to accoun", p1.getAmount(), is(equalTo(new BigDecimal(555))));
        assertThat("account from was changed when changing to accoun", p1.getFrom(), is(equalTo(from2)));
        assertThat("account to was not changed", p1.getTo(), is(equalTo(to2)));
        assertThat("date was changed when changing to accoun", p1.getSent(), is(equalTo(cal.getTime())));

        p1.setSent(ca2.getTime());
        manager.updatePayment(p1);
        p1 = manager.findPaymentById(paymentId);
        assertThat("amount was changed when changing date", p1.getAmount(), is(equalTo(new BigDecimal(555))));
        assertThat("account from was changed when changing date", p1.getFrom(), is(equalTo(from2)));
        assertThat("account to was changed when changing date", p1.getTo(), is(equalTo(to2)));
        assertThat("date was not changed", p1.getSent(), is(equalTo(ca2.getTime())));

        // Check if updates didn't affected other records
        assertDeepEquals(p2, manager.findPaymentById(p2.getId()));
    }

    @Test
    public void updatePaymentWithWrongAttributes() {

        Calendar cal = newCalendar(2016, 3, 12);
        Payment payment = newPayment(new BigDecimal(2000), new Account(), new Account(), cal.getTime());
        manager.createPayment(payment);
        Long paymentId = payment.getId();

        try {
            manager.updatePayment(null);
            fail("null payment not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment = manager.findPaymentById(paymentId);
            payment.setId(null);
            manager.updatePayment(payment);
            fail("null id not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment = manager.findPaymentById(paymentId);
            payment.setId(paymentId + 1);
            manager.updatePayment(payment);
            fail("changed id not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment = manager.findPaymentById(paymentId);
            payment.setAmount(null);
            manager.updatePayment(payment);
            fail("null amount not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment = manager.findPaymentById(paymentId);
            payment.setAmount(new BigDecimal(-520));
            manager.updatePayment(payment);
            fail("negative amount not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment = manager.findPaymentById(paymentId);
            payment.setTo(null);
            manager.updatePayment(payment);
            fail("null account to not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment = manager.findPaymentById(paymentId);
            payment.setFrom(null);
            manager.updatePayment(payment);
            fail("null account from not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            payment = manager.findPaymentById(paymentId);
            payment.setSent(null);
            manager.updatePayment(payment);
            fail("null date not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    @Test
    public void testFindAllPayments() throws Exception {
        Calendar cal = newCalendar(2016, 3, 12);
        assertTrue(manager.findAllPayments().isEmpty());

        Payment p1 = newPayment(new BigDecimal(2000), new Account(), new Account(), cal.getTime());
        Payment p2 = newPayment(new BigDecimal(2000), new Account(), new Account(), cal.getTime());

        manager.createPayment(p1);
        manager.createPayment(p2);

        List<Payment> expected = Arrays.asList(p1, p2);
        List<Payment> actual = manager.findAllPayments();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals("saved and retrieved payments differ", expected, actual);
        assertDeepEquals(expected, actual);

    }

    public static Payment newPayment(BigDecimal amount,  Account from, Account to, Date sent){
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setFrom(from);
        payment.setTo(to);
        payment.setSent(sent);
        return payment;
    }

    public static Account newAccount(String owner, BigDecimal balance){
        Account account = new Account();
        account.setOwner(owner);
        account.setBalance(balance);
        return account;
    }

    private void assertDeepEquals(Payment expected, Payment actual) {
        assertEquals("id value is not equal",expected.getId(), actual.getId());
        assertEquals("amount value is not equal",expected.getAmount(), actual.getAmount());
        assertEquals("from value is not equal",expected.getFrom(), actual.getFrom());
        assertEquals("to value is not equal",expected.getTo(), actual.getTo());
        assertEquals("date value is not equal",expected.getSent(), actual.getSent());
    }

    public static Calendar newCalendar(int year, int month, int day){
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