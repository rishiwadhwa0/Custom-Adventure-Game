import java.net.MalformedURLException;
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.net.URL;

/**
 * Navigator class is like a GPS for navigating through a world represented by a JSON Object
 */
public class Navigator {
    /* Siebel URL contains Siebel JSON object */
    private static final String SIEBEL_URL =
            "https://courses.engr.illinois.edu/cs126/adventure/siebel.json";

    /* Scanner to use to get input */
    private Scanner scanner = new Scanner(System.in);

    /* The world that we are navigating through. */
    private World world;

    /* The world's rooms */
    private List<World.Room> rooms;

    /* The current room we are in */
    private World.Room currentRoom;

    /**
     * Constructor for Navigator class. Calls the startNavigator method if not in Tester mode.
     * @param testing is for tester to construct a Navigator object without soliciting user input.
     */
    public Navigator(boolean testing) {
        if (testing == false) {
            startNavigator();
        } else {
            //TESTER MODE
            try {
                loadURL(SIEBEL_URL);
            } catch (Exception e) {
                System.out.println("====INVALID URL====");
            }
            currentRoom = updateLocation(world.getStartingRoom());
        }
    }

    /**
     * Main method creates a Navigator object.
     * @param args unused
     */
    public static void main(String[] args) {
        Navigator gps = new Navigator(false);
    }

    /**
     * Copy method of main method, but belongs to this object of Navigator only
     */
    public void startNavigator() {
        System.out.println("Welcome to NAVIAGATOR");
        setUpWorld();
        boolean firstTime = true;
        while (!checkIfAtEnd()) {
            printCurrentLocation();
            printDirections();
            if (firstTime) {
                System.out.println("You can let me know where you want to go to by sending " +
                        "the command 'go compass_direction'.");
                firstTime = false;
            }
            requestUserForNextMove();
        }
    }

    /**
     * Keeps soliciting commands from user until reach end or receive exit command
     */
    public void requestUserForNextMove() {
        String command = "";
        String direction = "";
        while (true) {
            command = scanner.nextLine();
            direction = commandChecker(command);
            if (direction != null) {
                //i.e. it was a valid command
                World.Room.Direction dirInList = checkIfValidDirection(direction);
                if (dirInList != null) {
                    //i.e. it was a feasible direction
                    updateLocation(dirInList.getRoom());
                    break;
                } else {
                    //i.e. it was an infeasible direction
                    System.out.println("I can't go in the direction of " + direction);
                }
            } else {
                //i.e. it was an invalid command
                System.out.println("I don't understand '" + command + "'");
            }
        }
    }

    /**
     * Checks if the "command" from the user is actually a command.
     * Returns the direction if it was a command.
     * @param command the entire command from the user.
     * @return only the direction part of the entire command statement, null if not a command.
     */
    public String commandChecker(String command) {
        if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit")) {
            System.exit(0);
        }
        String [] commandWords = command.split(" ");
        if (commandWords.length > 1 && commandWords[0].equalsIgnoreCase("go")) {
            String direction = "";
            for (int i = 1; i < commandWords.length; i++) {
                direction = direction + commandWords[i] + " ";
            }
            return direction.trim();
        } else {
            return null;
        }
    }

    /**
     * Checks if the directions is one of the directions in the list for the current room we are in.
     * @param direction the direction the user wants to go.
     * @return the direction object if that direction exists, null otherwise.
     */
    public World.Room.Direction checkIfValidDirection(String direction) {
        for (World.Room.Direction dir : currentRoom.getDirections()) {
            if (dir.getDirectionName().equalsIgnoreCase(direction)) {
                return dir;
            }
        }
        return null;
    }

    /**
     * Prints the directions and the associated rooms for the current room we are in.
     * @return the List of directions for the current room we are in.
     */
    public List<World.Room.Direction> printDirections() {
        List<World.Room.Direction> directions = currentRoom.getDirections();
        System.out.println("====DIRECTIONS TO ROOMS NEARBY====");
        for (int i = 0; i < directions.size(); i++) {
            System.out.println(directions.get(i).getDirectionName());
            //System.out.println(directions.get(i).getRoom() + "\n");
        }
        return directions;
    }

    /**
     * Checks if the current room is the ending room.
     * @return whether we are at the end or not.
     */
    public boolean checkIfAtEnd() {
        if (currentRoom.getName().equals(world.getEndingRoom())) {
            printCurrentLocation();
            System.out.println("====SUCCESS!!!====");
            System.out.println("You have reached your final destination.");
            return true;
        }
        return false;
    }

    /**
     * Prints the description of the current room we are in.
     */
    public void printCurrentLocation() {
        System.out.println("====CURRENT ROOM====");
        System.out.println(currentRoom.getDescription());
    }

    /**
     * Using Stream API and referencing https://www.baeldung.com/find-list-element-java
     * @param roomName the name of the room to move to.
     */
    public World.Room updateLocation(String roomName) {
        if (roomName == null) {
            return null;
        }

        World.Room roomToMove = rooms.stream().filter(room -> roomName.
                equalsIgnoreCase(room.getName())).findAny().orElse(null);

        if (roomToMove == null) {
            return null;
        } else {
            currentRoom = roomToMove;
            return currentRoom;
        }
    }

    /**
     * Return the current room object for testing.
     * @return the current room
     */
    public World.Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Returns the entire list of rooms for testing
     * @return the entire list of rooms
     */
    public List<World.Room> getRooms() {
        return rooms;
    }

    /**
     * Asks user for a URL, and attempts to get the JSON from the URL if possible
     * Otherwise, it loads the JSON from a link, known to work.
     */
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

    /**
     * Checks if the user didn't want to load from a url, and tries to
     * load from a url if the user specified one
     * @param url the url to load, could be a command to not load the url
     * @return whether any url was loaded from or not.
     * @throws Exception an exception if the url is invalid
     */
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

                if (!worldValidityChecker(world, rooms)) {
                    System.out.println("====WORLD NOT COMPATIBLE WITH NAVIGATOR====");
                    return false;
                }

                System.out.println("====WORLD LOADED FROM YOUR URL====");
                return true;
            }
            return false;
        }
    }

    private boolean worldValidityChecker(World worldToCheck, List<World.Room> roomsToCheck) {
        if (worldToCheck.getStartingRoom() == null || worldToCheck.getEndingRoom() == null ||
                roomsToCheck == null) {
            return false;
        }

        for (int i = 0; i < roomsToCheck.size(); i++) {
            World.Room room = roomsToCheck.get(i);
            if (room.getDirections() == null || room.getName() == null || room.getDescription() == null) {
                return false;
            }

            List<World.Room.Direction> directions = room.getDirections();
            for (int dI = 0; dI < directions.size(); dI++) {
                World.Room.Direction dir = directions.get(dI);
                if (dir.getDirectionName() == null || dir.getRoom() == null) {
                    return false;
                }
            }
        }
        return true;
    }
}