package com.example.singingsword.game.graphics;

import com.example.singingsword.GameController;
import com.example.singingsword.game.DamageCause;
import com.example.singingsword.game.Enemy;
import com.example.singingsword.game.graphics.images.ImageDrawer;
import com.example.singingsword.game.graphics.images.TextDrawer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import java.util.*;

import static com.example.singingsword.game.engine.GameEngine.HealthManager.maxHealth;
import static com.example.singingsword.game.graphics.images.SpriteUtils.*;
import static com.example.singingsword.game.graphics.images.SpriteUtils.swordSprite;
import static java.lang.Math.*;

public class GraphicsEngine {
    public static final int FPS = 60;

    private final Informator informator;
    private final GraphicsContext gc;
    private final long startNanoTime = System.nanoTime();

    private final static float backgroundMovingSpeed = 40f;
    private final static float floorMovingSpeed = 100f;

    private final static float unusedFloor = 120; // px
    private final static float heartsY = 64;

    float backgroundPos;
    float floorPos;

    private final static float damageAnimationTime = 0.3f;
    private final static float damageAnimationOpacity = 0.4f;
    private final static Color damageAnimationColor = Color.RED;
    private boolean damaged = false;
    private float lastDamageT = 0;

    private final static float healAnimationTime = 0.5f;
    private final static float healAnimationOpacity = 0.5f;
    private final static Color healAnimationColor = Color.PINK;
    private boolean healed = false;
    private float lastHealT = 0;

    private final ImageDrawer[] hearts = new ImageDrawer[maxHealth];

    private TextDrawer streakText = new TextDrawer("", Color.GOLD, 48);

    private final List<Enemy> recentlyKilled = new ArrayList<>();
    private final List<Pair<Integer, ImageDrawer>> recentlyRestored = new ArrayList<>();
    private final List<FallingImage> fallingImages = new ArrayList<>();

    private TextDrawer gameOverText = new TextDrawer("", Color.WHITE, 196);

    public void streakLost() {
        fallingImages.add(new FallingImage(512, (float) (gc.getCanvas().getHeight() - 16), streakText, 0, 0, (float) ((random() - 0.5) * 40)));
        streakText = new TextDrawer("", Color.GOLD, 48);
    }

    public void streakUpdated(float coefficient) {
        fallingImages.add(new FallingImage(512, (float) (gc.getCanvas().getHeight() - 16), streakText, (float) ((random() - 0.5) * 200), (float) (Math.random() + 1) * -200, (float) ((random() - 0.5) * 40)));
        streakText = new TextDrawer(String.format("x%.1f", coefficient), Color.GOLD, 48);
    }

    public void gameOver() {
        gameOverText = new TextDrawer("Game Over", Color.LIGHTGRAY, 128);
    }

    private static class KeptImageDrawer{
        public ImageDrawer imageDrawer;
        public int counter;
        KeptImageDrawer(ImageDrawer imageDrawer, int counter){
            this.imageDrawer = imageDrawer;
            this.counter = counter;
        }
    }
    private final List<KeptImageDrawer> keptImages = new ArrayList<>();

    public GraphicsEngine(GraphicsContext gc, Informator informator) {
        this.gc = gc;
        this.informator = informator;
        for (int i = 0; i < maxHealth; ++i)
            hearts[i] = getFilledHeartSprite();
    }

    public void healthLost(int health, DamageCause cause) {
        hearts[health] = getLostHeartSprite(cause);
        hearts[health].setAlpha(0.5f);
        damaged = true;
    }

    public void healthRestored(int health) {
        recentlyRestored.add(new Pair<>(health, hearts[health]));
        hearts[health] = getFilledHeartSprite();
        healed = true;
    }

    public void enemyKilled(Enemy enemy) {
        keptImages.add(new KeptImageDrawer(enemy.getImageDrawer(), FPS)); // keep for second
        recentlyKilled.add(enemy);
    }

