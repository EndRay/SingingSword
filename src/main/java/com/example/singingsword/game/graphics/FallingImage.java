package com.example.singingsword.game.graphics;

import com.example.singingsword.game.graphics.images.ImageDrawer;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;

public class FallingImage {
    public static final float FALLING_ACCELERATION = 800f;

    private float x;
    private float y;
    private float rotation;
    private float xSpeed;
    private float ySpeed;
    private float rSpeed;
    private boolean firstFrame = true;
    private float lastT;
    private ImageDrawer imageDrawer;

    public FallingImage(float x, float y, ImageDrawer imageDrawer, float xSpeed, float ySpeed, float rotation, float rSpeed) {
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.rotation = rotation;
        this.rSpeed = rSpeed;
        this.imageDrawer = imageDrawer;
    }

    public FallingImage(float x, float y, ImageDrawer imageDrawer) {
        this(x, y, imageDrawer, (float) (Math.random() + 1) * 100, (float) -600, 0, (float) (Math.random() - 0.5) * 40);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    private void move(float passed){
        if(firstFrame){
            firstFrame = false;
            lastT = passed;
        }
        ySpeed += FALLING_ACCELERATION * passed;
        x += xSpeed * passed;
        y += ySpeed * passed;
    }

    public ImageDrawer getImageDrawer() {
        return imageDrawer;
    }

    public void draw(GraphicsContext gc, float t){
        if(firstFrame)
            firstFrame = false;
        else {
            ySpeed += FALLING_ACCELERATION * (t - lastT);
            x += xSpeed * (t - lastT);
            y += ySpeed * (t - lastT);
            rotation += rSpeed * (t - lastT);
        }
        lastT = t;
        imageDrawer.drawImage(gc, x, y, t, rotation);
    }
}
