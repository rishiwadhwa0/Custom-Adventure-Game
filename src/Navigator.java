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
        while (!checkIfAtEnd()) {
            printCurrentLocation();
            printDirections();
            requestUserForNextMove();
        }
    }

    public void requestUserForNextMove() {
        String command = "";
        String direction = "";
        while (true) {
            System.out.println("You can let me know where you want to go to by sending the command 'go compass_direction'.");
            command = scanner.nextLine();
            direction = commandChecker(command);
            if (direction != null) {
                World.Room.Direction dirInList = checkIfValidDirection(direction);
                if (dirInList != null) {
                    updateLocation(dirInList.getRoom());
                    break;
                } else {
                    System.out.println("I can't go in the direction of " + direction);
                }
            } else {
                System.out.println("I don't understand " + command);
            }
            System.out.println("You can let me know where you want to go to by" +
                    " sending the command 'go compass_direction'.");
        }
    }

    public String commandChecker(String command) {
        if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit")) {
            System.exit(0);
        }
        String [] commandWords = command.split(" ");
        if (commandWords.length == 2 && commandWords[0].equalsIgnoreCase("go")) {
            return commandWords[1];
        } else {
            return null;
        }
    }

    public World.Room.Direction checkIfValidDirection(String direction) {
        for (World.Room.Direction dir : currentRoom.getDirections()) {
            if (dir.getDirectionName().equalsIgnoreCase(direction)) {
                return dir;
            }
        }
        return null;
    }

    public List<World.Room.Direction> printDirections() {
        List<World.Room.Direction> directions = currentRoom.getDirections();
        System.out.println("====DIRECTIONS TO ROOMS NEARBY====");
        for (int i = 0; i < directions.size(); i++) {
            System.out.println(directions.get(i).getDirectionName());
            System.out.println(directions.get(i).getRoom() + "\n");
        }
        return directions;
    }

    public boolean checkIfAtEnd() {
        if (currentRoom.getName().equals(world.getEndingRoom())) {
            System.out.println("You have reached your final destination");
            return true;
        }
        return false;
    }

    public void printCurrentLocation() {
        System.out.println("====CURRENT ROOM====");
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
        while (true) {
            System.out.println("Is there any particular world you would like me to navigate?" +
                    " Enter 'no' for Siebel, or copy-paste a URL for another.");
            try {
                if (loadURL(scanner.nextLine())) {
                    break;
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
