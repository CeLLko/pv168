package cz.muni.fi.pv168.autocamp;

import cz.muni.fi.pv168.project.autocamp.DBInteractionException;
import cz.muni.fi.pv168.project.autocamp.ParcelManager;
import cz.muni.fi.pv168.project.autocamp.Parcel;
import cz.muni.fi.pv168.project.autocamp.ParcelManagerImpl;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class ParcelManagerImplTest {
    
    private DataSource dataSource;
    private ParcelManager manager;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setUp() throws SQLException{
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("CREATE TABLE PARCEL ("
                    + "id bigint primary key generated always as identity,"
                    + "location varchar(10),"
                    + "electricity boolean,"
                    + "water boolean)").executeUpdate();
        }
        manager = new ParcelManagerImpl(dataSource);
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE PARCEL").executeUpdate();
        }
    }
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:parcelmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    @Test
    public void testCreateParcel()  {
        Parcel parcel = newParcel("a:12", false, true);
        manager.createParcel(parcel);
        
        Long id = parcel.getId();
        assertThat(id).isNotNull();
        
        assertThat(manager.findParcelByID(id))
                .isNotSameAs(parcel)
                .isEqualToComparingFieldByField(parcel);
    }
    
    @Test
    public void testFindAllParcels() {
        assertThat(manager.findAllParcels()).isEmpty();
        
        Parcel parcel_1 = newParcel("a:1", false, false);
        Parcel parcel_2 = newParcel("b:2", true, true);
        manager.createParcel(parcel_1);
        manager.createParcel(parcel_2);
        
        assertThat(manager.findAllParcels())
                .usingFieldByFieldElementComparator()
                .containsOnly(parcel_1, parcel_2);
   }
    
    @Test (expected = IllegalArgumentException.class)
    public void testCreateNullParcel() {
        manager.createParcel(null);
    }
    
    @Test
    public void testCreateParcelWithExistingId() {
        Parcel parcel = newParcel("a:1", false, false);
        parcel.setId(1L);
        
        expectedException.expect(IllegalArgumentException.class);
        manager.createParcel(parcel);
    }
    
    @Test
    public void testCreateParcelWithWrongLocationFormat() {
        Parcel parcel = newParcel("a1", false, false);
        
        expectedException.expect(IllegalArgumentException.class);
        manager.createParcel(parcel);
    }
    
    @Test
    public void testCreateParcelWithNullLocation() {
        Parcel parcel = newParcel(null, false, false);
        
        expectedException.expect(IllegalArgumentException.class);
        manager.createParcel(parcel);
    }
    
    @Test
    public void testUpdateLocation() {
        testUpdate((parcel) -> parcel.setLocation("a:12"));
    }
    
    @Test
    public void testUpdateWithElectricity() {
        testUpdate((parcel) -> parcel.setWithElectricity(true));
    }
    
    @Test
    public void testUpdateWithWater() {
        testUpdate((parcel) -> parcel.setWithWater(true));
    }
    
    private void testUpdate(Consumer<Parcel> updateOperation)  {
        Parcel parcelToUpdate = newParcel("a:1", false, false);
        Parcel otherParcel = newParcel("b:2", true, true);
        manager.createParcel(otherParcel);
        manager.createParcel(parcelToUpdate);
        
        updateOperation.accept(parcelToUpdate);
        manager.updateParcel(parcelToUpdate);
        
        assertThat(manager.findParcelByID(parcelToUpdate.getId()))
                .isEqualToComparingFieldByField(parcelToUpdate);
        
        //update should not affect other parcels
        assertThat(manager.findParcelByID(otherParcel.getId()))
                .isEqualToComparingFieldByField(otherParcel);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void testUpdateNullParcel() {
        manager.updateParcel(null);
    }
    
    @Test
    public void testUpdateParcelWithNullId() {
        Parcel parcel = newParcel("a:1", false, false);
        manager.createParcel(parcel);
        
        parcel.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateParcel(parcel);
    }
    
    @Test
    public void testUpdateParcelWithNonExistingId() {
        Parcel parcel = newParcel("a:1", false, false);
        manager.createParcel(parcel);
        
        parcel.setId(parcel.getId() + 1);
        expectedException.expect(DBInteractionException.class);
        manager.updateParcel(parcel);
    }
    
    @Test
    public void testUpdateParcelWithNullLocation() {
        Parcel parcel = newParcel("a:1", false, false);
        manager.createParcel(parcel);
        
        parcel.setLocation(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateParcel(parcel);
    }
    
    @Test
    public void testUpdateParcelWithWrongLocationFormat() {
        Parcel parcel = newParcel("a:1", false, false);
        manager.createParcel(parcel);
        
        parcel.setLocation("a1");
        expectedException.expect(IllegalArgumentException.class);
        manager.updateParcel(parcel);
    }
    
    @Test
    public void testDeleteParcel() {
        Parcel parcelToDelete = newParcel("a:1", false, false);
        Parcel otherParcel = newParcel("b:2", true, true);
        manager.createParcel(otherParcel);
        manager.createParcel(parcelToDelete);
        
        assertThat(manager.findParcelByID(parcelToDelete.getId())).isNotNull();
        assertThat(manager.findParcelByID(otherParcel.getId())).isNotNull();
        
        manager.deleteParcel(parcelToDelete);
        
        assertThat(manager.findParcelByID(parcelToDelete.getId())).isNull();
        assertThat(manager.findParcelByID(otherParcel.getId())).isNotNull();
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void testDeleteNullParcel() {
        manager.deleteParcel(null);
    }
    
    @Test
    public void testDeleteParcelWithNullId() {
        Parcel parcel = newParcel("a:1", false, false);
        manager.createParcel(parcel);
        
        parcel.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateParcel(parcel);
    }
    
    @Test
    public void testDeleteParcelWithNonExistingId() {
        Parcel parcel = newParcel("a:1", false, false);
        manager.createParcel(parcel);
        
        parcel.setId(parcel.getId()+1);
        expectedException.expect(DBInteractionException.class);
        manager.updateParcel(parcel);
    }

    private static Parcel newParcel(String location, boolean withElectricity, boolean withWater) {
        Parcel parcel = new Parcel();
        
        parcel.setLocation(location);
        parcel.setWithElectricity(withElectricity);
        parcel.setWithWater(withWater);
        
        return parcel;
    }
}
