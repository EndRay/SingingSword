package com.example.singingsword.game.images;

import javafx.scene.image.Image;

public interface ImageProvider {
    Image getImage(float t);
    int getWidth();
    int getHeight();
}
