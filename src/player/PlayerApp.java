package player;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Paths;

public class PlayerApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        PlayerPane p = new PlayerPane();
        Scene s = new Scene(p, 500, 215);
        s.getStylesheets().add(Paths.get("style/default-player-style.css").toUri().toString());
        primaryStage.setScene(s);
        primaryStage.setOnCloseRequest(evt->{
            p.close();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }
}
