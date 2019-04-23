package player;

import java.util.LinkedList;

public class Macro implements Runnable {
    private final LinkedList<Runnable> todo = new LinkedList<>();

    public Macro(){}

    public Macro(Runnable runnable) {
        add(runnable);
    }

    @Override
    public void run() {
        for (Runnable runnable : todo) {
            runnable.run();
        }
    }

    public void add(Runnable r){
        todo.add(r);
    }
}
