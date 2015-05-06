package gameworlds.flatland.sensor;

import java.util.ArrayList;
import java.util.List;

public class Sensed {
    public Items left;
    public Items front;
    public Items right;
    public Sensed(Items left, Items front, Items right){
        this.left = left;
        this.front = front;
        this.right = right;
    }


    /**
     * Gets a sensory list of left, top and right input values
     * left frist, then front, then right.
     * @return
     */
    public List<Double> getSensoryList(){
        ArrayList<Double> sensory = new ArrayList<>();
        sensory.add((double)left.value);
        sensory.add((double)front.value);
        sensory.add((double)right.value);
        return sensory;
    }
}
