package com.example.singingsword;

import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.engine.GameEngine;
import com.example.singingsword.game.graphics.GraphicsEngine;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public class GameController {
    @FXML
    private Canvas canvas;

    private final GameEngine gameEngine = new GameEngine(this);
    private GraphicsEngine graphicsEngine = new GraphicsEngine(this);

    public void healthLost(int health) {
        graphicsEngine.healthLost(health);
    }

    public void healthRestored(int health) {
        graphicsEngine.healthRestored(health);
    }

    public void enemyKilled(Enemy enemy) {
        graphicsEngine.enemyKilled(enemy);
    }

    public boolean isGameOver() {
        return gameEngine.isGameOver();
    }

    public List<Enemy> getEnemies() {
        return gameEngine.getEnemies();
    }

    public GraphicsContext getGraphicContext() {
        return canvas.getGraphicsContext2D();
    }

    public float getSinging() {
        return gameEngine.getSinging();
    }

    public float getSwordPosition() {
        return gameEngine.getSwordPosition();
    }
    
    public void initialize() {
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