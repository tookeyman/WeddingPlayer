package player;

import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
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

    public MediaManager(DoubleProperty volume){
        this.volume = volume;
    }

    public void close(){
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Macro createFadeMacro(double start, double end, long duration) {
        return new Macro(() -> {
            long f = System.currentTimeMillis()+duration;
            long c = 0;
            while(c < f){
                c = System.currentTimeMillis();
                double frac = ((double)f-(double)c)/(double)duration;
                player.setVolume(lerp.interpolate(end, start, frac));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void load(File f){
        currentlyPlaying = new Media(f.toURI().toString());
        player = new MediaPlayer(currentlyPlaying);

        if(volume.isBound()){

        }
        player.volumeProperty().bindBidirectional(volume);
    }

    public void loadDir(File f){

    }

    public void previous(){

    }

    public void play(){
        threadPool.submit(player::play);
    }

    public void next(){

    }

    public void stop(){
        Macro m = createFadeMacro(1, 0, 5000);
        m.add(() -> player.stop());
        threadPool.submit(m);
    }

    public void mute(){
        player.setVolume(0);
    }

    public void unMute(){
        player.setVolume(1);
    }
}
