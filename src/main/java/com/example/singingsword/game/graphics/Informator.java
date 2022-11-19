package com.example.singingsword.game.graphics;

import com.example.singingsword.game.Enemy;

import java.util.List;

public interface Informator {

    boolean isGameOver();

    List<Enemy> getEnemies();

    float getSinging();

    float getSwordPosition();

    int getScore();
}
