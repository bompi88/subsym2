package gameworlds.tracker;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Tracker {

    int currentTimestep = 0;
    int createTiles = 0;
    int createdPositiveTiles = 0;


    Random random;

    int height = 15;
    int width = 30;

    int tileLeftPos;
    int tileLength;
    int tileHeightPos;

    int positive = 0;
    int negative = 0;

    int platformLeftPos;
    int platformLength = 5;

    GameType gameType = GameType.NORMAL;

    int platformState;

    public Tracker(GameType gameType){
        this.gameType = gameType;
        random = new Random();
        this.platformState = 0;

        platformLeftPos = random.nextInt(width - platformLength);

    }

    public List<Double> getSensory(){
        List<Double> output = new LinkedList<>();

        if(gameType.equals(GameType.NOWRAP)) {
            if(platformLeftPos == 0)
                output.add(1.0);
            else
                output.add(0.0);
        }

        for (int i = platformLeftPos; i < platformLeftPos + platformLength; i++) {

            int tileUpperLimit = tileLeftPos + tileLength;

            if(i >= tileLeftPos && i <= tileUpperLimit)
                output.add(1.0);
            else
                output.add(0.0);
        }

        if(gameType.equals(GameType.NOWRAP)) {
            if(platformLeftPos + platformLength == width)
                output.add(0.0);
            else
                output.add(1.0);
        }

        return output;
    }

    public void newStep(Movement movement){
        currentTimestep++;

        platformLeftPos = (((platformLeftPos % width) + width) % width);
        
        // Move platform
        switch (movement) {
            case LEFT:
                if((platformLeftPos > 0) || (!gameType.equals(GameType.NOWRAP)))
                    platformLeftPos--;
                break;
            case RIGHT:
                if((platformLeftPos + platformLength < width) || (!gameType.equals(GameType.NOWRAP)))
                    platformLeftPos++;
                break;
            case PULLDOWN:
                tileHeightPos = 1;
                //positive++;
                break;
        }

        // Move falling tile. If crash then register result and start again.
        tileHeightPos--;

        if (tileHeightPos < 1) {

            int tileRightPos = tileLeftPos + tileLength - 1;
            int platformRightPos = platformLeftPos + platformLength - 1;

            boolean isTileLeftPosInside = tileLeftPos >= platformLeftPos && tileLeftPos <= platformRightPos;
            boolean isTileRightPosInside = tileRightPos >= platformLeftPos && tileRightPos <= platformRightPos;

            // platformState: 0 - neutral, 1 - positive, 2 - negative

            // Check if tile fully contained by platform
            if(isTileLeftPosInside && isTileRightPosInside){
                if(isSmallTile()) {
                    if (tileLeftPos >= platformLeftPos && tileRightPos <= platformLeftPos + platformLength) {
                        positive++;
                        platformState = 1;
                    }
                } else {
                    // If touching: Always give a penalty on large tiles
                    negative++;
                    platformState = 2;
                }

            } else if(isTileLeftPosInside || isTileRightPosInside) {
                // Else if part of tile is inside platform

                platformState = 0;
            } else {
                // Else: if not inside at all
                if(isSmallTile()){
                    negative++;
                    platformState = 2;
                } else {
                    positive++;
                    platformState = 1;
                }
            }

            createNewTile();
        }
    }

    private void createNewTile() {
        createTiles++;
        tileLength = random.nextInt(6) + 1;
        if(tileLength < 5)
            createdPositiveTiles++;

        if (!gameType.equals(GameType.NOWRAP)) {
            tileLeftPos = random.nextInt(width);
            tileLeftPos = (((tileLeftPos % width) + width) % width);
        } else {
            tileLeftPos = random.nextInt(width - tileLength);
        }

        tileHeightPos = height;
    }

    public double getStats(){
        if(gameType.equals(GameType.NOWRAP)){
            return (positive - 2 * negative) / (double)createdPositiveTiles;
        } else if (gameType.equals(GameType.NORMAL)){
            return Math.max(Math.pow(positive, 2) - Math.pow(negative, 2), 0) / (double)createdPositiveTiles;
        } else {
            //System.out.println("Stats: " + Math.max(positive - Math.pow(negative, 2), 0) / (double)createdPositiveTiles);
            //System.out.println("lol: " + positive + "  " + negative + " " + createdPositiveTiles);
            return Math.max(Math.pow(positive, 2) - Math.pow(negative, 2), 0);// / (double)createdPositiveTiles; // - Math.pow(negative, 2)
        }
    }

    public boolean isSmallTile() {
        return tileLength < 5;
    }

    public enum Movement {
        LEFT, RIGHT, PULLDOWN
    }

    public int getTileLeftPos() {
        return tileLeftPos;
    }

    public int getTileLength() {
        return tileLength;
    }

    public int getTileHeightPos() {
        return tileHeightPos;
    }

    public int getPlatformLeftPos() {
        return platformLeftPos;
    }

    public int getPlatformLength() {
        return platformLength;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCurrentTimestep() {
        return currentTimestep;
    }

    public enum GameType {
        NORMAL, NOWRAP, PULLDOWN
    }

    public boolean isAtBottom() { return getTileHeightPos() < 1; }

    public int getPlatformState() { return platformState;}

}
