import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pv168.Account;
import pv168.AccountManagerImpl;
import pv168.BankingManagerImpl;
import pv168.EntityNotFoundException;
import pv168.InsufficientBalanceException;
import pv168.Payment;
import pv168.PaymentManagerImpl;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static pv168.Account.*;
import static pv168.Payment.*;

/**
 * Created by Marek Vanèík on 27. 3. 2016.
 */
public class BankingManagerImplTest {

    private PaymentManagerImpl paymentManager;
    private AccountManagerImpl accountManager;
    private BankingManagerImpl bankingManager;
    private DataSource dataSource;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

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
        paymentManager = new PaymentManagerImpl(dataSource);
        accountManager = new AccountManagerImpl(dataSource);
        bankingManager = new BankingManagerImpl(dataSource);
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
        ds.setDatabaseName("memory:bankingbmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Test
    public void testExecutePayment() {
        Account from = newAccount("Pepa", new BigDecimal(1200));
        Account to = newAccount("Honza", new BigDecimal(1200));

        Payment payment = newPayment(new BigDecimal(1200), from, to, null);

        accountManager.createAccount(from);
        accountManager.createAccount(to);

        bankingManager.executePayment(payment);

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getSent()).isNotNull();
        assertThat(payment.getAmount()).isEqualTo(new BigDecimal(0));
        assertThat(from.getBalance()).isEqualTo(new BigDecimal(0));
        assertThat(to.getBalance()).isEqualTo(new BigDecimal(2400));

        Payment paymentResult = paymentManager.findPaymentById(payment.getId());
        Account accountFromResult = accountManager.findAccountById(from.getId());
        Account accountToResult = accountManager.findAccountById(to.getId());

        assertThat(payment).isEqualToComparingFieldByField(paymentResult);
        assertThat(payment).isNotSameAs(paymentResult);
        assertThat(from).isEqualToComparingFieldByField(accountFromResult);
        assertThat(from).isNotSameAs(accountFromResult);
        assertThat(to).isEqualToComparingFieldByField(accountToResult);
        assertThat(to).isNotSameAs(accountToResult);
    }

