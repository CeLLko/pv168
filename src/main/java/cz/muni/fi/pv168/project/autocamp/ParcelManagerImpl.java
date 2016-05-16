package cz.muni.fi.pv168.project.autocamp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.sql.DataSource;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 21, 2016
 */
public class ParcelManagerImpl implements ParcelManager {
    
    private final DataSource dataSource;
    
    public ParcelManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createParcel(Parcel parcel) throws DBInteractionException {
        validate(parcel);
        
        if (parcel.getId() != null) {
            throw new IllegalArgumentException("ID of the parcel is already set.");
        }
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "INSERT INTO PARCEL (location,electricity,water) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            
            st.setString(1, parcel.getLocation());
            st.setBoolean(2, parcel.isWithElectricity());
            st.setBoolean(3, parcel.isWithWater());
            
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new DBInteractionException("Internal Error: More rows were inserted ("
                    + addedRows + ") while trying to insert parcel " + parcel);
            }
            
            ResultSet keyRS = st.getGeneratedKeys();
            parcel.setId(getKey(keyRS, parcel));
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while inserting parcel " + parcel, ex);
        }
    }

    private void validate(Parcel parcel) throws IllegalArgumentException{
        if(parcel == null) {
            throw new IllegalArgumentException("Parcel is null");
        }
        if(parcel.getLocation() == null) {
            throw new IllegalArgumentException("Location is null");
        }
        if (!Pattern.matches("\\w+:\\d+", parcel.getLocation())) {
            throw new IllegalArgumentException("Location has wrong format");
        }
    }
    
    private Long getKey(ResultSet keyRS, Parcel parcel) throws SQLException, DBInteractionException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new DBInteractionException("Internal Error: Generated key"
                        + "retriving failed when trying to insert parcel " + parcel
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new DBInteractionException("Internal Error: Generated key"
                        + "retriving failed when trying to insert parcel " + parcel
                        + " - more keys found");
            }
            return result;
        } else {
            throw new DBInteractionException("Internal Error: Generated key"
                    + "retriving failed when trying to insert parcel " + parcel
                    + " - no key found");
        }
    }
    
    
    @Override
    public void updateParcel(Parcel parcel) {
        if (parcel == null) {
            throw new IllegalArgumentException("Parcel is null.");
        }
        
        if (parcel.getId() == null) {
            throw new IllegalArgumentException("Parcel's id is null.");
        }
        
        validate(parcel);
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                    "UPDATE parcel SET location = ?, electricity = ?, water = ? WHERE id = ?")) {
            
            st.setString(1, parcel.getLocation());
            st.setBoolean(2, parcel.isWithElectricity());
            st.setBoolean(3, parcel.isWithWater());
            st.setLong(4, parcel.getId());
            
            int count = st.executeUpdate();
            if (count == 0) {
                throw new DBInteractionException("Parcel " + parcel + " does not exist within the database.");
            }else if (count != 1) {
                throw new DBInteractionException("Invalid number of rows updated (only one row should be updated):" + count);
            }
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while updating parcel " + parcel, ex);
        }
    }

    @Override
    public void deleteParcel(Parcel parcel) {
        if (parcel == null) {
            throw new IllegalArgumentException("Parcel is null.");
        }
        
        if (parcel.getId() == null) {
            throw new IllegalArgumentException("Parcel's id is null.");
        }
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "DELETE FROM parcel WHERE id = ? AND id NOT IN "
                             + "(SELECT parcel FROM reservation)")) {
            
            st.setLong(1, parcel.getId());
            
            int count = st.executeUpdate();
            if (count == 0) {
                throw new DBInteractionException(
                        "Given parcel " + parcel + " does not exist within the database.");
            }
            else if(count != 1) {
                throw new DBInteractionException(
                        "Invalid number of rows deleted (only one row should be deleted):" + count);
            }
        } catch (SQLException ex) {
            throw new DBInteractionException(
                    "Error while deleting parcel " + parcel, ex);
        }
    }

    @Override
    public Parcel findParcelByID(Long id)  throws  DBInteractionException{
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id,location,electricity,water FROM parcel WHERE id = ?")) {
            
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                Parcel parcel = resultSetToParcel(rs);
                
                if (rs.next()) {
                    throw new DBInteractionException(
                        "Internal Error: More entities with the same id"
                        + "(given id:" + id +"; found parcels:" + parcel
                        + " and " + resultSetToParcel(rs) + ")");
                }
                
                return parcel;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new DBInteractionException(
                    "Error while retrieving parcel with id " + id, ex);
        }
    }
    @Override
    public Parcel findParcelByLocation(String location) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                        "SELECT * FROM PARCEL WHERE location = ?")) {
            
            st.setString(1, location);
            
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Parcel parcel = resultSetToParcel(rs);
                
                if (rs.next()) {
                    throw new DBInteractionException(
                        "Internal Error: More entities with the same location"
                        + "(given location:" + location +"; found parcels:" + parcel
                        + " and " + resultSetToParcel(rs) + ")");
                }
                
                return parcel;
            } else {
                return null;
            }

        } catch (SQLException ex) {
            throw new DBInteractionException("Error when retrieving parcels with location" + location, ex);
        }
    }

    @Override
    public List<Parcel> findAllParcels() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id,location,electricity,water FROM parcel")) {
            
            ResultSet rs = st.executeQuery();
            
            List<Parcel> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToParcel(rs));
            }
            return result;
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving all parcels.", ex);
        }
    }

    /**
     * 
     * @param rs
     * @return
     * @throws SQLException 
     */
   public static Parcel resultSetToParcel(ResultSet rs) throws SQLException{
        Parcel parcel = new Parcel();
        
        parcel.setId(rs.getLong("id"));
        parcel.setLocation(rs.getString("location"));
        parcel.setWithElectricity(rs.getBoolean("electricity"));
        parcel.setWithWater(rs.getBoolean("water"));
        
        return parcel;
    }

    @Override
    public List<Parcel> filterParcels(String filter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id,location,electricity,water FROM PARCEL WHERE LOWER(location) LIKE LOWER(?) OR id = ?")) {
            
            st.setString(1, "%"+filter+"%");
            try {
                st.setLong(2, Long.valueOf(filter));
            } catch (NumberFormatException ex) {
                st.setLong(2, (long) -1);
            }
            
            ResultSet rs = st.executeQuery();
            
            List<Parcel> result = new ArrayList<>();
            while (rs.next()) {
                Parcel parcel = resultSetToParcel(rs);
                result.add(parcel);
            }
            return result;
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving all parcels.", ex);
        }
    }
}
