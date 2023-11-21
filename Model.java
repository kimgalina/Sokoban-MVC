import java.io.File;
import java.util.HashMap;

public class Model implements GeneralModel {
    private DatabaseService dbService;
    private Player player;
    private Viewer viewer;
    private Client client;
    private String gameType;

    private final int SPACE = 0;
    private final int PLAYER = 1;
    private final int WALL = 2;
    private final int BOX = 3;
    private final int CHECK = 4;
    private final int COIN = 5;

    private final int LEFT = 37; //arrow left keycode
    private final int RIGHT = 39; //arrow right keycode
    private final int UP = 38; //arrow up keycode
    private final int DOWN = 40; //arrow down keycode
    private final int RESTART = 82; //'r' button keycode
    private final int EXIT = 27; //'esc' button keycode

    private final int LAST_LEVEL = 9;

    private final Music boxInTargetSound;
    private final Music wonSound;
    private final Music moveSnowSound;
    private final Music backgroundSnowMusic;
    private final Music coinSound;
    private final Music defaultMusic;
    private Music currentMusic;

    private boolean isMusicPlayed;
    private boolean isSoundOn = true;

    private String move;
    private int playerPosX;
    private int playerPosY;

    private int[][] map;
    private int[][] map2;
    private Levels levels;

    private int playerCount;
    private int boxesCount;
    private int checksCount;
    private int coinsCount;

    private int totalMoves;
    private int collectedCoins;

    private int[][] checksPos;
    private int[][] coinsPos;
    private boolean isPlayerFirstCompletedGame;
    private boolean isPlayerCompleteGame;

    public Model(Viewer viewer) {
        gameType = "alone";
        this.viewer = viewer;
        dbService = new DatabaseService();
        player = dbService.getPlayerInfo("Stive");

        isMusicPlayed = false;

        wonSound = new Music(new File("music/won.wav"));
        boxInTargetSound = new Music(new File("music/target.wav"));
        moveSnowSound = new Music(new File("music/move_snow.wav"));
        coinSound = new Music(new File("music/coin.wav"));

        backgroundSnowMusic = new Music(new File("music/backgroundSnowMusic.wav"));
        defaultMusic = new Music(new File("music/defaultMusic.wav"));
        backgroundSnowMusic.playLoop();
        backgroundSnowMusic.setVolume(0.75f);
        currentMusic = backgroundSnowMusic;


        playerPosX = -1;
        playerPosY = -1;
        move = "Down";
    }

    public boolean getIsPlayerCompleteGame() {
        return isPlayerCompleteGame;
    }

    public void doAction(int keyMessage) {
        System.out.println("in model do Action");

        if (keyMessage == RESTART && gameType.equals("alone")) {
            restart();
        } else if (keyMessage == EXIT) {
            collectedCoins = 0;
            client.closeClient();
            viewer.showMenu();
        }

        if (map == null) {
            System.out.println("NO MAP FOUND\n\n");
            return;
        }

        if (keyMessage == LEFT) {
            move = "Left";
            moveLeft();

        } else if (keyMessage == RIGHT) {
            move = "Right";
            moveRight();

        } else if (keyMessage == UP) {
            move = "Up";
            moveTop();

        } else if (keyMessage == DOWN) {
            move = "Down";
            moveBot();

        }

        returnCheck();
        viewer.update();// when play alone
        viewer.updateMyCanvas();// when play with enemy

        if (isWon()) {
            System.out.println("YOU WON ");
            doComplitingAction();
            if (gameType.equals("alone")) {
                askSoloPlayerFurtherAction();
            } else if (gameType.equals("battle")) {
                if (isPlayerFirstCompletedGame) {
                    askOnlinePlayerFurtherAction();
                } else {
                    client.sendDataToServer("complete");// if we complete game but not first
                    isPlayerCompleteGame = true;
                    System.out.println("SEND DATA COMPLETE ");
                }

            }
        }

    }

    private void doComplitingAction() {
        moveSnowSound.stop();
        boxInTargetSound.stop();
        wonSound.play();
        int passedLevel = levels.getCurrentLevel();
        dbService.writeCoins(player.getNickname(), passedLevel, collectedCoins);
        viewer.getLevelChooser().updateCoins(passedLevel, collectedCoins);
        collectedCoins = 0;
    }

    public void setIsPlayerFirstCompletedGame(boolean value) {
        isPlayerFirstCompletedGame = value;
    }

    public void setClient(Client client) {
        this.client = client;
        gameType = client.getGameType();
        prepareToNewGame();
    }

    private void prepareToNewGame() {
        isPlayerCompleteGame = false;
        isPlayerFirstCompletedGame = true;
        levels = new Levels(client);
    }

    public Client getClient() {
        return client;
    }

