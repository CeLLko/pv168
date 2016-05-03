/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Reservation;
import cz.muni.fi.pv168.project.autocamp.ReservationManagerImpl;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import javax.swing.table.AbstractTableModel;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * Adam Gdovin, 433305
 * @version May 3, 2016
 */
public class ReservationsTableModel extends AbstractTableModel {

    private List<Reservation> reservations;
    private DataSource dataSource;

    public ReservationsTableModel() {
        dataSource = prepareDataSource();
        reservations = new ReservationManagerImpl(dataSource).findAllReservations();
    }

    private DataSource prepareDataSource() {
        ClientDataSource ds = new ClientDataSource();
        ds.setDatabaseName("pv168");
        ds.setUser("pv168");
        ds.setPassword("pv168");
        return ds;
    }
    
    @Override
    public int getRowCount() {
        return reservations.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
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
