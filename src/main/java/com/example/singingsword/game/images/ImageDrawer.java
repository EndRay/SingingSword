package com.example.singingsword.game.images;

import javafx.scene.canvas.GraphicsContext;

public interface ImageDrawer {
    void drawImage(GraphicsContext gc, float x, float y, float t);
    int getWidth();
    int getHeight();

    default void drawImageLeftTop(GraphicsContext gc, float x, float y, float t){
        drawImage(gc, x + getWidth()/2f, y + getHeight()/2f, t);
    }
}
