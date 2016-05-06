package GUI2;

import java.awt.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import pv168.Account;

/**
 *
 * @author Vašek & Vítek
 */
public class TableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        if(value instanceof BigDecimal){
            BigDecimal decimal = (BigDecimal)value;
            decimal = decimal.setScale(2, RoundingMode.HALF_UP);  
            Currency c = Currency.getInstance("CZK");
            return new JLabel(decimal.toString() + " " + c, SwingConstants.RIGHT);
        } else if(value instanceof Date){
            Date date = (Date)value;
            DateFormat format = new SimpleDateFormat("d MM yyyy");
            return new JLabel(format.format(date));
        } else if(value instanceof Account){
            Account account = (Account)value;
            return new JLabel("ID " + account.getId() + ", " + account.getOwner(), SwingConstants.CENTER);
        } else{
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    
    
    
}
