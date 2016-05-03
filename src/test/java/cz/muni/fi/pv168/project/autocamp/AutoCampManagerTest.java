package cz.muni.fi.pv168.autocamp;

import cz.muni.fi.pv168.project.autocamp.AutoCampManager;
import cz.muni.fi.pv168.project.autocamp.AutoCampManagerImpl;
import cz.muni.fi.pv168.project.autocamp.Guest;
import cz.muni.fi.pv168.project.autocamp.GuestManager;
import cz.muni.fi.pv168.project.autocamp.GuestManagerImpl;
import cz.muni.fi.pv168.project.autocamp.Parcel;
import cz.muni.fi.pv168.project.autocamp.ParcelManager;
import cz.muni.fi.pv168.project.autocamp.ParcelManagerImpl;
import cz.muni.fi.pv168.project.autocamp.Reservation;
import cz.muni.fi.pv168.project.autocamp.ReservationManager;
import cz.muni.fi.pv168.project.autocamp.ReservationManagerImpl;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Adam Gdovin, 433305
 * @author Lenka Smitalova, 410198
 * @version Mar 11, 2016
 */
public class AutoCampManagerTest {

    private static final LocalDate TODAY = LocalDate.of(2016, 03, 29);
    
    private DataSource dataSource;
    private AutoCampManager manager;
    
    private final Parcel parcel1 = new Parcel("a:1", false, false);
    private final Parcel parcel2 = new Parcel("b:2", true, true);
    private final Parcel parcel3 = new Parcel("c:3", true, false);
    private final Parcel parcel4 = new Parcel("d:4", false, false);
    private final Parcel parcel5 = new Parcel("e:5", true, true);
    private final Parcel parcel6 = new Parcel("f:6", true, false);
    private final Parcel parcel7 = new Parcel("g:7", true, true);
    private final Parcel parcel8 = new Parcel("h:8", false, false);
    private final Parcel parcel9 = new Parcel("i:9", true, false);
    
    private final Guest guest1 = new Guest("Adam Gdovin", "123456789");
    private final Guest guest2 = new Guest("Lenka Smitalova", "987654321");
    private final Guest guest3 = new Guest("John Smith", "666999666");
    private final Guest guest4 = new Guest("Peter Mrkva", "111");
    private final Guest guest5 = new Guest("Joshua Bloch", "222");
    private final Guest guest6 = new Guest("Ana Frankova", "333");
    private final Guest guest7 = new Guest("Petr Adamek", "7");
    private final Guest guest8 = new Guest("Abraham Lincoln", "666");
    private final Guest guest9 = new Guest("Albert Einstein", "1894");
        
    private final Reservation reservationExpired1 = new Reservation(
            TODAY.minusDays(365),
            TODAY.minusDays(55),
            guest1, parcel1);
        
    private final Reservation reservationExpired2 = new Reservation(
            TODAY.minusDays(55),
            TODAY.minusDays(14),
            guest2, parcel2);
        
    private final Reservation reservationExpired3 = new Reservation(
            TODAY.minusDays(14),
            TODAY.minusDays(7),
            guest3, parcel3);
        
    private final Reservation reservationCurrent1 = new Reservation(
            TODAY.minusDays(365),
            TODAY.plusDays(365),
            guest4, parcel4);
        
    private final Reservation reservationCurrent2 = new Reservation(
            TODAY.minusDays(7),
            TODAY.plusDays(7),
            guest5, parcel5);
        
    private final Reservation reservationCurrent3 = new Reservation(
            TODAY.minusDays(1),
            TODAY.plusDays(1),
            guest6, parcel6);
        
    private final Reservation reservationFuture1 = new Reservation(
            TODAY.plusDays(7),
            TODAY.plusDays(14),
            guest7, parcel7);

    private final Reservation reservationFuture2 = new Reservation(
            TODAY.plusDays(14),
            TODAY.plusDays(55),
            guest8, parcel8);
    
