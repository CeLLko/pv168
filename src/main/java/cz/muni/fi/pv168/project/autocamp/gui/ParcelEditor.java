/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Parcel;
import cz.muni.fi.pv168.project.autocamp.ParcelManagerImpl;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 *
 * Adam Gdovin, 433305
 * @version May 17, 2016
 */
public class ParcelEditor extends AbstractCellEditor implements TableCellEditor, ActionListener{
    
    private Parcel currentParcel;
    private JButton field;
    private ParcelSelectPopup parcelSelectPopup;
    
    protected static final String EDIT = "edit";
    
    public ParcelEditor(JFrame parent){
        field = new JButton();

        field.setFocusPainted(false);
        field.setMargin(new Insets(0, 0, 0, 0));
        field.setContentAreaFilled(false);
        field.setBorderPainted(false);
        field.setOpaque(false);
        field.setActionCommand(EDIT);
        
        field.addActionListener(this);
        //Set up the dialog that the button brings up.
        parcelSelectPopup = new ParcelSelectPopup(parent, true, null, null, field);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            //The user has clicked the cell, so
            //bring up the dialog.
            parcelSelectPopup.setVisible(true);

            //Make the renderer reappear.
            fireEditingStopped();

        } else { //User pressed dialog's "OK" button.
            currentParcel = new ParcelManagerImpl(DBUtils.setDataSource()).findParcelByID(Long.valueOf(field.getToolTipText()));
        }
    }
    
    public Object getCellEditorValue(){
        return field.getText();
    }
    
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        currentParcel = (Parcel)value;
        return field;
    }

}
