package cz.muni.fi.pv168.project.autocamp;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for autocamp, e.g. find guests or parcels etc.
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public interface AutoCampManager {

    /**
     * Find all the available parcels for given time frame
     *
     * @param from since when should parcel be available
     * @param to until when should parcel be available
     * @return list of available parcels for given date
     */
    List<Parcel> findEmptyParcelsForGivenDate(LocalDate from, LocalDate to);

    /**
     * Find all the available parcels from today
     *
     * @param to until when should parcel be available
     * @return list of available parcels
     */
    List<Parcel> findEmptyParcelsUntilGivenDate(LocalDate to);

    /**
     * Find all currently available parcels
     *
     * @return list of available parcels
     */
    List<Parcel> findCurrentlyEmptyParcels();

    /**
     * Find which guest is currently occupying given parcel
     *
     * @param parcel parcel to find the guest of
     * @return guest staying on given parcel, NULL if empty
     */
    Guest findCurrentGuestOnParcel(Parcel parcel);

    /**
     * Find which parcel is currently occupied by given guest
     *
     * @param guest guest to find the parcel of
     * @return parcel occupied by given guest
     */
    Parcel findCurrentParcelOfGuest(Guest guest);

}
