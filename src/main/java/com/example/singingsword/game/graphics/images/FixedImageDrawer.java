package com.example.singingsword.game.graphics.images;

import javafx.scene.canvas.GraphicsContext;

public class FixedImageDrawer implements ImageDrawer {
    private final ImageDrawer imageDrawer;

    float t;

    public FixedImageDrawer(ImageDrawer imageDrawer, float t) {
        this.imageDrawer = imageDrawer;
        this.t = t;
    }

    public void drawImage(GraphicsContext gc, float x, float y) {
        imageDrawer.drawImage(gc, x, y, t);
    }

    @Override
    public void drawImage(GraphicsContext gc, float x, float y, float t) {
        this.drawImage(gc, x, y);
    }

    @Override
    public int getWidth() {
        return imageDrawer.getWidth();
    }

    @Override
    public int getHeight() {
        return imageDrawer.getHeight();
    }

    @Override
    public void setAlpha(float opacity) {
        imageDrawer.setAlpha(opacity);
    }

    @Override
    public FixedImageDrawer fix(float t){
        return this;
    }
}
