package project5;

import gameworlds.flatland.FlatlandQ;
import gameworlds.flatland.MovementQ;
import gameworlds.flatland.sensor.Items;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import math.linnalg.Vector2;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;

public class FXMLTableViewController {

    @FXML
    Canvas simulation;

    @FXML
    Text txtIsRunning;

    @FXML
    Text txtStepsTaken;

    @FXML
    Text txtPoisonEaten;

    @FXML
    TextField txtfSimulationInterval;

    @FXML
    TextField txtScenarioRunTimes;

    private GraphicsContext gc;

    private AIController aiController;

    private SimpleDoubleProperty stepsTaken;

    private SimpleDoubleProperty poisonEaten;

    private AnimationTimer aiRunner;

    private AnimationTimer simulationRunner;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private int numberOfFood = 0;
    private int startPosX = 0;
    private int startPosY = 0;

    Image arrowDown;
    Image arrowUp;
    Image arrowRight;
    Image arrowLeft;

    private FileChooser fileChooser;

    // nanoseconds.
    private LongProperty minSimulationUpdateInterval = new SimpleLongProperty(1000000000);

    /**
     * Run by JavaFX
     */
    public void initialize(){
        // ================================================
        // Setup variables
        // ================================================
        gc = simulation.getGraphicsContext2D();
        aiController = new AIController();

        stepsTaken = new SimpleDoubleProperty(0);

        poisonEaten = new SimpleDoubleProperty(0);

        arrowDown = new Image("file:arrow_d.png");
        arrowUp = new Image("file:arrow_u.png");
        arrowRight = new Image("file:arrow_r.png");
        arrowLeft = new Image("file:arrow_l.png");

        // ================================================
        // MVC Setup
        // ================================================

        stepsTaken.addListener((observable, oldValue, newValue) -> {
            txtStepsTaken.setText(decimalFormat.format(newValue));
        });

        poisonEaten.addListener((observable, oldValue, newValue) -> {
            txtPoisonEaten.setText(decimalFormat.format(newValue));
        });

        txtScenarioRunTimes.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0) {
                Integer value = new Integer(newValue);
                if (value > 0)
                    AIController.globalScenariosToRun = value;
            }

        });

        txtfSimulationInterval.setText(minSimulationUpdateInterval.getValue().toString());

        txtfSimulationInterval.textProperty().addListener(((observable, oldValue, newValue) -> {
            minSimulationUpdateInterval.set(Long.valueOf(newValue));
        }));

        // ================================================
        // AI runner
        // ================================================
//        aiRunner = new AnimationTimer() {
//            @Override
//            public void handle(long now) {
//
//                aiController.run();
//                System.out.println(Arrays.deepToString(aiController.getCurrentScenario().getWorld()));
////                stepsTaken.set(aiController.getSteps());
////                poisonEaten.set(aiController.getPoisonEaten());
//            }
//        };

        // ================================================
        // Run simulation for one individual
        // ================================================
