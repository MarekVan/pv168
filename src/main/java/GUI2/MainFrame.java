package GUI2;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.sql.DataSource;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import pv168.Account;
import static pv168.Account.newAccount;
import pv168.AccountManager;
import pv168.AccountManagerImpl;
import pv168.BankingManager;
import pv168.BankingManagerImpl;
import pv168.Payment;
import pv168.PaymentManager;
import pv168.PaymentManagerImpl;
import pv168.ServiceFailureException;


/**
 *
 * @author Vašek & Vítek
 */
public class MainFrame extends javax.swing.JFrame {

    private DataSource src;
    private AccountManager accountManager;
    private PaymentManager paymentManager;
    private BankingManager bankingManager;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("Bundle", Locale.getDefault());
    private static MainFrame INSTANCE;

    /**
     * Creates new form MainFrame
     */
    public static MainFrame getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MainFrame();
        }
        return INSTANCE;
    }

    private MainFrame() {

        try {
            setUpDatabase();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }

        initComponents();
        myInitComponets();
    }

    private void myInitComponets() {
        
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());

        MyListener listenerOwner = new MyListener();
        listenerOwner.setName("jTextFieldOwner");
        listenerOwner.setPreferences(prefs);
        jTextFieldOwner.getDocument().addDocumentListener(listenerOwner);
        jTextFieldOwner.setText(prefs.get("jTextFieldOwner", ""));
        
        MyListener listenerBalance = new MyListener();
        listenerBalance.setName("jTextFieldBalance");
        listenerBalance.setPreferences(prefs);      
        jTextFieldBalance.getDocument().addDocumentListener(listenerBalance);
        jTextFieldBalance.setText(prefs.get("jTextFieldBalance", ""));
        
      
        MyListener listenerAccount = new MyListener();
        listenerAccount.setName("jTextFieldAmountPayment");
        listenerAccount.setPreferences(prefs);
        jTextFieldAmountPayment.getDocument().addDocumentListener(listenerAccount);
        jTextFieldAmountPayment.setText(prefs.get("jTextFieldAmountPayment", ""));

    }

    private void setUpDatabase() throws SQLException, IOException {
        src = prepareDataSource();
        try (Connection connection = src.getConnection();
                PreparedStatement prepStatementAccounts = connection.prepareStatement("CREATE TABLE account ("
                        + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "owner VARCHAR(200) ,"
                        + "balance DECIMAL(17,2))");) {
            prepStatementAccounts.executeUpdate();
        }

        try (Connection connection = src.getConnection();
                PreparedStatement prepStatementPayments = connection.prepareStatement("CREATE TABLE payment ("
                        + "id BIGINT NOT NULL primary key generated always as identity, "
                        + "amount DECIMAL(17,2), "
                        + "fromAcc BIGINT NOT NULL, "
                        + "toAcc BIGINT NOT NULL, "
                        + "FOREIGN KEY (fromAcc) REFERENCES account (id),"
                        + "FOREIGN KEY (toAcc) REFERENCES account (id),"
                        + "dateSent TIMESTAMP )")) {
            prepStatementPayments.executeUpdate();
        }

        accountManager = new AccountManagerImpl(src);
        paymentManager = new PaymentManagerImpl(src);
        bankingManager = new BankingManagerImpl(src);

    }

    public DataSource prepareDataSource() throws IOException {
        Properties p =  new Properties();
        p.load(this.getClass().getResourceAsStream("/databaseConfiguration.properties"));
 
        BasicDataSource bds = new BasicDataSource(); 
        bds.setDriverClassName(p.getProperty("jdbc.driver"));
        bds.setUrl(p.getProperty("jdbc.url"));
        bds.setUsername(p.getProperty("jdbc.user"));
        bds.setPassword(p.getProperty("jdbc.password"));
        return bds;
    }
    
    
    
//    private DataSource prepareDataSource() throws SQLException {
//        EmbeddedDataSource ds = new EmbeddedDataSource();
//        ds.setDatabaseName("memory:account-test");
//        ds.setCreateDatabase("create");
//        return ds;
//    }

