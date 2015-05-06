package project4;

import algorithms.ea.statistics.GenerationStatistics;
import algorithms.ectrnn.IndividualCTRBrain;
import gameworlds.tracker.Tracker;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.text.DecimalFormat;

public class FXMLTableViewController {
    @FXML
    Canvas simulation;

    @FXML
    TableView<IndividualCTRBrain> tableView;

    @FXML
    Text txtIsRunning;

    @FXML
    Text txtCurrentEpoch;

    @FXML
    Text txtCurrentTotalFitness;

    @FXML
    Text txtCurrentBestFitness;

    @FXML
    TextField txtfSimulationInterval;

    @FXML
    LineChart<Number, Number> lcAiStatistics;

    private GraphicsContext gc;

    private ObservableList<IndividualCTRBrain> data;

    private AIController aiController;

    private SimpleIntegerProperty currentEpoch;

    private SimpleBooleanProperty isRunning;

    private SimpleDoubleProperty cTotalFitness;

    private SimpleDoubleProperty cBestFitness;

    private AnimationTimer aiRunner;

    private AnimationTimer simulationRunner;

    // nanoseconds.
    private LongProperty minSimulationUpdateInterval = new SimpleLongProperty(100000000);


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


        txtfSimulationInterval.setText(minSimulationUpdateInterval.getValue().toString());

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

        txtfSimulationInterval.textProperty().addListener(((observable, oldValue, newValue) -> {
            minSimulationUpdateInterval.set(Long.valueOf(newValue));
        }));

        tableView.setItems(data);
        tableView.sort();

        makeAiRunner();

        // ================================================
        // Run simulation for one individual
        // ================================================
        tableView.setRowFactory(tv -> {
                    TableRow<IndividualCTRBrain> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && (!row.isEmpty())) {
                            IndividualCTRBrain rowData = row.getItem();

                            if (simulationRunner != null)
                                simulationRunner.stop();

                            simulationRunner = makeSimulationRunner(rowData);

                            simulationRunner.start();
                        }
                    });

                    return row;
                }
        );
    }

    @FXML
    private void reset() {
        aiRunner.stop();
        aiController.reset();

        currentEpoch.set(0);
        isRunning.set(false);

        cBestFitness.set(0);
        cTotalFitness.set(0);

        if(simulationRunner != null)
            simulationRunner.stop();

        data.clear();
        data.setAll(aiController.getPopulation());
        tableView.setItems(data);
        tableView.sort();

        makeAiRunner();
    }


    @FXML
    protected void startAi(ActionEvent event) {
        isRunning.set(!isRunning.get());
    }

    private AnimationTimer makeSimulationRunner(final IndividualCTRBrain individual){

        final LongProperty lastUpdate = new SimpleLongProperty();
        return new AnimationTimer() {
            Tracker scenario = new Tracker(aiController.getGameType());

            @Override
            public void handle(long now) {

                int cStep = scenario.getCurrentTimestep();

                if (now - lastUpdate.get() > minSimulationUpdateInterval.get()) {
                    if (cStep < 600) {
                        Tracker.Movement move = AIController.helperIndividualFindMove(scenario, individual);
                        scenario.newStep(move);
                        redraw(scenario);

                    } else {
                        this.stop();
                    }

                    lastUpdate.set(now);
                }
            }
        };
    }

    @FXML
    public void tglTriggerGameActon(ActionEvent event) {
        Tracker.GameType gameType;

        switch (((RadioButton) event.getSource()).getText()){
            case "Normal":
                gameType = Tracker.GameType.NORMAL;
                break;
            case "No-Wrap":
                gameType = Tracker.GameType.NOWRAP;
                break;
            case "Pull down":
                gameType = Tracker.GameType.PULLDOWN;
                break;
            default:
                System.err.println("Wrong type on radio button list");
                return;
        }
        aiController.setTrackerGameType(gameType);

        reset();
    }


    private void redraw(Tracker tracker) {
        gc.clearRect(0, 0, simulation.getWidth(), simulation.getHeight());

        int xSize = (int)(simulation.getWidth() / tracker.getWidth());
        int ySize = (int)(simulation.getHeight() / tracker.getHeight());

        int width =  tracker.getWidth();

        if(tracker.getTileLength() > 4) {
            gc.setFill(Color.RED);
        } else {
            gc.setFill(Color.GREEN);
        }

        drawWrappedRectangle(tracker.getTileLeftPos(), tracker.getTileLength(), tracker.getHeight() - tracker.getTileHeightPos(), width, xSize, ySize);

        // Shows platform as green if the previous turn was positive, green when negative, else yellow.
        if(tracker.getPlatformState() == 0)
            gc.setFill(Color.YELLOW);
        else if(tracker.getPlatformState() == 1)
            gc.setFill(Color.GREEN);
        else if(tracker.getPlatformState() == 2)
            gc.setFill(Color.RED);

        drawWrappedRectangle(tracker.getPlatformLeftPos(), tracker.getPlatformLength(), tracker.getHeight(), width, xSize, ySize);
    }

    public void drawWrappedRectangle(int leftPos, int tileLength, int yPos, int width, int xSize, int ySize) {
        for(int i = 0; i < tileLength; i++) {

            int i_tmp = leftPos + i;

            i_tmp = (((i_tmp % width) + width) % width);
            gc.fillRect(i_tmp * xSize, yPos * ySize, xSize, ySize);
        }
    }

    public void makeAiRunner() {
        // ================================================
        // Graph data
        // ================================================
        XYChart.Series<Number, Number> seriesMean = new XYChart.Series<>();
        seriesMean.setName("Mean");
        XYChart.Series<Number, Number> seriesSD = new XYChart.Series<>();
        seriesSD.setName("Standard deviation");
        XYChart.Series<Number, Number> seriesFittest = new XYChart.Series<>();
        seriesFittest.setName("Fittest");

        lcAiStatistics.getData().clear();

        lcAiStatistics.getData().add(seriesMean);
        lcAiStatistics.getData().add(seriesSD);
        lcAiStatistics.getData().add(seriesFittest);

        // ================================================
        // AI runner
        // ================================================
        aiRunner = new AnimationTimer() {
            @Override
            public void handle(long now) {
                IndividualCTRBrain best = aiController.runOneEpoch();

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
    }
}
