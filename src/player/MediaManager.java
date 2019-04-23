package player;

import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MediaManager {
    private Media currentlyPlaying = null;
    private MediaPlayer player = null;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private StringProperty trackName = new SimpleStringProperty("");
    private DoubleProperty volume;
    private DoubleProperty playProgress;
    private DoubleProperty playDuration;
    private BooleanProperty sliderDrag = new SimpleBooleanProperty(false);


    private Interpolator lerp = Interpolator.EASE_IN;

    private LinkedList<File> loadedFiles = new LinkedList<>();

    private int currentTrackIdx = -1;

    MediaManager(DoubleProperty volume, DoubleProperty playProgress, DoubleProperty playDuration) {
        this.volume = volume;
        this.playProgress = playProgress;
        this.playDuration = playDuration;
        playProgress.addListener((obs, old, newVal)->{
            if(!sliderDrag.get() || player == null) return;
            player.setStartTime(new Duration((long)newVal.doubleValue()*60000));
            play();
        });
    }

    void close() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Threadpool closed");
    }

    private Macro createFadeMacro(double start, double end, long duration) {
        return new Macro(() -> {
            long f = System.currentTimeMillis() + duration;
            long c = 0;
            while (c < f) {
                c = System.currentTimeMillis();
                double frac = ((double) f - (double) c) / (double) duration;
                Platform.runLater(() -> player.setVolume(lerp.interpolate(end, start, frac)));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addFile(File f) {
        if (loadedFiles.contains(f)) return;
        loadedFiles.add(f);
        if (currentTrackIdx == -1) {
            currentTrackIdx = 0;
        }
    }

    public StringProperty trackNameProperty() {
        return trackName;
    }

    public BooleanProperty sliderDragProperty() {
        return sliderDrag;
    }

    void addDir(File f) {
        if (!Files.isDirectory(f.toPath())) return;
        File[] children = f.listFiles();
        assert children != null;
        for (File file : children) {
            addFile(file);
        }
    }

    void previous() {
        if (currentTrackIdx == 0) return;
        currentTrackIdx -= 1;
        loadSongAtIndex(currentTrackIdx);
        play();
    }

    void play() {
        if (currentlyPlaying == null) loadSongAtIndex(0);
        threadPool.submit(player::play);
    }

    private void loadSongAtIndex(int idx) {
        if (loadedFiles.size() - 1 < idx || loadedFiles.size() - 1 == idx) return;
        if (currentlyPlaying != null) {
            if (player.getStatus() == MediaPlayer.Status.PLAYING || player.getStatus() == MediaPlayer.Status.PAUSED)
                player.stop();
            player.volumeProperty().unbindBidirectional(volume);
            currentlyPlaying = null;
            player = null;
        }
        currentlyPlaying = new Media(loadedFiles.get(idx).toURI().toString());
        player = new MediaPlayer(currentlyPlaying);
        player.volumeProperty().bindBidirectional(volume);
        player.setOnEndOfMedia(player::stop);
        player.currentTimeProperty().addListener((obs, old, newVal) -> {
            Platform.runLater(() -> playProgress.setValue(newVal.toSeconds()));
        });
        playDuration.setValue(player.getStopTime().toSeconds());
        Platform.runLater(() -> trackName.set("Now Playing: " + loadedFiles.get(idx).getName()));
    }

    void next() {
        if (loadedFiles.size() - 1 == currentTrackIdx) return;
        currentTrackIdx += 1;
        loadSongAtIndex(currentTrackIdx);
        play();
    }

    void stop() {
        Macro m = createFadeMacro(volume.getValue(), 0, 5000);
        m.add(() -> player.stop());
        threadPool.submit(m);
    }

    void mute() {
        player.setVolume(0);
    }

    void unMute() {
        player.setVolume(1);
    }
}
