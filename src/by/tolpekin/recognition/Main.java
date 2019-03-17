package by.tolpekin.recognition;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("index.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root, 1200, 900));
        primaryStage.setTitle("OpenCV face tracking");
        primaryStage.show();

        IndexController controller = loader.getController();
        primaryStage.setOnCloseRequest((event -> controller.setClosed()));
    }


    public static void main(String[] args) {
        System.out.println("start");
        nu.pattern.OpenCV.loadLocally();
        launch(args);
    }
}
