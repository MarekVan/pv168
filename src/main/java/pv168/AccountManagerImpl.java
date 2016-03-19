package pv168;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public class AccountManagerImpl implements AccountManager

    public AccountManagerImpl(){}

    @Override
    public void createAccount(Account account){}

    @Override
    public void deleteAccount(Account account){}

    @Override
    public void updateAccount(Account account){}

    @Override
    public Account findAccountById(Long id){
        return new Account();
    }

    @Override
    public List<Account> findAllAccounts(){
        return new ArrayList<>();
    }


}
