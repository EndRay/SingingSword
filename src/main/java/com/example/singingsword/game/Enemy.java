package com.example.singingsword.game;

import com.example.singingsword.game.graphics.images.ImageDrawer;

import static com.example.singingsword.game.graphics.images.SpriteUtils.getEnemySprite;
import static java.lang.Math.*;

public class Enemy {
    private float x; // from right to left
    private float y;
    private float yMovePeriod;
    private float yMoveAmplitude;
    private float speed;
    private float timePassed;
    private EnemyType type;
    private ArmorType armorType;
    private ImageDrawer imageDrawer;
    private int giveScore;

    private float height = 0.02f;

    public Enemy(){
        this.x = 0;
        this.y = (float) Math.random() * 0.7f + 0.15f;
        {
            var rnd = Math.random();
            if(rnd < 0.05){
                type = EnemyType.HEALING;
                armorType = ArmorType.NONE;
            } else {
                rnd = Math.random();
                if(rnd < 0.05){
                    type = EnemyType.INFECTED;
                } else {
                    type = EnemyType.REGULAR;
                }
                rnd = Math.random();
                if(rnd < 0.2){
                    armorType = ArmorType.TOP;
                } else if(rnd < 0.4){
                    armorType = ArmorType.BOTTOM;
                } else if(rnd < 0.5 && type != EnemyType.INFECTED){
                    armorType = ArmorType.BOTH;
                } else {
                    armorType = ArmorType.NONE;
                }
                rnd = Math.random();
                if(rnd < 0.4)
                    armorType = armorType.strong();
            }
        }

        giveScore = switch (type){
            case REGULAR,INFECTED -> switch (armorType){
                case NONE -> 10;
                case TOP,BOTTOM -> 15;
                case STRONG_TOP,STRONG_BOTTOM -> 20;
                case BOTH, STRONG_BOTH -> 0;
            };
            case HEALING -> 0;
        };

        this.speed = (float) (0.1f + (Math.random()*2 - 1) * 0.05f);
        yMovePeriod = (float) (0.2f + (Math.random()*2 - 1) * 0.1f);
        yMoveAmplitude = (float) (0.05f + (Math.random()*2 - 1) * 0.02f);
        if(type == EnemyType.HEALING) {
            speed *= 1.5;
            yMoveAmplitude *= 3;
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

    public int getScore() {
        return giveScore;
    }

    public ArmorType getArmorType() {
        return armorType;
    }
}
