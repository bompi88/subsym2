package project5;

import algorithms.ea.statistics.GenerationStatistics;
import algorithms.eann.IndividualBrain;
import gameworlds.flatland.Flatland;
import gameworlds.flatland.Movement;
import gameworlds.flatland.sensor.Items;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import math.linnalg.Vector2;

import java.text.DecimalFormat;

public class FXMLTableViewController {
    @FXML
    Canvas simulation;

    @FXML
    TableView<IndividualBrain> tableView;

    @FXML
    Text txtIsRunning;

    @FXML
    Text txtCurrentEpoch;

    @FXML
    Text txtCurrentTotalFitness;

    @FXML
    Text txtCurrentBestFitness;

    @FXML
    LineChart<Number, Number> lcAiStatistics;

    @FXML
    TextField txtScenarioRunTimes;

    @FXML
    CheckBox cbStaticWorlds;

    private GraphicsContext gc;

    private ObservableList<IndividualBrain> data;

    private AIController aiController;

    private SimpleIntegerProperty currentEpoch;

    private SimpleBooleanProperty isRunning;

    private SimpleDoubleProperty cTotalFitness;

    private SimpleDoubleProperty cBestFitness;

    private AnimationTimer aiRunner;

    private AnimationTimer simulationRunner;

    DecimalFormat decimalFormat = new DecimalFormat("#.##");


    /**
     * Run by JavaFX
     */
    public void initialize(){
        // ================================================
        // Setup variables
        // ================================================
        gc = simulation.getGraphicsContext2D();
        aiController = new AIController();

        currentEpoch = new SimpleIntegerProperty(0);
        isRunning = new SimpleBooleanProperty(false);

        cBestFitness = new SimpleDoubleProperty(0);
        cTotalFitness = new SimpleDoubleProperty(0);

        data = FXCollections.observableArrayList(aiController.getPopulation());

        // ================================================
        // MVC Setup
        // ================================================
        currentEpoch.addListener((observable, oldValue, newValue) -> {
            txtCurrentEpoch.setText(newValue.toString());
        });

        cBestFitness.addListener((observable, oldValue, newValue) -> {
            txtCurrentBestFitness.setText(decimalFormat.format(newValue));
        });

        cTotalFitness.addListener((observable, oldValue, newValue) -> {
            txtCurrentTotalFitness.setText(decimalFormat.format(newValue));
        });

        isRunning.addListener(((observable, oldValue, newValue) -> {
            txtIsRunning.setText(Boolean.toString(newValue));
            if (newValue)
                aiRunner.start();
            else
                aiRunner.stop();
        }));

        txtScenarioRunTimes.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() > 0) {
                Integer value = new Integer(newValue);
                if(value > 0)
                    AIController.globalScenariosToRun = value;
            }

        });

        tableView.setItems(data);
        tableView.sort();

        // ================================================
        // Graph data
        // ================================================
        XYChart.Series<Number, Number> seriesMean = new XYChart.Series<>();
        seriesMean.setName("Mean");
        XYChart.Series<Number, Number> seriesSD = new XYChart.Series<>();
        seriesSD.setName("Standard deviation");
        XYChart.Series<Number, Number> seriesFittest = new XYChart.Series<>();
        seriesFittest.setName("Fittest");

        lcAiStatistics.getData().add(seriesMean);
        lcAiStatistics.getData().add(seriesSD);
        lcAiStatistics.getData().add(seriesFittest);

        // ================================================
        // AI runner
        // ================================================
        aiRunner = new AnimationTimer() {
            @Override
            public void handle(long now) {
                IndividualBrain best = aiController.runOneEpoch();

                currentEpoch.set(aiController.getEpoch());

                data.setAll(aiController.getPopulation());
                tableView.sort();

                GenerationStatistics gs = new GenerationStatistics(aiController.getPopulation(), aiController.getEpoch());

                seriesMean.getData().add(new XYChart.Data<>(gs.generation, gs.mean));
                seriesSD.getData().add(new XYChart.Data<>(gs.generation, gs.SD));
                seriesFittest.getData().add(new XYChart.Data<>(gs.generation, gs.bestIndividual.getFitness()));

                cTotalFitness.set(gs.total);
                cBestFitness.set(gs.bestIndividual.getFitness());
            }
        };

        // ================================================
        // Run simulation for one individual
        // ================================================
        tableView.setRowFactory(tv -> {
                    TableRow<IndividualBrain> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && (!row.isEmpty())) {
                            IndividualBrain rowData = row.getItem();

                            if (simulationRunner != null)
                                simulationRunner.stop();

                            System.out.println("Starting last flatland for: " + rowData.getId());

                            simulationRunner = makeSimulationRunner(rowData, aiController.getCurrentScenario());

                            simulationRunner.start();
                        } else if(event.getButton() == MouseButton.SECONDARY) {
                            IndividualBrain rowData = row.getItem();

                            if (simulationRunner != null)
                                simulationRunner.stop();

                            Flatland randomFlatland = new Flatland(10, 1/3.0, 1/3.0);
                            System.out.println("Starting random flatland for: " + rowData.getId());
                            simulationRunner = makeSimulationRunner(rowData, randomFlatland);
                            simulationRunner.start();
                        }
                    });

                    return row;
                }
        );
    }


    @FXML
    protected void startAi(ActionEvent event) {
        isRunning.set(!isRunning.get());
    }

    private AnimationTimer makeSimulationRunner(final IndividualBrain individual, final Flatland flatland){

        final LongProperty lastUpdate = new SimpleLongProperty();
        final long minUpdateInterval = 1000000000; // nanoseconds.
        return new AnimationTimer() {
            Flatland scenario = new Flatland(flatland);

            @Override
            public void handle(long now) {
                int cStep = scenario.getCurrentTotalSteps();

                if (now - lastUpdate.get() > minUpdateInterval) {
                    if (cStep < 60) {
                        Movement move = AIController.helperIndividualFindMove(scenario, individual);
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
    @FXML
    private void toggleStaticWorlds(ActionEvent event) {
        AIController.globalIsStatic = cbStaticWorlds.isSelected();
    }


    private void redraw(Items[][] world, Vector2 agentPosition) {
        gc.clearRect(0, 0, simulation.getWidth(), simulation.getHeight());

        int xSize = (int)(simulation.getWidth() / world[0].length);
        int ySize = (int)(simulation.getHeight() / world.length);

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
    }
}
