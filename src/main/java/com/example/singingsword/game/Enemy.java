package com.example.singingsword.game;

import com.example.singingsword.game.images.AnimatedImageProvider;
import com.example.singingsword.game.images.ImageDrawer;
import com.example.singingsword.game.images.SimpleImageDrawer;

import java.awt.*;

import static com.example.singingsword.game.images.SpriteUtils.getEnemySprite;
import static java.lang.Math.PI;
import static java.lang.Math.cos;

public class Enemy {
    private float x; // from right to left
    private float y;
    private float yMovePeriod;
    private float yMoveAmplitude;
    private float speed;
    private float timePassed;
    private ImageDrawer imageDrawer = getEnemySprite();

    public Enemy(){
        this.x = 0;
        this.y = (float) Math.random() * 0.7f + 0.15f;
        this.speed = (float) (0.1f + (Math.random()*2 - 1) * 0.05f);
        yMovePeriod = (float) (0.2f + (Math.random()*2 - 1) * 0.1f);
        yMoveAmplitude = (float) (0.05f + (Math.random()*2 - 1) * 0.02f);
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y + (float) cos(timePassed * yMovePeriod * 2 * PI) * yMoveAmplitude;
    }
    public void move(float passed){
        this.x += this.speed * passed;
        this.timePassed += passed;
    }
    public ImageDrawer getImageDrawer(){
        return imageDrawer;
    }
}
