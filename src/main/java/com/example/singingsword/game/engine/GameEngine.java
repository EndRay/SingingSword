package com.example.singingsword.game.engine;

import com.example.singingsword.game.DamageCause;
import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.EnemyType;
import com.example.singingsword.game.engine.sound.PitchExtractor;
import javafx.application.Platform;
import javafx.beans.property.*;

import javax.sound.sampled.*;

import java.util.*;

import static com.example.singingsword.game.engine.sound.Sound.SAMPLE_RATE;
import static java.lang.Math.*;
import static java.util.Collections.unmodifiableList;

public class GameEngine {
    private final Informable informable;

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

    private final float spawnPeriod = 1f; // in seconds
    private float nextSpawnTime = 0f;

    public class HealthManager {
        public static final int maxHealth = 3;
        private int health = maxHealth;

        public int getHealth() {
            return health;
        }

        private void loseHealth(DamageCause cause) {
            if (health > 0) {
                informable.healthLost(--health, cause);
                if (health == 0) {
                    gameOver();
                }
            }

        }

        private void restoreHealth() {
            if (health < maxHealth) {
                informable.healthRestored(health++);
            }
        }
    }
    public final HealthManager healthManager = new HealthManager();

    public class ScoreManager {
        private int score = 0;

        private final float streakMaxTime = 3f;
        private float streakTime = 0f;
        private int streak = 0;
        private float streakCoefficient = 1f;

        private final Map<Integer, Float> streakCoefficients = Map.of(
                3, 1.5f,
                5, 2f,
                7, 3f
        );

        public int getScore() {
            return score;
        }

        public void tick(float dt) {
            if (streakTime > 0) {
                streakTime -= dt;
                if (streakTime <= 0) {
                    informable.streakLost();
                    streak = 0;
                    streakCoefficient = 1f;
                }
            }
        }

        private void addScore(int score) {
            if(score > 0) {
                streak += 1;
                streakTime = streakMaxTime;
                if(streakCoefficients.containsKey(streak)) {
                    streakCoefficient = streakCoefficients.get(streak);
                    informable.streakUpdated(streakCoefficient);
                }
                this.score += score * streakCoefficient;
            }
        }
    }
    public final ScoreManager scoreManager = new ScoreManager();

    private final List<Enemy> enemies = new ArrayList<>();

    private final int swordPositionHistorySize = gameTickFrequency / 10;
    private final Deque<Float> swordPositionHistory = new ArrayDeque<>();

    public GameEngine(Informable informable) {
        this.informable = informable;
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
                while (!gameOver) {
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
        System.out.println("Game over");
    }

    public boolean isGameOver(){
        return gameOver;
    }

    private boolean checkTopBottomCut(Enemy enemy){
        return swordPositionHistory.getLast() < enemy.getHitboxStart() &&
                swordPositionHistory.getFirst() > enemy.getHitboxEnd();
    }

    private boolean checkBottomTopCut(Enemy enemy){
        return swordPositionHistory.getFirst() < enemy.getHitboxStart() &&
                swordPositionHistory.getLast() > enemy.getHitboxEnd();
    }

    private boolean checkEnemyKill(Enemy enemy){
        return ((checkBottomTopCut(enemy) &&
                    !(enemy.getArmorType().isBottom() && !enemy.getArmorType().isStrong())) ||
                (checkTopBottomCut(enemy) &&
                    !(enemy.getArmorType().isTop() && !enemy.getArmorType().isStrong())));
    }

    private boolean checkEnemyArmorDamage(Enemy enemy){
        return ((checkBottomTopCut(enemy) &&
                    enemy.getArmorType().isBottom() && enemy.getArmorType().isStrong()) ||
                (checkTopBottomCut(enemy) &&
                    enemy.getArmorType().isTop() && enemy.getArmorType().isStrong()));
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
                    healthManager.restoreHealth();
                }
                else if (enemies.get(i).getType() == EnemyType.INFECTED){
                    healthManager.loseHealth(DamageCause.INFECTION);
                }
                informable.enemyEscaped(enemies.get(i));
                enemies.remove(i--);
            }
            else if(enemies.get(i).getX() > 0.8f && enemies.get(i).getX() < 0.9f && isSinging() && checkEnemyKill(enemies.get(i))){
                if(checkEnemyArmorDamage(enemies.get(i))){
                    healthManager.loseHealth(DamageCause.DAMAGED_SWORD);
                }
                else {
                    scoreManager.addScore(enemies.get(i).getScore());
                }
                informable.enemyKilled(enemies.get(i));
                enemies.remove(i--);
            }
        }
        scoreManager.tick(passed);
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
}
