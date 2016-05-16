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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class GuestManagerImplTest {

    private DataSource dataSource;
    private GuestManager manager;
    private Guest guest1 = new Guest("Adam Gdovin", "123456789");
    private Guest guest2 = new Guest("Lenka Smitalova", "987654321");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setUp() throws SQLException{
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("CREATE TABLE GUEST("
                    + "id bigint primary key generated always as identity,"
                    + "fullname varchar(50),"
                    + "phone varchar(20))").executeUpdate();
            
            connection.prepareStatement("CREATE TABLE RESERVATION("
                    + "id bigint primary key generated always as identity,"
                    + "guest bigint)").executeUpdate();
        }
        manager = new GuestManagerImpl(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE GUEST").executeUpdate();
            connection.prepareStatement("DROP TABLE RESERVATION").executeUpdate();
        }
    }

    @Test
    public void testCreateGuest()  {
        manager.createGuest(guest1);

        Long id = guest1.getId();
        assertThat(id).isNotNull();

        assertThat(manager.findGuestByID(id))
                .isNotSameAs(guest1)
                .isEqualToComparingFieldByField(guest1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testCreateNullGuest() {
        manager.createGuest(null);
    }

    @Test
    public void testCreateGuestWithExistingId() {
        guest1.setId(1L);

        expectedException.expect(IllegalArgumentException.class);
        manager.createGuest(guest1);
    }
    
    @Test
    public void testCreateGuestWithNullFullName() {
        Guest guest = new Guest(null, "123456789");

        expectedException.expect(IllegalArgumentException.class);
        manager.createGuest(guest);
    }
    
    @Test
    public void testCreateGuestWithNullPhone() {
        Guest guest = new Guest("Adam Gdovin", null);

        expectedException.expect(IllegalArgumentException.class);
        manager.createGuest(guest);
    }
    
    @Test
    public void testUpdateFullName() {
        testUpdate((guest) -> guest.setFullName("Adam Gdovin"));
    }

    @Test
    public void testUpdatePhone() {
        testUpdate((guest) -> guest.setPhone("123456789"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testUpdateNullGuest() {
        manager.updateGuest(null);
    }

    @Test
    public void testUpdateGuestWithNullId() {
        manager.createGuest(guest1);

        guest1.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGuest(guest1);
    }

    @Test
    public void testUpdateGuestWithNonExistingId() {
        manager.createGuest(guest1);

        guest1.setId(guest1.getId() + 1);
        expectedException.expect(DBInteractionException.class);
        manager.updateGuest(guest1);
    }

    @Test
    public void testUpdateGuestWithNullFullName() {
        manager.createGuest(guest1);

        guest1.setFullName(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGuest(guest1);
    }

    @Test
    public void testUpdateGuestWithNullPhone() {
        manager.createGuest(guest1);

        guest1.setPhone(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGuest(guest1);
    }
    
    @Test
    public void testDeleteGuest() {
        manager.createGuest(guest1);
        manager.createGuest(guest2);

        assertThat(manager.findGuestByID(guest1.getId())).isNotNull();
        assertThat(manager.findGuestByID(guest2.getId())).isNotNull();

        manager.deleteGuest(guest1);

        assertThat(manager.findGuestByID(guest1.getId())).isNull();
        assertThat(manager.findGuestByID(guest2.getId())).isNotNull();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testDeleteNullGuest() {
        manager.deleteGuest(null);
    }

    @Test
    public void testDeleteGuestWithNullId() {
        manager.createGuest(guest1);

        guest1.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateGuest(guest1);
    }

    @Test
    public void testDeleteGuestWithNonExistingId() {
        manager.createGuest(guest1);

        guest1.setId(guest1.getId()+1);
        expectedException.expect(DBInteractionException.class);
        manager.updateGuest(guest1);
    }

    @Test
    public void testFindAllGuests() {
        assertThat(manager.findAllGuests()).isEmpty();

        manager.createGuest(guest1);
        manager.createGuest(guest2);

        assertThat(manager.findAllGuests())
                .usingFieldByFieldElementComparator()
                .containsOnly(guest1, guest2);
   }
    
    @Test
    public void testFindGuestsByName() {
        assertThat(manager.findGuestsByName("Adam Gdovin")).isEmpty();

        Guest guest3 = new Guest("Adam Gdovin", "123");
        manager.createGuest(guest1);
        manager.createGuest(guest2);
        manager.createGuest(guest3);

        assertThat(manager.findGuestsByName("Adam Gdovin"))
                .usingFieldByFieldElementComparator()
                .containsOnly(guest1, guest3);
    }

    private void testUpdate(Consumer<Guest> updateOperation)  {
        manager.createGuest(guest1);
        manager.createGuest(guest2);

        updateOperation.accept(guest1);
        manager.updateGuest(guest1);

        assertThat(manager.findGuestByID(guest1.getId()))
                .isEqualToComparingFieldByField(guest1);

        assertThat(manager.findGuestByID(guest2.getId()))
                .isEqualToComparingFieldByField(guest2);
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:guestmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
}