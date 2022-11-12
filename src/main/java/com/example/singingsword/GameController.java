package com.example.singingsword;

import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.GameEngine;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;

import static java.lang.Math.min;

public class GameController {
    @FXML
    private Canvas canvas;

    private static final Image swordImage = new Image(GameController.class.getResource("images/sword.png").toString(), 128, 128, true, false);
    private static final Image enemyImage = new Image(GameController.class.getResource("images/enemy.png").toString(), 128, 128, true, false);
    private static final Image backgroundImage = new Image(GameController.class.getResource("images/background.png").toString(), 960, 720, true, false);
    private static final Image floorImage = new Image(GameController.class.getResource("images/floor.png").toString(), 960, 720, true, false);

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
                float backgroundPos = (t * backgroundMovingSpeed) % ((float) backgroundImage.getWidth());
                gc.drawImage(backgroundImage, -backgroundPos, 0);
                gc.drawImage(backgroundImage, -backgroundPos + backgroundImage.getWidth(), 0);

                float floorPos = (t * floorMovingSpeed) % ((float) floorImage.getWidth());
                gc.drawImage(floorImage, -floorPos, 0);
                gc.drawImage(floorImage, -floorPos + floorImage.getWidth(), 0);

                float opacity = 0f;
                for(var swordPosition : swordPositionHistory){
                    gc.setGlobalAlpha(opacity);
                    opacity += 1f / swordPositionHistorySize;
                    gc.drawImage(swordImage,
                            (1-min(1, 2*swordPosition.getKey())) * -60,
                            (1 - swordPosition.getValue()) * (canvas.getHeight() - unusedFloor) - swordImage.getHeight()/2);
                }
                gc.setGlobalAlpha(1f);
                float swordX = (1-min(1, 2*gameEngine.getSinging())) * -60;
                float swordY = (float) ((1 - gameEngine.getSwordPosition()) * (canvas.getHeight() - unusedFloor) - swordImage.getHeight()/2);
                gc.drawImage(swordImage, swordX, swordY);
                swordPositionHistory.add(new Pair<>(gameEngine.getSinging(), gameEngine.getSwordPosition()));
                if(swordPositionHistory.size() > swordPositionHistorySize){
                    swordPositionHistory.removeFirst();
                }

                for(Enemy enemy : gameEngine.getEnemies()){
                    gc.drawImage(enemyImage, (1-enemy.getX()) * (canvas.getWidth() + enemyImage.getWidth()) - enemyImage.getWidth(), (1-enemy.getY()) * (canvas.getHeight() - unusedFloor) - enemyImage.getHeight()/2 - enemyImage.getHeight()/2);
                }
            }
        };
        timer.start();
    }
}