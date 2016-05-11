package GUI2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
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
    private static final ResourceBundle bundle = ResourceBundle.getBundle("Bundle", Locale.getDefault());

    private class ReadAllSwingWorker extends SwingWorker <List<Payment>, Void>{

        
        private final PaymentManager innerManager;
        
        public ReadAllSwingWorker(PaymentManager p){
            innerManager = p;
        }

        @Override
        protected List<Payment> doInBackground() throws Exception {
            return innerManager.findAllPayments();
        }

        @Override
        protected void done() {
            try {
                payments = get();
                fireTableDataChanged();
            } catch (InterruptedException | ExecutionException ex) {
//                Logovani chyb
            }
        }
           
    }
    
    private class DeleteSwingWorker extends SwingWorker <Void, Void> {

        private final PaymentManager innerManager;
        private final Payment toDelete;
        private final int rowIndex;
        
        public DeleteSwingWorker(PaymentManager manager, int row){
            
            innerManager = manager;
            toDelete = payments.get(row) ;
            rowIndex = row;
        }
       
        @Override
        protected Void doInBackground() throws Exception {
            innerManager.deletePayment(toDelete);
            return null;          
        }

        @Override
        protected void done() {
            payments.remove(rowIndex);       
            fireTableRowsDeleted(rowIndex, rowIndex);           
        }
        
        
   
   
   
    }
    
    private class UpdateSwingWorker extends SwingWorker <Void, Void> {

        private final PaymentManager innerManager;
        private final Payment toUpdate;
        private final int rowIndex;
        private final int columnIndex;
        
        public UpdateSwingWorker(PaymentManager manager, Payment p, int row, int column){
            innerManager = manager;
            toUpdate = p;
            rowIndex = row;
            columnIndex = column;
        }
        
        
        
        @Override
        protected Void doInBackground() throws Exception {
            innerManager.updatePayment(toUpdate);
            return null;
        }

        @Override
        protected void done() {
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    
    
    
    }
    
    private class ExecutePaymentSwingWorker extends SwingWorker <Void, Void> {

        private final BankingManager innerManager;
        private final Payment toExecute;
        
        public ExecutePaymentSwingWorker(BankingManager manager, Payment p){
            innerManager = manager;
            toExecute = p;
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            innerManager.executePayment(toExecute);      
            return null;
        }
        
        @Override
        protected void done() {
            
            try {
                get();
                
                payments.add(toExecute);
                int lastRow = payments.size()-1;
                MainFrame.getInstance().refreshAccountTable();            
                fireTableRowsInserted(lastRow, lastRow);
            } catch (InterruptedException ex) {
//   Logovani chyb
            } catch (ExecutionException ex) {
                JOptionPane.showMessageDialog(MainFrame.getInstance(), ex.getCause().getMessage());
            }
        }       
    
    
    }
    
//----------------------------------------------------------------------------//    
//End of workers section    
//----------------------------------------------------------------------------//    
    
    
    
    
    private ReadAllSwingWorker readWorker;
    private DeleteSwingWorker deleteWorker;
    private UpdateSwingWorker updateWorker;
    private ExecutePaymentSwingWorker executeWorker;
    
    private List <Payment> payments = new ArrayList<>();
    private final PaymentManager pManager;
    private final BankingManager bManager;
    
    PaymentTableModel(PaymentManager p, BankingManager b){
    
    pManager = p;
    bManager = b; 
    
    readWorker = new ReadAllSwingWorker(pManager);
    readWorker.execute();
    
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
                return bundle.getString("ID.TABLE");
            case 1 :
                return bundle.getString("TIME.DATE.TABLE");
            case 2 :
                return bundle.getString("AMOUNT.TABLE");
            case 3 :
                return bundle.getString("SENT BY.TABLE");
            case 4 :
                return bundle.getString("SENTTO.TABLE");
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
       
        updateWorker = new UpdateSwingWorker(pManager, p, rowIndex, columnIndex);
        updateWorker.execute();
    
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
        
        deleteWorker = new DeleteSwingWorker(pManager, rowIndex);
        deleteWorker.execute();  
    }    
    
    public void addRow(Payment p){      
        executeWorker = new ExecutePaymentSwingWorker(bManager, p);
        executeWorker.execute();       
    }
    
    public void refreshTable(){
        readWorker = new ReadAllSwingWorker(pManager);
        readWorker.execute();
    }
}
