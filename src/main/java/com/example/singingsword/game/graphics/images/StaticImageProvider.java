package com.example.singingsword.game.graphics.images;

import javafx.scene.image.Image;

public class StaticImageProvider implements ImageProvider {
    private final Image image;

    public StaticImageProvider(Image image) {
        this.image = image;
    }

    @Override
    public Image getImage(float t) {
        return image;
    }

    @Override
    public int getWidth() {
        return (int)image.getWidth();
    }

    @Override
    public int getHeight() {
        return (int)image.getHeight();
    }
}
