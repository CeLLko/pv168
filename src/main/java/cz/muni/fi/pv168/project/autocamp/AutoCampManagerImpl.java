package cz.muni.fi.pv168.project.autocamp;

import static cz.muni.fi.pv168.project.autocamp.ParcelManagerImpl.resultSetToParcel;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class AutoCampManagerImpl implements AutoCampManager {
    
    private final DataSource dataSource;
    private final Clock clock;
    private final LocalDate today;
    
    public AutoCampManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.clock = null;
        today = LocalDate.now();
    }
    
    public AutoCampManagerImpl(DataSource dataSource, Clock clock) {
        this.dataSource = dataSource;
        this.clock = clock;
        today = LocalDate.now(clock);
    }

    @Override
    public List<Parcel> findEmptyParcelsForGivenDate(LocalDate from, LocalDate to) {
        
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Date to(" + to + ") is before date from(" + from + ").");
        }
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT * FROM PARCEL where id NOT IN "
                        + "(SELECT parcel FROM RESERVATION "
                        + "WHERE((datefrom <= ? AND dateto >= ? )"
                        + "OR(datefrom >= ?  AND datefrom < ? )))")) {
            st.setObject(1, Date.valueOf(from), Types.DATE);
            st.setObject(2, Date.valueOf(from), Types.DATE);
            st.setObject(3, Date.valueOf(from), Types.DATE);
            st.setObject(4, Date.valueOf(to), Types.DATE);
            
            ResultSet rs = st.executeQuery();
                        
            List<Parcel> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToParcel(rs));
            }
            
            return result;
        }   catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving empty parcels for given date", ex);
        }
    }
    
    @Override
    public List<Parcel> findEmptyParcelsUntilGivenDate(LocalDate to) {
        return findEmptyParcelsForGivenDate(today, to);
    }

    @Override
    public List<Parcel> findCurrentlyEmptyParcels() {
        return findEmptyParcelsForGivenDate(today, today.plusDays(1));
    }

    @Override
    public Guest findCurrentGuestOnParcel(Parcel parcel) {
        if (parcel == null) {
            throw new IllegalArgumentException("Parcel is null.");
        }
        if (parcel.getId() == null) {
            throw new IllegalArgumentException("Parcel's id is null");
        }
        
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT * FROM GUEST WHERE id IN "
                                + "(SELECT guest FROM RESERVATION "
                                + "WHERE parcel = ? AND datefrom <= ? AND dateto >= ? )")) {
            st.setLong(1, parcel.getId());
            st.setObject(2, Date.valueOf(today), Types.DATE);
            st.setObject(3, Date.valueOf(today), Types.DATE);
            
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                Guest guest = GuestManagerImpl.resultSetToGuest(rs);
                
                if (rs.next()) {
                    throw new DBInteractionException(
                        "Internal Error: More current guests at the given parcel"
                        + parcel);
                }
                
                return guest;
            } else {
                return null;
            }
            
        }   catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving current guest of parcel "+ parcel, ex);
        }
    }

    @Override
    public Parcel findCurrentParcelOfGuest(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest is null.");
        }
        if (guest.getId() == null) {
            throw new IllegalArgumentException("Guest's id is null");
        }
        
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT * FROM PARCEL WHERE id IN "
                                + "(SELECT parcel FROM RESERVATION "
                                + "WHERE guest = ? AND datefrom <= ? AND dateto >= ? )")) {
            
            st.setLong(1, guest.getId());
            st.setObject(2, Date.valueOf(today), Types.DATE);
            st.setObject(3, Date.valueOf(today), Types.DATE);
            
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                Parcel parcel = ParcelManagerImpl.resultSetToParcel(rs);
                
                if (rs.next()) {
                    throw new DBInteractionException(
                        "Internal Error: More current guests at the given guest"
                        + guest);
                }
                
                return parcel;
            } else {
                return null;
            }
            
        }   catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving current guest of guest "+ guest, ex);
        }
   }

}
