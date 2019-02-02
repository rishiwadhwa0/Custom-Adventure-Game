import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.net.URL;

public class Navigator {
    private static final String SIEBEL_URL = "https://courses.engr.illinois.edu/cs126/adventure/siebel.json";

    private Scanner scanner = new Scanner(System.in);
    private World world;
    private List<World.Room> rooms;
    private World.Room currentRoom;

    public Navigator(boolean testing) {
        if (testing == false) {
            startNavigator();
        } else {
            try {
                loadURL(SIEBEL_URL);
            } catch (Exception e) {
                System.out.println("====INVALID URL====");
            }
            currentRoom = updateLocation(world.getStartingRoom());
        }
    }

    public static void main(String[] args) {
        Navigator gps = new Navigator(false);
    }

    public void startNavigator() {
        System.out.println("Welcome to NAVIAGATOR");
        setUpWorld();
        printCurrentLocation();
        checkIfAtEnd();
    }

    public boolean checkIfAtEnd() {
        if (currentRoom.getName().equals(world.getEndingRoom())) {
            return true;
        }
        return false;
    }

    public void printCurrentLocation() {
        System.out.println(currentRoom.getDescription());
    }

    /**
     * Using Stream API and referencing https://www.baeldung.com/find-list-element-java
     * @param roomName
     */
    public World.Room updateLocation(String roomName) {
        World.Room roomToMove = rooms.stream().filter(room -> roomName.equalsIgnoreCase(room.getName())).findAny().
                orElse(null);

        if (roomToMove == null) {
            return null;
        } else {
            currentRoom = roomToMove;
            return currentRoom;
        }
    }

    public World.Room getCurrentRoom() {
        return currentRoom;
    }

    public List<World.Room> getRooms() {
        return rooms;
    }

    public void setUpWorld() {
        boolean badAnswer = true;
        while (badAnswer) {
            System.out.println("Is there any particular world you would like me to navigate?" +
                    " Enter 'no' for Siebel, or copy-paste a URL for another.");
            try {
                if (loadURL(scanner.nextLine())) {
                    badAnswer = false;
                }
            } catch (Exception e) {
                System.out.println("====INVALID URL====");
            }
        }
        updateLocation(world.getStartingRoom());
    }

    public boolean loadURL(String url) throws Exception {
        Gson gson = new Gson();
        if (url.equalsIgnoreCase("no")) {
            // Make an HTTP request to default URL
            final HttpResponse<String> stringHttpResponse = Unirest.get(SIEBEL_URL).asString();
            String json = stringHttpResponse.getBody();
            world = gson.fromJson(json, World.class);
            rooms = world.getRooms();
            System.out.println("====WORLD LOADED FROM DEFAULT SIEBEL URL====");
            return true;
        } else {
            // Make an HTTP request to user's URL
            final HttpResponse<String> stringHttpResponse = Unirest.get(url).asString();
            if (stringHttpResponse.getStatus() == 200) {
                String json = stringHttpResponse.getBody();
                world = gson.fromJson(json, World.class);
                rooms = world.getRooms();
                System.out.println("====WORLD LOADED FROM YOUR URL====");
                return true;
            }
            return false;
        }
    }
}
