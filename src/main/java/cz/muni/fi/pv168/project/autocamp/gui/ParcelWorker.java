/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Parcel;
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
public class ParcelWorker {
    
    public static class CreateParcelWorker extends SwingWorker<List<Parcel>, Void> {
        
        private final Parcel parcel;
        private final ParcelsTableModel tableModel;
        public static List<Parcel> parcels;
        
        public CreateParcelWorker(Parcel parcel, ParcelsTableModel tableModel) {
            this.parcel = parcel;
            this.tableModel = tableModel;
        }

        @Override
        protected List<Parcel> doInBackground() throws Exception {
            tableModel.getManager().createParcel(parcel);
            parcels = tableModel.getManager().findAllParcels();
            return parcels;
        }
        
        protected void done() {
            tableModel.setParcels(parcels);
        }
    }
    
    public static class DeleteParcelWorker extends SwingWorker<List<Parcel>, Void> {

        private final ParcelsTableModel tableModel;
        private List<Parcel> parcels = new ArrayList<>();
        
        public DeleteParcelWorker(List<Parcel> parcels, ParcelsTableModel tableModel) {
            this.tableModel = tableModel;
            this.parcels.addAll(parcels);
        }
        
        @Override
        protected List<Parcel> doInBackground() throws Exception {
            for (int i = 0; i < parcels.size(); i++) {
                tableModel.getManager().deleteParcel(parcels.get(i));
            }
            parcels = tableModel.getManager().findAllParcels();
            return parcels;
        }

        @Override
        protected void done() {
            tableModel.setParcels(parcels);
        }
    }
    
    public static class FilterParcelWorker extends SwingWorker<List<Parcel>, Void> {

        private final ParcelsTableModel tableModel;
        private final String filter;
        private List<Parcel> parcels = new ArrayList<>();
        
        public FilterParcelWorker(String filter, ParcelsTableModel tableModel) {
            this.filter = filter;
            this.tableModel = tableModel;
        }
        
        @Override
        protected List<Parcel> doInBackground() throws Exception {
            parcels.addAll(tableModel.getManager().filterParcels(filter));
            return parcels;
        }

        @Override
        protected void done() {
            tableModel.setParcels(this.parcels);
        }
        
    }
}
