package player;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

import java.io.File;

class PlayerPane extends Pane {
    private Button prev = new Button("Previous");
    private Button play = new Button("Play");
    private Button stop = new Button("Stop");
    private Button next = new Button("Next");
    private Button mute = new Button("Mute");

    private Label volumeLabel = new Label();
    private Label trackNameLabel = new Label();
    private Label trackProgressLabel = new Label();

    private Slider volumeSlider = new Slider();
    private Slider progressSlider = new Slider();

    private boolean muted = false;

    private MediaManager manager = new MediaManager(volumeSlider.valueProperty(), progressSlider.valueProperty(), progressSlider.maxProperty());

    PlayerPane() {
        prev.setOnAction(evt -> manager.previous());
        play.setOnAction(evt -> manager.play());
        stop.setOnAction(evt -> manager.stop());
        next.setOnAction(evt -> manager.next());
        mute.setOnAction(evt -> mute());


        File f = new File("d:\\music");
        manager.addDir(f);

        Platform.runLater(this::init);
    }

    private void init() {
        play.layoutXProperty().bind(prev.layoutXProperty().add(prev.widthProperty()));
        stop.layoutXProperty().bind(play.layoutXProperty().add(play.widthProperty()));
        next.layoutXProperty().bind(stop.layoutXProperty().add(stop.widthProperty()));
        mute.layoutXProperty().bind(next.layoutXProperty().add(next.widthProperty()));


        volumeSlider.layoutYProperty().bind(play.layoutYProperty().add(play.heightProperty()).add(15.0));
        volumeSlider.minWidthProperty().bindBidirectional(volumeSlider.maxWidthProperty());
        volumeSlider.maxWidthProperty().bind(mute.layoutXProperty().add(mute.widthProperty()));
        volumeSlider.setValue(1.0);
        volumeSlider.setMin(0);
        volumeSlider.setMax(1.0);

        volumeLabel.layoutXProperty().bind(volumeSlider.layoutXProperty().add(volumeSlider.widthProperty()).add(15.0));
        volumeLabel.layoutYProperty().bind(volumeSlider.layoutYProperty());
        volumeLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("%.2f%%", volumeSlider.getValue() * 100.0), volumeSlider.valueProperty()));

        trackNameLabel.layoutYProperty().bind(volumeLabel.layoutYProperty().add(volumeLabel.heightProperty()));
        trackNameLabel.textProperty().bind(manager.trackNameProperty());

        progressSlider.layoutYProperty().bind(trackNameLabel.layoutYProperty().add(trackNameLabel.heightProperty()));
        progressSlider.layoutXProperty().bind(volumeSlider.layoutXProperty());

        progressSlider.setMin(0);
        progressSlider.minWidthProperty().bind(progressSlider.maxWidthProperty());
        progressSlider.maxWidthProperty().bind(progressSlider.widthProperty());

        getChildren().addAll(prev, play, stop, next, mute, volumeSlider, volumeLabel, trackNameLabel, trackProgressLabel, progressSlider);
    }

    void close() {
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
