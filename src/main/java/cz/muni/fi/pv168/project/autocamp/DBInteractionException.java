package cz.muni.fi.pv168.project.autocamp;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class DBInteractionException extends RuntimeException{
    
    public DBInteractionException(String error){
        super(error);
    }
    
    public DBInteractionException(Throwable cause){
        super(cause);
    }
    
    public DBInteractionException(String error, Throwable cause){
        super(error, cause);
    }
}
