package gameworlds.flatland;

import gameworlds.flatland.sensor.Items;
import gameworlds.flatland.sensor.Sensed;
import math.linnalg.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class FlatlandQ {

    private int foodEaten = 0;
    private int poisonEaten = 0;
    public int foodCreated = 0;

    private int currentTotalSteps = 0;

    private Vector2 agentPosition;
    private int agentDirection;

    private static Vector2[] directions = {
            new Vector2(-1, 0), // Left
            new Vector2(0, 1), // Up
            new Vector2(1, 0), // Right
            new Vector2(0, -1) // Down
    };


    private Items[][] world;


    private ArrayList<Vector2> agentPositionHistory;

    FlatlandQ(){
        agentPositionHistory = new ArrayList<>();
    }
    /**
     * Contstructs the Flatland world in a NxN grid with Food-poison distribution of
     * f & p.
     * @param n
     * @param f
     * @param p
     */
//    public FlatlandQ(int n, double f, double p){
//        this();
//        Random random = new Random();
//
//        // Upwards
//        agentDirection = 1;
//
//        // Create world todo backup of world?
//        world = createRandomWorld(random, n, f, p);
//        for (int i = 0; i < world.length; i++) {
//            for (int j = 0; j < world[i].length; j++) {
//                if(world[i][j].equals(Items.FOOD))
//                    foodCreated++;
//            }
//        }
//
//        // Robot placement
//        agentPosition = new Vector2();
//
//        do {
//            agentPosition.x = (int) (random.nextDouble() * n);
//            agentPosition.y = (int) (random.nextDouble() * n);
//        } while(world[(int)agentPosition.y][(int)agentPosition.x] != Items.NOTHING);
//
//        agentPositionHistory.add(new Vector2(agentPosition));
//    }

    public FlatlandQ(Items[][] scenario, int foodCreated, int posX, int posY){
        this();

        // Upwards
        agentDirection = 1;

        world = scenario;
        this.foodCreated = foodCreated;

        agentPosition = new Vector2();

        agentPosition.x = posX;
        agentPosition.y = posY;

        agentPositionHistory.add(new Vector2(agentPosition));
    }

    /**
     * Creates a new Flatland with an old board.
     * @param flatland
     */
    public FlatlandQ(FlatlandQ flatland){
        this();
        this.agentDirection = flatland.agentDirection;
        this.agentPosition = new Vector2(flatland.agentPosition);
        this.foodCreated = flatland.foodCreated;
        agentPositionHistory.add(new Vector2(agentPosition));

        this.world = new Items[flatland.world.length][flatland.world[0].length];

        for (int y = 0; y < flatland.world.length; y++) {
            for (int x = 0; x < flatland.world[0].length; x++) {
                this.world[y][x] = flatland.world[y][x];
            }
        }
    }

//    public List<> getSensory(){
//        // return sensory output for current position
//        Vector2 leftVec = new Vector2(agentPosition);
//        leftVec.add(directions[]);
//        fixVectorPosition(leftVec, world);
//
//        Vector2 frontVec = new Vector2(agentPosition);
//        frontVec.add(directions[agentDirection]);
//        fixVectorPosition(frontVec, world);
//
//        Vector2 rightVec = new Vector2(agentPosition);
//        rightVec.add(directions[goRight()]);
//        fixVectorPosition(rightVec, world);
//
//        return new Sensed(
//                world[(int)leftVec.y][(int)leftVec.x],
//                world[(int)frontVec.y][(int)frontVec.x],
//                world[(int)rightVec.y][(int)rightVec.x]);
//    }

    /**
     *
     * Fixes the vector in-memory.
     *
     * @param vector
     * @param worldBounds
     * @return
     */
    private Vector2 fixVectorPosition(Vector2 vector, Items[][] worldBounds){
        if(vector.x >= worldBounds[0].length)
            vector.x = 0;
        else if (vector.x < 0)
            vector.x = worldBounds[0].length - 1;

        if(vector.y >= worldBounds.length)
            vector.y = 0;
        else if (vector.y < 0)
            vector.y = worldBounds.length - 1;

        return vector;
    }


    /**
     * Performs move action and updates internal eat stats.
     *
     * @param movement
     * @return
     */
    public void move(MovementQ movement) {
        // Update movements done
        currentTotalSteps++;

        switch (movement) {
            // Move in the same direction
            case FORWARD:
                agentDirection = 1;
                break;
            case BACKWARD:
                agentDirection = 3;
                break;
            // Rotate left, then forward
            case LEFT:
                agentDirection = 0;
                break;
            // rotate right, then forward
            case RIGHT:
                agentDirection = 2;
                break;
        }


        // add the movement
        agentPosition.add(directions[agentDirection]);

        // Fix the map position
        fixVectorPosition(agentPosition, world);

        // Save the move for the history class
        agentPositionHistory.add(new Vector2(agentPosition));

        // Find sensed data
        Items found = world[(int)agentPosition.y][(int)agentPosition.x];

        switch (found) {
            case FOOD:
                foodEaten++;
                break;
            case POISON:
                poisonEaten++;
                break;
        }

        // Clear the world position
        world[(int)agentPosition.y][(int)agentPosition.x] = Items.NOTHING;
    }


    /**
     * Creates a new random world.
     *
     * @param n
     * @param f
     * @param p
     * @return
     */
    private Items[][] createRandomWorld(Random random, int n, double f, double p){
        Items[][] randomWorld = new Items[n][n];

        for (int i = 0; i < randomWorld.length; i++) {
            for (int j = 0; j < randomWorld[i].length; j++) {

                double chance = random.nextDouble();

                if (chance < f) {
                    randomWorld[i][j] = Items.FOOD;
                } else if (chance < f + p) {
                    randomWorld[i][j] = Items.POISON;
                } else {
                    randomWorld[i][j] = Items.NOTHING;
                }
            }
        }

        return randomWorld;
    }


    public double getStats(){
        double score = 0;
        score += world[(int)agentPosition.y][(int)agentPosition.x] == Items.POISON ? -100 : 0;
        score += world[(int)agentPosition.y][(int)agentPosition.x] == Items.FOOD ? 100 : 0;
        return score;
        //return Math.max(Math.pow(foodEaten, 2) - Math.pow(poisonEaten, 2), 0) / (double) foodCreated;
    }


    public Items[][] getWorld() {
        return world;
    }

    public Items[][] getCopyOfWorld() {

        Items[][] clone = new Items[world.length][world[0].length];

        for (int x = 0; x < world.length; x++)
        {
            for (int y = 0; y < world[0].length; y++)
            {
                clone[x][y] = world[x][y];
            }
        }

        return clone;
    }

    public Vector2 getAgentPosition() {
        return agentPosition;
    }

    public int getCurrentTotalSteps() {
        return currentTotalSteps;
    }

    public boolean isSolved() { return foodCreated == foodEaten; }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(world);
    }

    //Compare only account numbers
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        FlatlandQ other = (FlatlandQ) obj;
        if (hashCode() != other.hashCode())
            return false;
        return true;
    }
}
