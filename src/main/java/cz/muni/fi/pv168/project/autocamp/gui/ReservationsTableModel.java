/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Guest;
import cz.muni.fi.pv168.project.autocamp.GuestManagerImpl;
import cz.muni.fi.pv168.project.autocamp.Parcel;
import cz.muni.fi.pv168.project.autocamp.Reservation;
import cz.muni.fi.pv168.project.autocamp.ReservationManager;
import cz.muni.fi.pv168.project.autocamp.ReservationManagerImpl;
import cz.muni.fi.pv168.project.autocamp.ParcelManagerImpl;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Reservation reservation = reservations.get(rowIndex);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        switch (columnIndex) {
            case 0:
                reservation.setId((Long) aValue);
                break;
            case 1:
                LocalDate from = LocalDate.parse((CharSequence) aValue, dtf);
                reservation.setFrom(from);
                break;
            case 2:
                LocalDate to = LocalDate.parse((CharSequence) aValue, dtf);
                reservation.setTo(to);
                break;
            case 3:
                reservation.setParcel((Parcel) new ParcelManagerImpl(dataSource).findParcelByLocation((String) aValue));
                break;
            case 4:
                reservation.setGuest((Guest) new GuestManagerImpl(dataSource).findGuestsByName((String) aValue));
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        updateReservation(reservation, rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "ID";
            case 1:
                return LocalizationWizard.getString("From");
            case 2:
                return LocalizationWizard.getString("To");
            case 3:
                return LocalizationWizard.getString("Parcel");
            case 4:
                return LocalizationWizard.getString("Guest");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return true;
            case 3:
                return true;
            case 4:
                return true;
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

 /*private class CreateReservationWorker extends SwingWorker<List<Reservation>, Void> {

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
    public void updateReservation(Reservation reservation, int rowIndex, int columnIndex) {
        UpdateReservationWorker updateReservationWorker = new UpdateReservationWorker(reservation, rowIndex, columnIndex, ReservationsTableModel.this);
        updateReservationWorker.execute();
    }

    private class UpdateReservationWorker extends SwingWorker<Reservation, Void> {

        private final Reservation reservation;
        private int rowIndex;
        private int coulmnIndex;

        public UpdateReservationWorker(Reservation reservation, int rowIndex, int ColumnIndex, ReservationsTableModel tableModel) {
            this.reservation = reservation;
            this.rowIndex = rowIndex;
            this.coulmnIndex = coulmnIndex;
        }

        @Override
        protected Reservation doInBackground() throws Exception {
            ReservationsTableModel.this.manager.updateReservation(reservation);
            return reservation;
        }

        protected void done() {
            fireTableCellUpdated(rowIndex, coulmnIndex);
        }
    }

    public void deleteReservation(int[] rows) {
        DeleteReservationWorker deleteReservationWorker = new DeleteReservationWorker(rows);
        deleteReservationWorker.execute();
    }

    private class DeleteReservationWorker extends SwingWorker<int[], Void> {

        private int[] rows;

        public DeleteReservationWorker(int[] rows) {
            this.rows = rows;
        }

        @Override
        protected int[] doInBackground() throws Exception {
            for (int i = rows.length - 1; i >= 0; i--) {
                Reservation reservation = ReservationsTableModel.this.reservations.get(rows[i]);
                ReservationsTableModel.this.manager.deleteReservation(reservation);
                ReservationsTableModel.this.reservations.remove(reservation);
            }
            return rows;
        }

        @Override
        protected void done() {
            for (int i = rows.length - 1; i >= 0; i--) {
                ReservationsTableModel.this.fireTableRowsDeleted(rows[i], rows[i]);
            }
        }
    }

    public void filterReservations(String filter) {
        FilterReservationWorker filterReservationWorker = new FilterReservationWorker(filter);
        filterReservationWorker.execute();
    }

    private class FilterReservationWorker extends SwingWorker<List<Reservation>, Void> {

        private final String filter;

        public FilterReservationWorker(String filter) {
            this.filter = filter;
        }

        @Override
        protected List<Reservation> doInBackground() throws Exception {
            ReservationsTableModel.this.setReservations(ReservationsTableModel.this.manager.filterReservations(filter));
            return ReservationsTableModel.this.reservations;
        }

        @Override
        protected void done() {
            ReservationsTableModel.this.fireTableDataChanged();
        }
    }
}
