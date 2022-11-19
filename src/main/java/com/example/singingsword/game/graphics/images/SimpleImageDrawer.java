package com.example.singingsword.game.graphics.images;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class SimpleImageDrawer implements ImageDrawer{
    private final ImageProvider imageProvider;
    private float opacity = 1f;
    private boolean isGlobalAlpha = true;

    public SimpleImageDrawer(ImageProvider imageProvider) {
        this.imageProvider = imageProvider;
    }

    @Override
    public void drawImage(GraphicsContext gc, float x, float y, float t) {
        var oldOpacity = gc.getGlobalAlpha();
        if(!isGlobalAlpha)
            gc.setGlobalAlpha(opacity);
        Image image = imageProvider.getImage(t);
        gc.drawImage(image, x - image.getWidth()/2, y - image.getHeight()/2);
        if(!isGlobalAlpha)
            gc.setGlobalAlpha(oldOpacity);
    }

    @Override
    public int getWidth() {
        return imageProvider.getWidth();
    }

    @Override
    public int getHeight() {
        return imageProvider.getHeight();
    }

    @Override
    public void setAlpha(float opacity) {
        isGlobalAlpha = false;
        this.opacity = opacity;
    }

    @Override
    public void useGlobalAlpha() {
        isGlobalAlpha = true;
    }

    @Override
    public boolean isGlobalAlpha(){
        return isGlobalAlpha;
    }
}
