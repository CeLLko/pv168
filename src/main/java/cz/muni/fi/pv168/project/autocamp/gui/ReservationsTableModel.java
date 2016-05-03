/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Reservation;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * Adam Gdovin, 433305
 * @version May 3, 2016
 */
public class ReservationsTableModel extends AbstractTableModel {

    private List<Reservation> reservations = new ArrayList<Reservation>();

    @Override
    public int getRowCount() {
        return reservations.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Reservation reservation = reservations.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return reservation.getId();
            case 1:
                return reservation.getFrom();
            case 2:
                return reservation.getTo();
            case 3:
                return reservation.getGuest().getFullName();
            case 4:
                return reservation.getParcel().getLocation();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
}
