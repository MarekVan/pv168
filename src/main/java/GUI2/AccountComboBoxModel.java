package GUI2;

import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import pv168.Account;
import pv168.AccountManager;

/**
 *
 * @author Vašek & Vítek
 */
public class AccountComboBoxModel extends AbstractListModel implements ComboBoxModel{

    public AccountComboBoxModel(AccountManager m){
        manager = m; 
//        
        accounts = manager.findAllAccounts();
//        
    }
    
    private List<Account> accounts;
    private Account selected;
    private final AccountManager manager;
    
    
    
    @Override
    public int getSize() {
        return accounts.size();
    }

    @Override
    public Object getElementAt(int index) {
        return accounts.get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (Account)anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }
    
    public void refresh(){
//        
        accounts = manager.findAllAccounts();        
//      
        if(!accounts.contains(selected)){
        selected = null;
        }
        
        fireContentsChanged(accounts, 0, accounts.size());
    }
    
}
