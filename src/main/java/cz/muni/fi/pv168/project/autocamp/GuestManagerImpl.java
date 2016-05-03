package cz.muni.fi.pv168.project.autocamp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class GuestManagerImpl implements GuestManager {
    
    private final DataSource dataSource;
    
    public GuestManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createGuest(Guest guest) throws DBInteractionException {
        validate(guest);
        
        if (guest.getId() != null) {
            throw new IllegalArgumentException("ID of the guest is already set.");
        }
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "INSERT INTO GUEST (fullname,phone) VALUES (?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            
            st.setString(1, guest.getFullName());
            st.setString(2, guest.getPhone());
            
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new DBInteractionException("Internal Error: More rows were inserted ("
                    + addedRows + ") while trying to insert guest " + guest);
            }
            
            ResultSet keyRS = st.getGeneratedKeys();
            guest.setId(getKey(keyRS, guest));
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while inserting guest " + guest, ex);
        }
    }    
    
    @Override
    public void updateGuest(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest is null.");
        }
        
        if (guest.getId() == null) {
            throw new IllegalArgumentException("Guest's id is null.");
        }
        
        validate(guest);
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                    "UPDATE guest SET fullname = ?, phone = ? WHERE id = ?")) {
            
            st.setString(1, guest.getFullName());
            st.setString(2, guest.getPhone());
            st.setLong(3, guest.getId());
            
            int count = st.executeUpdate();
            if (count == 0) {
                throw new DBInteractionException("Guest " + guest + " does not exist within the database.");
            }else if (count != 1) {
                throw new DBInteractionException("Invalid number of rows updated (only one row should be updated):" + count);
            }
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while updating guest " + guest, ex);
        }
    }

    @Override
    public void deleteGuest(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest is null.");
        }
        
        if (guest.getId() == null) {
            throw new IllegalArgumentException("Guest's id is null.");
        }
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "DELETE FROM GUEST WHERE id = ?")) {
            
            st.setLong(1, guest.getId());
            
            int count = st.executeUpdate();
            if (count == 0) {
                throw new DBInteractionException(
                        "Given guest " + guest + " does not exist within the database.");
            }
            else if(count != 1) {
                throw new DBInteractionException(
                        "Invalid number of rows deleted (only one row should be deleted):" + count);
            }
        } catch (SQLException ex) {
            throw new DBInteractionException(
                    "Error while deleting guest " + guest, ex);
        }
    }

    @Override
    public Guest findGuestByID(Long id)  throws  DBInteractionException{
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id,fullname,phone FROM GUEST WHERE id = ?")) {
            
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                Guest guest = resultSetToGuest(rs);
                
                if (rs.next()) {
                    throw new DBInteractionException(
                        "Internal Error: More entities with the same id"
                        + "(given id:" + id +"; found guests:" + guest
                        + " and " + resultSetToGuest(rs) + ")");
                }
                
                return guest;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new DBInteractionException(
                    "Error while retrieving guest with id " + id, ex);
        }
    }

    @Override
    public List<Guest> findAllGuests() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id,fullname,phone FROM GUEST")) {
            
            ResultSet rs = st.executeQuery();
            
            List<Guest> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToGuest(rs));
            }
            return result;
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving all guests.", ex);
        }
    }
    
    @Override
    public List<Guest> findGuestsByName(String fullName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                        "SELECT id,fullname,phone FROM GUEST WHERE fullname = ?")) {
            
            st.setString(1, fullName);
            
            ResultSet rs = st.executeQuery();

            List<Guest> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToGuest(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new DBInteractionException("Error when retrieving guests with name" + fullName, ex);
        }
    }

    @Override
    public List<Guest> filterGuestsWithGivenParamter(String param) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                        "SELECT id,fullname,phone FROM GUEST WHERE LOWER(fullname) LIKE LOWER(?) OR LOWER(phone) LIKE LOWER(?) OR id = ?")) {
            
            st.setString(1, "%"+param+"%");
            st.setString(2, "%"+param+"%");
            try{
                st.setLong(3, Long.valueOf(param));
            } catch(NumberFormatException ex){
                st.setLong(3, Long.valueOf(-1));
            }
            
            ResultSet rs = st.executeQuery();
            
            List<Guest> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToGuest(rs));
            }
            return result;
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving all guests.", ex);
        }
    }

    public static Guest resultSetToGuest(ResultSet rs) throws SQLException{
        Guest guest = new Guest();
        
        guest.setId(rs.getLong("id"));
        guest.setFullName(rs.getString("fullname"));
        guest.setPhone(rs.getString("phone"));
        
        return guest;
    }

    private void validate(Guest guest) throws IllegalArgumentException{
        if(guest == null) {
            throw new IllegalArgumentException("Guest is null");
        }
        if(guest.getFullName()== null) {
            throw new IllegalArgumentException("Name is null");
        }
        if(guest.getPhone()== null) {
            throw new IllegalArgumentException("Phone is null");
        }
    }
    
    private Long getKey(ResultSet keyRS, Guest guest) throws SQLException, DBInteractionException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new DBInteractionException("Internal Error: Generated key"
                        + "retriving failed when trying to insert guest " + guest
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new DBInteractionException("Internal Error: Generated key"
                        + "retriving failed when trying to insert guest " + guest
                        + " - more keys found");
            }
            return result;
        } else {
            throw new DBInteractionException("Internal Error: Generated key"
                    + "retriving failed when trying to insert guest " + guest
                    + " - no key found");
        }
    }
}