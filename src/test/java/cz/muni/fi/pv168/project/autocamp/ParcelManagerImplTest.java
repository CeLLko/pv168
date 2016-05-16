package cz.muni.fi.pv168.project.autocamp;

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
    
    private Parcel correctParcel1;
    private Parcel correctParcel2;
    private Parcel correctParcel3;
    private Parcel correctParcel4;
    private Parcel correctParcel5;
    private Parcel correctParcel6;
    private Parcel correctParcel7;
    private Parcel correctParcel8;
    private Parcel incorrectParcel1;
    private Parcel incorrectParcel2;
    
    
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
            
            connection.prepareStatement("CREATE TABLE RESERVATION("
                    + "id bigint primary key generated always as identity,"
                    + "parcel bigint)").executeUpdate();
        }
        manager = new ParcelManagerImpl(dataSource);
        
        correctParcel1 = new Parcel("a:1", false, false);
        correctParcel2 = new Parcel("ab:1", true, true);
        correctParcel3 = new Parcel("ba:1", true, true);
        correctParcel4 = new Parcel("bab:1", false, false);
        correctParcel5 = new Parcel("b:1", false, false);
        correctParcel6 = new Parcel("b:12", true, true);
        correctParcel7 = new Parcel("bb:21", true, true);
        correctParcel8 = new Parcel("bab:212", false, false);
        incorrectParcel1 = new Parcel("a12", false, true);
        incorrectParcel2 = new Parcel(null, false, true);
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE PARCEL").executeUpdate();
            connection.prepareStatement("DROP TABLE RESERVATION").executeUpdate();
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
        manager.createParcel(correctParcel1);
        
        Long id = correctParcel1.getId();
        assertThat(id).isNotNull();
        
        assertThat(manager.findParcelByID(id))
                .isNotSameAs(correctParcel1)
                .isEqualToComparingFieldByField(correctParcel1);
    }
    
    @Test
    public void testFindAllParcels() {
        assertThat(manager.findAllParcels()).isEmpty();
        
        manager.createParcel(correctParcel1);
        manager.createParcel(correctParcel2);
        
        assertThat(manager.findAllParcels())
                .usingFieldByFieldElementComparator()
                .containsOnly(correctParcel1, correctParcel2);
   }
    
    @Test (expected = IllegalArgumentException.class)
    public void testCreateNullParcel() {
        manager.createParcel(null);
    }
    
    @Test
    public void testCreateParcelWithExistingId() {
        correctParcel1.setId(1L);
        
        expectedException.expect(IllegalArgumentException.class);
        manager.createParcel(correctParcel1);
    }
    
    @Test
    public void testCreateParcelWithWrongLocationFormat() {
        expectedException.expect(IllegalArgumentException.class);
        manager.createParcel(incorrectParcel1);
    }
    
    @Test
    public void testCreateParcelWithNullLocation() {
        expectedException.expect(IllegalArgumentException.class);
        manager.createParcel(incorrectParcel2);
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
        manager.createParcel(correctParcel1);
        manager.createParcel(correctParcel2);
        
        updateOperation.accept(correctParcel2);
        manager.updateParcel(correctParcel2);
        
        assertThat(manager.findParcelByID(correctParcel2.getId()))
                .isEqualToComparingFieldByField(correctParcel2);
        
        //update should not affect other parcels
        assertThat(manager.findParcelByID(correctParcel1.getId()))
                .isEqualToComparingFieldByField(correctParcel1);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void testUpdateNullParcel() {
        manager.updateParcel(null);
    }
    
    @Test
    public void testUpdateParcelWithNullId() {
        manager.createParcel(correctParcel1);
        
        correctParcel1.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateParcel(correctParcel1);
    }
    
    @Test
    public void testUpdateParcelWithNonExistingId() {
        manager.createParcel(correctParcel1);
        
        correctParcel1.setId(correctParcel1.getId() + 1);
        expectedException.expect(DBInteractionException.class);
        manager.updateParcel(correctParcel1);
    }
    
    @Test
    public void testUpdateParcelWithNullLocation() {
        manager.createParcel(correctParcel1);
        
        correctParcel1.setLocation(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateParcel(correctParcel1);
    }
    
    @Test
    public void testUpdateParcelWithWrongLocationFormat() {
        manager.createParcel(correctParcel1);
        
        correctParcel1.setLocation("a1");
        expectedException.expect(IllegalArgumentException.class);
        manager.updateParcel(correctParcel1);
    }
    
    @Test
    public void testDeleteParcel() {
        manager.createParcel(correctParcel2);
        manager.createParcel(correctParcel1);
        
        assertThat(manager.findParcelByID(correctParcel1.getId())).isNotNull();
        assertThat(manager.findParcelByID(correctParcel2.getId())).isNotNull();
        
        manager.deleteParcel(correctParcel1);
        
        assertThat(manager.findParcelByID(correctParcel1.getId())).isNull();
        assertThat(manager.findParcelByID(correctParcel2.getId())).isNotNull();
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void testDeleteNullParcel() {
        manager.deleteParcel(null);
    }
    
    @Test
    public void testDeleteParcelWithNullId() {
        manager.createParcel(correctParcel1);
        
        correctParcel1.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateParcel(correctParcel1);
    }
    
    @Test
    public void testDeleteParcelWithNonExistingId() {
        manager.createParcel(correctParcel1);
        
        correctParcel1.setId(correctParcel1.getId()+1);
        expectedException.expect(DBInteractionException.class);
        manager.updateParcel(correctParcel1);
    }
    
    @Test
    public void testFilterUsingEmptyString()  {
        prepareTestFilter();
        
        assertThat(manager.filterParcels(""))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel1, 
                                           correctParcel2,
                                           correctParcel3, 
                                           correctParcel4,
                                           correctParcel5, 
                                           correctParcel6,
                                           correctParcel7, 
                                           correctParcel8);
    }
    
    @Test
    public void testFilterSingleLetter()  {
        prepareTestFilter();
        
        assertThat(manager.filterParcels("a"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel1, 
                                           correctParcel2,
                                           correctParcel3, 
                                           correctParcel4,
                                           correctParcel8);
    }
    
    @Test
    public void testFilterSingleDigit()  {
        prepareTestFilter();
        
        assertThat(manager.filterParcels("2"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel2,  //id of the parcel is 2 -> filter should catch it
                                           correctParcel6,
                                           correctParcel7, 
                                           correctParcel8);
    }
    
    @Test
    public void testFilterLetters()  {
        prepareTestFilter();
        
        assertThat(manager.filterParcels("ab"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel2,
                                           correctParcel4,
                                           correctParcel8);
        
        assertThat(manager.filterParcels("ba"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel3,
                                           correctParcel4,
                                           correctParcel8);
    }
    
    @Test
    public void testFilterDigits()  {
        prepareTestFilter();
        
        assertThat(manager.filterParcels("12"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel6,
                                           correctParcel8);
        
        assertThat(manager.filterParcels("21"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel7,
                                           correctParcel8);
    }
    
    @Test
    public void testFilterMiddle()  {
        prepareTestFilter();
        
        assertThat(manager.filterParcels("b:1"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel2,
                                           correctParcel4,
                                           correctParcel5,
                                           correctParcel6);
        
        assertThat(manager.filterParcels("a:1"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(correctParcel1,
                                           correctParcel3);
    }
    
    private void prepareTestFilter() {
        manager.createParcel(correctParcel1);
        manager.createParcel(correctParcel2);
        manager.createParcel(correctParcel3);
        manager.createParcel(correctParcel4);
        manager.createParcel(correctParcel5);
        manager.createParcel(correctParcel6);
        manager.createParcel(correctParcel7);
        manager.createParcel(correctParcel8);
    }
}
