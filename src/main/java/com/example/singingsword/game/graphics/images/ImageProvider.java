package com.example.singingsword.game.graphics.images;

import javafx.scene.image.Image;

public interface ImageProvider {
    Image getImage(float t);
    int getWidth();
    int getHeight();
}
