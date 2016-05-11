package GUI2;

import java.awt.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
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
    
    private static final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        if(value instanceof BigDecimal){
            BigDecimal decimal = (BigDecimal)value;
            decimal = decimal.setScale(2, RoundingMode.HALF_UP);  
            return new JLabel(numberFormat.format(decimal));
        } else if(value instanceof Date){
            Date date = (Date)value;
            return new JLabel(dateFormat.format(date));
        } else if(value instanceof Account){
            Account account = (Account)value;
            return new JLabel("ID " + account.getId() + ", " + account.getOwner(), SwingConstants.CENTER);
        } else{
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    
    
    
}