    public void enemyEscaped(Enemy enemy){
        keptImages.add(new KeptImageDrawer(enemy.getImageDrawer(), FPS)); // keep for second
    }

    private float getEnemyX(Enemy enemy) {
        float enemyWidth = enemy.getImageDrawer().getWidth();
        return (float) ((1 - enemy.getX()) * (gc.getCanvas().getWidth() + enemyWidth) - enemyWidth / 2);
    }

    private float getPlayableY(float y) {
        return (float) ((1 - y) * (gc.getCanvas().getHeight() - unusedFloor));
    }

    private float getHeartX(int index){
        return (float) (gc.getCanvas().getWidth() - 56 - 112 * index);
    }

    public void draw(long now) {
        float t = (now - startNanoTime) / 1000000000f;
        clearBackground();
        drawBackground(t);
        updateFallingImages(t);
        moveFallingImages(t);
        drawEnemies(t);
        drawKeptImages(t);
        drawSword(t);
        drawHearts(t);
        printScore(t);
        drawDamageAnimation(t);
        drawGameOver(t);
    }

    private void drawDamageAnimation(float t) {
        if(damaged) {
            lastDamageT = t;
            damaged = false;
        }
        gc.setFill(damageAnimationColor);
        gc.setGlobalAlpha(damageAnimationOpacity * (1 - min(1, (t - lastDamageT) / damageAnimationTime)));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setGlobalAlpha(1f);

        if(healed) {
            lastHealT = t;
            healed = false;
        }
        gc.setFill(healAnimationColor);
        gc.setGlobalAlpha(healAnimationOpacity * (1 - min(1, (t - lastHealT) / healAnimationTime)));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setGlobalAlpha(1f);
    }

    private void clearBackground() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    private void drawBackground(float t) {
        if (!informator.isGameOver()) {
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
        fallingImages.removeIf(img -> img.getY() > 2*gc.getCanvas().getHeight() + hypot(img.getImageDrawer().getWidth(), img.getImageDrawer().getHeight()) / 2);
    }

    void drawKeptImages(float t){
        for (int i = 0; i < keptImages.size(); i++) {
            KeptImageDrawer keptImage = keptImages.get(i);
            keptImage.imageDrawer.drawImageLeftTop(gc, (float) gc.getCanvas().getWidth(), (float) gc.getCanvas().getHeight(), t);
            keptImage.counter--;
            if (keptImage.counter <= 0){
                keptImages.remove(i);
                i--;
            }
        }
    }

    private void drawEnemies(float t) {
        for (Enemy enemy : informator.getEnemies()) {
            enemy.getImageDrawer().drawImage(gc, getEnemyX(enemy), getPlayableY(enemy.getY()), t);
        }
    }

    private void drawSword(float t) {
        float swordX = min(1, 2 * informator.getSinging()) * 64;
        float swordY = (float) ((1 - informator.getSwordPosition()) * (gc.getCanvas().getHeight() - unusedFloor));
        swordSprite.drawImage(gc, swordX, swordY, t);
    }

    private void drawHearts(float t) {
        for (int i = 0; i < maxHealth; i++) {
            hearts[i].drawImage(gc, getHeartX(i), heartsY, t);
        }
    }

    private void printScore(float t){
        gc.setFill(Color.LIGHTGRAY);
        Font font = Font.loadFont(Objects.requireNonNull(GameController.class.getResource("fonts/pixeloid-font/PixeloidMono.ttf")).toString(), 48);
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Score: " + informator.getScore(), 24, gc.getCanvas().getHeight() - 16);
        streakText.drawImage(gc, 512, (float) (gc.getCanvas().getHeight() - 16), t);
    }

    private void drawGameOver(float t){
        if (informator.isGameOver()){
            gameOverText.drawImage(gc, (float) (gc.getCanvas().getWidth() / 2), (float) (gc.getCanvas().getHeight() / 2), t);
        }
    }
}
