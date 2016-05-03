package GUI2;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import pv168.Account;

/**
 *
 * @author Vašek & Vítek
 */
public class ComboBoxCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    JLabel renderer = (JLabel) new DefaultListCellRenderer().getListCellRendererComponent(list, value, index,
        isSelected, cellHasFocus);
  
    if(value != null){
    Account a = (Account)value;    
    renderer.setText("ID: " + a.getId() + ", " + a.getOwner());
    }
    
    return renderer;
    }
    
}
