package player;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class MediaManager {
    private Media currentlyPlaying = null;
    private MediaPlayer player = null;

    public MediaManager(){

    }

    public void load(File f){
        currentlyPlaying = new Media(f.toURI().toString());
        player = new MediaPlayer(currentlyPlaying);
    }

    public void previous(){

    }

    public void play(){

    }

    public void next(){

    }

    public void stop(){

    }

    public void mute(){

    }
}
