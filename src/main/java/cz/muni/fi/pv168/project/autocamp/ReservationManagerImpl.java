package cz.muni.fi.pv168.project.autocamp;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class ReservationManagerImpl implements ReservationManager {

    private final DataSource dataSource;

    public ReservationManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createReservation(Reservation reservation) throws DBInteractionException {
        validate(reservation);

        if (reservation.getId() != null) {
            throw new IllegalArgumentException("ID of the reservation is already set.");
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "INSERT INTO RESERVATION (datefrom,dateto,parcel,guest) VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            st.setObject(1, Date.valueOf(reservation.getFrom()), Types.DATE);
            st.setObject(2, Date.valueOf(reservation.getTo()), Types.DATE);
            st.setLong(3, reservation.getParcel().getId());
            st.setLong(4, reservation.getGuest().getId());

            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new DBInteractionException("Internal Error: More rows were inserted ("
                        + addedRows + ") while trying to insert reservation " + reservation);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            reservation.setId(getKey(keyRS, reservation));

        } catch (SQLException ex) {
            throw new DBInteractionException("Error while inserting reservation " + reservation, ex);
        }
    }

    @Override
    public void updateReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation is null.");
        }
        
        validateUpdate(reservation);

        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "UPDATE reservation SET datefrom = ?, dateto = ?, parcel = ?, guest = ? WHERE id = ?")) {

            st.setObject(1, Date.valueOf(reservation.getFrom()), Types.DATE);
            st.setObject(2, Date.valueOf(reservation.getTo()), Types.DATE);
            st.setLong(3, reservation.getParcel().getId());
            st.setLong(4, reservation.getGuest().getId());
            st.setLong(5, reservation.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new DBInteractionException("Reservation " + reservation + " does not exist within the database.");
            } else if (count != 1) {
                throw new DBInteractionException("Invalid number of rows updated (only one row should be updated):" + count);
            }

        } catch (SQLException ex) {
            throw new DBInteractionException("Error while updating reservation " + reservation, ex);
        }
    }

    @Override
    public void deleteReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation is null.");
        }

        if (reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation's id is null.");
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "DELETE FROM RESERVATION WHERE id = ?")) {

            st.setLong(1, reservation.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new DBInteractionException(
                        "Given reservation " + reservation + " does not exist within the database.");
            } else if (count != 1) {
                throw new DBInteractionException(
                        "Invalid number of rows deleted (only one row should be deleted):" + count);
            }
        } catch (SQLException ex) {
            throw new DBInteractionException(
                    "Error while deleting reservation " + reservation, ex);
        }
    }

    @Override
    public Reservation findReservationByID(Long id) throws DBInteractionException {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,datefrom,dateto, parcel, guest FROM RESERVATION WHERE id = ?")) {

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Reservation reservation = resultSetToReservation(rs);

                if (rs.next()) {
                    throw new DBInteractionException(
                            "Internal Error: More entities with the same id"
                            + "(given id:" + id + "; found reservations:" + reservation
                            + " and " + resultSetToReservation(rs) + ")");
                }

                return reservation;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new DBInteractionException(
                    "Error while retrieving reservation with id " + id, ex);
        }
    }

    @Override
    public List<Reservation> findReservationsByParcel(Parcel parcel) throws DBInteractionException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,datefrom,dateto, parcel, guest FROM RESERVATION WHERE parcel = ?")) {

            st.setLong(1, parcel.getId());
            ResultSet rs = st.executeQuery();

            List<Reservation> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToReservation(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving all reservations.", ex);
        }
    }

    @Override
    public List<Reservation> findReservationsByGuest(Guest guest) throws DBInteractionException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT * FROM RESERVATION WHERE guest = ?")) {

            st.setLong(1, guest.getId());
            ResultSet rs = st.executeQuery();

            List<Reservation> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToReservation(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving all reservations.", ex);
        }
    }

    @Override
    public List<Reservation> findAllReservations() {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,datefrom,dateto, parcel, guest FROM RESERVATION")) {

            ResultSet rs = st.executeQuery();

            List<Reservation> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToReservation(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving all reservations.", ex);
        }
    }

    @Override
    public List<Reservation> filterReservations(String filter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "SELECT DISTINCT reservation.id, "
                                + "reservation.datefrom, "
                                + "reservation.dateto, "
                                + "reservation.parcel, "
                                + "reservation.guest "
                   + "FROM reservation, parcel, guest "
                   + "WHERE LOWER(reservation.datefrom) LIKE LOWER(?) "
                      + "OR LOWER(reservation.dateto) LIKE LOWER(?) "
                      + "OR ((reservation.parcel = parcel.id) AND LOWER(parcel.location) LIKE LOWER(?)) "
                      + "OR ((reservation.guest = guest.id) AND LOWER(guest.fullname) LIKE LOWER(?)) "
                      + "OR reservation.id = ?")) {
            
            st.setString(1, "%"+filter+"%");
            st.setString(2, "%"+filter+"%");
            st.setString(3, "%"+filter+"%");
            st.setString(4, "%"+filter+"%");
            try {
                st.setLong(5, Long.valueOf(filter));
            } catch (NumberFormatException ex) {
                st.setLong(5, (long) -1);
            }
            
            ResultSet rs = st.executeQuery();
            
            List<Reservation> result = new ArrayList<>();
            while (rs.next()) {
                Reservation reservation = resultSetToReservation(rs);
                result.add(reservation);
            }
            return result;
            
        } catch (SQLException ex) {
            throw new DBInteractionException("Error while retrieving all reservations.", ex);
        }
    }

    private Reservation resultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        ParcelManager parcelManager = new ParcelManagerImpl(dataSource);
        GuestManager guestManager = new GuestManagerImpl(dataSource);

        reservation.setId(rs.getLong("id"));
        reservation.setFrom(rs.getDate("datefrom").toLocalDate());
        reservation.setTo(rs.getDate("dateto").toLocalDate());
        reservation.setParcel(parcelManager.findParcelByID(rs.getLong("parcel")));
        reservation.setGuest(guestManager.findGuestByID(rs.getLong("guest")));

        return reservation;
    }

    private void validate(Reservation reservation) throws IllegalArgumentException {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation is null");
        }
        if (reservation.getFrom() == null) {
            throw new IllegalArgumentException("date From is null");
        }
        if (reservation.getTo() == null) {
            throw new IllegalArgumentException("date To is null");
        }
        if (reservation.getFrom().isAfter(reservation.getTo())) {
            throw new IllegalArgumentException("date From is after date To");
        }
        if (reservation.getParcel() == null) {
            throw new IllegalArgumentException("Parcel is null");
        }
        if (reservation.getGuest() == null) {
            throw new IllegalArgumentException("Guest is null");
        }
    }
    
    private void validateUpdate(Reservation reservation) throws IllegalArgumentException {
        validate(reservation);

        if (reservation.getId() != null) {
            Reservation update = new ReservationManagerImpl(dataSource).findReservationByID(reservation.getId());
            if (update == null) {
                throw new DBInteractionException("Reservation with given ID doesn't exist within the database.");
            }

            AutoCampManager autocampManager = new AutoCampManagerImpl(dataSource);
            List<Parcel> emptyParcels = autocampManager.findEmptyParcelsForGivenDate(reservation.getFrom(), reservation.getTo());

            if (!emptyParcels.contains(reservation.getParcel()) 
                    && !update.getParcel().equals(reservation.getParcel())) {
                throw new DBInteractionException("Parcel is occupied");
            }
        } else{
            throw new IllegalArgumentException("Reservation's id is null.");
        }
    }

    private Long getKey(ResultSet keyRS, Reservation reservation) throws SQLException, DBInteractionException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new DBInteractionException("Internal Error: Generated key"
                        + "retriving failed when trying to insert reservation " + reservation
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new DBInteractionException("Internal Error: Generated key"
                        + "retriving failed when trying to insert reservation " + reservation
                        + " - more keys found");
            }
            return result;
        } else {
            throw new DBInteractionException("Internal Error: Generated key"
                    + "retriving failed when trying to insert reservation " + reservation
                    + " - no key found");
        }
    }
}
