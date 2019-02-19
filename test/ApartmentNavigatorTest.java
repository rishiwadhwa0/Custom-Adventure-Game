import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//used testing for console printing from https://stackoverflow.com/questions/1119385/junit-test-for-system-out-println

public class ApartmentNavigatorTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private Navigator tGps;

    @Before
    public void setUp() throws Exception {
        tGps = new Navigator(true);
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void checkValidPickupCommand() {
        assertEquals("pickup", tGps.findCommandType("pickup berries"));
    }

    @Test
    public void checkInvalidPickupCommand() {
        assertEquals("Not A Command", tGps.findCommandType("pickup"));
    }

    @Test
    public void checkValidUsewithCommand() {
        assertEquals("usewith", tGps.findCommandType("Use berries With Bananas"));
    }

    @Test
    public void checkInvalidUsewithCommand() {
        assertEquals("Not A Command", tGps.findCommandType("Use berries With"));
    }

    @Test
    public void checkIfPickedUpValidItem() {
        assertTrue(tGps.handlePickupCommand("pickup Key_Under_Mat"));
    }

    @Test
    public void checkIfPickedUpInvalidItem() {
        assertFalse(tGps.handlePickupCommand("pickup basketball"));
    }

    @Test
    public void checkIfUsedValidItem() {
        tGps.handlePickupCommand("pickup Key_Under_Mat");
        assertTrue(tGps.handleUsewithCommand("use key_under_mat with east"));
    }

    @Test
    public void checkUsewithInvalidDirection() {
        tGps.handlePickupCommand("pickup Key_Under_Mat");
        tGps.handleUsewithCommand("use key_under_mat with west");
        assertEquals("That's not a valid direction.\r\n", outContent.toString());
    }

    @Test
    public void checkUsewithInvalidItem() {
        tGps.handlePickupCommand("pickup Key_Under_Mat");
        tGps.updateLocation("Rishi's Bedroom");
        tGps.handleUsewithCommand("use key_under_mat with north");
        assertEquals("I can't use key_under_mat to go in that direction.\r\n", outContent.toString());
    }

    @Test
    public void checkIfHaveAnItem() {
        tGps.updateLocation("Rishi's Bedroom");
        tGps.handleUsewithCommand("use Mystery_Key with north");
        assertEquals("You don't have that item.\r\n", outContent.toString());
    }

    @Test
    public void checkIfMonster() {
        tGps.updateLocation("Balcony");
        assertTrue(tGps.checkIfMonster());
    }

    @Test
    public void checkIfNoMonster() {
        assertFalse(tGps.checkIfMonster());
    }
}
