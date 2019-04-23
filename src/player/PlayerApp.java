package player;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PlayerApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        PlayerPane p = new PlayerPane();
        Scene s = new Scene(p, 500, 150);
        primaryStage.setScene(s);
        primaryStage.setOnCloseRequest(evt->{
            p.close();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }
}
