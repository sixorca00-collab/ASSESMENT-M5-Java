package org.hotelNova.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hotelNova.DAO.UserDAO;
import org.hotelNova.config.DbInit;
import org.hotelNova.models.Booking;
import org.hotelNova.models.Guest;
import org.hotelNova.models.Room;
import org.hotelNova.models.User;
import org.hotelNova.services.BookingService;
import org.hotelNova.services.RoomService;
import org.hotelNova.services.UserService;
import org.hotelNova.utils.PasswordHasher;
import org.hotelnova.config.AppConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AccountAndBookingSteps {

    private final UserService userService = new UserService();
    private final BookingService bookingService = new BookingService();
    private final RoomService roomService = new RoomService();
    private final UserDAO userDAO = new UserDAO();

    private Path dbPath;
    private User createdUser;
    private User loggedInUser;
    private List<Booking> visibleBookings;
    private int roomCounter;

    @Before
    public void setUpScenario() throws Exception {
        dbPath = Path.of("/tmp", "hotelnova-cucumber-" + UUID.randomUUID() + ".db");
        System.setProperty("hotelNova.db.url", "jdbc:sqlite:" + dbPath);
        AppConfig.reset();
        DbInit.init();
        roomCounter = 100;
        createdUser = null;
        loggedInUser = null;
        visibleBookings = List.of();
    }

    @After
    public void tearDownScenario() throws Exception {
        System.clearProperty("hotelNova.db.url");
        AppConfig.reset();
        Files.deleteIfExists(dbPath);
    }

    @When("I create a guest account with username {string} and password {string}")
    public void iCreateAGuestAccountWithUsernameAndPassword(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRol("GUEST");
        user.setActive(true);

        Guest guest = new Guest();
        guest.setName("Guest " + username);
        guest.setDocument("DOC-" + username);
        guest.setPhone("3000000000");
        guest.setEmail(username + "@mail.com");
        guest.setActive(true);

        userService.createGuestAccount(user, guest);
        createdUser = userDAO.findByUsername(username);
    }

    @Then("the guest account should be linked to its guest profile")
    public void theGuestAccountShouldBeLinkedToItsGuestProfile() {
        assertNotNull(createdUser);
        assertNotNull(createdUser.getGuestId());
    }

    @Then("the stored password for username {string} should be hashed instead of {string}")
    public void theStoredPasswordForUsernameShouldBeHashedInsteadOf(String username, String rawPassword) {
        User storedUser = userDAO.findByUsername(username);
        assertNotNull(storedUser);
        assertNotEquals(rawPassword, storedUser.getPassword());
        assertTrue(PasswordHasher.isHashed(storedUser.getPassword()));
        assertTrue(PasswordHasher.matches(rawPassword, storedUser.getPassword()));
    }

    @When("I create a receptionist account with username {string} and password {string}")
    public void iCreateAReceptionistAccountWithUsernameAndPassword(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRol("RECEPTIONIST");
        user.setActive(true);

        userService.create(user);
        createdUser = userDAO.findByUsername(username);
    }

    @Then("the receptionist account should not be linked to a guest profile")
    public void theReceptionistAccountShouldNotBeLinkedToAGuestProfile() {
        assertNotNull(createdUser);
        assertNull(createdUser.getGuestId());
    }

    @Given("there is a guest account {string} with password {string}")
    public void thereIsAGuestAccountWithPassword(String username, String password) {
        iCreateAGuestAccountWithUsernameAndPassword(username, password);
    }

    @Given("guest {string} has an occupied booking")
    public void guestHasAnOccupiedBooking(String username) {
        User guestUser = userDAO.findByUsername(username);
        assertNotNull(guestUser);
        assertNotNull(guestUser.getGuestId());

        Room room = new Room();
        room.setNumber(roomCounter++);
        room.setType("SINGLE");
        room.setPrice(180.0);
        room.setAvailable(true);
        roomService.create(room);

        Room savedRoom = roomService.findAll().stream()
                .filter(existing -> existing.getNumber() == room.getNumber())
                .findFirst()
                .orElseThrow();

        Booking booking = new Booking();
        booking.setGuestId(guestUser.getGuestId());
        booking.setRoomId(savedRoom.getId());
        booking.setStartDate(LocalDate.now().plusDays(1).toString());
        booking.setEndDate(LocalDate.now().plusDays(3).toString());
        booking.setStatus("RESERVED");
        bookingService.checkIn(booking);
    }

    @When("I log in as {string} with password {string}")
    public void iLogInAsWithPassword(String username, String password) {
        loggedInUser = userService.login(username, password);
    }

    @When("I load the bookings visible for the logged guest")
    public void iLoadTheBookingsVisibleForTheLoggedGuest() {
        assertNotNull(loggedInUser);
        assertNotNull(loggedInUser.getGuestId());
        visibleBookings = bookingService.findByGuestId(loggedInUser.getGuestId());
    }

    @Then("I should only see {int} booking for the logged guest")
    public void iShouldOnlySeeBookingForTheLoggedGuest(Integer expectedCount) {
        assertEquals(expectedCount, visibleBookings.size());
    }

    @Then("every visible booking should belong to username {string}")
    public void everyVisibleBookingShouldBelongToUsername(String username) {
        User guestUser = userDAO.findByUsername(username);
        assertNotNull(guestUser);
        assertNotNull(guestUser.getGuestId());
        assertFalse(visibleBookings.isEmpty());
        assertTrue(visibleBookings.stream().allMatch(booking -> booking.getGuestId() == guestUser.getGuestId()));
    }
}
