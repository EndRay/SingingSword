package com.example.singingsword;

import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.engine.GameEngine;
import com.example.singingsword.game.images.AnimatedImageProvider;
import com.example.singingsword.game.images.ImageDrawer;
import com.example.singingsword.game.images.SimpleImageDrawer;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.util.*;

import static com.example.singingsword.game.images.SpriteUtils.*;
import static java.lang.Math.min;

public class GameController {
    @FXML
    private Canvas canvas;

    private static final float backgroundMovingSpeed = 40f;
    private static final float floorMovingSpeed = 100f;

    private static final float unusedFloor = 120; // px

    final private GameEngine gameEngine = new GameEngine();

    public void initialize() {


        GraphicsContext gc = canvas.getGraphicsContext2D();

        final long startNanoTime = System.nanoTime();

        Deque<Pair<Float, Float>> swordPositionHistory = new ArrayDeque<>();
        float swordPositionHistorySize = 10;

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameEngine.handle(now);
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                float t = (now - startNanoTime) / 1000000000f;
                float backgroundPos = (t * backgroundMovingSpeed) % ((float) backgroundSprite.getWidth());
                backgroundSprite.drawImageLeftTop(gc, -backgroundPos, 0, t);
                backgroundSprite.drawImageLeftTop(gc, -backgroundPos + backgroundSprite.getWidth(), 0, t);

                float floorPos = (t * floorMovingSpeed) % ((float) floorSprite.getWidth());
                floorSprite.drawImageLeftTop(gc, -floorPos, 0, t);
                floorSprite.drawImageLeftTop(gc, -floorPos + floorSprite.getWidth(), 0, t);

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

                for(Enemy enemy : gameEngine.getEnemies()){
                    enemy.getImageDrawer().drawImage(gc, (float) ((1 - enemy.getX()) * canvas.getWidth()), (float) ((1 - enemy.getY()) * (canvas.getHeight() - unusedFloor)), t);
                    //gc.drawImage(enemyImage, (1-enemy.getX()) * (canvas.getWidth() + enemyImage.getWidth()) - enemyImage.getWidth(), (1-enemy.getY()) * (canvas.getHeight() - unusedFloor) - enemyImage.getHeight()/2 - enemyImage.getHeight()/2);
                }
            }
        };
        timer.start();
    }
}