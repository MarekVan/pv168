import org.junit.Test;
import pv168.Account;
import pv168.AccountManagerImpl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;

import static org.junit.Assert.*;
import org.junit.Before;
import static pv168.Account.newAccount;
import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import pv168.EntityNotFoundException;


/**
 *
 * @author Vašek & Vítek
 * Nepsat hlasky, je to zbytecny
 * Pouzivat anotaci rule, abych mohl ocekavat vyjimky na konkretnim radku
 * 
 */
public class AccountManagerImplTest {

    
    private AccountManagerImpl manager;
    private DataSource dataSource;
   
    @Rule
    public ExpectedException exception = ExpectedException.none();

    public AccountManagerImplTest() {
    }

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStatement = connection.prepareStatement("CREATE TABLE account ("
                    + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "owner VARCHAR(200) ,"
                    + "balance DECIMAL)");   
        ) {
        
            prepStatement.executeUpdate();
                
        }
        manager = new AccountManagerImpl(dataSource);
        
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE account").executeUpdate();
        }
    }
 
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:account-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    /**
     * Test of createAccount method, of class AccountManagerImpl.
     */
    @Test
    public void testCreateAccount() {
        Account account = newAccount("Pepa", new BigDecimal(1200));

        manager.createAccount(account);
        assertNotNull(account.getId());

        Long accountId = account.getId();
        Account result = manager.findAccountById(accountId);

        assertEquals(account, result); 
//isNotTheSame - jestli je to stejna reference
        assertThat(account).isNotSameAs(result);
    }
    
    @Test
    public void testCreateAccountWithNull(){
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Passed account is null!");
        manager.createAccount(null);
    }
    
    @Test
    public void testCreateAccountWithoutOwner() {
        Account account = new Account();
        account.setBalance(new BigDecimal(500));

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Owner must be set!");
        manager.createAccount(account);

    }
    
    @Test
    public void testCreateAccountWithoutBalance() {
        Account account = new Account();
        account.setOwner("Pepa");
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Balance must be set!");
        manager.createAccount(account);

    }
    
    @Test
    public void testCreateAccountWithAssignedId() {
        Account account = newAccount("Pepa", new BigDecimal(500));

        account.setId(1L);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Passed account.id must not be set!");
        manager.createAccount(account);

    }

    @Test
    public void testDeleteAccount() {
        Account acc1 = newAccount("John", new BigDecimal(50));
        Account acc2 = newAccount("Paul", new BigDecimal(800));
        Account acc3 = newAccount("George", new BigDecimal(0));
        Account acc4 = newAccount("Jacob", new BigDecimal(355));

        manager.createAccount(acc1);
        manager.createAccount(acc2);
        manager.createAccount(acc3);
        manager.createAccount(acc4);

        manager.deleteAccount(acc1);
        manager.deleteAccount(acc3);
 
        assertThat(manager.findAccountById(acc1.getId())).isNull();
        assertThat(manager.findAccountById(acc2.getId())).isNotNull();
        assertThat(manager.findAccountById(acc3.getId())).isNull();
        assertThat(manager.findAccountById(acc4.getId())).isNotNull();
    }

    @Test
    public void testDeleteAccountWithNull() {
        Account account = newAccount("Lennie", new BigDecimal(0));

        manager.createAccount(account);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Passed account is null!");
        manager.deleteAccount(null);
    }

    @Test
    public void TestDeleteAccountWithNullId() {
        Account account = newAccount("Trevor", new BigDecimal(45));

        manager.createAccount(account);

        account.setId(null);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Id of account to be deleted must be set!");
        manager.deleteAccount(account);
    }

    @Test
    public void testUpdateAccount() {
        Account account = newAccount("Percy", new BigDecimal(999));
        
        manager.createAccount(account);
        
        account.setBalance(new BigDecimal(50000));
        account.setOwner("Carol");

        manager.updateAccount(account);

        Account result = manager.findAccountById(account.getId());

        assertThat(result).isEqualToComparingFieldByField(account);
    }
    
    @Test
    public void testUpdateNonExistingAccount(){
        Account account = newAccount("Susan", new BigDecimal(650));
        account.setId(1L);
        
        exception.expect(EntityNotFoundException.class);
        exception.expectMessage("was not found in the database!");
        manager.updateAccount(account);
    }

    @Test
    public void testUpdateAccountWithNull() {
        Account account = newAccount("Lisa", new BigDecimal(600));

        manager.createAccount(account);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Passed account is null!");
        manager.updateAccount(null);
    }

    @Test
    public void testUpdateAccountWithoutId() {
        Account account = newAccount("Jane", new BigDecimal(600));

        manager.createAccount(account);
        account.setId(null);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Id of account to be updated must be set!");
        manager.updateAccount(account);

    }

    @Test
    public void testUpdateAccountWithoutOwner() {
        Account account = newAccount("Mike", new BigDecimal(700));

        manager.createAccount(account);
        account.setOwner(null);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Owner must be set!");
        manager.updateAccount(account);

    }

    @Test
    public void testUpdateAccountWithoutBalance() {
        Account account = newAccount("Bruce", new BigDecimal(125));

        manager.createAccount(account);
        account.setBalance(null);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Balance must be set!");
        manager.updateAccount(account);
    }

    @Test
    public void testFindAccountById() {
        Account account = newAccount("Leo", new BigDecimal(750));

        manager.createAccount(account);
        Account result = manager.findAccountById(account.getId());

        assertThat(result).isEqualTo(account);
        assertThat(result).isNotSameAs(account);

        Account account2 = newAccount("Dick", new BigDecimal(60));
        manager.createAccount(account2);
        manager.deleteAccount(account2);
//account2 object has id but is not in the database
        result = manager.findAccountById(account2.getId());
        assertThat(result).isNull();

    }
    
    @Test
    public void testFindAccountByIdWithNullId() {
        Account account = newAccount("Fred", new BigDecimal(600));

        manager.createAccount(account);
        
        account.setId(null);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Passed id is null, cannot find account without id!");
        manager.findAccountById(account.getId());
    }
    
    @Test
    public void testFindNonExistingAccountById(){
        Account account = newAccount("Maggie", new BigDecimal(3000));
        account.setId(1L);
        
        Account result = manager.findAccountById(account.getId());
    
        assertThat(result).isNull();
    }

    @Test
    public void testFindAllAccounts() {
        Account acc1 = newAccount("John", new BigDecimal(50));
        Account acc2 = newAccount("Paul", new BigDecimal(800));
        Account acc3 = newAccount("George", new BigDecimal(0));
        Account acc4 = newAccount("Jacob", new BigDecimal(355));

        List<Account> initialAccounts = new ArrayList<>();
        initialAccounts.add(acc1);
        initialAccounts.add(acc2);
        initialAccounts.add(acc3);
        initialAccounts.add(acc4);

        manager.createAccount(acc1);
        manager.createAccount(acc2);
        manager.createAccount(acc3);
        manager.createAccount(acc4);

        List<Account> result = manager.findAllAccounts();

//porovna dve kolekce, jestli obsahujou stejny prvky
        assertThat(initialAccounts).containsAll(result);
        

    }

}