    @Test
    public void testExecutePaymentWithNullPayment() {
        assertThatThrownBy(() -> bankingManager.executePayment(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecutePaymentWithNullAmount() {
        Account from = newAccount("Pepa", new BigDecimal(1200));
        Account to = newAccount("Honza", new BigDecimal(1200));

        Payment payment = newPayment(null, from, to, null);

        accountManager.createAccount(from);
        accountManager.createAccount(to);

        assertThatThrownBy(() -> bankingManager.executePayment(payment))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(payment.getId()).isNull();
        assertThat(payment.getSent()).isNull();
    }

    @Test
    public void testExecutePaymentWithNullFromAccount() {
        Account to = newAccount("Honza", new BigDecimal(1200));

        Payment payment = newPayment(new BigDecimal(1200), null, to, null);

        accountManager.createAccount(to);

        assertThatThrownBy(() -> bankingManager.executePayment(payment))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(payment.getId()).isNull();
        assertThat(payment.getSent()).isNull();
    }

    @Test
    public void testExecutePaymentWithNullToAccount() {
        Account from = newAccount("Honza", new BigDecimal(1200));

        Payment payment = newPayment(new BigDecimal(1200), from, null, null);

        accountManager.createAccount(from);

        assertThatThrownBy(() -> bankingManager.executePayment(payment))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(payment.getId()).isNull();
        assertThat(payment.getSent()).isNull();
    }

    @Test
    public void testExecutePaymentWithSameFromAndToAccount() {
        Account from = newAccount("Honza", new BigDecimal(1200));

        Payment payment = newPayment(new BigDecimal(1200), from, from, null);

        accountManager.createAccount(from);

        assertThatThrownBy(() -> bankingManager.executePayment(payment))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(payment.getId()).isNull();
        assertThat(payment.getSent()).isNull();
    }

    @Test
    public void testExecutePaymentWithLowBalance() {
        Account from = newAccount("Pepa", new BigDecimal(1200));
        Account to = newAccount("Honza", new BigDecimal(1200));

        Payment payment = newPayment(new BigDecimal(12000), from, to, null);

        accountManager.createAccount(from);
        accountManager.createAccount(to);

        assertThatThrownBy(() -> bankingManager.executePayment(payment))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(payment.getId()).isNull();
        assertThat(payment.getSent()).isNull();
    }

    @Test
    public void testFindAllIncomingPaymentsToAccount() {
        Account from = newAccount("Pepa", new BigDecimal(120000));
        Account to = newAccount("Honza", new BigDecimal(18000));
        Account to2 = newAccount("Juraj", new BigDecimal(20000));

        Payment payment1 = newPayment(new BigDecimal(1100), from, to, null);
        Payment payment2 = newPayment(new BigDecimal(1200), from, to, null);
        Payment payment3 = newPayment(new BigDecimal(1300), from, to, null);
        Payment payment4 = newPayment(new BigDecimal(1400), from, to2, null);

        List<Payment> payments = new ArrayList<>();
        List<Payment> result = bankingManager.findAllIncomingPaymentsToAccount(to);

        assertThat(result).isEmpty();

        bankingManager.executePayment(payment1);
        payments.add(payment1);
        result = bankingManager.findAllIncomingPaymentsToAccount(to);
        assertThat(payments).isEqualTo(result);


        bankingManager.executePayment(payment2);
        payments.add(payment2);
        result = bankingManager.findAllIncomingPaymentsToAccount(to);
        assertThat(payments).isEqualTo(result);

        bankingManager.executePayment(payment3);
        payments.add(payment3);
        result = bankingManager.findAllIncomingPaymentsToAccount(to);
        assertThat(payments).isEqualTo(result);

        bankingManager.executePayment(payment4);
        result = bankingManager.findAllIncomingPaymentsToAccount(to);
        assertThat(payments).isEqualTo(result);
        assertThat(payments).isNotSameAs(result);
    }

    @Test
    public void testFindAllIncomingPaymentsToAccountWithNullAccount() {
        assertThatThrownBy(() -> bankingManager.findAllIncomingPaymentsToAccount(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFindAllIncomingPaymentsToAccountWithAccountWithNullOwner() {
        Account account = newAccount(null, new BigDecimal(120000));
        assertThatThrownBy(() -> bankingManager.findAllIncomingPaymentsToAccount(account))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFindAllIncomingPaymentsToAccountWithAccountWithNullBalance() {
        Account account = newAccount("Pepa", null);
        assertThatThrownBy(() -> bankingManager.findAllIncomingPaymentsToAccount(account))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFindAllIncomingPaymentsToAccountWithNonExistingAccount() {
        Account account = newAccount("Pepa", new BigDecimal(120000));
        assertThatThrownBy(() -> bankingManager.findAllIncomingPaymentsToAccount(account))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testFindOutgoingPaymentsToAccount() {
        Account from = newAccount("Pepa", new BigDecimal(120000));
        Account from2 = newAccount("Honza", new BigDecimal(18000));
        Account to = newAccount("Juraj", new BigDecimal(20000));

        Payment payment1 = newPayment(new BigDecimal(1100), from, to, null);
        Payment payment2 = newPayment(new BigDecimal(1200), from, to, null);
        Payment payment3 = newPayment(new BigDecimal(1300), from, to, null);
        Payment payment4 = newPayment(new BigDecimal(1400), from2, to, null);

        List<Payment> payments = new ArrayList<>();
        List<Payment> result = bankingManager.findOutgoingPaymentsToAccount(from);

        assertThat(result).isEmpty();

        bankingManager.executePayment(payment1);
        payments.add(payment1);
        result = bankingManager.findOutgoingPaymentsToAccount(from);
        assertThat(payments).isEqualTo(result);


        bankingManager.executePayment(payment2);
        payments.add(payment2);
        result = bankingManager.findOutgoingPaymentsToAccount(from);
        assertThat(payments).isEqualTo(result);

        bankingManager.executePayment(payment3);
        payments.add(payment3);
        result = bankingManager.findOutgoingPaymentsToAccount(from);
        assertThat(payments).isEqualTo(result);

        bankingManager.executePayment(payment4);
        result = bankingManager.findOutgoingPaymentsToAccount(from);
        assertThat(payments).isEqualTo(result);
        assertThat(payments).isNotSameAs(result);
    }

    @Test
    public void testFindOutgoingPaymentsToAccountWithNullAccount() {
        assertThatThrownBy(() -> bankingManager.findOutgoingPaymentsToAccount(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFindOutgoingPaymentsToAccountWithAccountWithNullOwner() {
        Account account = newAccount(null, new BigDecimal(120000));
        assertThatThrownBy(() -> bankingManager.findOutgoingPaymentsToAccount(account))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFindOutgoingPaymentsToAccountWithAccountWithNullBalance() {
        Account account = newAccount("Pepa", null);
        assertThatThrownBy(() -> bankingManager.findOutgoingPaymentsToAccount(account))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFindOutgoingPaymentsToAccountWithNonExistingAccount() {
        Account account = newAccount("Pepa", new BigDecimal(120000));
        assertThatThrownBy(() -> bankingManager.findOutgoingPaymentsToAccount(account))
                .isInstanceOf(EntityNotFoundException.class);
    }


}