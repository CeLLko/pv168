/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.DBInteractionException;
import cz.muni.fi.pv168.project.autocamp.Guest;
import cz.muni.fi.pv168.project.autocamp.GuestManager;
import cz.muni.fi.pv168.project.autocamp.GuestManagerImpl;
import cz.muni.fi.pv168.project.autocamp.Parcel;
import cz.muni.fi.pv168.project.autocamp.ParcelManager;
import cz.muni.fi.pv168.project.autocamp.Reservation;
import cz.muni.fi.pv168.project.autocamp.ReservationManager;
import cz.muni.fi.pv168.project.autocamp.ReservationManagerImpl;
import cz.muni.fi.pv168.project.autocamp.ParcelManagerImpl;
import static cz.muni.fi.pv168.project.autocamp.gui.AutoCampMenu.logger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

/**
 *
 * Adam Gdovin, 433305
 *
 * @version May 3, 2016
 */
public class ReservationsTableModel extends AbstractTableModel {

    private final List<Reservation> reservations;
    private final DataSource dataSource;
    private final ReservationManager manager;
    private final GuestManager guestManager;
    private final ParcelManager parcelManager;
    private final JTable table;

    public ReservationsTableModel(JTable table) {
        dataSource = DBUtils.setDataSource();
        manager = new ReservationManagerImpl(dataSource);
        guestManager = new GuestManagerImpl(dataSource);
        parcelManager = new ParcelManagerImpl(dataSource);
        reservations = Collections.synchronizedList(manager.findAllReservations());
        this.table = table;
    }

    public ReservationManager getManager() {
        return manager;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        clearReservationTable();
        synchronized (reservations) {
            reservations.stream().forEach((reservation) -> {
                this.reservations.add(reservation);
            });
        }
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
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Parcel.class;
            case 4:
                return Guest.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
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
                return reservation.getParcel();
            case 4:
                return reservation.getGuest();
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
                reservation.setParcel((Parcel) parcelManager.findParcelByID(Long.valueOf((String) aValue)));
                break;
            case 4:
                reservation.setGuest((Guest) guestManager.findGuestByID(Long.valueOf((String) aValue)));
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

    public void createReservation(Date from, Date to, Long guest, Long parcel) {
        CreateReservationWorker createReservationWorker = new CreateReservationWorker(from, to, guest, parcel, ReservationsTableModel.this);
        createReservationWorker.execute();
    }

    private class CreateReservationWorker extends SwingWorker<Reservation, Void> {

        private final Reservation reservation;

        public CreateReservationWorker(Date from, Date to, Long guestId, Long parcelId, ReservationsTableModel tableModel) {
            Guest guest = new GuestManagerImpl(dataSource).findGuestByID(guestId);
            Parcel parcel = new ParcelManagerImpl(dataSource).findParcelByID(parcelId);
            LocalDate fromLocal = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toLocal = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            this.reservation = new Reservation(fromLocal, toLocal, guest, parcel);
        }

        @Override
        protected Reservation doInBackground() throws Exception {
            ReservationsTableModel.this.manager.createReservation(reservation);
            ReservationsTableModel.this.reservations.add(reservation);
            return reservation;
        }

        protected void done() {
            try {
                get();
                AutoCampMenu.logger.info("CREATE:" + reservation.toString() + " was succesfully created.");
                int row = ReservationsTableModel.this.reservations.size() - 1;
                ReservationsTableModel.this.fireTableRowsInserted(row, row);
            } catch (InterruptedException | ExecutionException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Create_reservation") + "\n"
                        + LocalizationWizard.getString("Log_file_info"));
                logger.error(ex.getMessage());
            }
        }
    }

    public void updateReservation(Reservation reservation, int rowIndex, int columnIndex) {
        UpdateReservationWorker updateReservationWorker = new UpdateReservationWorker(reservation, rowIndex, columnIndex, ReservationsTableModel.this);
        updateReservationWorker.execute();
    }

    private class UpdateReservationWorker extends SwingWorker<Reservation, Void> {

        private final Reservation reservation;
        private int rowIndex;
        private int coulmnIndex;

        public UpdateReservationWorker(Reservation reservation, int rowIndex, int columnIndex, ReservationsTableModel tableModel) {
            this.reservation = reservation;
            this.rowIndex = rowIndex;
            this.coulmnIndex = columnIndex;
        }

        @Override
        protected Reservation doInBackground() throws Exception {
            Reservation oldReservation = manager.findReservationByID(reservation.getId());
            ReservationsTableModel.this.manager.updateReservation(reservation);
            AutoCampMenu.logger.info("UPDATE:" + oldReservation.toString() + " was succesfully updated to " + reservation.toString());
            return reservation;
        }

        protected void done() {
            try {
                get();
                fireTableCellUpdated(rowIndex, coulmnIndex);
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Update_reservation") + "\n"
                        + LocalizationWizard.getString("Log_file_info"));
                AutoCampMenu.logger.error(ex.getMessage());
            }
        }
    }

    public void deleteReservation(int[] rows) {
        DeleteReservationWorker deleteReservationWorker = new DeleteReservationWorker(rows);
        deleteReservationWorker.execute();
    }

    private class DeleteReservationWorker extends SwingWorker<Boolean, Void> {

        private int[] rows;
        private boolean result = true;

        public DeleteReservationWorker(int[] rows) {
            this.rows = rows;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            for (int i = rows.length - 1; i >= 0; i--) {
                Reservation reservation = ReservationsTableModel.this.reservations.get(rows[i]);
                try {
                    ReservationsTableModel.this.manager.deleteReservation(reservation);
                    ReservationsTableModel.this.reservations.remove(reservation);
                    AutoCampMenu.logger.info("DELETE: " + reservation.toString() + " was succesfully deleted.");
                } catch (DBInteractionException e) {
                    AutoCampMenu.logger.error("Could not delete " + reservation.toString() + "; Exception: " + e.getMessage());
                    result = false;
                }

            }
            return result;
        }

        @Override
        protected void done() {
            try {
                if (!get()) {
                    JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Delete_reservation") + "\n"
                            + LocalizationWizard.getString("Log_file_info"));
                }
                for (int i = rows.length - 1; i >= 0; i--) {
                    ReservationsTableModel.this.fireTableRowsDeleted(rows[i], rows[i]);
                }
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Delete") + "\n"
                        + LocalizationWizard.getString("Log_file_info"));
                logger.error(ex.getMessage());
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
            ReservationsTableModel.this.setReservations(Collections.synchronizedList(ReservationsTableModel.this.manager.filterReservations(filter)));
            return ReservationsTableModel.this.reservations;
        }

        @Override
        protected void done() {
            try {
                get();
                AutoCampMenu.logger.info("FILTER: Reservations were succesfully filtered, filter: " + filter + ".");
                ReservationsTableModel.this.fireTableDataChanged();
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Filter_error") + "\n"
                        + LocalizationWizard.getString("Log_file_info"));
                AutoCampMenu.logger.error(ex.getMessage());
            }
        }
    }

    public void clearReservationTable() {
        this.reservations.clear();
        this.fireTableDataChanged();
    }
}
