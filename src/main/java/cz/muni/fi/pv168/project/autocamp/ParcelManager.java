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
     * find specific parcel by its location
     * @param location of the parcel
     * @return parcel with given location
     */
    Parcel findParcelByLocation(String location);
    
    /**
     * finds all parcels
     * @return list of all parcels
     */
    List<Parcel> findAllParcels();
    
    /**
     * Find all parcels with given filter parameter in their 
     * ID or location.
     *
     * @param filter wanted value
     * @return list of parcels whose attributes contain given filter, 
     *         in case of id values must be the same
     */
    List<Parcel> filterParcels(String filter);
}
