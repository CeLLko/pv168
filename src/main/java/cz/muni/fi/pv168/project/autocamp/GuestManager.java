package cz.muni.fi.pv168.project.autocamp;

import cz.muni.fi.pv168.project.autocamp.Guest;
import java.util.List;

/**
 * Interface for guests, e.g. find guest
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public interface GuestManager {
    
    /**
     * creates new guest
     * @param guest 
     */
    void createGuest (Guest guest);
    
    /**
     * updates the guest
     * @param guest 
     */
    void updateGuest (Guest guest);
    
    /**
     * deletes the guest
     * @param guest 
     */
    void deleteGuest (Guest guest);
    
    /**
     * find specific guest by his ID
     * @param id of the guest
     * @return guest with given ID
     */
    Guest findGuestByID(Long id);
    
    /**
     * find specific guest by his name
     * @param fullName of the guest
     * @return guest with given name
     */
    List<Guest> findGuestsByName(String fullName);
    
    /**
     * finds all guests
     * @return list of all guests
     */
    List<Guest> findAllGuests();
    
    /**
     * Find all guests with given parameter in their ID or name or phone number
     *
     * @param param filter
     * @return list of guests whose attributes contain given param
     */
    List<Guest> filterGuests(String param);
}
