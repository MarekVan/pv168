package pv168;


import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public interface AccountManager {

    /**
     * @param account
     * Account object with all specified atributes with exception of id, id will
     * be assigned inside this method automaticaly. Passing reference with id 
     * will cause IllegalArgument Exception. Passing null reference causes Illegal 
     * Argument Exception as well. 
     * 
     * 
    Creates record of an account in database. 
    */
    public void createAccount(Account account);
    
    /**
     * @param account 
     * Account object with specified id. Passing null reference causes Illegal 
     * Argument Exception. 
     * 
     *Deletes record of specified Account object in database if such object exists.
     */
    public void deleteAccount(Account account);
    
    
    /**
     * @param account
     * Account object with all specified atributes.
     * 
     * 
     * If account with id equal to account.id exists in database, record is updated
     * according to given atributes. In other case IllegalArgumentException is thrown.
     */
    public void updateAccount(Account account);
    
    
    /**
     * 
     * @param id
     * Id of object stored in database. Passing null reference causes IllegalArgument
     * Exception.
     * @return 
     * Returns Account from database if account with given id exists in the database.
     * Returns null if no such Account exists.
     */
    public Account findAccountById(Long id);
    
    /**
     * 
     * @return
     * Returns List of all Accounts stored in the database.
     */
    
    public List<Account> findAllAccounts();

}
