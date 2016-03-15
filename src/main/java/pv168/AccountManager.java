package pv168;


import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public interface AccountManager {

    public void createAccount(Account account);

    public void deleteAccount(Account account);

    public void updateAccount(Account account);

    public Account findAccountById(Long id);

    public List<Account> findAllAccounts();

}
