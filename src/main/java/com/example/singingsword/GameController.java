package com.example.singingsword;

import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.GameEngine;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameController {
    @FXML
    private Canvas canvas;

    private static final Image swordImage = new Image(GameController.class.getResource("images/sword.png").toString(), 128, 128, true, false);
    private static final Image enemyImage = new Image(GameController.class.getResource("images/enemy.png").toString(), 128, 128, true, false);
    private static final Image backgroundImage = new Image(GameController.class.getResource("images/background.png").toString(), 960, 720, true, false);
    private static final Image floorImage = new Image(GameController.class.getResource("images/floor.png").toString(), 960, 720, true, false);

    private static final float backgroundMovingSpeed = 40f;
    private static final float floorMovingSpeed = 100f;

    final private GameEngine gameEngine = new GameEngine();

    public void initialize() {


        GraphicsContext gc = canvas.getGraphicsContext2D();

        final long startNanoTime = System.nanoTime();
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

                gc.drawImage(swordImage,
                        (1-gameEngine.getSinging()) * -60,
                        (1 - gameEngine.getSwordPosition()) * (canvas.getHeight() - swordImage.getHeight()));

                for(Enemy enemy : gameEngine.getEnemies()){
                    gc.drawImage(enemyImage, (1-enemy.getX()) * (canvas.getWidth() - enemyImage.getWidth()), (1-enemy.getY()) * (canvas.getHeight() - enemyImage.getHeight()));
                }
            }
        };
        timer.start();
    }
}