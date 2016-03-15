package pv168;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public class AccountManagerImpl implements AccountManager {

    public AccountManagerImpl(){}

    public void createAccount(Account account){}

    public void deleteAccount(Account account){}

    public void updateAccount(Account account){}

    public Account findAccountById(Long id){
        return new Account();
    }

    public List<Account> findAllAccounts(){
        return new ArrayList<Account>();
    }


}
