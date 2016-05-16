/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Guest;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Lenka
 */
public class GuestWorker {
    
    public static class CreateGuestWorker extends SwingWorker<List<Guest>, Void> {
        
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
    
    public static class DeleteGuestWorker extends SwingWorker<List<Guest>, Void> {

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
    
    public static class FilterGuestWorker extends SwingWorker<List<Guest>, Void> {

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
