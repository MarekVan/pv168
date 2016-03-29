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
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import org.junit.rules.Timeout;
import static pv168.Account.*;
import pv168.BankingManager;
import static pv168.Payment.*;
import pv168.ServiceFailureException;

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
    @Rule
    public Timeout timeout = new Timeout(5000);

    @Before
    public void setUp() throws Exception {

        dataSource = prepareDataSource();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStatement = connection.prepareStatement("CREATE TABLE account ("
                     + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                     + "owner VARCHAR(200) ,"
                     + "balance DECIMAL (20,0))")) {

            prepStatement.executeUpdate();

            try (PreparedStatement prepStatement2 = connection.prepareStatement("CREATE TABLE payment (" +
                    "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "amount DECIMAL (20,0), " +
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
            connection.prepareStatement("DROP TABLE payment").executeUpdate();
            connection.prepareStatement("DROP TABLE account").executeUpdate();
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
        assertThat(from.getBalance()).isEqualTo(new BigDecimal(0));
        assertThat(to.getBalance()).isEqualTo(new BigDecimal(2400));

        Payment paymentResult = paymentManager.findPaymentById(payment.getId());
        Account accountFromResult = accountManager.findAccountById(from.getId());
        Account accountToResult = accountManager.findAccountById(to.getId());

        assertThat(payment).isEqualTo(paymentResult);
        assertThat(payment).isNotSameAs(paymentResult);
        assertThat(from).isEqualToIgnoringGivenFields(accountFromResult, "balance");
        assertThat(from).isNotSameAs(accountFromResult);
        assertThat(to).isEqualToIgnoringGivenFields(accountToResult, "balance");
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
        Account from = newAccount("Pepa", new BigDecimal(12000));
        Account to = newAccount("Honza", new BigDecimal(18000));
        Account to2 = newAccount("Juraj", new BigDecimal(20000));

        Payment payment1 = newPayment(new BigDecimal(1100), from, to, null);
        Payment payment2 = newPayment(new BigDecimal(1200), from, to, null);
        Payment payment3 = newPayment(new BigDecimal(1300), from, to, null);
        Payment payment4 = newPayment(new BigDecimal(1400), from, to2, null);

        accountManager.createAccount(from);
        accountManager.createAccount(to);
        accountManager.createAccount(to2);

          
        List<Payment> payments = new ArrayList<>();
        List<Payment> result = new ArrayList<>();        
        
        assertThat(result).isEmpty();

        bankingManager.executePayment(payment1);
        payments.add(payment1);
        result = bankingManager.findAllIncomingPaymentsToAccount(to);
        assertThat(payments).containsAll(result);


        bankingManager.executePayment(payment2);
        payments.add(payment2);
        result = bankingManager.findAllIncomingPaymentsToAccount(to);
        assertThat(payments).containsAll(result);

        bankingManager.executePayment(payment3);
        payments.add(payment3);
        result = bankingManager.findAllIncomingPaymentsToAccount(to);
        assertThat(payments).containsAll(result);

        bankingManager.executePayment(payment4);
        result = bankingManager.findAllIncomingPaymentsToAccount(to);
        assertThat(payments).containsAll(result);
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
        accountManager.createAccount(account);
        accountManager.deleteAccount(account);
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

        accountManager.createAccount(from);
        accountManager.createAccount(from2);
        accountManager.createAccount(to);

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
        accountManager.createAccount(account);
        accountManager.deleteAccount(account);
        assertThatThrownBy(() -> bankingManager.findOutgoingPaymentsToAccount(account))
                .isInstanceOf(EntityNotFoundException.class);
    }

    
    @Test
    public void testExecutePaymentInterruptionReaction(){        
    Account from = newAccount("Steve", new BigDecimal(90000));
    Account to = newAccount("Mary", new BigDecimal(23000.5));
    
    accountManager.createAccount(from);
    accountManager.createAccount(to);
    
    Payment payment = newPayment(new BigDecimal(30000), from, to, null);
        

    BankingManager bm = new BankingManager(){

        @Override
        public void executePayment(Payment payment) {
        if (payment == null){
            throw new IllegalArgumentException("Payment is null!");
        }
        if (payment.getId() != null) {
            throw new IllegalArgumentException("Payment cannot have set id before execution!");
        }
        if (payment.getFrom() == null) {
            throw new IllegalArgumentException("Sender must be specified to execute a payment!");
        }
        if (payment.getTo() == null) {
            throw new IllegalArgumentException("Reciever must be specified to execute a payment!");
        }
        if (payment.getAmount() == null) {
            throw new IllegalArgumentException("Ammount of money must be specified for payment!");
        }
        if (payment.getSent() != null) {
            throw new IllegalArgumentException("Timestamp of payment is assigned during this method!");
        }
        if (payment.getFrom() == payment.getTo()){
            throw new IllegalArgumentException("Sender and receiver are the same accounts!");
        }
        if (payment.getFrom().getBalance().compareTo(payment.getAmount()) < 0) {
            throw new InsufficientBalanceException("The sending account does not have enough money for the payment!");
        }



        try (Connection connection = dataSource.getConnection();
        ) {
            connection.setAutoCommit(false);

            payment.getFrom().setBalance(payment.getFrom().getBalance().subtract(payment.getAmount()));
            payment.getTo().setBalance(payment.getTo().getBalance().add(payment.getAmount()));

            payment.setSent(new Date());

            try {

                accountManager.updateAccount(payment.getFrom(), connection);
                accountManager.updateAccount(payment.getTo(), connection);

                
                throw new SQLException();
//throwing SQLException to check whether interupted transaction does repair accounts and payment

            } catch (SQLException ex) {
                connection.rollback();
                connection.setAutoCommit(true);
                
                payment.setSent(null);
                payment.setId(null);

                payment.getFrom().setBalance(payment.getFrom().getBalance().add(payment.getAmount()));
                payment.getTo().setBalance(payment.getTo().getBalance().subtract(payment.getAmount()));
                throw new ServiceFailureException("Failed to execute payment " + payment, ex);
            }


        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to execute payment " + payment, ex);
        }
        
        }

        @Override
        public List<Payment> findAllIncomingPaymentsToAccount(Account account) {
            throw new UnsupportedOperationException("Not neede for the test."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<Payment> findOutgoingPaymentsToAccount(Account account) {
            throw new UnsupportedOperationException("Not needed for the test."); //To change body of generated methods, choose Tools | Templates.
        }
    
        
        
    };
    
    exception.expect(ServiceFailureException.class);
    bm.executePayment(payment);
    
    assertThat(from.getBalance()).isEqualTo(new BigDecimal(90000));
    assertThat(from.getOwner()).isEqualTo("Steve");
    
    assertThat(to.getBalance()).isEqualTo(new BigDecimal(23000.5));
    assertThat(to.getOwner()).isEqualTo("Mary");
    
    List<Payment> resultPayments = paymentManager.findAllPayments();
    assertThat(resultPayments).isEmpty();
    
    List<Account> resultAccounts = accountManager.findAllAccounts();
    
    assertThat(resultAccounts).contains(from, to);
    assertThat(resultAccounts.get(0)).isEqualTo(from);
    assertThat(resultAccounts.get(1)).isEqualTo(to);   
    
    assertThat(payment.getSent()).isNull();
    assertThat(payment.getId()).isNull();
    }
    
    

}