/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Parcel;
import cz.muni.fi.pv168.project.autocamp.ParcelManagerImpl;
import java.util.ArrayList;

import java.util.List;
import javax.sql.DataSource;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 *
 * @version May 3, 2016
 */
public class ParcelsTableModel extends AbstractTableModel {

    private List<Parcel> parcels;
    private DataSource dataSource;
    private ParcelManagerImpl manager;

    public ParcelsTableModel() {
        dataSource = prepareDataSource();
        manager = new ParcelManagerImpl(dataSource);
        parcels = manager.findAllParcels();
    }

    private DataSource prepareDataSource() {
        ClientDataSource ds = new ClientDataSource();
        ds.setDatabaseName("pv168");
        ds.setUser("pv168");
        ds.setPassword("pv168");
        return ds;
    }

    public ParcelManagerImpl getManager() {
        return manager;
    }

    public List<Parcel> getParcels() {
        return parcels;
    }

    public void setParcels(List<Parcel> parcels) {
        clearParcelTable();
        parcels.stream().forEach((parcel) -> {
            this.parcels.add(parcel);
        });
    }

    @Override
    public int getRowCount() {
        return parcels.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "ID";
            case 1:
                return "Location";
            case 2:
                return "Electricity";
            case 3:
                return "Water";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Parcel parcel = parcels.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return parcel.getId();
            case 1:
                return parcel.getLocation();
            case 2:
                return parcel.isWithElectricity();
            case 3:
                return parcel.isWithWater();
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
                return Boolean.class;
            case 3:
                return Boolean.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public void createParcel(Parcel parcel) {
        CreateParcelWorker createParcelWorker = new CreateParcelWorker(parcel);
        createParcelWorker.execute();
    }
    
    private class CreateParcelWorker extends SwingWorker<List<Parcel>, Void> {
        
        private final Parcel parcel;
        public List<Parcel> parcels;
        
        public CreateParcelWorker(Parcel parcel) {
            this.parcel = parcel;
        }

        @Override
        protected List<Parcel> doInBackground() throws Exception {
            ParcelsTableModel.this.manager.createParcel(parcel);
            parcels = ParcelsTableModel.this.manager.findAllParcels();
            return parcels;
        }
        
        protected void done() {
            ParcelsTableModel.this.setParcels(parcels);
        }
    }

    public void deleteParcel(int[] rows) {
        DeleteParcelWorker deleteParcelWorker = new DeleteParcelWorker(rows);
        deleteParcelWorker.execute();
    }
    
    public class DeleteParcelWorker extends SwingWorker<int[], Void> {

        private int[] rows;
        
        public DeleteParcelWorker(int[] rows) {
            this.rows = rows;
        }
        
        @Override
        protected int[] doInBackground() throws Exception {
            for (int i = rows.length-1; i >= 0; i--) {
                Parcel parcel = ParcelsTableModel.this.parcels.get(rows[i]);
                ParcelsTableModel.this.manager.deleteParcel(parcel);
                ParcelsTableModel.this.parcels.remove(parcel);
            }
            return rows;
        }

        @Override
        protected void done() {
            for (int i = rows.length-1; i >= 0; i--) {
                ParcelsTableModel.this.fireTableRowsDeleted(rows[i], rows[i]);
            }
        }
    }

    public void filterParcels(String filter) {
        FilterParcelWorker filterParcelWorker = new FilterParcelWorker(filter);
        filterParcelWorker.execute();
    }
    
    public class FilterParcelWorker extends SwingWorker<List<Parcel>, Void> {

        private final String filter;
        private List<Parcel> parcels = new ArrayList<>();
        
        public FilterParcelWorker(String filter) {
            this.filter = filter;
        }
        
        @Override
        protected List<Parcel> doInBackground() throws Exception {
            parcels.addAll(ParcelsTableModel.this.manager.filterParcels(filter));
            return parcels;
        }

        @Override
        protected void done() {
            ParcelsTableModel.this.setParcels(this.parcels);
        }
        
    }

    public void clearParcelTable() {
        this.parcels.clear();
        this.fireTableDataChanged();
    }
}
