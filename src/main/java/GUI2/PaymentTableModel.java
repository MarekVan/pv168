package GUI2;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pv168.Account;
import pv168.BankingManager;
import pv168.Payment;
import pv168.PaymentManager;

/**
 *
 * @author Vašek & Vítek
 */
public class PaymentTableModel extends AbstractTableModel {

    private List <Payment> payments;
    private final PaymentManager pManager;
    private final BankingManager bManager;
    
    PaymentTableModel(PaymentManager p, BankingManager b){
    
    pManager = p;
    bManager = b; 
//    
    payments = pManager.findAllPayments();
//       
    }
    
    @Override
    public int getRowCount() {
        return payments.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Payment p = payments.get(rowIndex);
        
        switch(columnIndex){
            case 0 :
                return p.getId();
            case 1 :
                return p.getSent();
            case 2 :
                return p.getAmount();
            case 3 :
                return p.getFrom();
            case 4 :
                return p.getTo();
            default :
                throw new IndexOutOfBoundsException();        
        }
    }
    
    @Override
    public String getColumnName(int columnIndex){
        switch(columnIndex){
            case 0 :
                return "ID";
            case 1 :
                return "Time&Date";
            case 2 :
                return "Amount";
            case 3 :
                return "Sent by";
            case 4 :
                return "Sent to";
            default :
                throw new IndexOutOfBoundsException();
        }
    
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
                return Date.class;
            case 2:
                return BigDecimal.class;
            case 3 :
            case 4 :
                return Account.class;
            default:
                throw new IndexOutOfBoundsException();
        }
    }  
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex){
        Payment p = payments.get(rowIndex);
        
        switch(columnIndex){
            case 0 :
                p.setId((Long)value);
                break;
            case 1 :
                p.setSent((Date)value);
                break;
            case 2 :
                p.setAmount((BigDecimal)value);
                break;
            case 3 :
                p.setFrom((Account)value);
                break;
            case 4 :
                p.setTo((Account)value);
                break;
            default :
                throw new IndexOutOfBoundsException();
        }
//        
        pManager.updatePayment(p);
//        
        fireTableCellUpdated(rowIndex, columnIndex);
    
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        switch(columnIndex){
            case 0 :
            case 1 :
                return false;
            case 2 :
            case 3 :
            case 4 :
                return true;
            default :
                throw new IndexOutOfBoundsException();
        }
    }
    
//------------------------------------------------------------------------------
    
    public void deleteRow(int rowIndex){
        Payment p = payments.get(rowIndex);      
//        
        pManager.deletePayment(p);
//        
        payments.remove(rowIndex);       
        fireTableRowsDeleted(rowIndex, rowIndex);
    }    
    
    public void addRow(Payment p){      
//        
        bManager.executePayment(p);
//        
        
        payments.add(p);       
        int lastRow = payments.size()-1;
        fireTableRowsInserted(lastRow, lastRow);  
    }
    
    public void refreshTable(){
    payments = pManager.findAllPayments();
    
    fireTableDataChanged();
    }
}
