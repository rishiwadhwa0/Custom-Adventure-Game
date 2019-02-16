import org.junit.Before;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    //=====================Helper Method TESTS==================

    //============LOAD URL METHOD TESTS==========
    @Test
    public void checkValidURL() {
        try {
            assertEquals(true, tGps.loadURL("https://courses.engr.illinois.edu/cs126/adventure/siebel.json"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void checkLoadDefault() {
        String loadingURLResult = "";
        try {
             if (tGps.loadURL("No")) {
                 loadingURLResult = "successful";
             }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            loadingURLResult = "exception";
        }

        assertEquals("successful", loadingURLResult);
    }

    @Test
    public void checkLoadNull() {
        String loadingURLResult = "";
        try {
            if (tGps.loadURL(null)) {
                loadingURLResult = "successful";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            loadingURLResult = "exception";
        }

        assertEquals("exception", loadingURLResult);
    }

    @Test
    public void checkInvalidURL() {
        String loadingURLResult = "";
        try {
            if (tGps.loadURL("invalid url")) {
                loadingURLResult = "successful";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            loadingURLResult = "exception";
        }

        assertEquals("exception", loadingURLResult);
    }

    @Test
    public void checkIncompatibleWorld() {
        String loadingURLResult = "";
        try {
            if (tGps.loadURL("http://api.tvmaze.com/singlesearch/" +
                    "shows?q=game-of-thrones&embed=episodes")) {
                loadingURLResult = "successful";
            } else {
                loadingURLResult = "validURLButInvalidJSON";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            loadingURLResult = "exception";
        }

        assertEquals("validURLButInvalidJSON", loadingURLResult);
    }

    //============UPDATE LOCATION METHOD TESTS============
    @Test
    public void moveToSiebel112() {
        assertEquals(sRooms.get(4), tGps.updateLocation("Siebel1112"));
    }

    public void moveToNonexistentRoom() {
        assertEquals(null, tGps.updateLocation("Chuck E. Cheese's - Where a Kid Can Be a Kid"));
    }

    @Test
    public void moveToNull() {
        assertEquals(null, tGps.updateLocation(null));
    }

    //============GET CURRENT ROOM METHOD TESTS==========
    @Test
    public void checkIfAtBeginning() {
        assertEquals(siebel.getStartingRoom(), tGps.getCurrentRoom().getName());
    }

    @Test
    public void checkIfAtSiebelEastHallway() {
        tGps.updateLocation("SiebelEastHallway");
        assertEquals(sRooms.get(5).getName(), tGps.getCurrentRoom().getName());
    }

    @Test
    public void checkIfAtEnd() {
        tGps.updateLocation("Siebel1314");
        assertEquals(siebel.getEndingRoom(), tGps.getCurrentRoom().getName());
    }


    //============CHECK IF AT END METHOD TESTS============
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

    //============PRINT DIRECTIONS METHOD TESTS===========
    @Test
    public void checkListOfDirections() {
        List<World.Room.Direction> sEntryDirections = sRooms.get(1).getDirections();
        tGps.updateLocation("SiebelEntry");
        assertEquals(sEntryDirections, tGps.printDirections());
    }

    //============COMMAND CHECKER METHOD TESTS==============
    @Test
    public void checkValidCommand() {
        assertEquals("fish.", tGps.getDirectionFromGo("gO fish."));
    }

    @Test
    public void checkValidCommandThreeWord() {
        assertEquals("salmon fish.", tGps.getDirectionFromGo("Go salmon fish."));
    }

    @Test
    public void checkInvalidCommand() {
        assertEquals(null, tGps.getDirectionFromGo(": - ) : - ) : - )"));
    }

    @Test
    public void checkValidCommandAndDirection() {
        assertEquals("EasT", tGps.getDirectionFromGo("GO EasT    "));
    }

    //===========CHECK IF VALID DIR METHOD TESTS===============
    @Test
    public void checkInvalidDirection() {
        assertEquals(null, tGps.checkIfValidDirection("EastNorth"));
    }

    @Test
    public void checkValidDirection() {
        assertEquals(sRooms.get(0).getDirections().get(0), tGps.checkIfValidDirection("EasT"));
    }
}
