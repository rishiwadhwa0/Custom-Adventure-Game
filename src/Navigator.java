import java.net.MalformedURLException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Random;
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
    /* Guessing bound */
    public static final int GUESS_BOUND = 21;

    /** Difference to kill the monster */
    public static final int KILL_DIFFERENCE = 2;

    /* Difference to get away from monster without any damage. */
    public static final int NO_DAMAGE_DIFFERENCE = 5;

    /* Difference to take 50 HP points of damage. */
    public static final int HALF_DAMAGE_DIFFERENCE = 10;

    /** Amount of HP damage done if guess withing 10. */
    public static final int DAMAGE_VALUE = 50;

    /* Monster directions */
    private static final String DIRECTIONS = "You have encountered a monster.\n" +
                                             "The monster will pick an integer between 0 and " + GUESS_BOUND + ".\n" +
                                             "You're number is within 2 of the monster's   --> you kill it.\n" +
                                             "You're number is within 5 of the Monster's   --> nothing happens.\n" +
                                             "You're number is within 10 of the Monster's  --> you lose 50 HP.\n" +
                                             "Otherwise                                    --> your HP goes to 0.\n";

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


    public Navigator(boolean testing) {
        if (testing == false) {
            startNavigator();
        } else {
            //TESTER MODE
            try {
                loadURL("default");
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
        boolean firstTime = true;
        while (!checkIfAtEnd()) {
            printCurrentLocation();
            printDirections();
            handleMonster();
            checkIfAlive();
            if (firstTime) {
                System.out.println("You can let me know where you want to go to by sending " +
                        "the command 'go compass_direction'.");
                firstTime = false;
            }
            requestUserForNextMove();
        }
    }

    public void handleMonster() {
        if (checkIfMonster()) {
            System.out.println("========MONSTER ALERT!!!!=======");
            System.out.println(DIRECTIONS);
            Random rand = new Random();
            int monsterNumber = rand.nextInt(GUESS_BOUND);
            int userNumber = scanner.nextInt();
            scanner.nextLine();
            System.out.println("The monster's number was: " + monsterNumber);
            if (Math.abs(monsterNumber - userNumber) <= KILL_DIFFERENCE) {
                currentRoom.setMonster("false");
                System.out.println("====You defeated the monster====\n");
            } else if (Math.abs(monsterNumber - userNumber) <= NO_DAMAGE_DIFFERENCE) {
                System.out.println("====You get away unscathed====\n");
            } else if (Math.abs(monsterNumber - userNumber) <= HALF_DAMAGE_DIFFERENCE) {
                world.getPlayer().setHp(world.getPlayer().getHp() - DAMAGE_VALUE);
                System.out.println("====You took a hard hit====\n");
            } else {
                world.getPlayer().setHp(0);
            }
            printCurrentLocation();
            printDirections();
        }
    }

    private void checkIfAlive() {
        if (world.getPlayer().getHp() <= 0) {
            System.out.println("====Unfortunately, you're HP is 0 and you're in no condition to continue the game====");
            System.exit(0);
        }
    }

    public boolean checkIfMonster() {
        if (currentRoom.getMonster().equalsIgnoreCase("true")) {
            return true;
        }
        return false;
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

    public String findCommandType(String command) {
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

    public boolean handlePickupCommand(String command) {
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

    public boolean handleUsewithCommand(String command) {
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
        }
        return directions;
    }


    public boolean checkIfAtEnd() {
        if (currentRoom.getName().equals(world.getEndingRoom())) {
            printCurrentLocation();
            System.out.println("====SUCCESS!!!====");
            System.out.println("You have reached your final destination.");
            return true;
        }
        return false;
    }


    public void printCurrentLocation() {
        System.out.println("====CURRENT ROOM====");
        System.out.println(currentRoom.getDescription());
        System.out.print("ITEMS: ");
        for (World.Room.Item item : currentRoom.getItems()) {
            System.out.print(item.getName() + " ");
        }
        System.out.println("\nHP: " + world.getPlayer().getHp());
    }


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


    public World.Room getCurrentRoom() {
        return currentRoom;
    }


    public List<World.Room> getRooms() {
        return rooms;
    }

    public World getWorld() {
        return world;
    }

    public void setUpWorld() {
        while (true) {
            System.out.println("Is there any particular world you would like me to navigate? " +
                    "Enter 'local' for local file, 'default' for Apartment file, or copy-paste a URL for remote file.");
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
        if (url.equalsIgnoreCase("local")) {
            System.out.println("Enter file name to use as: file_name.type");
            String fileName = scanner.nextLine();
            world = gson.fromJson(Data.getFileContentsAsString(fileName), World.class);
            rooms = world.getRooms();

            if (!worldValidityChecker(world)) {
                System.out.println("====WORLD NOT COMPATIBLE WITH NAVIGATOR====");
                return false;
            }

            System.out.println("====WORLD LOADED FROM SPECIFIED FILE====");
            return true;
        } else if (url.equalsIgnoreCase("default")) {
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

                if (!worldValidityChecker(world)) {
                    System.out.println("====WORLD NOT COMPATIBLE WITH NAVIGATOR====");
                    return false;
                }

                System.out.println("====WORLD LOADED FROM YOUR URL====");
                return true;
            }
            return false;
        }
    }

    private static boolean worldValidityChecker(World worldToCheck) {
        if (worldToCheck == null) {
            return false;
        } else if (worldToCheck.checkNullFields()) {
            return false;
        } else {
            return true;
        }
    }
}