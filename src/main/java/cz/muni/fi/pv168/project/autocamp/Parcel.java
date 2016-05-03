package cz.muni.fi.pv168.project.autocamp;

import java.util.Objects;

/**
 * This entity represents a parcel in autocamp. Parcel can be with electricity
 * and with water and it has specified location. Location has to have this
 * pattern: sector:number, where sector can contain letters, digits and char '_'
 * and number can contain only digits.
 * 
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class Parcel {
    
    private Long id;
    private String location;
    private boolean withElectricity;
    private boolean withWater;

    /**
     * Creates new parcel with null attributes
     */
    public Parcel(){
    }
    
    /**
     * Creates new parcel with given attributes
     * @param location location of the parcel
     * @param withElectricity has electricity connected
     * @param withWater has water connected
     */
    public Parcel(String location, boolean withElectricity, boolean withWater){
        this.location=location;
        this.withElectricity=withElectricity;
        this.withWater=withWater;
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
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the withElectricity
     */
    public boolean isWithElectricity() {
        return withElectricity;
    }

    /**
     * @param withElectricity the withElectricity to set
     */
    public void setWithElectricity(boolean withElectricity) {
        this.withElectricity = withElectricity;
    }

    /**
     * @return the withWater
     */
    public boolean isWithWater() {
        return withWater;
    }

    /**
     * @param withWater the withWater to set
     */
    public void setWithWater(boolean withWater) {
        this.withWater = withWater;
    }

    @Override
    public String toString() {
        return "Parcel:" + "id=" + id + ", location=" + location + ", withElectricity=" + withElectricity + ", withWater=" + withWater;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        //if both parcels' id is not assigned yet, they should be evaluated as non equal
        if (this.id == null && this != obj) {
            return false;
        }
        final Parcel other = (Parcel) obj;
        return Objects.equals(other.id, this.id);
    }
    
    
}