    public String getNickName() {
        return player.getNickname();
    }

    public int[][] getDesktop() {
        return map;
    }

    public Music getCurrentMusic() {
        return currentMusic;
    }

    public void playCurrentMusic() {
        if (currentMusic != null) {
            stopMusic();
            currentMusic.playLoop();
            isMusicPlayed = true;
        }
    }

    public void playDefaultMusic() {
        if (defaultMusic != null) {
            stopMusic();
            defaultMusic.playLoop();
            isMusicPlayed = true;
            currentMusic = defaultMusic;
        }
    }

    public void playJingleBellsMusic() {
        stopMusic();

        if (backgroundSnowMusic != null) {
            backgroundSnowMusic.playLoop();
            isMusicPlayed = true;
            currentMusic = backgroundSnowMusic;
        }
    }

    public void stopMusic() {
        if (defaultMusic != null) {
            defaultMusic.stop();
        }

        if (backgroundSnowMusic != null) {
            backgroundSnowMusic.stop();
        }
        isMusicPlayed = false;
    }

    public void stopAllSounds() {
        defaultMusic.stop();
        backgroundSnowMusic.stop();
        wonSound.stop();
        boxInTargetSound.stop();
        moveSnowSound.stop();
    }

    public boolean isMusicPlayed() {
        return isMusicPlayed;
    }

    public void startNotPassedLevel() {
        HashMap<Integer, Integer> passedLevels = player.getCoinsOnLevels();
        int level = 1;

        for(int i = 1; i < 10; i++){
            if(!passedLevels.containsKey(i)) {
                level = i;
                break;
            }
        }

        changeLevel("Level " + level);
    }

    public void changeLevel(String command) {
        String stringLevelNumber = command.substring(command.length() - 1, command.length());
        int levelNumber = Integer.parseInt(stringLevelNumber);
        levels.setCurrentLevel(levelNumber);
        map = levels.getCurrentMap();
        System.out.println("Creating duplicate");
        map2 = new int[map.length][];
        for (int i = 0; i < map.length; i++) {
            map2[i] = new int[map[i].length];
            for (int j = 0; j < map[i].length; j++) {
                map2[i][j] = map[i][j];
            }
        }
        if (map != null) {
            scanMap();

        }

        viewer.showCanvas(gameType);
        totalMoves = 0;

    }

    public void changeLevel() {
        map = levels.getRandomLevelFromServer();
        String nickNameAndSkin = player.getNickname() + ";" + player.getCurrentSkin().getType() + ";";
        client.sendDataToServer(nickNameAndSkin);

        if (map != null) {
            scanMap();
        }

        viewer.showCanvas(gameType);
        totalMoves = 0;
    }

    public void restart() {
        collectedCoins = 0;

        for (int i = 0; i < map2.length; i++) {
            for (int j = 0; j < map2[i].length; j++) {
                map[i][j] = map2[i][j];
            }
        }
        if (map != null) {
            scanMap();
        }
    }

    public String getMove() {
        return move;
    }

    public int getTotalMoves() {
        return totalMoves;
    }

    public int getCollectedCoins() {
        return collectedCoins;
    }

    public Player setPlayer(String nickname) {
        player = dbService.getPlayerInfo(nickname);
        viewer.updateSettings(player);
        viewer.updateSkin();
        viewer.updateButtonText();
        return player;
    }

    public Player getPlayer() {
        return player;
    }

    public void getNextLevel() {
        // map = levels.getNextMap();
        changeLevel(String.valueOf(levels.getCurrentLevel() + 1));
        if (map != null) {
            scanMap();
        }

        viewer.showCanvas();
    }

    public int getCurrentLevelNumber() {
        return levels.getCurrentLevel();
    }

    public void updateCurrentSkin(String skinType) {
        dbService.updateCurrentSkin(player.getNickname(), skinType);
        PlayerSkin skin = null;

        switch (skinType) {
            case "Default Skin":
                skin = new DefaultSkin();
                break;
            case "Santa Skin":
                skin = new SantaSkin();
                break;
            case "Premium Skin":
                skin = new PremiumSkin();
                break;
        }

        player.setCurrentSkin(skin);
        viewer.updateSkin();
    }

    public void buyPremiumSkin(int premiumSkinCost) {
        String nickname = player.getNickname();
        String skinType = "Premium Skin";

        dbService.updateTotalCoins(nickname, player.getTotalCoins() - premiumSkinCost);
        dbService.addSkin(nickname, skinType);
        updateCurrentSkin("Premium Skin");

        player = dbService.getPlayerInfo(nickname);
        viewer.updateSettings(player);
        viewer.updateButtonText();
    }

