package cz.muni.fi.pv168.project.autocamp;

import cz.muni.fi.pv168.project.autocamp.Parcel;
import cz.muni.fi.pv168.project.autocamp.Guest;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class Reservation {
    
    private Long id;
    private LocalDate from;
    private LocalDate to;
    private Guest guest;
    private Parcel parcel;
    
    /**
     * creates new reservation with NULL attributes
     */
    public Reservation(){
    }
    
    /**
     * creates new reservation with given attributes
     * @param from when the reservation begins
     * @param to when the reservation ends
     * @param guest reservee
     * @param parcel reserved parcel
     */
    public Reservation(LocalDate from, LocalDate to, Guest guest, Parcel parcel){
        this.from = from;
        this.to = to;
        this.guest = guest;
        this.parcel = parcel; 
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the from
     */
    public LocalDate getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(LocalDate from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    public LocalDate getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(LocalDate to) {
        this.to = to;
    }

    /**
     * @return the guest
     */
    public Guest getGuest() {
        return guest;
    }

    /**
     * @param guest the guest to set
     */
    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    /**
     * @return the parcel
     */
    public Parcel getParcel() {
        return parcel;
    }

    /**
     * @param parcel the parcel to set
     */
    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }

    @Override
    public String toString() {
        return "Reservation: " + "id=" + id + ", from=" + from + ", to=" + to + 
                ", parcel=" + parcel.getId() + ", guest=" + guest.getId() + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        //if both reservations' id is not assigned yet, 
        //they should be evaluated as non equal
        if (this.id == null && this != obj) {
            return false;
        }
        final Reservation other = (Reservation) obj;
        return Objects.equals(this.id, other.getId());
    }
    
    
}
