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
import javax.swing.JTable;
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

    private List<Guest> guests = new ArrayList<Guest>();

    DataSource dataSource;
    GuestManager guestManager;

    public GuestsTableModel() {
        dataSource = prepareDataSource();
        guestManager = new GuestManagerImpl(dataSource);
        fillTable("");
    }

    public void filterTable(String filter) {
        fillTable(filter);
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
        guestManager.updateGuest(guest);
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
    public void addGuest(Guest guest) {
        this.guests.add(guest);
        int lastRow = this.guests.size() - 1;
        this.fireTableRowsInserted(lastRow, lastRow);
    }

    public void fillTable() {
        GuestsTableModel.fillingWorker mySwingWorker = new GuestsTableModel.fillingWorker();
        mySwingWorker.execute();
    }

    public void fillTable(String param) {
        GuestsTableModel.fillingWorker mySwingWorker = new GuestsTableModel.fillingWorker(param);
        mySwingWorker.execute();
    }
    
    public void clearTable(){
        this.guests.clear();
    }

    public void removeGuest(long ID, int row, JTable myTable) {
        GuestManagerImpl manager = new GuestManagerImpl(this.dataSource);
        Guest guest = manager.findGuestByID(ID);
        if(guest == null) {
            throw new UnsupportedOperationException("guest already deleted");
        } else {
            manager.deleteGuest(guest);
            this.guests.remove(guest);
        }
    }
    
    private DataSource prepareDataSource() {
        ClientDataSource ds = new ClientDataSource();
        ds.setDatabaseName("pv168");
        ds.setUser("pv168");
        ds.setPassword("pv168");
        return ds;
    }
    
    private class fillingWorker extends SwingWorker<List<Guest>, Void> {
        private List<Guest> filteredGuests;
        private String filter = "";

        private fillingWorker() {
        }
        private fillingWorker(String param) {
            filter = param;
        }

        protected List<Guest> doInBackground() throws Exception {
            GuestsTableModel.this.dataSource = prepareDataSource();
            GuestManager manager = new GuestManagerImpl(GuestsTableModel.this.dataSource);
            this.filteredGuests = new ArrayList(manager.filterGuestsWithGivenParamter(filter));
            return this.filteredGuests;
        }

        protected void done() {
            GuestsTableModel.this.clearTable();
            for(int i = 0; i < this.filteredGuests.size(); ++i) {
                GuestsTableModel.this.addGuest((Guest)this.filteredGuests.get(i));
            }
        }
    }
}
