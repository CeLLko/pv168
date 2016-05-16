/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Guest;
import cz.muni.fi.pv168.project.autocamp.GuestManager;
import cz.muni.fi.pv168.project.autocamp.GuestManagerImpl;
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
public class GuestsTableModel extends AbstractTableModel {

    private List<Guest> guests;
    private DataSource dataSource;
    private GuestManager manager;

    public GuestsTableModel() {
        dataSource = prepareDataSource();
        manager = new GuestManagerImpl(dataSource);
        guests = manager.findAllGuests();
    }

    private DataSource prepareDataSource() {
        ClientDataSource ds = new ClientDataSource();
        ds.setDatabaseName("pv168");
        ds.setUser("pv168");
        ds.setPassword("pv168");
        return ds;
    }

    public GuestManager getManager() {
        return manager;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        clearGuestTable();
        guests.stream().forEach((guest) -> {
            this.guests.add(guest);
        });
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
                return "Full Name";
            case 2:
                return "Phone Number";
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
        manager.updateGuest(guest);
        fireTableCellUpdated(rowIndex, columnIndex);
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

    public void clearGuestTable() {
        this.guests.clear();
        this.fireTableDataChanged();
    }

    public void createGuest(String fullName, String phone) {
        CreateGuestWorker createGuestWorker = new CreateGuestWorker(fullName, phone, GuestsTableModel.this);
        createGuestWorker.execute();
    }
    private static class CreateGuestWorker extends SwingWorker<List<Guest>, Void> {
        
        private final Guest guest;
        private final GuestsTableModel tableModel;
        public static List<Guest> guests;
        
        public CreateGuestWorker(String fullName, String phone, GuestsTableModel tableModel) {
            this.guest = new Guest(fullName, phone);
            this.tableModel = tableModel;
        }

        @Override
        protected List<Guest> doInBackground() throws Exception {
            tableModel.getManager().createGuest(guest);
            guests = tableModel.getManager().findAllGuests();
            return guests;
        }
        
        protected void done() {
            tableModel.setGuests(guests);
        }
    }
    

    public void deleteGuest(int[] rows) {
        DeleteGuestWorker deleteGuestWorker = new DeleteGuestWorker(guests, GuestsTableModel.this);
        deleteGuestWorker.execute();
    }

    public void filterGuests(String filter) {
        FilterGuestWorker filterGuestWorker = new FilterGuestWorker(filter, GuestsTableModel.this);
        filterGuestWorker.execute();
    }
    private static class DeleteGuestWorker extends SwingWorker<List<Guest>, Void> {

        private final GuestsTableModel tableModel;
        private List<Guest> guests = new ArrayList<>();
        
        public DeleteGuestWorker(List<Guest> guests, GuestsTableModel tableModel) {
            this.tableModel = tableModel;
            this.guests.addAll(guests);
        }
        
        @Override
        protected List<Guest> doInBackground() throws Exception {
            for (int i = 0; i < guests.size(); i++) {
                tableModel.getManager().deleteGuest(guests.get(i));
            }
            guests = tableModel.getManager().findAllGuests();
            return guests;
        }

        @Override
        protected void done() {
            tableModel.setGuests(guests);
        }
    }
    
    private static class FilterGuestWorker extends SwingWorker<List<Guest>, Void> {

        private final GuestsTableModel tableModel;
        private final String filter;
        private List<Guest> guests = new ArrayList<>();
        
        public FilterGuestWorker(String filter, GuestsTableModel tableModel) {
            this.filter = filter;
            this.tableModel = tableModel;
        }
        
        @Override
        protected List<Guest> doInBackground() throws Exception {
            guests.addAll(tableModel.getManager().filterGuests(filter));
            return guests;
        }

        @Override
        protected void done() {
            tableModel.clearGuestTable();
            tableModel.setGuests(this.guests);
        }
    }
}
