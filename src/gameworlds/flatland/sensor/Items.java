package gameworlds.flatland.sensor;

public enum Items {

    FOOD(1), POISON(-1), NOTHING(0);

    public int value;

    Items(int value){
        this.value = value;
    }
}
