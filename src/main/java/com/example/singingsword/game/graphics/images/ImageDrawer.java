package com.example.singingsword.game.graphics.images;

import javafx.scene.canvas.GraphicsContext;

public interface ImageDrawer {
    void drawImage(GraphicsContext gc, float x, float y, float t);
    int getWidth();
    int getHeight();

    default void drawImageLeftTop(GraphicsContext gc, float x, float y, float t){
        drawImage(gc, x + getWidth()/2f, y + getHeight()/2f, t);
    }

    default void drawImage(GraphicsContext gc, float x, float y, float t, float rotate){
        gc.save();
        gc.translate(x, y);
        gc.rotate(rotate);
        drawImage(gc, 0, 0, t);
        gc.restore();
    }

    default FixedImageDrawer fix(float t){
        return new FixedImageDrawer(this, t);
    }
}
