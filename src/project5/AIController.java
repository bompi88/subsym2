package project5;

import algorithms.qlearning.QLearning;
import gameworlds.flatland.FlatlandQ;
import gameworlds.flatland.MovementQ;

import java.util.*;

public class AIController {

    private QLearning qLearn;
    private FlatlandQ currentScenario;

    private int startPosX = 0;
    private int startPosY = 0;
    private int numberOfFood = 0;

    public static int globalScenariosToRun = 1;

    public AIController() {
        qLearn = new QLearning(currentScenario);
    }


    public void run(int k){
        qLearn.run(k);
    }

    public void run(){
        qLearn.run(500);
    }

    public MovementQ helperIndividualFindMove(FlatlandQ flatland){
       return qLearn.chooseAction();
    }

    static Comparator<Map.Entry<Integer, Double>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(
            entry2.getValue());

    public FlatlandQ getCurrentScenario() {
        return currentScenario;
    }

    public int getNumberOfFood() {
        return numberOfFood;
    }

    public void setNumberOfFood(int numberOfFood) {
        this.numberOfFood = numberOfFood;
    }

    public int getStartPosY() {
        return startPosY;
    }

    public void setStartPosY(int startPosY) {
        this.startPosY = startPosY;
    }

    public int getStartPosX() {
        return startPosX;
    }

    public void setStartPosX(int startPosX) {
        this.startPosX = startPosX;
    }

    public void setScenario(FlatlandQ scenario) {
        currentScenario = scenario;
        qLearn.setScenario(scenario);
    }

    public QLearning getLearner() {
        return qLearn;
    }
}
