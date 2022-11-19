package com.example.singingsword.game.engine;

import com.example.singingsword.GameController;
import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.EnemyType;
import com.example.singingsword.game.engine.sound.PitchExtractor;
import javafx.application.Platform;
import javafx.beans.property.*;

import javax.sound.sampled.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static com.example.singingsword.game.engine.sound.Sound.SAMPLE_RATE;
import static java.lang.Math.*;
import static java.util.Collections.unmodifiableList;

public class GameEngine {
    private final GameController gameController;

    private final FloatProperty swordTargetPositionProperty = new SimpleFloatProperty();
    private final FloatProperty swordPositionProperty = new SimpleFloatProperty();

    private final FloatProperty singingProperty = new SimpleFloatProperty();

    private Thread soundParsingThread;

    private boolean gameOver = false;

    private final float minFrequency = 100;
    private final float maxFrequency = 700;

    private float lastT = System.nanoTime() / 1e9f;
    
    private final int gameTickFrequency = 60;
    private final float gameTickPeriod = 1f / gameTickFrequency;

    private final float spawnPeriod = 2f; // in seconds
    private float nextSpawnTime = 0f;

    public static int maxHealth = 3;
    private int health = maxHealth;

    private final List<Enemy> enemies = new ArrayList<>();

    private final int swordPositionHistorySize = gameTickFrequency / 10;
    private final Deque<Float> swordPositionHistory = new ArrayDeque<>();

    public GameEngine(GameController gameController) {
        this.gameController = gameController;
        soundParsingThread = new Thread(() -> {
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
                while (!soundParsingThread.isInterrupted()) {
                    byte[] data = new byte[1024];
                    int numBytesRead = line.read(data, 0, data.length);
                    pitchExtractor.feedRawData(data, numBytesRead);
                    if (pitchExtractor.isRecognizable()) {
                        Platform.runLater(() -> {
                            float value = (float) (log(pitchExtractor.getPitch() / minFrequency) / log(maxFrequency / minFrequency));
                            value = max(0, min(1, value));
                            swordTargetPositionProperty.set(value);
                            if(singingProperty.get() == 0) {
                                swordPositionProperty.set(value);
                                swordPositionHistory.clear();
                            }
                            singingProperty.set(pitchExtractor.getRecognizability());
                        });
                    }
                    else {
                        Platform.runLater(() -> singingProperty.set(pitchExtractor.getRecognizability()));
                    }

                }
            } catch (LineUnavailableException ex) {
                throw new RuntimeException("Line unavailable");
            }
        });
        soundParsingThread.setDaemon(true);
        soundParsingThread.start();
    }
    
    private void moveSword(){
        float target = swordTargetPositionProperty.floatValue();
        float current = swordPositionProperty.floatValue();
        float diff = target - current;
        swordPositionProperty.set(current + diff * 0.1f);
        swordPositionHistory.addLast(swordPositionProperty.floatValue());
        if(swordPositionHistory.size() > swordPositionHistorySize) {
            swordPositionHistory.removeFirst();
        }
    }

    private void spawnEnemy(){
        enemies.add(new Enemy());
    }

    private void gameOver(){
        gameOver = true;
        soundParsingThread.interrupt();
        System.out.println("Game over");
    }

    public boolean isGameOver(){
        return gameOver;
    }

    private void loseHealth(){
        if(health > 0) {
            gameController.healthLost(--health);
            if (health == 0)
                gameOver();
        }
    }

    private void restoreHealth(){
        if(health < maxHealth) {
            gameController.healthRestored(health++);
        }
    }

    private boolean checkEnemyKill(Enemy enemy){
        return ((swordPositionHistory.getFirst() < enemy.getHitboxStart() &&
                        swordPositionHistory.getLast() > enemy.getHitboxEnd() &&
                        (enemy.getType() != EnemyType.BOTTOM_ARMORED)) ||
                (swordPositionHistory.getLast() < enemy.getHitboxStart() &&
                        swordPositionHistory.getFirst() > enemy.getHitboxEnd() &&
                        (enemy.getType() != EnemyType.TOP_ARMORED)));
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
                if(enemies.get(i).getType() == EnemyType.HEALING){
                    restoreHealth();
                }
                else {
                    loseHealth();
                }
                enemies.remove(i--);
            }
            else if(enemies.get(i).getX() > 0.8f && isSinging() && checkEnemyKill(enemies.get(i))){
                if(enemies.get(i).getType() == EnemyType.HEALING){
                    loseHealth();
                }
                gameController.enemyKilled(enemies.get(i));
                enemies.remove(i--);
            }
        }
    }
    
    public void handle(long now){ // seconds
        float t = now / 1e9f;
        if(!gameOver && t - lastT > gameTickPeriod){
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

    public int getHealth(){
        return health;
    }
}
