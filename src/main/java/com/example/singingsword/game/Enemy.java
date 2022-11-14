package com.example.singingsword.game;

import com.example.singingsword.game.images.ImageDrawer;

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
    private EnemyType type;
    private ImageDrawer imageDrawer;

    private float height = 0.02f;

    public Enemy(){
        this.x = 0;
        this.y = (float) Math.random() * 0.7f + 0.15f;
        this.speed = (float) (0.1f + (Math.random()*2 - 1) * 0.05f);
        yMovePeriod = (float) (0.2f + (Math.random()*2 - 1) * 0.1f);
        yMoveAmplitude = (float) (0.05f + (Math.random()*2 - 1) * 0.02f);
        {
            var rnd = Math.random();
            if(rnd < 0.1){
                type = EnemyType.BOTTOM_ARMORED;
            } else if(rnd < 0.2){
                type = EnemyType.TOP_ARMORED;
            } else {
                type = EnemyType.REGULAR;
            }
        }
        imageDrawer = getEnemySprite(this);
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

    public EnemyType getType() {
        return type;
    }

    public Float getHitboxStart() {
        return getY() - height/2;
    }

    public Float getHitboxEnd() {
        return getY() + height/2;
    }

}
