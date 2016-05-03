package cz.muni.fi.pv168.project.autocamp;

import java.util.Objects;

/**
 * Class representing a guest of the autocamp
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class Guest {
    
    private Long id=null;
    private String fullName=null;
    private String phone=null;
    
    /**
     * Creates new guest with null attributes
     */
    public Guest(){
    }
    
    /**
     * Creates new guest with given attributes
     * @param id identification number of the guest
     * @param fullName guest's full name
     * @param phone guest's contact number
     */
    public Guest(String fullName, String phone){
        this.fullName = fullName;
        this.phone = phone;
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
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
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
        if (this.id == null && this != obj) {
            return false;
        }
        
        final Guest other = (Guest) obj;
        return Objects.equals(id, other.getId());
    }
    
    
}
