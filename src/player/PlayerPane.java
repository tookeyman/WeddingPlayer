package player;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class PlayerPane extends Pane {
    Button prev = new Button("Previous");
    Button play = new Button("Play");
    Button stop = new Button("Stop");
    Button next = new Button("Next");
    Button mute = new Button("Mute");

    private MediaManager manager = new MediaManager();

    public PlayerPane(){


        Platform.runLater(this::init);
    }

    private void init(){
        play.layoutXProperty().bind(prev.layoutXProperty().add(prev.widthProperty()));
        stop.layoutXProperty().bind(play.layoutXProperty().add(play.widthProperty()));
        next.layoutXProperty().bind(stop.layoutXProperty().add(stop.widthProperty()));
        mute.layoutXProperty().bind(next.layoutXProperty().add(next.widthProperty()));
        getChildren().addAll(prev, play, stop, next, mute);
    }
}
