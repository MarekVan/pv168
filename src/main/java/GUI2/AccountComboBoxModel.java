package GUI2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingWorker;
import pv168.Account;
import pv168.AccountManager;

/**
 *
 * @author Vašek & Vítek
 */
public class AccountComboBoxModel extends AbstractListModel implements ComboBoxModel{

    private class ReadAllSwingWorker extends SwingWorker <List<Account>, Void> {

        private final AccountManager innerManager;
        
        public ReadAllSwingWorker(AccountManager m){
            innerManager = m;
        }
        
        
        
        @Override
        protected List<Account> doInBackground() throws Exception {
            return innerManager.findAllAccounts();
        }
        
        @Override    
        protected void done() {
            try {
                accounts = get(); 
                
                if(!accounts.contains(selected)){
                selected = null;
                }
                
                fireContentsChanged(accounts, 0, accounts.size());
                
                
            } catch (InterruptedException | ExecutionException ex) {
//                Logovani chyb
            }

        }
    
    }
    
//----------------------------------------------------------------------------//
//End of workers section    
//----------------------------------------------------------------------------//
    
    
    
    public AccountComboBoxModel(AccountManager m){
        manager = m;         
        worker = new ReadAllSwingWorker(manager);
        worker.execute();
    }
    
    private ReadAllSwingWorker worker;
    private List<Account> accounts = new ArrayList<>();
    private Account selected = null;
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
        worker = new ReadAllSwingWorker(manager);
        worker.execute();       
    }
    
}
