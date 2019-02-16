import java.util.List;

/**
 * Classes representing the JSON objects
 *
 * World
 *      Player
 *      Room
 *          Monster
 *          Item
 *          Direction
 */

//===============================================
public class World {
    private String startingRoom;
    private String endingRoom;
    private Player player;
    private List<Room> rooms;

    public String getStartingRoom() {
        return startingRoom;
    }

    public String getEndingRoom() {
        return endingRoom;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    //===========================================
    public class Player {
        private List<Room.Item> items;

        public void addItem(Room.Item itemToAdd) {
            items.add(itemToAdd);
        }

        public List<Room.Item> getItems() {
            return items;
        }


    }
    //===========================================
    public class Room {
        private String name;
        private String description;
        private String monster;
        private List<Item> items;
        private List<Direction> directions;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public void setMonster(String monster) {
            this.monster = monster;
        }

        public String getMonster() {
            return monster;
        }

        public List<Item> getItems() {
            return items;
        }

        public List<Direction> getDirections() {
            return directions;
        }

        //=======================================
        public class Item {
            private String name;

            public String getName() {
                return name;
            }
        }

        //========================================
        public class Direction {
            private String directionName;
            private String room;
            private String enabled;
            private List<String> validKeyNames;

            public String getDirectionName() {
                return directionName;
            }

            public String getRoom() {
                return room;
            }

            public void setEnabled(String enabled) {
                this.enabled = enabled;
            }

            public String getEnabled() {
                return enabled;
            }

            public List<String> getValidKeyNames() {
                return validKeyNames;
            }
        }
    }
}