package cz.muni.fi.pv168.project.autocamp;

import java.util.List;

/**
 * Interface for parcels, e.g. find parcels
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public interface ParcelManager {
    
    /**
     * Creates new parcel. Parcel have parameters id, location, withElectricity,
     * withWater. It is expected, that:
     *      id is null,
     *      location is not null and is equal to this regex pattern:"\\w+:\\d+",
     *      withElectricity has boolean value,
     *      withWater has boolean value.
     * @param parcel 
     */
    void createParcel (Parcel parcel);
    
    /**
     * updates the parcel
     * @param parcel 
     */
    void updateParcel (Parcel parcel);
    
    /**
     * deletes the parcel
     * @param parcel 
     */
    void deleteParcel (Parcel parcel);
    
    /**
     * find specific parcel by its ID
     * @param id of the parcel
     * @return parcel with given ID
     */
    Parcel findParcelByID(Long id);
    
    /**
     * finds all parcels
     * @return list of all parcels
     */
    List<Parcel> findAllParcels();
    
}
