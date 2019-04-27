package player;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Paths;

class PlayerPane extends Pane {
    private Button prev = new Button("Previous");
    private Button play = new Button("Play");
    private Button stop = new Button("Stop");
    private Button next = new Button("Next");
    private Button mute = new Button("Mute");

    private Label volumeLevelLabel = new Label();
    private Label volumeLabel = new Label("Volume:");
    private Label progressLabel = new Label("Progress:");
    private Label trackNameLabel = new Label();
    private Label trackProgressLabel = new Label();

    private Slider volumeSlider = new Slider();
    private Slider progressSlider = new Slider();

    private boolean muted = false;

    private MenuBar menuBar = new MenuBar();

    private CheckBox fadeIn = new CheckBox("Fade In");
    private CheckBox fadeOut = new CheckBox("Fade Out");

    private final BooleanProperty dragStarted = new SimpleBooleanProperty(false);

    private MediaManager manager = new MediaManager(volumeSlider.valueProperty(), progressSlider.valueProperty(), progressSlider.maxProperty());

    PlayerPane() {
        prev.setOnAction(evt -> {
            manager.stop();
            manager.previous();
        });
        play.setOnAction(evt -> manager.play());
        stop.setOnAction(evt -> manager.stop());
        next.setOnAction(evt -> {
            manager.stop();
            manager.next();
        });
        mute.setOnAction(evt -> mute());

        manager.bindFadeIn(fadeIn.selectedProperty());
        manager.bindFadeOut(fadeOut.selectedProperty());
        manager.setDragging(dragStarted);

        Menu fileMenu = new Menu("File");
        MenuItem openDir = new MenuItem("OpenDirectory");
        openDir.setOnAction(evt -> openDir());
        menuBar.getMenus().addAll(fileMenu);
        fileMenu.getItems().addAll(openDir);

        progressSlider.setOnDragDetected(evt -> {
            dragStarted.set(true);
        });

        progressSlider.setOnMousePressed(evt->{
            dragStarted.set(true);
        });

        progressSlider.valueProperty().addListener((obs, old, newVal) -> {
            updateTrackProgressLabel();
        });

        progressSlider.setOnMouseReleased(evt->{
            if (!dragStarted.get()) return;
            dragStarted.set(false);
            double startTime = progressSlider.getValue();
            manager.startPlaybackFrom(startTime);
        });


        Platform.runLater(this::init);
    }

    void updateTrackProgressLabel(){
        trackProgressLabel.setText(String.format("%.2f/%.2fm", progressSlider.valueProperty().get()/60.0,progressSlider.getMax()/60.0));
    }

