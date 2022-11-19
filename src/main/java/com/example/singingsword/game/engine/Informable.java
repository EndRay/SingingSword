package com.example.singingsword.game.engine;

import com.example.singingsword.game.Enemy;

public interface Informable {

    void enemyKilled(Enemy enemy);

    void healthRestored(int health);

    void healthLost(int health);
}
