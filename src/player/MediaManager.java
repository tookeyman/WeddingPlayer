package player;

import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Track;

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

    private Interpolator lerp = Interpolator.EASE_IN;

    private LinkedList<File> loadedFiles = new LinkedList<>();

    private int currentTrackIdx = -1;

    public MediaManager(DoubleProperty volume) {
        this.volume = volume;
    }

    public void close() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Macro createFadeMacro(double start, double end, long duration) {
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

    public void addFile(File f) {
        if (loadedFiles.contains(f)) return;
        loadedFiles.add(f);
        if (currentTrackIdx == -1) {
            currentTrackIdx = 0;
        }
    }

    public void addDir(File f) {
        if (!Files.isDirectory(f.toPath())) return;
        File[] children = f.listFiles();
        assert children != null;
        for (File file : children) {
            addFile(file);
        }
    }

    public void previous() {
        if (currentTrackIdx == 0) return;
        currentTrackIdx -= 1;
        loadSongAtIndex(currentTrackIdx);
        play();
    }

    public void play() {
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
    }

    public void next() {
        if (loadedFiles.size() - 1 == currentTrackIdx) return;
        currentTrackIdx += 1;
        loadSongAtIndex(currentTrackIdx);
        play();
    }

    public void stop() {
        Macro m = createFadeMacro(volume.getValue(), 0, 5000);
        m.add(() -> player.stop());
        threadPool.submit(m);
    }

    public void mute() {
        player.setVolume(0);
    }

    public void unMute() {
        player.setVolume(1);
    }
}