//        tableView.setRowFactory(tv -> {
//                    TableRow<IndividualBrain> row = new TableRow<>();
//                    row.setOnMouseClicked(event -> {
//                        if (event.getClickCount() == 2 && (!row.isEmpty())) {
//                            IndividualBrain rowData = row.getItem();
//
//                            if (simulationRunner != null)
//                                simulationRunner.stop();
//
//                            System.out.println("Starting last flatland for: " + rowData.getId());
//
//                            simulationRunner = makeSimulationRunner(rowData, aiController.getCurrentScenario());
//
//                            simulationRunner.start();
//                        } else if(event.getButton() == MouseButton.SECONDARY) {
//                            IndividualBrain rowData = row.getItem();
//
//                            if (simulationRunner != null)
//                                simulationRunner.stop();
//
//                            Flatland randomFlatland = new Flatland(10, 1/3.0, 1/3.0);
//                            System.out.println("Starting random flatland for: " + rowData.getId());
//                            simulationRunner = makeSimulationRunner(rowData, randomFlatland);
//                            simulationRunner.start();
//                        }
//                    });
//
//                    return row;
//                }
//        );
    }

    @FXML
    protected void startAi(ActionEvent event) {
        if(simulationRunner != null)
            simulationRunner.stop();

        aiController.run(Integer.valueOf(txtScenarioRunTimes.getText()));
        System.out.println(aiController.getLearner().getQ());
    }

    @FXML
    protected void show(ActionEvent event) {
        if(simulationRunner != null)
            simulationRunner.stop();

        simulationRunner = makeSimulationRunner( aiController.getCurrentScenario());

        simulationRunner.start();
    }

    @FXML
    protected void openScenario(ActionEvent event) {

        if(simulationRunner != null)
            simulationRunner.stop();

        fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(txtIsRunning.getScene().getWindow());
        fileChooser.setTitle("Open Scenario file");

        if (file != null) {
            Items[][] scenario = readScenarioFromFile(file.toPath().toString());
            aiController.setScenario(new FlatlandQ(scenario, numberOfFood, startPosX, startPosY));

            redraw(aiController.getCurrentScenario().getWorld(), aiController.getCurrentScenario().getAgentPosition());
        }
    }

    private AnimationTimer makeSimulationRunner(final FlatlandQ flatland){

        final LongProperty lastUpdate = new SimpleLongProperty();
        final long minUpdateInterval = minSimulationUpdateInterval.get(); // nanoseconds.
        return new AnimationTimer() {
            FlatlandQ scenario = new FlatlandQ(flatland);

            @Override
            public void handle(long now) {
                int cStep = scenario.getCurrentTotalSteps();

                if (now - lastUpdate.get() > minUpdateInterval) {
                    if (!scenario.isSolved()) {
                        MovementQ move = aiController.helperIndividualFindMove(scenario);
                        scenario.move(move);
                        redraw(scenario.getWorld(), scenario.getAgentPosition());

                    } else {
                        this.stop();
                    }

                    lastUpdate.set(now);
                }
            }
        };
}

    private void redraw(Items[][] world, Vector2 agentPosition) {

        MovementQ[][] directions = aiController.getLearner().getDirectionMap();

        gc.clearRect(0, 0, simulation.getWidth(), simulation.getHeight());

        int xSize = (int)(simulation.getWidth() / world[0].length);
        int ySize = (int)(simulation.getHeight() / world.length);

        // Draw poison, food and agent
        for (int y = 0; y < world.length; y++) {
            for (int x = 0; x < world[0].length; x++) {
                if(agentPosition.x == x && agentPosition.y == y) {
                    world[y][x] = Items.NOTHING;
                    gc.setFill(Color.YELLOW);

                } else {
                    switch (world[y][x]) {
                        case POISON:
                            gc.setFill(Color.RED);
                            break;
                        case FOOD:
                            gc.setFill(Color.GREEN);
                            break;
                        case NOTHING:
                            gc.setFill(Color.WHITE);
                            break;
                    }
                }

                gc.fillRect(x * xSize, y * ySize, xSize, ySize);
            }
        }

        if(directions != null) {
            // Draw arrows
            gc.setGlobalAlpha(0.4);

            for (int y = 0; y < directions[0].length; y++) {
                for (int x = 0; x < directions.length; x++) {

                    if(directions[x][y] == null)
                        continue;

                    switch (directions[x][y]) {
                        case LEFT:
                            gc.drawImage(arrowLeft, x * xSize, y * ySize, xSize, ySize);
                            break;
                        case RIGHT:
                            gc.drawImage(arrowRight, x * xSize, y * ySize, xSize, ySize);
                            break;
                        case FORWARD:
                            gc.drawImage(arrowUp, x * xSize, y * ySize, xSize, ySize);
                            break;
                        case BACKWARD:
                            gc.drawImage(arrowDown, x * xSize, y * ySize, xSize, ySize);
                            break;
                    }
                }
            }
            gc.setGlobalAlpha(1);
        }
    }

    public Items[][] readScenarioFromFile(String fileName){
        Scanner scanner = null;
        Scanner scenarioFile = null;
        Items[][] scenario = null;

        try {

            scenarioFile = new Scanner(new File(fileName));

            String firstLine = scenarioFile.nextLine();
            scanner = new Scanner(firstLine);
            scanner.useDelimiter(" ");

            int w = scanner.nextInt();
            int h = scanner.nextInt();
            startPosX = scanner.nextInt();
            startPosY = scanner.nextInt();
            numberOfFood = scanner.nextInt();

            scanner.close();

            scenario = new Items[w][h];

            for(int i = 0; i < h; i++) {
                String line = scenarioFile.nextLine();
                scanner = new Scanner(line);
                scanner.useDelimiter(" ");

                for(int j = 0; j < w; j++) {
                    int cell = scanner.nextInt();

                    switch (cell) {
                        case -2: scenario[j][i] = Items.START; break;
                        case -1: scenario[j][i] = Items.POISON; break;
                        case 0: scenario[j][i] = Items.NOTHING; break;
                        default: scenario[j][i] = Items.FOOD; break;
                    }
                }
                scanner.close();
            }

            scenarioFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null)scanner.close();
            if (scenarioFile != null) scenarioFile.close();
        }
        return scenario;
    }
}
