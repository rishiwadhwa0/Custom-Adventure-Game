import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.junit.Before;
import org.junit.Test;

import javax.xml.catalog.Catalog;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ParsingApartmentJSONTest {
    private static final String APARTMENT_JSON = Data.getFileContentsAsString("apartment.json");

    private World apartment;
    private List<World.Room> aRooms;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();
        apartment = gson.fromJson(APARTMENT_JSON, World.class);
        aRooms = apartment.getRooms();
    }

    @Test
    public void checkLivingRoomName() {
        World.Room livingRoom = aRooms.get(1);
        assertEquals("Living Room", livingRoom.getName());
    }

    @Test
    public void checkLivingRoomItemListSize() {
        World.Room livingRoom = aRooms.get(1);
        assertEquals(0, livingRoom.getItems().size());
    }

    @Test
    public void checkLivingRoomDirectionKeyNames() {
        World.Room livingRoom = aRooms.get(1);
        assertEquals(0, livingRoom.getDirections().get(1).getValidKeyNames().size());
    }

    @Test
    public void checkIfMonster() {
        World.Room livingRoom = aRooms.get(2);
        assertEquals("true", livingRoom.getMonster());
    }

    @Test
    public void checkIfNoMonster() {
        assertEquals("false", aRooms.get(0).getMonster());
    }

    @Test
    public void checkIncompatibleWorld() {
        Navigator tGps = new Navigator(true);
        try {
            tGps.loadURL("https://courses.engr.illinois.edu/cs126/adventure/siebel.json");
        } catch (Exception e) {
            fail();
        }
    }
}
