/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Guest;
import cz.muni.fi.pv168.project.autocamp.GuestManagerImpl;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

/**
 *
 * Adam Gdovin, 433305
 *
 * @version May 16, 2016
 */
public class GuestSelectPopupTableModel extends AbstractTableModel {

    private List<Guest> guests;
    private List<Guest> emptyGuests;
    private DataSource dataSource;
    private GuestManagerImpl manager;

    public GuestSelectPopupTableModel() {
        dataSource = DBUtils.setDataSource();
        manager = new GuestManagerImpl(dataSource);
        guests = manager.findAllGuests();
    }

    public GuestManagerImpl getManager() {
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

    public Guest selectGuest(int row) {
        return this.guests.get(row);
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
            GuestSelectPopupTableModel.this.setGuests(GuestSelectPopupTableModel.this.manager.filterGuests(filter));
            return GuestSelectPopupTableModel.this.guests;
        }

        @Override
        protected void done() {
            GuestSelectPopupTableModel.this.fireTableDataChanged();
        }
    }

    public void clearGuestTable() {
        this.guests.clear();
        this.fireTableDataChanged();
    }
}
