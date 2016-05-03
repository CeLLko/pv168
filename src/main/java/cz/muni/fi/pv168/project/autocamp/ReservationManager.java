package cz.muni.fi.pv168.project.autocamp;

import cz.muni.fi.pv168.project.autocamp.Parcel;
import cz.muni.fi.pv168.project.autocamp.Guest;
import java.util.List;

/**
 * Interface for reservations, e.g. find reservation
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public interface ReservationManager {
    
    /**
     * creates new reservation
     * @param reservation 
     */
    void createReservation (Reservation reservation);
    
    /**
     * updates the reservation
     * @param reservation 
     */
    void updateReservation (Reservation reservation);
    
    /**
     * deletes the reservation
     * @param reservation 
     */
    void deleteReservation (Reservation reservation);
    
    /**
     * find specific reservation by its ID
     * @param id of the reservation
     * @return reservation with given ID
     */
    Reservation findReservationByID(Long id);
    
    /**
     * find all reservations with given parcel
     * @param parcel of the reservation
     * @return list of all reservations with given parcel
     */
    List<Reservation> findReservationsByParcel(Parcel parcel);
    
    /**
     * find all reservations with given guest
     * @param guest of the reservation
     * @return list of all reservations with given parcel
     */
    List<Reservation> findReservationsByGuest(Guest guest);
    
    /**
     * finds all reservations
     * @return list of all reservations
     */
    List<Reservation> findAllReservations();
    
}
