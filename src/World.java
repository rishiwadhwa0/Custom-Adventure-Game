import java.util.List;

/**
 * Classes representing the JSON objects
 *
 * World
 *      Room
 *          Direction
 */

//===============================================
public class World {
    private String startingRoom;
    private String endingRoom;
    private List<Room> rooms;

    public String getStartingRoom() {
        return startingRoom;
    }

    public String getEndingRoom() {
        return endingRoom;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    //============================================
    public class Room {
        private String name;
        private String description;
        private List<Direction> directions;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<Direction> getDirections() {
            return directions;
        }

        //========================================
        public class Direction {
            private String directionName;
            private String room;

            public String getDirectionName() {
                return directionName;
            }

            public String getRoom() {
                return room;
            }
        }
    }
}