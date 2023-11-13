public class Model {
    private DBService dbService;
    private Player player;
    private Viewer viewer;

    private final int SPACE = 0;
    private final int PLAYER = 1;
    private final int WALL = 2;
    private final int BOX = 3;
    private final int CHECK = 4;

    private final char LEFT =  'a';
    private final char RIGHT = 'd';
    private final char UP = 'w';
    private final char DOWN =  's';
    private final char RESTART = 'r';
    private final char EXIT = '\u001B'; //escape

    private String move;
    private int playerPosX;
    private int playerPosY;

    private int[][] map;
    private Levels levelList;

    private int totalMoves = 0;

    private int playerCount;
    private int boxesCount;
    private int checksCount;

    private int[][] checksPos;

    public Model(Viewer viewer) {
        this.viewer = viewer;
        dbService = new DBService();
        initPlayer("Stive");
        levelList = new Levels();
        playerPosX = -1;
        playerPosY = -1;
        move = "Down";
    }

    public int[][] getDesktop(){
        return map;
    }

    public void doAction(char message) {
        System.out.println("got -- " + message); //debug
        if (message == RESTART) {
            System.out.println("------------ Map restarted ------------\n\n");
            map = levelList.getCurrentMap();
            scanMap();
        } else if (message == EXIT) {
            System.exit(0);
        }

        if (map == null) {
            return;
        }

        if (message == LEFT) {
            move = "Left";
            moveLeft();
        } else if(message == RIGHT) {
            move = "Right";
            moveRight();
        } else if(message == UP) {
            move = "Up";
            moveTop();
        } else if(message == DOWN) {
            move = "Down";
            moveBot();
        }

        returnCheck();
        viewer.update();

        System.out.println("Moves: " + totalMoves); //debug

        if (isWon()) {
            javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(), "You win!");
            map = levelList.getNextLevel();

            if(map != null) {
                scanMap();
            }

            viewer.update();
            totalMoves = 0;
        }
    }

    public void changeLevel(String command) {
        String stringLevelNumber = command.substring(command.length() - 1, command.length());
        int levelNumber = Integer.parseInt(stringLevelNumber);
        levelList.setCurrentLevel(levelNumber);
        map = levelList.getNextLevel();

        if(map != null) {
            scanMap();
        }

        viewer.showCanvas();
        totalMoves = 0;
    }

    public String getMove() {
        return move;
    }

    public void initPlayer(String nickname) {
        player = dbService.getPlayerInfo(nickname);
        System.out.println(player.getNickname());
        System.out.println(player.getAvailableSkins());
        System.out.println(player.getTotalCoins());
    }

    private void scanMap() {
        playerCount = 0;
        boxesCount = 0;
        checksCount = 0;
        for(int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j] + " ");
                if (map[i][j] == PLAYER) {
                    playerPosX = j;
                    playerPosY = i;
                    playerCount++;
                } else if (map[i][j] == BOX) {
                    boxesCount++;
                } else if (map[i][j] == CHECK) {
                    checksCount++;
                }
            }
            System.out.println();
        }

        if (playerCount != 1 || boxesCount != checksCount || boxesCount == 0 && checksCount == 0) {
            System.out.println("Map have invalid game parameters");
            System.out.println(playerCount);
            System.out.println(boxesCount);
            System.out.println(checksCount);
            map = null;
            return;
        }

        checksPos = new int[checksCount][2];
        int checksQueue = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == CHECK) {
                    checksPos[checksQueue][0] = i;
                    checksPos[checksQueue][1] = j;
                    checksQueue++;
                }
            }
        }
    }

    private boolean isWon() {
        for (int i = 0; i < checksPos.length; i++) {
            int checkPosY = checksPos[i][0];
            int checkPosX = checksPos[i][1];
            if (map[checkPosY][checkPosX] != BOX) {
                return false;
            }
        }
        return true;
    }

    private void returnCheck() {
       for (int i = 0; i < checksPos.length; i++) {
           int checkPosY = checksPos[i][0];
           int checkPosX = checksPos[i][1];
           if (map[checkPosY][checkPosX] == SPACE) {
               map[checkPosY][checkPosX] = CHECK;
               break;
           }
       }
   }

    private void moveLeft() {
        if ((map[playerPosY][playerPosX - 1] == WALL)) {
            System.out.println("Impossible move to the left"); //debug
            return;
        }

        if (map[playerPosY][playerPosX - 1] == BOX && !canMoveBoxToLeft()) {
            return;
        }

        if (map[playerPosY][playerPosX - 1] == BOX) {
            map[playerPosY][playerPosX - 1] = SPACE;
            map[playerPosY][playerPosX - 2] = BOX;
        }

        map[playerPosY][playerPosX - 1] = PLAYER;
        map[playerPosY][playerPosX] = SPACE;
        playerPosX -= 1;
        totalMoves++;
    }

    private void moveRight() {
        if ((map[playerPosY][playerPosX + 1] == WALL)) {
            System.out.println("Impossible move to the right"); //debug
            return;
        }

        if(map[playerPosY][playerPosX + 1] == BOX && !canMoveBoxToRight()) {
            return;
        }

        if(map[playerPosY][playerPosX + 1] == BOX) {
            map[playerPosY][playerPosX + 1] = SPACE;
            map[playerPosY][playerPosX + 2] = BOX;
        }

        map[playerPosY][playerPosX + 1] = PLAYER;
        map[playerPosY][playerPosX] = SPACE;
        playerPosX += 1;
        totalMoves++;
    }

    private void moveTop() {
        if ((map[playerPosY - 1][playerPosX] == WALL)) {
            System.out.println("Impossible move to the top"); //debug
            return;
        }

        if(map[playerPosY - 1][playerPosX] == BOX && !canMoveBoxToTop()) {
            return;
        }

        if (map[playerPosY - 1][playerPosX] == BOX) {
            map[playerPosY - 1][playerPosX] = SPACE;
            map[playerPosY - 2][playerPosX] = BOX;
        }

        map[playerPosY - 1][playerPosX] = PLAYER;
        map[playerPosY][playerPosX] = SPACE;
        playerPosY -= 1;
        totalMoves++;
    }

    private void moveBot() {
        if (map[playerPosY + 1][playerPosX] == WALL) {
            System.out.println("Impossible move to the bottom"); //debug
            return;
        }

        if(map[playerPosY + 1][playerPosX] == BOX && !canMoveBoxToBot()) {
            return;
        }

        if(map[playerPosY + 1][playerPosX] == BOX) {
            map[playerPosY + 1][playerPosX] = SPACE;
            map[playerPosY + 2][playerPosX] = BOX;
        }

        map[playerPosY + 1][playerPosX] = PLAYER;
        map[playerPosY][playerPosX] = SPACE;
        playerPosY += 1;
        totalMoves++;
    }

    private boolean canMoveBoxToLeft() {
        if (((map[playerPosY][playerPosX - 2] == WALL) || (map[playerPosY][playerPosX - 2] == BOX)) && (playerPosX - 2 >= 0)) {
            System.out.println("Impossible move box to the left"); //debug
            return false;
        }
        return true;
    }

    private boolean canMoveBoxToRight() {
        if (((map[playerPosY][playerPosX + 2] == WALL) || (map[playerPosY][playerPosX + 2] == BOX)) && (playerPosX + 2 < map.length)) {
            System.out.println("Impossible move box to the right"); //debug
            return false;
        }
        return true;
    }

    private boolean canMoveBoxToTop() {
        if (((map[playerPosY - 2][playerPosX] == WALL) || (map[playerPosY - 2][playerPosX] == BOX)) && (playerPosY - 2 >= 0)) {
            System.out.println("Impossible move box to the top"); //debug
            return false;
        }
        return true;
    }

    private boolean canMoveBoxToBot() {
        if (((map[playerPosY + 2][playerPosX] == WALL) || (map[playerPosY + 2][playerPosX] == BOX)) && (playerPosY + 2 < map.length)) {
            System.out.println("Impossible move box to the bottom"); //debug
            return false;
        }
        return true;
    }
}
