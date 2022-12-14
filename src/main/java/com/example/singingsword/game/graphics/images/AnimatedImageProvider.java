package com.example.singingsword.game.graphics.images;

import javafx.scene.image.Image;

import java.util.List;

public class AnimatedImageProvider implements ImageProvider{

    private List<Image> images;
    private float period;

    private boolean firstAsked = true;
    private float startT;

    public AnimatedImageProvider(List<Image> images, float period) {
        this.images = images;
        this.period = period;
        // check if images not empty
        if(images.isEmpty()){
            throw new IllegalArgumentException("images must not be empty");
        }
        // check if all images have the same size
        var firstImage = images.get(0);
        for(var image : images){
            if(image.getWidth() != firstImage.getWidth() || image.getHeight() != firstImage.getHeight()){
                throw new IllegalArgumentException("All images must have the same size");
            }
        }
    }

    @Override
    public Image getImage(float t) {
        if(firstAsked){
            startT = t;
            firstAsked = false;
        }
        return images.get((int)(((t - startT) % period / period) * images.size()));
    }

    @Override
    public int getWidth() {
        return (int)images.get(0).getWidth();
    }

    @Override
    public int getHeight() {
        return (int)images.get(0).getHeight();
    }
}