    private void askSoloPlayerFurtherAction() {
        String playerChoice = viewer.showSoloEndLevelDialog();

        if (playerChoice.equals("Next level")) {
            // map = levels.getNextMap();
            // changeLevel(level.getCurrentLevel() + 1);
            // scanMap();
            // viewer.update();
            getNextLevel();
        } else if (playerChoice.equals("Back to menu")) {
            map = null;
            client.closeClient();
            viewer.showMenu();
        } else {
            map = null;
        }
    }

    private void askOnlinePlayerFurtherAction() {
        String playerChoice = viewer.showOnlineEndLevelDialog();

        if (playerChoice.equals("Wait results (30 sec)")) {
            System.out.println("Send data to server : You have 30 seconds");
            viewer.disableMyCanvas();
            client.sendDataToServer("You have 30 seconds");
            viewer.getEnemyCanvas().setTimer(client, viewer);
            viewer.updateEnemyCanvas();
        } else if (playerChoice.equals("Give up")) {
            giveUp();
        } else {
            map = null;
        }
    }

    public void giveUp() {
        System.out.println("Send data to server : given up");
        client.sendDataToServer("Given up");
        client.closeClient();
        map = null;
        viewer.showMenu();
    }

    private void scanMap() {
        deleteMapValues();

        if (!allWallSidesValid()) {
            map = null;
            return;
        }

        setMapValues();

        if (!isMapPlayable()) {
            map = null;
            return;
        }

        saveChecksPos();
        saveCoinsPos();
    }

    private void deleteMapValues() {
        playerCount = 0;
        boxesCount = 0;
        checksCount = 0;
        totalMoves = 0;
        coinsCount = 0;
        collectedCoins = 0;
    }

