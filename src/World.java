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

    public boolean checkNullFields() {
        if (startingRoom == null || endingRoom == null || player == null || rooms == null) {
            return true;
        }
        if (player.checkNullFields()) {
            return true;
        }
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).checkNullFields()) {
                return true;
            }
        }
        return false;
    }

    //===========================================
    public class Player {
        private List<Room.Item> items;
        private int hp;

        public void addItem(Room.Item itemToAdd) {
            items.add(itemToAdd);
        }

        public List<Room.Item> getItems() {
            return items;
        }

        public void setHp(int hp) {
            this.hp = hp;
        }

        public int getHp() {
            return hp;
        }

        public boolean checkNullFields() {
            if (items == null || hp <= 0) {
                return true;
            }
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).checkNullFields()) {
                    return true;
                }
            }
            return false;
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

        public boolean checkNullFields() {
            if (name == null || description == null || monster == null || items == null || directions == null) {
                return true;
            }
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).checkNullFields()) {
                    return true;
                }
            }
            for (int i = 0; i < directions.size(); i++) {
                if (directions.get(i).checkNullFields()) {
                    return true;
                }
            }
            return false;
        }

        //=======================================
        public class Item {
            private String name;

            public String getName() {
                return name;
            }

            public boolean checkNullFields() {
                if (name == null) {
                    return true;
                }
                return false;
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

            public boolean checkNullFields() {
                if (directionName == null || room ==  null || validKeyNames == null) {
                    return true;
                }
                return false;
            }
        }
    }
}