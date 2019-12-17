package kr.pe.firstfloor.util;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ticker extends Thread {
    private int tick;
    private long lastTicked;

    private TickHandler handler;

    private List<TickListener> listeners = Collections.synchronizedList(new ArrayList<TickListener>());


    public Ticker(int tickPerSecond) {
        // 10^9 ns / tickPerSecond => 1 second / tickPerSecond
        this.tick = (int)(Math.pow(10, 9)/tickPerSecond); // in nanoseconds
        this.lastTicked = System.nanoTime();

        this.handler = new TickHandler(this);

        this.start();
    }


    public interface TickListener { void onTick(); }
    public void addTickListener(TickListener listener) {
        listeners.add(listener);
    }


    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(0, tick);
            } catch (InterruptedException exception) {}

            long current = System.nanoTime();
            for (; current >= lastTicked + tick; lastTicked += tick)
                handler.sendEmptyMessage(0);
        }
    }


    private static class TickHandler extends Handler {
        Ticker ticker;

        TickHandler(Ticker ticker) {
            this.ticker = ticker;
        }

        @Override
        public void handleMessage(Message message) {
            for (TickListener listener : ticker.listeners)
                listener.onTick();
        }
    }
}
