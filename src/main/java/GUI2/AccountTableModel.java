package GUI2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import pv168.Account;
import static pv168.Account.newAccount;
import pv168.AccountManager;

/**
 *
 * @author Vašek & Vítek
 */
public class AccountTableModel extends AbstractTableModel {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("Bundle", Locale.getDefault());
    
    
    private class ReadAllSwingWorker extends SwingWorker <List<Account>, Void>{

        private final AccountManager innerManager;
        
        public ReadAllSwingWorker(AccountManager manager){
            innerManager = manager;
        }
        
        
        @Override
        protected List<Account> doInBackground() throws Exception {
            return innerManager.findAllAccounts();
        }

        @Override
        protected void done() {
            try {
                accounts = get();
                fireTableDataChanged();
            } catch (InterruptedException | ExecutionException ex) {
//      Logovani chyb
            }
        }
   
    }
    
    private class AddSwingWorker extends SwingWorker <Void, Void> {

        private final AccountManager innerManager;
        private final Account toAdd;
        
        public AddSwingWorker(AccountManager manager, Account a){
            innerManager = manager;
            toAdd = a;
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            innerManager.createAccount(toAdd);
            return null;
        }

        @Override
        protected void done() {
            accounts.add(toAdd);       
            int lastRow = accounts.size() - 1;
            MainFrame.getInstance().refreshComboBoxAccountModels();
            fireTableRowsInserted(lastRow, lastRow);  
        }     
    
    }
    
    private class DeleteSwingWorker extends SwingWorker <Void, Void>{

        private final AccountManager innerManager;
        private final Account toDelete;
        private final int rowIndex;
        
        public DeleteSwingWorker(AccountManager manager, int row){
            innerManager = manager;
            rowIndex = row;
            toDelete = accounts.get(row);
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            innerManager.deleteAccount(toDelete);
            return null;
        }

        @Override
        protected void done() {
            accounts.remove(rowIndex);
            MainFrame.getInstance().refreshComboBoxAccountModels();
            fireTableRowsDeleted(rowIndex, rowIndex);   
        }
        
        
    
    
    }
    
    private class UpdateSwingWorker extends SwingWorker <Void, Void> {

        private final AccountManager innerManager;
        private final Account toUpdate;
        private final int rowIndex;
        private final int columnIndex;
        
        public UpdateSwingWorker(AccountManager manager, int row, int column){
            innerManager = manager;
            toUpdate = accounts.get(row);
            rowIndex = row;
            columnIndex = column;
        }
        
        
        @Override
        protected Void doInBackground() throws Exception {
            innerManager.updateAccount(toUpdate);
            return null;
        }

        @Override
        protected void done() {
            MainFrame.getInstance().refreshComboBoxAccountModels();
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    
    
    
    
    
    }
    
      
    
    
    private ReadAllSwingWorker readWorker;
    private AddSwingWorker addWorker;
    private DeleteSwingWorker deleteWorker;
    private UpdateSwingWorker updateWorker;
    
    private List<Account> accounts = new ArrayList<>();
    private final AccountManager manager;
    
    
    public AccountTableModel(AccountManager m){
        
    manager = m;
    
    m.createAccount(newAccount("Pepa", new BigDecimal(500)));
    m.createAccount(newAccount("Ondra", new BigDecimal(0)));
    
    readWorker = new ReadAllSwingWorker(manager);
    readWorker.execute();
    }
       
    
//------------------------------------------------------------------------------
    
    @Override
    public int getRowCount(){
        return accounts.size();
    }
    
    @Override
    public int getColumnCount(){
        return 3;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex){
        Account a = accounts.get(rowIndex);
        
        switch(columnIndex){
            case 0 :
                return a.getId();
            case 1 :
                return a.getOwner();
            case 2 :
                return a.getBalance();
            default:
                throw new IndexOutOfBoundsException();
        }
                
    }
    
    @Override
    public String getColumnName(int columnIndex){
        switch(columnIndex){
            case 0 :
                return bundle.getString("ID.TABLE");
            case 1 :
                return bundle.getString("OWNER.TABLE");
            case 2 :
                return bundle.getString("BALANCE");
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
                return String.class;
            case 2:
                return BigDecimal.class;
            default:
                throw new IndexOutOfBoundsException();
        }
    }  
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex){
        Account a = accounts.get(rowIndex);
        
        switch(columnIndex){
            case 0 : 
                a.setId((Long)value);
                break;
            case 1 :
                a.setOwner((String)value);
                break;
            case 2 :
                a.setBalance((BigDecimal)value);
                break;
            default :
                throw new IndexOutOfBoundsException();
                
        }
      
        updateWorker = new UpdateSwingWorker(manager, rowIndex, columnIndex);
        updateWorker.execute(); 
    }
     
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        switch(columnIndex){
            case 0 :
                return false;
            case 1 :
            case 2 :
                return true;
            default : 
                throw new IndexOutOfBoundsException();
        }
        
    }
    
//------------------------------------------------------------------------------
    
    public void deleteRow(int rowIndex){
        deleteWorker = new DeleteSwingWorker(manager, rowIndex);
        deleteWorker.execute();
    }    
    
    public void addRow(Account a){      
      
        addWorker = new AddSwingWorker(manager, a);
        addWorker.execute();
       
    }
    
    public void refreshTable(){
        readWorker = new ReadAllSwingWorker(manager);
        readWorker.execute();        
    }
    
    
    
    
}
