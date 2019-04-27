package player;

import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
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

    private final LongProperty fadeDuration = new SimpleLongProperty(5000);

    private BooleanProperty sliderDrag = new SimpleBooleanProperty(false);
    private final BooleanProperty fadeIn = new SimpleBooleanProperty();
    private final BooleanProperty fadeOut = new SimpleBooleanProperty();
    private BooleanProperty dragging;


    private Interpolator lerp = Interpolator.EASE_IN;

    private LinkedList<File> loadedFiles = new LinkedList<>();

    private int currentTrackIdx = -1;

    MediaManager(DoubleProperty volume, DoubleProperty playProgress, DoubleProperty playDuration) {
        this.volume = volume;
        this.playProgress = playProgress;
        this.playDuration = playDuration;
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

    void clear() {
        loadedFiles = new LinkedList<>();
        trackName.set("");
        currentlyPlaying = null;
        player = null;
    }

    public void setDragging(BooleanProperty b) {
        this.dragging = b;
    }

    public void bindFadeIn(BooleanProperty b) {
        if (fadeIn.isBound()) {
            fadeIn.unbind();
        }
        fadeIn.bind(b);
    }

    public void bindFadeOut(BooleanProperty b) {
        if (fadeOut.isBound()) {
            fadeOut.unbind();
        }
        fadeOut.bind(b);
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
        loadedFiles.sort(Comparator.comparing(File::getName));
        for (File af : loadedFiles) {
            System.out.println("Loaded: " + af);
        }
    }

    void previous() {
        if (currentTrackIdx == 0) return;
        currentTrackIdx -= 1;
        loadSongAtIndex(currentTrackIdx);
    }

    void play() {
        if (fadeIn.get()) {
            player.setVolume(0.0);
            Macro m = new Macro(player::play);
            m.add(createFadeMacro(0, 1.0, fadeDuration.get()));
            threadPool.submit(m);
        } else {
            threadPool.submit(player::play);
        }
    }

    void stop() {
        dragging.set(false);
        if (fadeOut.get()) {
            Macro m = createFadeMacro(volume.getValue(), 0, fadeDuration.get());
            m.add(player::stop);
            threadPool.submit(m);
        } else {
            threadPool.submit(player::stop);
        }
    }

    void next() {
        if (currentTrackIdx + 1 == loadedFiles.size()) {
            System.out.printf("Attempted to access index %d of %d\n", currentTrackIdx, loadedFiles.size());
            return;
        }
        currentTrackIdx += 1;
        loadSongAtIndex(currentTrackIdx);
    }

    void mute() {
        player.setVolume(0);
    }

    private void loadSongAtIndex(int idx) {
        if (loadedFiles.size() - 1 < idx || loadedFiles.size() - 1 == idx) return;
        if (currentlyPlaying != null) {
            if (player.getStatus() == MediaPlayer.Status.PLAYING || player.getStatus() == MediaPlayer.Status.PAUSED)
                player.stop();
            player.volumeProperty().unbindBidirectional(volume);
            Platform.runLater(() -> playProgress.set(0));
            currentlyPlaying = null;
            player = null;
        }
        currentlyPlaying = new Media(loadedFiles.get(idx).toURI().toString());

        player = new MediaPlayer(currentlyPlaying);
        player.volumeProperty().bindBidirectional(volume);
        player.setOnEndOfMedia(player::stop);
        player.currentTimeProperty().addListener((obs, old, newVal) -> {
            if (dragging.get()) return;
            Platform.runLater(() -> playProgress.setValue(newVal.toSeconds()));
        });
        player.setOnReady(() -> playDuration.setValue(player.getTotalDuration().toSeconds()));

        Platform.runLater(() -> trackName.set("Now Playing: " + loadedFiles.get(idx).getName()));
    }

    public void startPlaybackFrom(double startInSeconds) {
        stop();
        player.setStartTime(new Duration(startInSeconds * 1000.0));
        player.play();
    }

    void unMute() {
        player.setVolume(1);
    }
}
