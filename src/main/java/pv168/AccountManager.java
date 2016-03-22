package pv168;


import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public interface AccountManager {

    /**
     * Creates record of an account in database. 
     * 
     * @param account
     * Account object with all specified atributes with exception of id, id will
     * be assigned inside this method automatically. Passing reference with id 
     * will cause IllegalArgument Exception. Passing null reference causes Illegal 
     * Argument Exception as well. 
     * 
     * 
    */
    public void createAccount(Account account);
    
    /**
     * Deletes record of specified Account object in database if such object exists.t
     * 
     * @param account 
     * Account object with specified id. Passing null reference causes Illegal 
     * Argument Exception. 
     * 
     */
    public void deleteAccount(Account account);
    
    
    /**
     * 
     * If account with id equal to account.id exists in the database, record is updated
     * according to given atributes. If no matching object exists, EntityNotFoundException is thrown.
     * 
     * 
     * @param account
     * Account object with all specified atributes. If not specified throws IllegalArgumentException.
     */
    public void updateAccount(Account account);
    
    
    /**
     * Retrieves account with specified id from database if account with specified id exists.
     * 
     * @param id
     * Id of account stored in database. Passing null reference causes IllegalArgument
     * Exception.
     * @return 
     * Returns account from database if account with given id exists in the database.
     * Returns null if no such account exists.
     */
    public Account findAccountById(Long id);
    
    /**
     * Retrieves all account records from database in a List.
     * @return
     * Returns List of all Accounts stored in the database.
     */
    
    public List<Account> findAllAccounts();

}
