package com.example.singingsword.game.graphics.images;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class SimpleImageDrawer implements ImageDrawer{
    private final ImageProvider imageProvider;

    public SimpleImageDrawer(ImageProvider imageProvider) {
        this.imageProvider = imageProvider;
    }

    @Override
    public void drawImage(GraphicsContext gc, float x, float y, float t) {
        Image image = imageProvider.getImage(t);
        gc.drawImage(image, x - image.getWidth()/2, y - image.getHeight()/2);
    }

    @Override
    public int getWidth() {
        return imageProvider.getWidth();
    }

    @Override
    public int getHeight() {
        return imageProvider.getHeight();
    }
}
