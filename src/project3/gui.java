package project3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class gui extends Application{

    Image food;
    Image poisong;
    Image robot;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // ================================================
        // Setup GUI
        // ================================================
        stage.setTitle("Flatland - EANN");
        Pane myPane = FXMLLoader.load(getClass().getResource("gui.fxml"));
        Scene myScene = new Scene(myPane);

        stage.setScene(myScene);
        stage.show();
    }
}