//------------------------------------------------------------------------------
    public void refreshComboBoxAccountModels() {

        AccountComboBoxModel boxFromModel = (AccountComboBoxModel) jComboBoxFromAccounts.getModel();
        boxFromModel.refresh();
        AccountComboBoxModel boxToModel = (AccountComboBoxModel) jComboBoxToAccounts.getModel();
        boxToModel.refresh();
    }

    public void refreshAccountTable() {
        AccountTableModel model = (AccountTableModel) jTableAccounts.getModel();
        model.refreshTable();
    }

    public void refreshPaymentTable() {
        PaymentTableModel model = (PaymentTableModel) jTablePayments.getModel();
        model.refreshTable();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableAccounts = new javax.swing.JTable();
        jButtonDeleteSelectedAccount = new javax.swing.JButton();
        jButtonCreateAccount = new javax.swing.JButton();
        jTextFieldOwner = new javax.swing.JTextField();
        jTextFieldBalance = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTablePayments = new javax.swing.JTable();
        jButtonDeleteSelectedPayment = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jTextFieldAmountPayment = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxFromAccounts = new javax.swing.JComboBox();
        jComboBoxToAccounts = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButtonExecutePayment = new javax.swing.JButton();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();

        jMenu1.setText(bundle.getString("FILE.MENUBAR.ITEM")); // NOI18N
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("Bundle", Locale.getDefault()); // NOI18N
        setTitle(bundle.getString("MAIN.TITLE")); // NOI18N

        jTabbedPane1.setToolTipText("");

        jTableAccounts.setModel(new AccountTableModel(accountManager));
        jTableAccounts.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTableAccountsPropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(jTableAccounts);
        jTableAccounts.setDefaultRenderer(Object.class, new TableCellRenderer());

        jButtonDeleteSelectedAccount.setText(bundle.getString("DELETE.SELECTED.BUTTON")); // NOI18N
        jButtonDeleteSelectedAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteSelectedAccountActionPerformed(evt);
            }
        });

        jButtonCreateAccount.setText(bundle.getString("CREATE.NEW.ACCOUNT.BUTTON")); // NOI18N
        jButtonCreateAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateAccountActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle.getString("OWNER.LABEL")); // NOI18N

        jLabel2.setText(bundle.getString("BALANCE.LABEL")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(128, 128, 128)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonCreateAccount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldOwner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldBalance, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(126, 126, 126))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(131, 131, 131)
                .addComponent(jButtonDeleteSelectedAccount, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jButtonDeleteSelectedAccount)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldOwner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(29, 29, 29)
                .addComponent(jButtonCreateAccount)
                .addGap(40, 40, 40))
        );

        jTabbedPane1.addTab(bundle.getString("ACCOUNTS.TAB"), jPanel1); // NOI18N

        jTablePayments.setModel(new PaymentTableModel(paymentManager, bankingManager));
        jScrollPane2.setViewportView(jTablePayments);
        jTablePayments.setDefaultRenderer(Object.class, new TableCellRenderer());

        jButtonDeleteSelectedPayment.setText(bundle.getString("DELETE.SELECTED.BUTTON")); // NOI18N
        jButtonDeleteSelectedPayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteSelectedPaymentActionPerformed(evt);
            }
        });

        jLabel3.setText(bundle.getString("AMOUNT.LABEL")); // NOI18N

        jComboBoxFromAccounts.setModel(new AccountComboBoxModel(accountManager));

        jComboBoxToAccounts.setModel(new AccountComboBoxModel(accountManager));

        jLabel4.setText(bundle.getString("SENDFROM.LABEL")); // NOI18N

        jLabel5.setText(bundle.getString("SENDTO.LABEL")); // NOI18N

        jButtonExecutePayment.setText(bundle.getString("EXECUTEPAYMENT.BUTTON")); // NOI18N
        jButtonExecutePayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecutePaymentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jSeparator3)
                .addContainerGap())
            .addComponent(jScrollPane2)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 168, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(97, 97, 97)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jComboBoxToAccounts, 0, 156, Short.MAX_VALUE)
                                .addComponent(jComboBoxFromAccounts, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jTextFieldAmountPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButtonExecutePayment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(113, 113, 113))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(131, 131, 131)
                .addComponent(jButtonDeleteSelectedPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jButtonDeleteSelectedPayment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldAmountPayment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxFromAccounts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxToAccounts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jButtonExecutePayment)
                .addGap(0, 51, Short.MAX_VALUE))
        );

        jComboBoxFromAccounts.setRenderer(new ComboBoxCellRenderer());
        jComboBoxToAccounts.setRenderer(new ComboBoxCellRenderer());

        jTabbedPane1.addTab(bundle.getString("PAYMENTS.TAB"), jPanel2); // NOI18N

        jMenuFile.setText(bundle.getString("FILE.MENUBAR.ITEM")); // NOI18N

        jMenuItemAbout.setText(bundle.getString("ABOUT.MENU.ITEM")); // NOI18N
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemAbout);
        jMenuFile.add(jSeparator1);

        jMenuItemExit.setText(bundle.getString("EXIT.MENU.ITEM")); // NOI18N
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar2.add(jMenuFile);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 614, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 587, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDeleteSelectedAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteSelectedAccountActionPerformed
        AccountTableModel model = (AccountTableModel) jTableAccounts.getModel();

        try {

            int selected = jTableAccounts.getSelectedRow();

            if (selected != -1) {
                model.deleteRow(selected);
            }

        } catch (ServiceFailureException ex) {

            JOptionPane.showMessageDialog(MainFrame.this, bundle.getString("UNKNOWN.ERROR"));

        }

    }//GEN-LAST:event_jButtonDeleteSelectedAccountActionPerformed

    private void jButtonCreateAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateAccountActionPerformed

        try {
            Account a = newAccount(jTextFieldOwner.getText(), new BigDecimal(jTextFieldBalance.getText()));

            AccountTableModel model = (AccountTableModel) jTableAccounts.getModel();
            model.addRow(a);

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(MainFrame.this, bundle.getString("INVALID.INPUT.BALANCE"));

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(MainFrame.this, bundle.getString("UNKNOWN.ERROR"));

        }

    }//GEN-LAST:event_jButtonCreateAccountActionPerformed

    private void jButtonDeleteSelectedPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteSelectedPaymentActionPerformed
        PaymentTableModel model = (PaymentTableModel) jTablePayments.getModel();

        int selected = jTablePayments.getSelectedRow();

        if (selected != -1) {
            model.deleteRow(selected);
        }

    }//GEN-LAST:event_jButtonDeleteSelectedPaymentActionPerformed

    private void jButtonExecutePaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecutePaymentActionPerformed
        try {

            Payment p = new Payment();

            p.setAmount(new BigDecimal(jTextFieldAmountPayment.getText()));
            p.setFrom((Account) jComboBoxFromAccounts.getSelectedItem());
            p.setTo((Account) jComboBoxToAccounts.getSelectedItem());

            jComboBoxFromAccounts.getModel().setSelectedItem(null);
            jComboBoxToAccounts.setSelectedItem(null);
            refreshComboBoxAccountModels();

            PaymentTableModel paymentModel = (PaymentTableModel) jTablePayments.getModel();
            paymentModel.addRow(p);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, bundle.getString("INVALID.INPUT.AMOUNT"));
        }

    }//GEN-LAST:event_jButtonExecutePaymentActionPerformed

    private void jTableAccountsPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableAccountsPropertyChange
        refreshComboBoxAccountModels();
        PaymentTableModel model = (PaymentTableModel) jTablePayments.getModel();
        model.refreshTable();
    }//GEN-LAST:event_jTableAccountsPropertyChange

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        About aboutWindow = new About();
        aboutWindow.setVisible(true);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame.getInstance().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCreateAccount;
    private javax.swing.JButton jButtonDeleteSelectedAccount;
    private javax.swing.JButton jButtonDeleteSelectedPayment;
    private javax.swing.JButton jButtonExecutePayment;
    private javax.swing.JComboBox jComboBoxFromAccounts;
    private javax.swing.JComboBox jComboBoxToAccounts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableAccounts;
    private javax.swing.JTable jTablePayments;
    private javax.swing.JTextField jTextFieldAmountPayment;
    private javax.swing.JTextField jTextFieldBalance;
    private javax.swing.JTextField jTextFieldOwner;
    // End of variables declaration//GEN-END:variables
}
