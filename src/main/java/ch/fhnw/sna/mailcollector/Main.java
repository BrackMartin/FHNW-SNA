package ch.fhnw.sna.mailcollector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/CredentialForm.fxml"));
        primaryStage.setTitle("SNA Mail Collector");
        Scene scene = new Scene(root, 600, 300);
        scene.getStylesheets().add("/styles/styles.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
