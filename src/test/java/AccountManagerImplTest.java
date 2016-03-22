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
        assertFalse("Null id not permitted!", account.getId() == null);

        Long accountId = account.getId();
        Account result = manager.findAccountById(accountId);

        assertTrue("Retrieved object must be equal to the inserted one!", account.equals(result));
        assertFalse("Retrieved object must not be the same instance!", account == result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAccountWithNull() throws Exception {
        manager.createAccount(null);
        fail("Should throw IllegalArgumentException if null is passed!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAccountWithoutOwner() {
        Account account = new Account();
        account.setBalance(new BigDecimal(500));

        manager.createAccount(account);
        fail("Should throw IllegalArgumentException if owner is not initialized!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAccountWithoutBalance() {
        Account account = new Account();
        account.setOwner("Pepa");

        manager.createAccount(account);
        fail("Should throw IllegalArgumentException if balance is not initialized!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAccountWithAssignedId() {
        Account account = newAccount("Pepa", new BigDecimal(500));

        account.setId(1L);

        manager.createAccount(account);
        fail("Should throw IllegalArgumentException if id is assigned!");
    }

    /**
     * Test of deleteAccount method, of class AccountManagerImpl.
     */
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
 
        assertEquals(manager.findAccountById(acc1.getId()), null);
        assertFalse("Existing record must not retrieve null", manager.findAccountById(acc2.getId()) == null);
        assertEquals(manager.findAccountById(acc1.getId()), null);
        assertFalse("Existing record must not retrieve null", manager.findAccountById(acc4.getId()) == null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAccountWithNull() {
        Account account = newAccount("Lennie", new BigDecimal(0));

        manager.createAccount(account);

        manager.deleteAccount(null);
        fail("Null must not be passed to deleteAccount method but passed!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void TestDeleteAccountWithNullId() {
        Account account = newAccount("Trevor", new BigDecimal(45));

        manager.createAccount(account);

        account.setId(null);

        manager.deleteAccount(account);
        fail("Passing account without assigned id is not permitted but passed!");
    }

    /**
     * Test of updateAccount method, of class AccountManagerImpl.
     */
    @Test
    public void testUpdateAccount() {
        Account account = newAccount("Percy", new BigDecimal(999));
        
        manager.createAccount(account);
        
        account.setBalance(new BigDecimal(50000));
        account.setOwner("Carol");

        manager.updateAccount(account);

        Account result = manager.findAccountById(account.getId());

        assertEquals(result, account);

        assertEquals(new BigDecimal(50000), result.getBalance());
        assertEquals("Carol", result.getOwner());


    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNonExistingAccount(){
        Account account = newAccount("Susan", new BigDecimal(650));
        
        manager.updateAccount(account);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAccountWithNull() {
        Account account = newAccount("Lisa", new BigDecimal(600));

        manager.createAccount(account);
        manager.updateAccount(null);
        fail("Passing null instead of Account instance should cause IllegalArgumentException!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAccountWithoutId() {
        Account account = newAccount("Jane", new BigDecimal(600));

        manager.createAccount(account);
        account.setId(null);
        manager.updateAccount(account);

        fail("Null id should not pass! InvalidArgumentException should be thrown!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAccountWithoutOwner() {
        Account account = newAccount("Mike", new BigDecimal(700));

        manager.createAccount(account);
        account.setOwner(null);
        manager.updateAccount(account);

        fail("Account used for update should not pass without all inicialized fields!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAccountWithoutBalance() {
        Account account = newAccount("Bruce", new BigDecimal(125));

        manager.createAccount(account);
        account.setBalance(null);

        manager.updateAccount(account);
        fail("Account used for update should not pass without all inicialized fields!");
    }

    /**
     * Test of findAccountById method, of class AccountManagerImpl.
     */
    @Test
    public void testFindAccountById() {
        Account account = newAccount("Leo", new BigDecimal(750));

        manager.createAccount(account);
        Account result = manager.findAccountById(account.getId());

        assertEquals(result, account);
        assertFalse("Inserted and retrieved Accounts should not be same instances", result == account);

        Account account2 = newAccount("Dick", new BigDecimal(60));
        manager.createAccount(account2);
        manager.deleteAccount(account2);
//account2 object has id but is not in the database
        result = manager.findAccountById(account2.getId());
        assertEquals(result, null);

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFindAccountByIdWithNullId() {
        Account account = newAccount("Fred", new BigDecimal(600));

        manager.createAccount(account);

        manager.findAccountById(null);
        fail("Passing null id should result in IllegalArgumentException!");
    }

    /**
     * Test of findAllAccounts method, of class AccountManagerImpl.
     */
    @Test
    public void testFindAllAccounts() {
        Account acc1 = newAccount("John", new BigDecimal(50));
        Account acc2 = newAccount("Paul", new BigDecimal(800));
        Account acc3 = newAccount("George", new BigDecimal(0));
        Account acc4 = newAccount("Jacob", new BigDecimal(355));

        List<Account> initialAccounts = new ArrayList<Account>();
        initialAccounts.add(acc1);
        initialAccounts.add(acc2);
        initialAccounts.add(acc3);
        initialAccounts.add(acc4);

        manager.createAccount(acc1);
        manager.createAccount(acc2);
        manager.createAccount(acc3);
        manager.createAccount(acc4);

        List<Account> result = manager.findAllAccounts();

        assertTrue("List of inserted and list of retrieved Accounts must not differ in length!", initialAccounts.size() == result.size());

        for (int i = 0; i < result.size(); i++) {
            assertTrue("List of retrieved Accounts must contain all inserted Accounts!", result.contains(initialAccounts.get(i)));
        }

    }

}
