package GUI2;

import java.math.BigDecimal;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pv168.Account;
import static pv168.Account.newAccount;
import pv168.AccountManager;

/**
 *
 * @author Vašek & Vítek
 */
public class AccountTableModel extends AbstractTableModel {
    
    private List<Account> accounts;
    private final AccountManager manager;
    
    
    public AccountTableModel(AccountManager m){
        
    manager = m;
    
    m.createAccount(newAccount("Pepa", new BigDecimal(500)));
    m.createAccount(newAccount("Ondra", new BigDecimal(0)));
//    
    accounts = manager.findAllAccounts();
//    
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
                return "ID";
            case 1 :
                return "Owner";
            case 2 :
                return "Balance";
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
//        
        manager.updateAccount(a);
//        
        fireTableCellUpdated(rowIndex, columnIndex);   
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
        Account a = accounts.get(rowIndex); 
//        
        manager.deleteAccount(a);
//        
        accounts.remove(rowIndex);
        
        fireTableRowsDeleted(rowIndex, rowIndex);
    }    
    
    public void addRow(Account a){      
        accounts.add(a);       
        int lastRow = accounts.size()-1;
//        
        manager.createAccount(a);
//        
        fireTableRowsInserted(lastRow, lastRow);  
    }
    
    public void refreshTable(){
    
//    
        accounts = manager.findAllAccounts();
//        
        fireTableDataChanged();
        
    }
    
    
    
    
}
