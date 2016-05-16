/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI2;

import java.util.prefs.Preferences;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author Marek Vanèík
 */
class MyListener implements DocumentListener {

    //private static final String SAVED_PREFERENCES = "preferences";
    private String name;
    private Preferences prefs;
    
    public void setName(String name){
        this.name = name;
    }
    
     void setPreferences(Preferences prefs) {
       this.prefs = prefs;
    }
    
    @Override
    public void changedUpdate(DocumentEvent event) {
         final Document document = event.getDocument();
        // get the preferences associated with your application
        //Preferences prefs = Preferences.userRoot().node(SAVED_PREFERENCES);
        try {
            // save textfield value in the preferences object
            prefs.put(name, document.getText(0, document.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        final Document document = event.getDocument();
        // get the preferences associated with your application
        //Preferences prefs = Preferences.userRoot().node(SAVED_PREFERENCES);
        try {
            // save textfield value in the preferences object
            prefs.put(name, document.getText(0, document.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        final Document document = event.getDocument();
        // get the preferences associated with your application
        //Preferences prefs = Preferences.userRoot().node(SAVED_PREFERENCES);
        try {
            // save textfield value in the preferences object
            prefs.put(name, document.getText(0, document.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