    private void init() {
        menuBar.minWidthProperty().bindBidirectional(menuBar.maxWidthProperty());
        menuBar.maxWidthProperty().bind(widthProperty());

        prev.layoutXProperty().bind(widthProperty().divide(2.0).subtract(totalButtonWidth().divide(2.0)));
        prev.layoutYProperty().bindBidirectional(play.layoutYProperty());

        play.layoutXProperty().bind(prev.layoutXProperty().add(prev.widthProperty()));
        play.layoutYProperty().bind(menuBar.layoutYProperty().add(menuBar.heightProperty()).add(15.0));

        stop.layoutXProperty().bind(play.layoutXProperty().add(play.widthProperty()));
        stop.layoutYProperty().bindBidirectional(play.layoutYProperty());
        next.layoutXProperty().bind(stop.layoutXProperty().add(stop.widthProperty()));
        next.layoutYProperty().bindBidirectional(play.layoutYProperty());
        mute.layoutXProperty().bind(next.layoutXProperty().add(next.widthProperty()));
        mute.layoutYProperty().bindBidirectional(play.layoutYProperty());

        fadeIn.layoutXProperty().bind(mute.layoutXProperty().add(mute.widthProperty()).add(10.0));
        fadeIn.layoutYProperty().bind(play.layoutYProperty());
        fadeOut.layoutXProperty().bind(fadeIn.layoutXProperty());
        fadeOut.layoutYProperty().bind(fadeIn.layoutYProperty().add(fadeIn.heightProperty()).add(2.0));

        trackNameLabel.layoutYProperty().bind(play.layoutYProperty().add(play.heightProperty()).add(15.0));
        trackNameLabel.layoutXProperty().bind(widthProperty().divide(2.0).subtract(trackNameLabel.widthProperty().divide(2.0)));
        trackNameLabel.textProperty().bind(manager.trackNameProperty());



        volumeLabel.layoutYProperty().bind(trackNameLabel.layoutYProperty().add(trackNameLabel.heightProperty()).add(15.0));
        volumeLabel.layoutXProperty().bind(volumeSlider.layoutXProperty());
        volumeSlider.layoutYProperty().bind(volumeLabel.layoutYProperty().add(volumeLabel.heightProperty()));
        volumeSlider.minWidthProperty().bind(volumeSlider.maxWidthProperty());
        volumeSlider.maxWidthProperty().bind(totalButtonWidth());
        volumeSlider.layoutXProperty().bind(prev.layoutXProperty());
        volumeSlider.setValue(1.0);
        volumeSlider.setMin(0);
        volumeSlider.setMax(1.0);

        volumeLevelLabel.layoutXProperty().bind(volumeSlider.layoutXProperty().add(volumeSlider.widthProperty()).add(15.0));
        volumeLevelLabel.layoutYProperty().bind(volumeSlider.layoutYProperty());
        volumeLevelLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("%.2f%%", volumeSlider.getValue() * 100.0), volumeSlider.valueProperty()));


        progressLabel.layoutYProperty().bind(volumeSlider.layoutYProperty().add(volumeSlider.heightProperty()).add(15.0));
        progressLabel.layoutXProperty().bind(progressSlider.layoutXProperty());
        trackProgressLabel.layoutYProperty().bind(progressSlider.layoutYProperty());
        trackProgressLabel.layoutXProperty().bind(progressSlider.layoutXProperty().add(progressSlider.widthProperty().add(15.0)));
        progressSlider.layoutYProperty().bind(progressLabel.layoutYProperty().add(progressLabel.heightProperty()));
        progressSlider.layoutXProperty().bind(volumeSlider.layoutXProperty());

        progressSlider.setMin(0);
        progressSlider.minWidthProperty().bind(progressSlider.maxWidthProperty());
        progressSlider.maxWidthProperty().bind(volumeSlider.widthProperty());

        getChildren().addAll(menuBar, prev, play, stop, next, mute, volumeSlider, volumeLevelLabel, trackNameLabel, trackProgressLabel, progressSlider, fadeIn, fadeOut, volumeLabel, progressLabel);
    }

    private File chooseFile(File startingDir, FileChooser.ExtensionFilter... filters) {
        FileChooser fc = new FileChooser();
        for (FileChooser.ExtensionFilter ef : filters) {
            fc.getExtensionFilters().add(ef);
        }
        fc.setInitialDirectory(startingDir);
        File f = fc.showOpenDialog(this.getScene().getWindow());
        return f;
    }

    private File chooseDirectory(File startingDir) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(startingDir);
        return dc.showDialog(this.getScene().getWindow());
    }

    private void openDir() {
        File dir = chooseDirectory();
        if (dir == null) return;
        manager.clear();
        manager.addDir(dir);
        manager.next();
    }

    private File chooseDirectory() {
        return chooseDirectory(Paths.get("/Users/david").toFile());
    }

    private File chooseFile(FileChooser.ExtensionFilter... filters) {
        return chooseFile(Paths.get("/Users/david").toFile(), filters);
    }

    private DoubleBinding totalButtonWidth() {
        return prev.widthProperty().add(play.widthProperty()).add(stop.widthProperty()).add(next.widthProperty()).add(mute.widthProperty());
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
