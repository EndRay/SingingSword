package com.example.singingsword.game.engine;

import com.example.singingsword.game.DamageCause;
import com.example.singingsword.game.Enemy;

public interface Informable {

    void enemyKilled(Enemy enemy);
    void enemyEscaped(Enemy enemy);

    void streakLost();
    void streakUpdated(float coefficient);

    void healthRestored(int health);

    void healthLost(int health, DamageCause damageCause);

    void gameOver();
}
