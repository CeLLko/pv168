package cz.muni.fi.pv168.project.autocamp;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.*;

import static org.assertj.core.api.Assertions.*;

import org.junit.rules.ExpectedException;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class ReservationManagerImplTest {

    private static final LocalDate TODAY = LocalDate.of(2016, 03, 29);
    
    private DataSource dataSource;
    private ReservationManager manager;
    
    private final Parcel parcel1 = new Parcel("a:1", false, false);
    private final Parcel parcel2 = new Parcel("b:2", true, true);
    private final Parcel parcel3 = new Parcel("c:3", true, false);
    private final Guest guest1 = new Guest("Adam Gdovin", "123456789");
    private final Guest guest2 = new Guest("Lenka Smitalova", "987654321");
    private final Guest guest3 = new Guest("John Smith", "666999666");
        
    private final Reservation reservationExpired1 = new Reservation(TODAY.minusDays(7), TODAY.minusDays(1), guest1, parcel1);
    private final Reservation reservationExpired2 = new Reservation(TODAY.minusDays(7), TODAY.minusDays(1), guest2, parcel2);
    private final Reservation reservationFuture1 = new Reservation(TODAY.plusDays(1), TODAY.plusDays(7), guest1, parcel1);
    private final Reservation reservationFuture2 = new Reservation(TODAY.plusDays(1), TODAY.plusDays(7), guest2, parcel2);
    private final Reservation reservationCurrent1 = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
    private final Reservation reservationCurrent2 = new Reservation(TODAY.minusDays(1), TODAY.plusDays(1), guest2, parcel2);
    private final Reservation reservationCurrent3 = new Reservation(TODAY.minusDays(1), TODAY, guest3, parcel3);
    

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
            
            connection.prepareStatement("CREATE TABLE GUEST("
                    + "id bigint primary key generated always as identity,"
                    + "fullname varchar(50),"
                    + "phone varchar(20))").executeUpdate();
            
            connection.prepareStatement("CREATE TABLE RESERVATION("
                    + "id bigint primary key generated always as identity,"
                    + "datefrom date,"
                    + "dateto date,"
                    + "parcel bigint,"
                    + "guest bigint)").executeUpdate();        
        }
        ParcelManager parcelManager = new ParcelManagerImpl(dataSource);
        GuestManager guestManager = new GuestManagerImpl(dataSource);
        
        parcelManager.createParcel(parcel1);
        parcelManager.createParcel(parcel2);
        parcelManager.createParcel(parcel3);
        
        guestManager.createGuest(guest1);
        guestManager.createGuest(guest2);
        guestManager.createGuest(guest3);
        
        manager = new ReservationManagerImpl(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE RESERVATION").executeUpdate();
            connection.prepareStatement("DROP TABLE PARCEL").executeUpdate();
            connection.prepareStatement("DROP TABLE GUEST").executeUpdate();
        }
    }

    @Test
    public void testCreateReservation()  {
        manager.createReservation(reservationExpired1);

        Long id = reservationExpired1.getId();
        assertThat(id).isNotNull();
        
        Reservation newres = manager.findReservationByID(id);
        assertThat(newres)
                .isNotSameAs(reservationExpired1)
                .isEqualToComparingFieldByField(reservationExpired1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testCreateNullReservation() {
        manager.createReservation(null);
    }

    @Test
    public void testCreateReservationWithExistingId() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        reservation.setId(1L);

        expectedException.expect(IllegalArgumentException.class);
        manager.createReservation(reservation);
    }
    
    @Test
    public void testCreateReservationWithNullFromDate() {
        Reservation reservation = new Reservation(null, TODAY.plusDays(7), guest1, parcel1);

        expectedException.expect(IllegalArgumentException.class);
        manager.createReservation(reservation);
    }
    
    @Test
    public void testCreateReservationWithNullToDate() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), null, guest1, parcel1);

        expectedException.expect(IllegalArgumentException.class);
        manager.createReservation(reservation);
    }
    
    @Test
    public void testCreateReservationWithNullParcel() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), null, parcel1);

        expectedException.expect(IllegalArgumentException.class);
        manager.createReservation(reservation);
    }
    
    @Test
    public void testCreateReservationWithNullGuest() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, null);

        expectedException.expect(IllegalArgumentException.class);
        manager.createReservation(reservation);
    }
    
    @Test
    public void testUpdateFromDate() {
        testUpdate((reservation) -> reservation.setFrom(TODAY.minusDays(1)));
    }
    
    @Test
    public void testUpdateToDate() {
        testUpdate((reservation) -> reservation.setFrom(TODAY.plusDays(1)));
    }

    @Test
    public void testUpdateParcel() {
        testUpdate((reservation) -> reservation.setParcel(parcel3));
    }

    @Test
    public void testUpdateGuest() {
        testUpdate((reservation) -> reservation.setGuest(guest1));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testUpdateNullReservation() {
        manager.updateReservation(null);
    }

    @Test
    public void testUpdateReservationWithNullId() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        manager.createReservation(reservation);

        reservation.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateReservation(reservation);
    }

    @Test
    public void testUpdateReservationWithNonExistingId() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        manager.createReservation(reservation);

        reservation.setId(reservation.getId() + 1);
        expectedException.expect(DBInteractionException.class);
        manager.updateReservation(reservation);
    }

    @Test
    public void testUpdateReservationWithNullFromDate() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        manager.createReservation(reservation);

        reservation.setFrom(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateReservation(reservation);
    }

    @Test
    public void testUpdateReservationWithNullToDate() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        manager.createReservation(reservation);

        reservation.setTo(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateReservation(reservation);
    }

    @Test
    public void testUpdateReservationWithNullParcel() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        manager.createReservation(reservation);

        reservation.setParcel(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateReservation(reservation);
    }

    @Test
    public void testUpdateReservationWithNullGuest() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        manager.createReservation(reservation);

        reservation.setGuest(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateReservation(reservation);
    }

    
    @Test
    public void testDeleteReservation() {
        manager.createReservation(reservationCurrent1);
        manager.createReservation(reservationCurrent2);

        assertThat(manager.findReservationByID(reservationCurrent1.getId())).isNotNull();
        assertThat(manager.findReservationByID(reservationCurrent2.getId())).isNotNull();

        manager.deleteReservation(reservationCurrent1);

        assertThat(manager.findReservationByID(reservationCurrent1.getId())).isNull();
        assertThat(manager.findReservationByID(reservationCurrent2.getId())).isNotNull();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testDeleteNullReservation() {
        manager.deleteReservation(null);
    }

    @Test
    public void testDeleteReservationWithNullId() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        manager.createReservation(reservation);

        reservation.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateReservation(reservation);
    }

    @Test
    public void testDeleteReservationWithNonExistingId() {
        Reservation reservation = new Reservation(TODAY.minusDays(7), TODAY.plusDays(7), guest1, parcel1);
        manager.createReservation(reservation);

        reservation.setId(reservation.getId()+1);
        expectedException.expect(DBInteractionException.class);
        manager.updateReservation(reservation);
    }

    @Test
    public void testFindAllReservations() {
        assertThat(manager.findAllReservations()).isEmpty();

        manager.createReservation(reservationCurrent1);
        manager.createReservation(reservationCurrent2);
        manager.createReservation(reservationCurrent3);

        assertThat(manager.findAllReservations())
                .usingFieldByFieldElementComparator()
                .containsOnly(reservationCurrent1, reservationCurrent2, reservationCurrent3);
    }
    
    @Test
    public void testFindReservationsByParcel() {
        assertThat(manager.findReservationsByParcel(parcel1)).isEmpty();

        manager.createReservation(reservationCurrent1); //contains parcel1
        manager.createReservation(reservationCurrent2); //contains parcel2
        manager.createReservation(reservationCurrent3); //contains parcel3

        assertThat(manager.findReservationsByParcel(parcel1))
                .usingFieldByFieldElementComparator()
                .containsOnly(reservationCurrent1);
    }
    
    @Test
    public void testFindReservationsByGuest() {
        assertThat(manager.findReservationsByGuest(guest1)).isEmpty();

        manager.createReservation(reservationCurrent1); //contains guest1
        manager.createReservation(reservationCurrent2); //contains guest2
        manager.createReservation(reservationCurrent3); //contains guest3

        assertThat(manager.findReservationsByGuest(guest1))
                .usingFieldByFieldElementComparator()
                .containsOnly(reservationCurrent1);
    }

    private void testUpdate(Consumer<Reservation> updateOperation)  {
        manager.createReservation(reservationCurrent1);
        manager.createReservation(reservationCurrent2);

        updateOperation.accept(reservationCurrent1);
        manager.updateReservation(reservationCurrent1);

        assertThat(manager.findReservationByID(reservationCurrent1.getId()))
                .isEqualToComparingFieldByField(reservationCurrent1);

        assertThat(manager.findReservationByID(reservationCurrent2.getId()))
                .isEqualToComparingFieldByField(reservationCurrent2);
    }


    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:reservationmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
}