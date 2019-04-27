import javafx.application.Application;
import player.PlayerApp;

class launch {
    public static void main(String... args) {
        Thread t = new Thread(() -> Application.launch(PlayerApp.class));

        try{
            t.start();
            t.join();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
