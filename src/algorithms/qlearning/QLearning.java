package algorithms.qlearning;

import gameworlds.flatland.FlatlandQ;
import gameworlds.flatland.MovementQ;
import gameworlds.flatland.sensor.Items;
import math.linnalg.Vector2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QLearning {

    private FlatlandQ flatlandInitial;

    private double learningRate = 1.5;
    private double discountRate = 0.6;
    private double selectBestMoveProbability = 0.6;

    private Map<ArrayHolder, Map> Q;

    private MovementQ[][] directionMap;

    public QLearning(FlatlandQ flatland) {
        // create a copy
        this.flatlandInitial = flatland;
        Q = new HashMap<>();

        if(flatland != null) {
            Items[][] items = flatland.getWorld();
            directionMap = new MovementQ[items[0].length][items.length];
        }

    }

    public void run(int k) {
        //    for 1 to K do
        //      Game ← Restart scenario
        //        while Game is not finished do
        //          a ← Select action to do
        //          UpdateGame(Game, a)
        //          state ← State after doing a in the game
        //          r ← Reward after doing a in the game
        //          UpdateQArray(Q, state, a, r)
        //    end while end for

        for(int i = 0; i < k; i++) {
            // Create a new board from the initial flatland state
            FlatlandQ flatland = new FlatlandQ(flatlandInitial);

            while (!flatland.isSolved()) {
                // Get state after performed move
                Items[][] oldState = flatland.getCopyOfWorld();

                // Get the already registered state value if any
                Map actionRewards = Q.get(new ArrayHolder(oldState));

                if(actionRewards == null) {
                    actionRewards = new HashMap<MovementQ, Double>();
                    System.out.println(new ArrayHolder(oldState).hashCode());
                    Q.put(new ArrayHolder(oldState), actionRewards);
                }

                MovementQ action = selectAction(actionRewards);

                Vector2 agentPos = flatland.getAgentPosition();
                directionMap[(int)agentPos.x][(int)agentPos.y] = getBestAction();

                // Move the agent
                flatland.move(action);

                // Get state after performed move
                Items[][] newState = flatland.getCopyOfWorld();

                // Get the reward given the move
                double reward = flatland.getStats();

                // Get the the next already registered state value if any
                Map nextActionRewards = Q.get(new ArrayHolder(newState));

                // If not previously defined, add a new hash table and action-reward map those values
                if(nextActionRewards == null) {

                    nextActionRewards = new HashMap<MovementQ, Double>();

                    // put the action hash table into Action State hash table,
                    // so we later can look up actions based on the world state
                    Q.put(new ArrayHolder(newState), nextActionRewards);
                }

                double bestQ = 0;

                // Get the best Q-value in the next chosen state
                for (Object value : nextActionRewards.values()) {
                    if((double)value > bestQ)
                        bestQ = (double) value;
                }

                double oldReward = (double)actionRewards.getOrDefault(action, 0.0);

                double updatedReward = oldReward + (learningRate * (reward + (discountRate * bestQ) - oldReward));

                // add the action to the hashtable
                actionRewards.put(action, updatedReward);

            }
        }
    }

    public MovementQ getBestAction() {
        Map<MovementQ, Double> possibleActions = Q.get(new ArrayHolder(flatlandInitial.getWorld()));

        double bestActionValue = 0;
        MovementQ bestAction = null;

        if(possibleActions == null)
            return null;

        // Get the best Q-value in the next chosen state
        for (Map.Entry<MovementQ, Double> entry : possibleActions.entrySet()) {
            MovementQ key = entry.getKey();
            double value = entry.getValue();

            if (value > bestActionValue) {
                bestAction = key;
                bestActionValue = value;
            }
        }

        return bestAction;
    }

    public MovementQ chooseAction() {
        Map<MovementQ, Double> possibleActions = Q.get(new ArrayHolder(flatlandInitial.getWorld()));

        double bestActionValue = 0;
        MovementQ bestAction = null;

        if(possibleActions == null)
            return selectRandom();

        // Get the best Q-value in the next chosen state
        for (Map.Entry<MovementQ, Double> entry : possibleActions.entrySet()) {
            MovementQ key = entry.getKey();
            double value = entry.getValue();

            if (value > bestActionValue) {
                bestAction = key;
                bestActionValue = value;
            }
        }

        if(bestAction == null)
            bestAction = selectRandom();

        return bestAction;
    }

    public MovementQ selectAction(Map<MovementQ, Double> actionRewards) {

        if(Math.random() <= selectBestMoveProbability) {
            // Select best move
            double bestActionValue = 0;
            MovementQ bestAction = null;

            if(actionRewards == null)
                return selectRandom();

            // Get the best Q-value in the next chosen state
            for (Map.Entry<MovementQ, Double> entry : actionRewards.entrySet()) {
                MovementQ key = entry.getKey();
                double value = entry.getValue();

                if (value > bestActionValue) {
                    bestAction = key;
                    bestActionValue = value;
                }
            }

            if(bestAction == null)
                bestAction = selectRandom();

            return bestAction;
        } else {
            return selectRandom();
        }
    }

    private MovementQ selectRandom() {
        Random r = new Random();

        int move = r.nextInt(4);

        switch (move) {
            case 0: return MovementQ.FORWARD;
            case 1: return MovementQ.BACKWARD;
            case 2: return MovementQ.LEFT;
            case 3: return MovementQ.RIGHT;
        }

        return null;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public double getSelectBestMoveProbability() {
        return selectBestMoveProbability;
    }

    public void setSelectBestMoveProbability(double selectBestMoveProbability) {
        this.selectBestMoveProbability = selectBestMoveProbability;
    }

    public void setScenario(FlatlandQ scenario) {
        this.flatlandInitial = scenario;
        Items[][] items = scenario.getWorld();
        directionMap = new MovementQ[items[0].length][items.length];
    }

    public Map<ArrayHolder, Map> getQ() {
        return Q;
    }

    public MovementQ[][] getDirectionMap() {
        return directionMap;
    }
}

class ArrayHolder {
    Items[][] a;

    public ArrayHolder(Items[][] a) {
        this.a = a;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayHolder)) {
            return false;
        }
        return Arrays.deepEquals(a, ((ArrayHolder) obj).a);
    }

    public int hashCode() {
        return Arrays.deepHashCode(a);
    }
}
