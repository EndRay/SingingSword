package com.example.singingsword;

import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.engine.GameEngine;
import com.example.singingsword.game.graphics.GraphicsEngine;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;

public class GameController {
    @FXML
    private Canvas canvas;

    private final GameEngine gameEngine = new GameEngine(this);
    private GraphicsEngine graphicsEngine;

    public void healthLost(int health) {
        graphicsEngine.healthLost(health);
    }

    public void enemyKilled(Enemy enemy) {
        graphicsEngine.enemyKilled(enemy);
    }
    
    public void initialize() {
        graphicsEngine = new GraphicsEngine(canvas.getGraphicsContext2D(), gameEngine);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameEngine.handle(now);
                graphicsEngine.draw(now);
            }
        };
        timer.start();
    }
}