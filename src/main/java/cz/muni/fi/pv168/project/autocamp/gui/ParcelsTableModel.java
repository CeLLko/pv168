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
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * Adam Gdovin, 433305
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

    @Override
    public int getRowCount() {
        return parcels.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
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
        manager.createParcel(parcel);
        parcels = manager.findAllParcels(); 
    }
    
    public void deleteParcel(Parcel parcel) {
        manager.deleteParcel(parcel);
        parcels = manager.findAllParcels();
    }
}