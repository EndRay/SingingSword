package com.example.singingsword.game.engine;

import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.engine.sound.PitchExtractor;
import javafx.application.Platform;
import javafx.beans.property.*;

import javax.sound.sampled.*;

import java.util.ArrayList;
import java.util.List;

import static com.example.singingsword.game.engine.sound.Sound.SAMPLE_RATE;
import static java.lang.Math.*;
import static java.util.Collections.unmodifiableList;

public class GameEngine {
    private final FloatProperty swordTargetPositionProperty = new SimpleFloatProperty();
    private final FloatProperty swordPositionProperty = new SimpleFloatProperty();

    private final FloatProperty frequencyProperty = new SimpleFloatProperty();
    private final FloatProperty singingProperty = new SimpleFloatProperty();

    private final float minFrequency = 70;
    private final float maxFrequency = 800;

    private float lastT = System.nanoTime() / 1e9f;
    
    private final int gameTickFrequency = 60;
    private final float gameTickPeriod = 1f / gameTickFrequency;

    private final float spawnPeriod = 2f; // in seconds
    private float nextSpawnTime = 0f;

    private final List<Enemy> enemies = new ArrayList<>();

    public GameEngine() {
        Thread soundParsingThread = new Thread(() -> {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
            TargetDataLine line;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                    format); // format is an AudioFormat object
            if (!AudioSystem.isLineSupported(info)) {
                throw new RuntimeException("Line not supported");
            }
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                PitchExtractor pitchExtractor = new PitchExtractor();
                while (true) {
                    byte[] data = new byte[1024];
                    int numBytesRead = line.read(data, 0, data.length);
                    pitchExtractor.feedRawData(data, numBytesRead);
                    if (pitchExtractor.isRecognizable()) {
                        Platform.runLater(() -> frequencyProperty.set(pitchExtractor.getPitch()));
                    }
                    Platform.runLater(() -> singingProperty.set(pitchExtractor.getRecognizability()));

                }
            } catch (LineUnavailableException ex) {
                throw new RuntimeException("Line unavailable");
            }
        });
        soundParsingThread.setDaemon(true);
        soundParsingThread.start();

        frequencyProperty.addListener(((observable, oldValue, newValue) -> {
            float value = (float) (log(newValue.floatValue() / minFrequency) / log(maxFrequency / minFrequency));
            value = max(0, min(1, value));
            swordTargetPositionProperty.set(value);
        }));
    }
    
    private void moveSword(){
        float target = swordTargetPositionProperty.floatValue();
        float current = swordPositionProperty.floatValue();
        float diff = target - current;
        swordPositionProperty.set(current + diff * 0.1f);
    }

    private void spawnEnemy(){
        enemies.add(new Enemy());
    }
    
    private void gameTick(float t){
        float passed = t - lastT;
        moveSword();
        if(t >= nextSpawnTime){
            spawnEnemy();
            nextSpawnTime = t+spawnPeriod;
        }
        for(Enemy enemy : enemies){
            enemy.move(passed);
        }
        for(int i = 0; i < enemies.size(); i++){
            if(enemies.get(i).getX() > 1) {
                System.out.println("Enemy escaped");
                enemies.remove(i--);

            }
            else if(enemies.get(i).getX() > 0.8f && isSinging() && abs(enemies.get(i).getY() - swordPositionProperty.floatValue()) < 0.1f){
                System.out.println("Enemy killed");
                enemies.remove(i--);
            }
        }
    }
    
    public void handle(long now){ // seconds
        float t = now / 1e9f;
        if(t - lastT > gameTickPeriod){
            gameTick(t);
            lastT = t;
        }
    }

    public List<Enemy> getEnemies() {
        return unmodifiableList(enemies);
    }

    public boolean isSinging() {
        return singingProperty.get() > 0;
    }

    public float getSinging() {
        return singingProperty.get();
    }

    public float getSwordPosition() {
        return swordPositionProperty.get();
    }
}
