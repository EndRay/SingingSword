package com.example.singingsword.game;

public class Enemy {
    private float x; // from right to left
    private float y;
    private float speed;

    public Enemy(){
        this.x = 0;
        this.y = (float) Math.random();
        this.speed = 0.1f;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public void move(float passed){
        this.x += this.speed * passed;
    }
}
