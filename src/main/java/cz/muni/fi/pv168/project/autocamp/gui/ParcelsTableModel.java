/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.Guest;
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
                return LocalizationWizard.getString("Location");
            case 2:
                return LocalizationWizard.getString("Electricity");
            case 3:
                return LocalizationWizard.getString("Water");
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Parcel parcel = parcels.get(rowIndex);
        switch (columnIndex) {
            case 0:
                parcel.setId((Long) aValue);
                break;
            case 1:
                parcel.setLocation((String) aValue);
                break;
            case 2:
                parcel.setWithElectricity((boolean) aValue);
                break;
            case 3:
                parcel.setWithElectricity((boolean) aValue);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        updateParcel(parcel, rowIndex, columnIndex);
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
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public void updateParcel(Parcel parcel, int rowIndex, int columnIndex) {
        UpdateParcelWorker updateParcelWorker = new UpdateParcelWorker(parcel, rowIndex, columnIndex, ParcelsTableModel.this);
        updateParcelWorker.execute();
    }

    private class UpdateParcelWorker extends SwingWorker<Parcel, Void> {

        private final Parcel parcel;
        private int rowIndex;
        private int coulmnIndex;

        public UpdateParcelWorker(Parcel parcel, int rowIndex, int ColumnIndex, ParcelsTableModel tableModel) {
            this.parcel = parcel;
            this.rowIndex = rowIndex;
            this.coulmnIndex = coulmnIndex;
        }

        @Override
        protected Parcel doInBackground() throws Exception {
            ParcelsTableModel.this.manager.updateParcel(parcel);
            return parcel;
        }

        protected void done() {
            fireTableCellUpdated(rowIndex, coulmnIndex);
        }
    }

    public void createParcel(String location, Boolean withElectricity, Boolean withWater) {
        CreateParcelWorker createParcelWorker = new CreateParcelWorker(location, withElectricity, withWater);
        createParcelWorker.execute();
    }

    private class CreateParcelWorker extends SwingWorker<Parcel, Void> {

        private final Parcel parcel;

        public CreateParcelWorker(String location, Boolean withElectricity, Boolean withWater) {
            this.parcel = new Parcel(location, withElectricity, withWater);
        }

        @Override
        protected Parcel doInBackground() throws Exception {
            ParcelsTableModel.this.manager.createParcel(parcel);
            ParcelsTableModel.this.parcels.add(parcel);
            return parcel;
        }

        protected void done() {
            int row = ParcelsTableModel.this.parcels.size() - 1;
            ParcelsTableModel.this.fireTableRowsInserted(row, row);
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
            for (int i = rows.length - 1; i >= 0; i--) {
                Parcel parcel = ParcelsTableModel.this.parcels.get(rows[i]);
                ParcelsTableModel.this.manager.deleteParcel(parcel);
                ParcelsTableModel.this.parcels.remove(parcel);
            }
            return rows;
        }

        @Override
        protected void done() {
            for (int i = rows.length - 1; i >= 0; i--) {
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

        public FilterParcelWorker(String filter) {
            this.filter = filter;
        }

        @Override
        protected List<Parcel> doInBackground() throws Exception {
            ParcelsTableModel.this.setParcels(ParcelsTableModel.this.manager.filterParcels(filter));
            return ParcelsTableModel.this.parcels;
        }

        @Override
        protected void done() {
            ParcelsTableModel.this.fireTableDataChanged();
        }

    }

    public void clearParcelTable() {
        this.parcels.clear();
        this.fireTableDataChanged();
    }
}
