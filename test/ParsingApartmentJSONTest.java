import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
}
