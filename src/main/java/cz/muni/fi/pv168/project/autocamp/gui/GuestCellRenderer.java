/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Guest;
import static cz.muni.fi.pv168.project.autocamp.gui.GuestCellEditor.EDIT;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * Adam Gdovin, 433305
 * @version May 17, 2016
 */
public class GuestCellRenderer extends JButton implements TableCellRenderer {

    public GuestCellRenderer(){
        this.setFocusPainted(false);
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setOpaque(false);
    }
    
    public Component getTableCellRendererComponent(
            JTable table, Object guest,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        Guest newGuest = (Guest) guest;
        setText(newGuest.getFullName());
        setToolTipText(String.valueOf(newGuest.getId()));
        return this;
    }
}
