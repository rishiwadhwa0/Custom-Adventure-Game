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
    /* JSON string */
    private static final String APARTMENT_JSON = Data.getFileContentsAsString("apartment.json");

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
                loadURL(APARTMENT_JSON  );
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
            if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
            
            String commandType = findCommandType(command);
            if (commandType.equals("go")) {
                if (handleGoCommand(command)) {
                    break;
                }
            } else if (commandType.equals("usewith")) {
                if (handleUsewithCommand(command)) {
                    break;
                }
            } else if (commandType.equals("pickup")) {
                if (handlePickupCommand(command)) {
                    break;
                }
            } else {
                System.out.println("I don't understand '" + command + "'");
            }
        }
    }

    private String findCommandType(String command) {
        String [] commandWords = command.split(" ");
        if (commandWords[0].equalsIgnoreCase("go")) {
            return "go";
        } else if (commandWords.length > 3 && commandWords[0].equalsIgnoreCase("use") &&
                commandWords[2].equalsIgnoreCase("with")) {
            return "usewith";
        } else if (commandWords.length > 1 && commandWords[0].equalsIgnoreCase("pickup")) {
            return "pickup";
        } else {
            return "Not A Command";
        }
    }

    private boolean handlePickupCommand(String command) {
        String [] commandWords = command.split(" ");
        String itemToPickUp = commandWords[1];
        List<World.Room.Item> roomItems = currentRoom.getItems();

        World.Room.Item itemInRoom = roomItems.stream().filter(item -> itemToPickUp.
                equalsIgnoreCase(item.getName())).findAny().orElse(null);

        if (itemInRoom == null) {
            System.out.println("That item does not exist in this room.");
            return false;
        }

        world.getPlayer().addItem(itemInRoom);
        roomItems.remove(itemInRoom);
        return true;
    }

    private boolean handleUsewithCommand(String command) {
        String [] commandWords = command.split(" ");
        String itemNameToCheck = commandWords[1];
        List<World.Room.Item> playerItems = world.getPlayer().getItems();

        World.Room.Item itemToUse = playerItems.stream().filter(item -> itemNameToCheck.
                equalsIgnoreCase(item.getName())).findAny().orElse(null);

        if (itemToUse == null) {
            System.out.println("You don't have that item.");
            return false;
        }

        String direction = commandWords[3];
        World.Room.Direction validDir = checkIfValidDirection(direction);
        if (validDir == null) {
            System.out.println("That's not a valid direction.");
            return false;
        } else if (!(itemInValidKeyNames(itemToUse, validDir))) {
            System.out.println("I can't use " + commandWords[1] + " to go in that direction.");
            return false;
        }

        updateLocation(validDir.getRoom());
        return true;
    }

    private boolean itemInValidKeyNames(World.Room.Item itemToUse, World.Room.Direction validDir) {
        for (String keyName : validDir.getValidKeyNames()) {
            if (keyName.equalsIgnoreCase(itemToUse.getName())) {
                validDir.setEnabled("true");
                return true;
            }
        }
        return false;
    }

    private boolean handleGoCommand(String command) {
        String direction = getDirectionFromGo(command);
        if (direction == null) {
            System.out.println("I don't understand '" + command + "'");
            return false;
        }

        World.Room.Direction validDir = checkIfValidDirection(direction);
        if (validDir == null || validDir.getEnabled().equalsIgnoreCase("false")) {
            System.out.println("I can't go in the direction of " + direction);
            return false;
        }

        updateLocation(validDir.getRoom());
        return true;
    }

    /**
     * Checks if the "command" from the user is actually a command.
     * Returns the direction if it was a command.
     * @param command the entire command from the user.
     * @return only the direction part of the entire command statement, null if not a command.
     */
    public String getDirectionFromGo(String command) {
        String [] commandWords = command.split(" ");
        if (commandWords.length > 1) {
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
        System.out.print("ITEMS: ");
        for (World.Room.Item item : currentRoom.getItems()) {
            System.out.print(item.getName() + " ");
        }
        System.out.println("");
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
                    " Enter 'no' for Apartment, or copy-paste a URL for another.");
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
            world = gson.fromJson(APARTMENT_JSON, World.class);
            rooms = world.getRooms();
            System.out.println("====WORLD LOADED FROM DEFAULT APARTMENT FILE====");
            return true;
        } else {
            // Make an HTTP request to user's URL
            final HttpResponse<String> stringHttpResponse = Unirest.get(url).asString();
            if (stringHttpResponse.getStatus() == 200) {
                String json = stringHttpResponse.getBody();
                world = gson.fromJson(json, World.class);
                rooms = world.getRooms();

                /*
                if (!worldValidityChecker(world, rooms)) {
                    System.out.println("====WORLD NOT COMPATIBLE WITH NAVIGATOR====");
                    return false;
                }
                */

                System.out.println("====WORLD LOADED FROM YOUR URL====");
                return true;
            }
            return false;
        }
    }

    /*
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
    */
}