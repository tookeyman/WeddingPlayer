package player;

import java.util.LinkedList;

class Macro implements Runnable {
    private final LinkedList<Runnable> todo = new LinkedList<>();

    Macro(Runnable runnable) {
        add(runnable);
    }

    @Override
    public void run() {
        for (Runnable runnable : todo) {
            runnable.run();
        }
    }

    void add(Runnable r){
        todo.add(r);
    }
}