    private void setMapValues() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == PLAYER) {
                    playerPosX = j;
                    playerPosY = i;
                    playerCount++;
                } else if (map[i][j] == BOX) {
                    boxesCount++;
                } else if (map[i][j] == CHECK) {
                    checksCount++;
                } else if (map[i][j] == COIN) {
                    coinsCount++;
                }
            }
        }
    }

    private boolean allWallSidesValid() {
        return isTopWallsCorrect() &&
                isLeftWallsCorrect() &&
                isRightWallsCorrect() &&
                isDownWallsCorrect();
    }

    private boolean isLeftWallsCorrect() {
        int wallX = -1;
        int wallY = -1;
        int prevWallX = -1;
        int prevWallY = -1;
        for (int i = 0; i < map.length; i++) {
            boolean wallFound = false;
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 2) {
                    wallFound = true;
                    wallY = i;
                    wallX = j;
                    break;
                }
            }

            if (i != 0) {
                int differenceBetweenWalls = Math.abs(prevWallX - wallX);
                if (prevWallX >= wallX) {
                    for (int k = prevWallX - 1; k >= prevWallX - differenceBetweenWalls; k--) {
                        if (map[wallY][k] != 2) {
                            System.out.println("isLeftWallsCorrect(): problem in a mapline" + i + ", in a row " + k + "\n(prevWallX>wallX)");
                            return false;
                        }
                    }
                }

                if (wallX > prevWallX) {
                    for (int k = prevWallX; k < wallX; k++) {
                        if (map[prevWallY][k] != 2) {
                            System.out.println("isLeftWallsCorrect(): problem in a mapline" + i + ", in a row " + k + "\n(wallX>prevWallX)");
                            return false;
                        }
                    }
                }
            }
            prevWallY = wallY;
            prevWallX = wallX;
            wallFound = false;
        }

        return true;
    }

    private boolean isRightWallsCorrect() {
        for (int i = 0; i < map.length - 1; i++) {
            int currentMapLineLength = map[i].length;
            int nextMapLineLength = map[i + 1].length;

            if (nextMapLineLength <= currentMapLineLength) {
                continue;
            }

            int nextMapLineLastElementOfCurrentLine = map[i + 1][map[i].length];
            int nextMapLineLastElement = map[i + 1][map[i + 1].length - 1];

            if ((nextMapLineLastElementOfCurrentLine == 0 || nextMapLineLastElement != 2)) {
                System.out.println("isRightWallsCorrect(): problems with element in mapline " + (i + 1));
                return false;
            }
        }
        return true;
    }

    private boolean isTopWallsCorrect() {
        boolean wallFound = false;

        for (int i = 0; i < map[0].length; i++) {
            if ((map[0][i] == 2) && !wallFound) {
                wallFound = true;
            } else if ((map[0][i] != 2) && wallFound) {
                System.out.println("isTopWallsCorrect(): top wall was not found in first mapline on column " + i);
                return false;
            }
        }

        return true;
    }

    private boolean isDownWallsCorrect() {
        boolean wallFound = false;
        int lastArrayIndex = map.length - 1;
        for (int i = 0; i < map[lastArrayIndex].length; i++) {
            if ((map[lastArrayIndex][i] == 2) && !wallFound) {
                wallFound = true;
            } else if ((map[lastArrayIndex][i] != 2) && wallFound) {
                System.out.println("isDownWallsCorrect(): top wall was not found in last mapline on column " + i);
                return false;
            }
        }
        return true;
    }

    private boolean isMapPlayable() {
        if (playerCount != 1 || boxesCount != checksCount || boxesCount == 0 && checksCount == 0) {
            System.out.println("isMapPlayable(): map have invalid game parameters");
            System.out.println("players: " + playerCount + ("(should be equal to 1)"));
            System.out.println("boxes: " + boxesCount);
            System.out.println("checks: " + checksCount);
            return false;
        }
        return true;
    }

    private void saveChecksPos() {
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

    private void saveCoinsPos() {
        coinsPos = new int[coinsCount][2];
        int coinsQueue = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == COIN) {
                    coinsPos[coinsQueue][0] = i;
                    coinsPos[coinsQueue][1] = j;
                    coinsQueue++;
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

        for (int i = 0; i < coinsPos.length; i++) {
            int coinsPosY = coinsPos[i][0];
            int coinsPosX = coinsPos[i][1];
            boolean coinsValid = coinsPosY != -1 && coinsPosX != -1;

            if (coinsValid && map[coinsPosY][coinsPosX] == SPACE) {
                map[coinsPosY][coinsPosX] = COIN;
                break;
            } else if (coinsValid && map[coinsPosY][coinsPosX] == BOX) {
                coinsPos[i][0] = -1;
                coinsPos[i][1] = -1;
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
            if (map[playerPosY][playerPosX - 2] == COIN) {
                coinSound.play();
                collectedCoins++;
            }
            map[playerPosY][playerPosX - 1] = SPACE;

            if (map[playerPosY][playerPosX - 2] == CHECK) {
                boxInTargetSound.play();
            }
            map[playerPosY][playerPosX - 2] = BOX;
        }

        if (gameType.equals("battle")) {
            System.out.println("Left");
            client.sendDataToServer("Left");
        }
        moveSnowSound.play();
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

        if (map[playerPosY][playerPosX + 1] == BOX && !canMoveBoxToRight()) {
            return;
        }

        if (map[playerPosY][playerPosX + 1] == BOX) {
            if (map[playerPosY][playerPosX + 2] == COIN) {
                coinSound.play();
                collectedCoins++;
            }
            map[playerPosY][playerPosX + 1] = SPACE;

            if (map[playerPosY][playerPosX + 2] == CHECK) {
                boxInTargetSound.play();
            }
            map[playerPosY][playerPosX + 2] = BOX;
        }

        if (gameType.equals("battle")) {
            System.out.println("Right");
            client.sendDataToServer("Right");
        }

        moveSnowSound.play();
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

        if (map[playerPosY - 1][playerPosX] == BOX && !canMoveBoxToTop()) {
            return;
        }

        if (map[playerPosY - 1][playerPosX] == BOX) {
            if (map[playerPosY - 2][playerPosX] == COIN) {
                coinSound.play();
                collectedCoins++;
            }
            map[playerPosY - 1][playerPosX] = SPACE;

            if (map[playerPosY - 2][playerPosX] == CHECK) {
                boxInTargetSound.play();
            }
            map[playerPosY - 2][playerPosX] = BOX;
        }

        if (gameType.equals("battle")) {
            System.out.println("Up");
            client.sendDataToServer("Up");
        }

        moveSnowSound.play();
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

        if (map[playerPosY + 1][playerPosX] == BOX && !canMoveBoxToBot()) {
            return;
        }

        if (map[playerPosY + 1][playerPosX] == BOX) {
            if (map[playerPosY + 2][playerPosX] == COIN) {
                coinSound.play();
                collectedCoins++;
            }
            map[playerPosY + 1][playerPosX] = SPACE;

            if (map[playerPosY + 2][playerPosX] == CHECK) {
                boxInTargetSound.play();
            }
            map[playerPosY + 2][playerPosX] = BOX;
        }

        if (gameType.equals("battle")) {
            System.out.println("Down");
            client.sendDataToServer("Down");
        }

        moveSnowSound.play();
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
        if (((map[playerPosY][playerPosX + 2] == WALL) || (map[playerPosY][playerPosX + 2] == BOX)) && (playerPosX + 2 < map[playerPosY].length)) {
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
        if (((map[playerPosY + 2][playerPosX] == WALL) || (map[playerPosY + 2][playerPosX] == BOX)) && (playerPosY + 2 < map[playerPosX].length)) {
            System.out.println("Impossible move box to the bottom"); //debug
            return false;
        }

        return true;
    }
}
