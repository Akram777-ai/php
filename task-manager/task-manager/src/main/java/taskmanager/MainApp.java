package taskmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import taskmanager.controller.MainController;

import java.io.IOException;

/** Точка входа: загружает главное окно из FXML. */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/taskmanager/main.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(MainApp.class.getResource("/taskmanager/styles.css").toExternalForm());

        MainController controller = loader.getController();
        stage.setOnCloseRequest(e -> controller.saveSilently()); // автосохранение при закрытии

        stage.setTitle("Task Manager — менеджер задач");
        stage.setMinWidth(760);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
