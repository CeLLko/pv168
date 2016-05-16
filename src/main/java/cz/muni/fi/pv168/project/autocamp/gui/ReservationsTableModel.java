/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Reservation;
import cz.muni.fi.pv168.project.autocamp.ReservationManager;
import cz.muni.fi.pv168.project.autocamp.ReservationManagerImpl;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * Adam Gdovin, 433305
 *
 * @version May 3, 2016
 */
public class ReservationsTableModel extends AbstractTableModel {

    private List<Reservation> reservations;
    private DataSource dataSource;
    private ReservationManager manager;

    public ReservationsTableModel() {
        dataSource = prepareDataSource();
        manager = new ReservationManagerImpl(dataSource);
        reservations = manager.findAllReservations();
    }

    private DataSource prepareDataSource() {
        ClientDataSource ds = new ClientDataSource();
        ds.setDatabaseName("pv168");
        ds.setUser("pv168");
        ds.setPassword("pv168");
        return ds;
    }

    public ReservationManager getManager() {
        return manager;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        clearReservationTable();
        reservations.stream().forEach((reservation) -> {
            this.reservations.add(reservation);
        });
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
                return reservation.getParcel().getLocation();
            case 4:
                return reservation.getGuest().getFullName();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "ID";
            case 1:
                return "From";
            case 2:
                return "To";
            case 3:
                return "Parcel";
            case 4:
                return "Guest";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public void clearReservationTable() {
        this.reservations.clear();
        this.fireTableDataChanged();
    }

    /*
    public void createReservation(String fullName, String phone) {
        CreateReservationWorker createReservationWorker = new CreateReservationWorker(fullName, phone, ReservationsTableModel.this);
        createReservationWorker.execute();
    }*/

 /*private static class CreateReservationWorker extends SwingWorker<List<Reservation>, Void> {

        private final Reservation reservation;
        private final ReservationsTableModel tableModel;
        public static List<Reservation> reservations;

        public CreateReservationWorker(String dateFrom, String dateTo, ReservationsTableModel tableModel) {
            this.reservation = new Reservation(, );
            this.tableModel = tableModel;
        }

        @Override
        protected List<Reservation> doInBackground() throws Exception {
            tableModel.getManager().createReservation(reservation);
            reservations = tableModel.getManager().findAllReservations();
            return reservations;
        }

        protected void done() {
            tableModel.setReservations(reservations);
        }
    }*/
    
    public void deleteReservation(int[] rows) {
        DeleteReservationWorker deleteReservationWorker = new DeleteReservationWorker(rows, ReservationsTableModel.this);
        deleteReservationWorker.execute();
    }

    public void filterReservations(String filter) {
        FilterReservationWorker filterReservationWorker = new FilterReservationWorker(filter, ReservationsTableModel.this);
        filterReservationWorker.execute();
    }

    private static class DeleteReservationWorker extends SwingWorker<List<Reservation>, Void> {

        private final ReservationsTableModel tableModel;
        private int[] rows;

        public DeleteReservationWorker(int[] rows, ReservationsTableModel tableModel) {
            this.tableModel = tableModel;
            this.rows = rows;
        }

        @Override
        protected List<Reservation> doInBackground() throws Exception {
            //for (int i = 0; i < reservations.size(); i++) {
            //    tableModel.getManager().deleteReservation(reservations.get(i));
            //}
            //reservations = tableModel.getManager().findAllReservations();
            //return reservations;
            return null;
        }

        @Override
        protected void done() {
            //tableModel.setReservations(reservations);
        }
    }

    private static class FilterReservationWorker extends SwingWorker<List<Reservation>, Void> {

        private final ReservationsTableModel tableModel;
        private final String filter;
        private List<Reservation> reservations = new ArrayList<>();

        public FilterReservationWorker(String filter, ReservationsTableModel tableModel) {
            this.filter = filter;
            this.tableModel = tableModel;
        }

        @Override
        protected List<Reservation> doInBackground() throws Exception {
            reservations.addAll(tableModel.getManager().filterReservations(filter));
            return reservations;
        }

        @Override
        protected void done() {
            tableModel.clearReservationTable();
            tableModel.setReservations(this.reservations);
        }
    }
}