    private final Reservation reservationFuture3 = new Reservation(
            TODAY.plusDays(55),
            TODAY.plusDays(365),
            guest9, parcel9);
        
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
                    + "guest bigint,"
                    + "FOREIGN KEY (PARCEL) REFERENCES PARCEL(ID),"
                    + "FOREIGN KEY (GUEST) REFERENCES GUEST(ID))").executeUpdate();
        }
        ParcelManager parcelManager = new ParcelManagerImpl(dataSource);
        GuestManager guestManager = new GuestManagerImpl(dataSource);
        ReservationManager reservationManager = new ReservationManagerImpl(dataSource);
        
        parcelManager.createParcel(parcel1);
        parcelManager.createParcel(parcel2);
        parcelManager.createParcel(parcel3);
        parcelManager.createParcel(parcel4);
        parcelManager.createParcel(parcel5);
        parcelManager.createParcel(parcel6);
        parcelManager.createParcel(parcel7);
        parcelManager.createParcel(parcel8);
        parcelManager.createParcel(parcel9);
        
        guestManager.createGuest(guest1);
        guestManager.createGuest(guest2);
        guestManager.createGuest(guest3);
        guestManager.createGuest(guest4);
        guestManager.createGuest(guest5);
        guestManager.createGuest(guest6);
        guestManager.createGuest(guest7);
        guestManager.createGuest(guest8);
        guestManager.createGuest(guest9);
        
        reservationManager.createReservation(reservationExpired1);
        reservationManager.createReservation(reservationExpired2);
        reservationManager.createReservation(reservationExpired3);
        reservationManager.createReservation(reservationCurrent1);
        reservationManager.createReservation(reservationCurrent2);
        reservationManager.createReservation(reservationCurrent3);
        reservationManager.createReservation(reservationFuture1);
        reservationManager.createReservation(reservationFuture2);
        reservationManager.createReservation(reservationFuture3);
        
        manager = new AutoCampManagerImpl(dataSource, 
                Clock.fixed(TODAY
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC), ZoneId.of("UTC")));
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE RESERVATION").executeUpdate();
            connection.prepareStatement("DROP TABLE PARCEL").executeUpdate();
            connection.prepareStatement("DROP TABLE GUEST").executeUpdate();
        }
    }
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:autocampmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Test
    public void testFindEmptyParcelsForGivenDate() {
        // past
        assertThat(manager.findEmptyParcelsForGivenDate(TODAY.minusDays(350), TODAY.minusDays(340)))
                .usingFieldByFieldElementComparator()
                .containsOnly(parcel2,parcel3,parcel5,parcel6,parcel7,parcel8,parcel9);
        // past
        assertThat(manager.findEmptyParcelsForGivenDate(TODAY.minusDays(350), TODAY.minusDays(8)))
                .usingFieldByFieldElementComparator()
                .containsOnly(parcel5,parcel6,parcel7,parcel8,parcel9);
        // current
        assertThat(manager.findEmptyParcelsForGivenDate(TODAY.minusDays(1), TODAY.minusDays(1)))
                .usingFieldByFieldElementComparator()
                .containsOnly(parcel1,parcel2, parcel3, parcel7, parcel8, parcel9);
        // current
        assertThat(manager.findEmptyParcelsForGivenDate(TODAY.minusDays(365), TODAY.plusDays(365)))
                .usingFieldByFieldElementComparator()
                .isEmpty();
        // future
        assertThat(manager.findEmptyParcelsForGivenDate(TODAY.plusDays(8), TODAY.plusDays(365)))
                .usingFieldByFieldElementComparator()
                .containsOnly(parcel1,parcel2,parcel3,parcel5,parcel6);
    }    
    
    @Test
    public void testFindEmptyParcelsUntilGivenDate() {
        // past
        expectedException.expect(IllegalArgumentException.class);
        assertThat(manager.findEmptyParcelsUntilGivenDate(TODAY.minusDays(340)));
        
        // current
        expectedException.none();
        assertThat(manager.findEmptyParcelsUntilGivenDate(TODAY.plusDays(365)))
                .usingFieldByFieldElementComparator()
                .containsOnly(parcel1,parcel2,parcel3);
    }
    
    @Test
    public void testFindCurrentlyEmptyParcels() {
        assertThat(manager.findCurrentlyEmptyParcels())
                .usingFieldByFieldElementComparator()
                .containsOnly(parcel1,parcel2,parcel3,parcel7,parcel8,parcel9);
    }
    
    @Test
    public void testFindCurrentGuestOnParcel() {
        assertThat(manager.findCurrentGuestOnParcel(parcel1)).isNull();
        assertThat(manager.findCurrentGuestOnParcel(parcel2)).isNull();
        assertThat(manager.findCurrentGuestOnParcel(parcel3)).isNull();
        
        assertThat(manager.findCurrentGuestOnParcel(parcel4)).isEqualToComparingFieldByField(guest4);
        assertThat(manager.findCurrentGuestOnParcel(parcel5)).isEqualToComparingFieldByField(guest5);
        assertThat(manager.findCurrentGuestOnParcel(parcel6)).isEqualToComparingFieldByField(guest6);
        
        assertThat(manager.findCurrentGuestOnParcel(parcel7)).isNull();
        assertThat(manager.findCurrentGuestOnParcel(parcel8)).isNull();
        assertThat(manager.findCurrentGuestOnParcel(parcel9)).isNull();
    }
    
    @Test
    public void testFindCurrentParcelOfGuest() {
        assertThat(manager.findCurrentParcelOfGuest(guest1)).isNull();
        assertThat(manager.findCurrentParcelOfGuest(guest2)).isNull();
        assertThat(manager.findCurrentParcelOfGuest(guest3)).isNull();
        
        assertThat(manager.findCurrentParcelOfGuest(guest4)).isEqualToComparingFieldByField(parcel4);
        assertThat(manager.findCurrentParcelOfGuest(guest5)).isEqualToComparingFieldByField(parcel5);
        assertThat(manager.findCurrentParcelOfGuest(guest6)).isEqualToComparingFieldByField(parcel6);
        
        assertThat(manager.findCurrentParcelOfGuest(guest7)).isNull();
        assertThat(manager.findCurrentParcelOfGuest(guest8)).isNull();
        assertThat(manager.findCurrentParcelOfGuest(guest9)).isNull();
    }
}
