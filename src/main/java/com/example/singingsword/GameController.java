package com.example.singingsword;

import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.engine.GameEngine;
import com.example.singingsword.game.images.ImageDrawer;
import com.example.singingsword.game.images.SimpleImageDrawer;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Pair;

import java.util.*;

import static com.example.singingsword.game.engine.GameEngine.maxHealth;
import static com.example.singingsword.game.images.SpriteUtils.*;
import static java.lang.Math.min;

public class GameController {
    @FXML
    private Canvas canvas;

    private final static float backgroundMovingSpeed = 40f;
    private final static float floorMovingSpeed = 100f;

    private final static float unusedFloor = 120; // px

    private final GameEngine gameEngine = new GameEngine(this);

    private final ImageDrawer[] hearts = new ImageDrawer[maxHealth];

    public void healthLost(int health) {
        hearts[health] = getLostHeartSprite();
    }
    
    public void initialize() {
        for (int i = 0; i < maxHealth; i++) {
            hearts[i] = getFilledHeartSprite();
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        final long startNanoTime = System.nanoTime();

        Deque<Pair<Float, Float>> swordPositionHistory = new ArrayDeque<>();
        float swordPositionHistorySize = 10;


        AnimationTimer timer = new AnimationTimer() {
            float backgroundPos;
            float floorPos;

            @Override
            public void handle(long now) {
                gameEngine.handle(now);
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                float t = (now - startNanoTime) / 1000000000f;
                if(!gameEngine.isGameOver()) {
                    backgroundPos = (t * backgroundMovingSpeed) % ((float) backgroundSprite.getWidth());
                    floorPos = (t * floorMovingSpeed) % ((float) floorSprite.getWidth());
                }
                backgroundSprite.drawImageLeftTop(gc, -backgroundPos, 0, t);
                backgroundSprite.drawImageLeftTop(gc, -backgroundPos + backgroundSprite.getWidth(), 0, t);
                floorSprite.drawImageLeftTop(gc, -floorPos, 0, t);
                floorSprite.drawImageLeftTop(gc, -floorPos + floorSprite.getWidth(), 0, t);

                for(Enemy enemy : gameEngine.getEnemies()){
                    enemy.getImageDrawer().drawImage(gc, (float) ((1 - enemy.getX()) * canvas.getWidth()), (float) ((1 - enemy.getY()) * (canvas.getHeight() - unusedFloor)), t);
                }

                float opacity = 0f;
                for(var swordPosition : swordPositionHistory){
                    gc.setGlobalAlpha(opacity);
                    opacity += 1f / swordPositionHistorySize;
                    swordSprite.drawImage(gc, swordPosition.getKey(), swordPosition.getValue(), t);
                }
                gc.setGlobalAlpha(1f);
                float swordX = min(1, 2*gameEngine.getSinging()) * 64;
                float swordY = (float) ((1 - gameEngine.getSwordPosition()) * (canvas.getHeight() - unusedFloor));
                swordSprite.drawImage(gc, swordX, swordY, t);
                swordPositionHistory.add(new Pair<>(swordX, swordY));
                if(swordPositionHistory.size() > swordPositionHistorySize){
                    swordPositionHistory.removeFirst();
                }

                for(int i = 0; i < gameEngine.getHealth(); i++){
                    hearts[i].drawImage(gc, (float) (canvas.getWidth() - 56 - 104*i), 64, t);
                }
                gc.setGlobalAlpha(0.4f);
                for(int i = gameEngine.getHealth(); i < maxHealth; i++){
                    hearts[i].drawImage(gc, (float) (canvas.getWidth() - 56 - 104*i), 64, t);
                }
                gc.setGlobalAlpha(1f);
            }
        };
        timer.start();
    }
}