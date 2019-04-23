package player;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

import java.io.File;

public class PlayerPane extends Pane {
    private Button prev = new Button("Previous");
    private Button play = new Button("Play");
    private Button stop = new Button("Stop");
    private Button next = new Button("Next");
    private Button mute = new Button("Mute");

    private Slider volume = new Slider();

    private boolean muted = false;

    private MediaManager manager = new MediaManager(volume.valueProperty());

    public PlayerPane() {
        prev.setOnAction(evt -> manager.previous());
        play.setOnAction(evt -> manager.play());
        stop.setOnAction(evt -> manager.stop());
        next.setOnAction(evt -> manager.next());
        mute.setOnAction(evt -> mute());


        File f = new File("d:\\music\\01_Children_of_the_Omnissiah.mp3");
        manager.load(f);

        Platform.runLater(this::init);
    }

    private void init() {
        play.layoutXProperty().bind(prev.layoutXProperty().add(prev.widthProperty()));
        stop.layoutXProperty().bind(play.layoutXProperty().add(play.widthProperty()));
        next.layoutXProperty().bind(stop.layoutXProperty().add(stop.widthProperty()));
        mute.layoutXProperty().bind(next.layoutXProperty().add(next.widthProperty()));
        volume.layoutYProperty().bind(play.layoutYProperty().add(play.heightProperty()).add(15.0));
        volume.minWidthProperty().bindBidirectional(volume.maxWidthProperty());
        volume.maxWidthProperty().bind(mute.layoutXProperty().add(mute.widthProperty()));
        volume.setMin(0);
        volume.setMax(1.0);

        getChildren().addAll(prev, play, stop, next, mute, volume);
    }

    public void close() {
        manager.close();
    }

    private void mute() {
        if (muted) {
            manager.unMute();
            mute.setText("Mute");
            muted = false;
        } else {
            manager.mute();
            mute.setText("Unmute");
            muted = true;
        }
    }
}
