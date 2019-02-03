import org.junit.Before;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NavigatorTester {

    private final String SIEBEL_URL = "https://courses.engr.illinois.edu/cs126/adventure/siebel.json";
    private final String SIEBEL_JSON = Data.getFileContentsAsString("siebel.json");

    private World siebel;
    private List<World.Room> sRooms;
    private Navigator tGps;

    /**
     * Method resembles code on Github page linked on Lectures Page on CS 126 Course Website
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        // Make an HTTP request to the above URL
        final HttpResponse<String> stringHttpResponse = Unirest.get(SIEBEL_URL).asString();

        // Check to see if the request was successful; if so, convert the payload JSON into Java objects
        if (stringHttpResponse.getStatus() == 200) {
            String json = stringHttpResponse.getBody();
            siebel = gson.fromJson(json, World.class);
        } else {
            siebel = gson.fromJson(SIEBEL_JSON, World.class);
        }

        tGps = new Navigator(true);
        sRooms = tGps.getRooms();
    }

    //============PARSING TESTS============
    @Test
    public void getStartingRoom() {
        assertEquals("MatthewsStreet", siebel.getStartingRoom());
    }

    @Test
    public void getEndingRoom() {
        assertEquals("Siebel1314", siebel.getEndingRoom());
    }

    @Test
    public void getFirstRoomName() {
        assertEquals("MatthewsStreet", sRooms.get(0).getName());
    }

    @Test
    public void getFirstRoomDescription() {
        assertEquals("You are on Matthews, outside the Siebel Center", sRooms.get(0).getDescription());
    }

    @Test
    public void getFirstRoomFirstDirection() {
        assertEquals("East", sRooms.get(0).getDirections().get(0).getDirectionName());
    }

    @Test
    public void getFirstRoomFirstDirectionRoom() {
        assertEquals("SiebelEntry", sRooms.get(0).getDirections().get(0).getRoom());
    }

    //============Helper Method TESTS============
    @Test
    public void checkValidURL() {
        try {
            assertEquals(true, tGps.loadURL("https://courses.engr.illinois.edu/cs126/adventure/siebel.json"));
        } catch (Exception e) {
            System.out.println("invalid url");
        }
    }

    @Test
    public void checkLoadDefault() {
        try {
            assertEquals(true, tGps.loadURL("No"));
        } catch (Exception e) {
            System.out.println("invalid url");
        }
    }

    @Test (expected = RuntimeException.class)
    public void checkInvalidURL() throws Exception {
        assertEquals(true, tGps.loadURL("lmao"));
    }

    @Test
    public void moveToSiebel112() {
        assertEquals(sRooms.get(4), tGps.updateLocation("Siebel1112"));
    }

    @Test
    public void checkIfAtBeginning() {
        assertEquals("MatthewsStreet", tGps.getCurrentRoom().getName());
    }

    @Test
    public void moveToEndAndCheck() {
        tGps.updateLocation("Siebel1314");
        assertEquals(true, tGps.checkIfAtEnd());
    }

    @Test
    public void checkIfNotAtEnd() {
        tGps.updateLocation("SiebelBasement");
        assertEquals(false, tGps.checkIfAtEnd());
    }

    @Test
    public void checkListOfDirections() {
        List<World.Room.Direction> sEntryDirections = sRooms.get(1).getDirections();
        tGps.updateLocation("SiebelEntry");
        assertEquals(sEntryDirections, tGps.printDirections());
    }
}
