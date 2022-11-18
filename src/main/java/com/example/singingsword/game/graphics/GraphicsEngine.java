package com.example.singingsword.game.graphics;

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
import static java.lang.Math.hypot;
import static java.lang.Math.min;

public class GraphicsEngine {
    private final GraphicsContext gc;
    private final long startNanoTime = System.nanoTime();
    private final GameEngine gameEngine;

    private final static float backgroundMovingSpeed = 40f;
    private final static float floorMovingSpeed = 100f;

    private final static float unusedFloor = 120; // px

    float backgroundPos;
    float floorPos;

    private final ImageDrawer[] hearts = new ImageDrawer[maxHealth];
    
    private final List<Enemy> recentlyKilled = new ArrayList<>();
    private final List<FallingImage> fallingImages = new ArrayList<>();

    float swordPositionHistorySize = 10;
    Deque<Pair<Float, Float>> swordPositionHistory = new ArrayDeque<>();

    public GraphicsEngine(GraphicsContext gc, GameEngine gameEngine) {
        this.gc = gc;
        this.gameEngine = gameEngine;
        for(int i = 0; i < maxHealth; ++i)
            hearts[i] = getFilledHeartSprite();
    }

    public void healthLost(int health) {
        hearts[health] = getLostHeartSprite();
    }

    public void enemyKilled(Enemy enemy) {
        recentlyKilled.add(enemy);
    }

    public float getEnemyX(Enemy enemy){
        float enemyWidth = enemy.getImageDrawer().getWidth();
        return (float) ((1 - enemy.getX()) * (gc.getCanvas().getWidth() + enemyWidth) - enemyWidth / 2);
    }
    public float getPlayableY(float y){
        return (float) ((1 - y) * (gc.getCanvas().getHeight() - unusedFloor));
    }


    public void draw(long now){

        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        float t = (now - startNanoTime) / 1000000000f;
        if(!gameEngine.isGameOver()) {
            backgroundPos = (t * backgroundMovingSpeed) % ((float) backgroundSprite.getWidth());
            floorPos = (t * floorMovingSpeed) % ((float) floorSprite.getWidth());
        }
        backgroundSprite.drawImageLeftTop(gc, -backgroundPos, 0, t);
        backgroundSprite.drawImageLeftTop(gc, -backgroundPos + backgroundSprite.getWidth(), 0, t);
        floorSprite.drawImageLeftTop(gc, -floorPos, 0, t);
        floorSprite.drawImageLeftTop(gc, -floorPos + floorSprite.getWidth(), 0, t);

        for(Enemy enemy : recentlyKilled)
            fallingImages.add(new FallingImage(getEnemyX(enemy), getPlayableY(enemy.getY()), enemy.getImageDrawer().fix(t)));
        recentlyKilled.clear();

        for(FallingImage fallingImage : fallingImages) {
            fallingImage.draw(gc, t);
        }
        fallingImages.removeIf(img -> img.getY() > gc.getCanvas().getHeight() + hypot(img.getImageDrawer().getWidth(), img.getImageDrawer().getHeight())/2);

        for(Enemy enemy : gameEngine.getEnemies()){
            enemy.getImageDrawer().drawImage(gc, getEnemyX(enemy), getPlayableY(enemy.getY()), t);
        }

        float opacity = 0f;
        for(var swordPosition : swordPositionHistory){
            gc.setGlobalAlpha(opacity);
            opacity += 1f / swordPositionHistorySize;
            swordSprite.drawImage(gc, swordPosition.getKey(), swordPosition.getValue(), t);
        }
        gc.setGlobalAlpha(1f);
        float swordX = min(1, 2*gameEngine.getSinging()) * 64;
        float swordY = (float) ((1 - gameEngine.getSwordPosition()) * (gc.getCanvas().getHeight() - unusedFloor));
        swordSprite.drawImage(gc, swordX, swordY, t);
        swordPositionHistory.add(new Pair<>(swordX, swordY));
        if(swordPositionHistory.size() > swordPositionHistorySize){
            swordPositionHistory.removeFirst();
        }

        for(int i = 0; i < gameEngine.getHealth(); i++){
            hearts[i].drawImage(gc, (float) (gc.getCanvas().getWidth() - 56 - 104*i), 64, t);
        }
        gc.setGlobalAlpha(0.4f);
        for(int i = gameEngine.getHealth(); i < maxHealth; i++){
            hearts[i].drawImage(gc, (float) (gc.getCanvas().getWidth() - 56 - 104*i), 64, t);
        }
        gc.setGlobalAlpha(1f);
    }
}
