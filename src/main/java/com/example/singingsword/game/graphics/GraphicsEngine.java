package com.example.singingsword.game.graphics;

import com.example.singingsword.GameController;
import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.engine.GameEngine;
import com.example.singingsword.game.graphics.images.ImageDrawer;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static com.example.singingsword.game.engine.GameEngine.maxHealth;
import static com.example.singingsword.game.graphics.images.SpriteUtils.*;
import static com.example.singingsword.game.graphics.images.SpriteUtils.swordSprite;
import static java.lang.Math.*;

public class GraphicsEngine {
    private final GameController gameController;
    private GraphicsContext gc;
    private final long startNanoTime = System.nanoTime();

    private final static float backgroundMovingSpeed = 40f;
    private final static float floorMovingSpeed = 100f;

    private final static float unusedFloor = 120; // px
    private final static float heartsY = 64;

    float backgroundPos;
    float floorPos;

    private final ImageDrawer[] hearts = new ImageDrawer[maxHealth];

    private final List<Enemy> recentlyKilled = new ArrayList<>();
    private final List<Pair<Integer, ImageDrawer>> recentlyRestored = new ArrayList<>();
    private final List<FallingImage> fallingImages = new ArrayList<>();

    float swordPositionHistorySize = 10;
    Deque<Pair<Float, Float>> swordPositionHistory = new ArrayDeque<>();

    public GraphicsEngine(GameController gameController) {
        this.gameController = gameController;
        for (int i = 0; i < maxHealth; ++i)
            hearts[i] = getFilledHeartSprite();
    }

    public void healthLost(int health) {
        hearts[health] = getLostHeartSprite();
        hearts[health].setAlpha(0.5f);
    }

    public void healthRestored(int health) {
        recentlyRestored.add(new Pair<>(health, hearts[health]));
        hearts[health] = getFilledHeartSprite();
    }

    public void enemyKilled(Enemy enemy) {
        recentlyKilled.add(enemy);
    }

    private float getEnemyX(Enemy enemy) {
        float enemyWidth = enemy.getImageDrawer().getWidth();
        return (float) ((1 - enemy.getX()) * (gc.getCanvas().getWidth() + enemyWidth) - enemyWidth / 2);
    }

    private float getPlayableY(float y) {
        return (float) ((1 - y) * (gc.getCanvas().getHeight() - unusedFloor));
    }

    private float getHeartX(int index){
        return (float) (gc.getCanvas().getWidth() - 56 - 104 * index);
    }

    public void draw(long now) {
        gc = gameController.getGraphicContext();
        float t = (now - startNanoTime) / 1000000000f;
        clearBackground();
        drawBackground(t);
        updateFallingImages(t);
        moveFallingImages(t);
        drawEnemies(t);
        drawSwordTrace(t);
        drawSword(t);
        drawHearts(t);
    }

    private void clearBackground() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    private void drawBackground(float t) {
        if (!gameController.isGameOver()) {
            backgroundPos = (t * backgroundMovingSpeed) % ((float) backgroundSprite.getWidth());
            floorPos = (t * floorMovingSpeed) % ((float) floorSprite.getWidth());
        }
        backgroundSprite.drawImageLeftTop(gc, -backgroundPos, 0, t);
        backgroundSprite.drawImageLeftTop(gc, -backgroundPos + backgroundSprite.getWidth(), 0, t);
        floorSprite.drawImageLeftTop(gc, -floorPos, 0, t);
        floorSprite.drawImageLeftTop(gc, -floorPos + floorSprite.getWidth(), 0, t);
    }

    private void updateFallingImages(float t){
        for (Enemy enemy : recentlyKilled) {
            for (var segment : enemy.getImageDrawer().fixDivided(t)) {
                fallingImages.add(new FallingImage(getEnemyX(enemy), getPlayableY(enemy.getY()), segment,
                        (float) (Math.random() + 1) * 100, (float) (Math.random() + 1) * -300, (float) (Math.random() - 0.5) * 40));
            }
        }
        recentlyKilled.clear();
        for (var heart : recentlyRestored) {
            for (var segment : heart.getValue().fixDivided(t))
                fallingImages.add(new FallingImage(getHeartX(heart.getKey()), heartsY, segment,
                        (float) (Math.random() - 0.5) * 200, (float) (Math.random() + 1) * -200, (float) (Math.random() - 0.5) * 40));
        }
        recentlyRestored.clear();
    }

    private void moveFallingImages(float t){
        for (FallingImage fallingImage : fallingImages) {
            fallingImage.draw(gc, t);
        }
        fallingImages.removeIf(img -> img.getY() > gc.getCanvas().getHeight() + hypot(img.getImageDrawer().getWidth(), img.getImageDrawer().getHeight()) / 2);
    }

    private void drawEnemies(float t) {
        for (Enemy enemy : gameController.getEnemies()) {
            enemy.getImageDrawer().drawImage(gc, getEnemyX(enemy), getPlayableY(enemy.getY()), t);
        }
    }

    private void drawSwordTrace(float t) {
        float opacity = 0f;
        for (var swordPosition : swordPositionHistory) {
            gc.setGlobalAlpha(opacity);
            opacity += 1f / swordPositionHistorySize;
            swordSprite.drawImage(gc, swordPosition.getKey(), swordPosition.getValue(), t);
        }
        gc.setGlobalAlpha(1f);
    }

    private void drawSword(float t) {
        float swordX = min(1, 2 * gameController.getSinging()) * 64;
        float swordY = (float) ((1 - gameController.getSwordPosition()) * (gc.getCanvas().getHeight() - unusedFloor));
        swordSprite.drawImage(gc, swordX, swordY, t);
        swordPositionHistory.add(new Pair<>(swordX, swordY));
        if (swordPositionHistory.size() > swordPositionHistorySize) {
            swordPositionHistory.removeFirst();
        }
    }

    private void drawHearts(float t) {
        for (int i = 0; i < maxHealth; i++) {
            hearts[i].drawImage(gc, getHeartX(i), heartsY, t);
        }
    }

}
