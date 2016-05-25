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
import java.util.Collections;

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
public class GuestsTableModel extends AbstractTableModel {

    private final List<Guest> guests;
    private final DataSource dataSource;
    private final GuestManager manager;
    private final JTable table;

    public GuestsTableModel(JTable table) {
        dataSource = DBUtils.setDataSource();
        manager = new GuestManagerImpl(dataSource);
        guests = Collections.synchronizedList(manager.findAllGuests());
        this.table = table;
    }

    public GuestManager getManager() {
        return manager;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        clearGuestTable();
        synchronized (guests) {
            guests.stream().forEach((guest) -> {
                this.guests.add(guest);
            });
        }
    }

    @Override
    public int getRowCount() {
        return guests.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Guest guest = guests.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return guest.getId();
            case 1:
                return guest.getFullName();
            case 2:
                return guest.getPhone();
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
                return LocalizationWizard.getString("Full_name");
            case 2:
                return LocalizationWizard.getString("Phone_number");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
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
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Guest guest = guests.get(rowIndex);
        switch (columnIndex) {
            case 0:
                guest.setId((Long) aValue);
                break;
            case 1:
                guest.setFullName((String) aValue);
                break;
            case 2:
                guest.setPhone((String) aValue);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        updateGuest(guest, rowIndex, columnIndex);
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
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public void updateGuest(Guest guest, int rowIndex, int columnIndex) {
        UpdateGuestWorker updateGuestWorker = new UpdateGuestWorker(guest, rowIndex, columnIndex, GuestsTableModel.this);
        updateGuestWorker.execute();
    }

    private class UpdateGuestWorker extends SwingWorker<Guest, Void> {

        private final Guest guest;
        private int rowIndex;
        private int coulmnIndex;

        public UpdateGuestWorker(Guest guest, int rowIndex, int ColumnIndex, GuestsTableModel tableModel) {
            this.guest = guest;
            this.rowIndex = rowIndex;
            this.coulmnIndex = coulmnIndex;
        }

        @Override
        protected Guest doInBackground() throws Exception {
            Guest oldGuest = manager.findGuestByID(guest.getId());
            GuestsTableModel.this.manager.updateGuest(guest);
            AutoCampMenu.logger.info("UPDATE:" + oldGuest.toString() + " was succesfully updated to " + guest.toString());
            return guest;
        }

        protected void done() {
            try {
                get();
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Update_guest") + "\n"
                        + LocalizationWizard.getString("Log_file_info"));
                AutoCampMenu.logger.error(ex.getMessage());
            }
            fireTableCellUpdated(rowIndex, coulmnIndex);
        }
    }

    public void createGuest(String fullName, String phone) {
        CreateGuestWorker createGuestWorker = new CreateGuestWorker(fullName, phone, GuestsTableModel.this);
        createGuestWorker.execute();
    }

    private class CreateGuestWorker extends SwingWorker<Guest, Void> {

        private final Guest guest;

        public CreateGuestWorker(String fullName, String phone, GuestsTableModel tableModel) {
            this.guest = new Guest(fullName, phone);
        }

        @Override
        protected Guest doInBackground() throws Exception {
            GuestsTableModel.this.manager.createGuest(guest);
            GuestsTableModel.this.guests.add(guest);
            return guest;
        }

        protected void done() {
            try {
                get();
                AutoCampMenu.logger.info("CREATE:" + guest.toString() + " was succesfully created.");
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Delete_guest") + "\n"
                        + LocalizationWizard.getString("Log_file_info"));
                AutoCampMenu.logger.error(ex.getMessage());
            }
            int row = GuestsTableModel.this.guests.size() - 1;
            GuestsTableModel.this.fireTableRowsInserted(row, row);
        }
    }

    public void deleteGuest(int[] rows) {
        DeleteGuestWorker deleteGuestWorker = new DeleteGuestWorker(rows);
        deleteGuestWorker.execute();
    }

    private class DeleteGuestWorker extends SwingWorker<Boolean, Void> {

        private int[] rows;
        private boolean result = true;

        public DeleteGuestWorker(int[] rows) {
            this.rows = rows;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            for (int i = rows.length - 1; i >= 0; i--) {
                Guest guest = GuestsTableModel.this.guests.get(rows[i]);
                try {
                    GuestsTableModel.this.manager.deleteGuest(guest);
                    GuestsTableModel.this.guests.remove(guest);
                    AutoCampMenu.logger.info("DELETE: " + guest.toString() + " was succesfully deleted.");
                } catch (DBInteractionException e) {
                    AutoCampMenu.logger.error("Could not delete " + guest.toString() + "; Exception: " + e.getMessage());
                    result = false;
                }
            }
            return result;
        }

        @Override
        protected void done() {
            try {
                if (!get()) {
                    JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Delete_guest") + "\n"
                            + LocalizationWizard.getString("Log_file_info"));
                } else {
                    AutoCampMenu.logger.info("DELETE: All selected guests were succesfully deleted.");
                }
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Delete") + "\n"
                        + LocalizationWizard.getString("Log_file_info"));
                AutoCampMenu.logger.error(ex.getMessage());
            }
            for (int i = rows.length - 1; i >= 0; i--) {
                GuestsTableModel.this.fireTableRowsDeleted(rows[i], rows[i]);
            }
        }
    }

    public void filterGuests(String filter) {
        FilterGuestWorker filterGuestWorker = new FilterGuestWorker(filter);
        filterGuestWorker.execute();
    }

    private class FilterGuestWorker extends SwingWorker<List<Guest>, Void> {

        private final String filter;

        public FilterGuestWorker(String filter) {
            this.filter = filter;
        }

        @Override
        protected List<Guest> doInBackground() throws Exception {
            GuestsTableModel.this.setGuests(GuestsTableModel.this.manager.filterGuests(filter));
            return GuestsTableModel.this.guests;
        }

        @Override
        protected void done() {
            try {
                get();
                AutoCampMenu.logger.info("FILTER: Guests were succesfully filtered, filter: " + filter + ".");
                GuestsTableModel.this.fireTableDataChanged();
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(table, LocalizationWizard.getString("Filter_error") + "\n"
                        + LocalizationWizard.getString("Log_file_info"));
                AutoCampMenu.logger.error(ex.getMessage());
            }
        }
    }

    public void clearGuestTable() {
        this.guests.clear();
        this.fireTableDataChanged();
    }
}
