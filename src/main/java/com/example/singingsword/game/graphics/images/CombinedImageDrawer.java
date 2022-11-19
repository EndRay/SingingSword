package com.example.singingsword.game.graphics.images;

import javafx.scene.canvas.GraphicsContext;

import java.util.List;

import static java.lang.Math.max;

public class CombinedImageDrawer implements ImageDrawer {
    private final List<ImageProvider> imageProviders;
    private final int width, height;
    private float opacity = 1f;
    private boolean isGlobalAlpha = true;

    public CombinedImageDrawer(List<ImageProvider> imageProviders) {
        this.imageProviders = imageProviders;
        int w = 0;
        int h = 0;
        for(var imageProvider : imageProviders){
            w = max(w, imageProvider.getWidth());
            h = max(h, imageProvider.getHeight());
        }
        width = w;
        height = h;
    }

    @Override
    public void drawImage(GraphicsContext gc, float x, float y, float t) {
        var oldOpacity = gc.getGlobalAlpha();
        if(!isGlobalAlpha)
            gc.setGlobalAlpha(opacity);
        for(var imageProvider : imageProviders) {
            var image = imageProvider.getImage(t);
            gc.drawImage(image, x - image.getWidth()/2, y - image.getHeight()/2);
        }
        if(!isGlobalAlpha)
            gc.setGlobalAlpha(oldOpacity);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
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

    @Override
    public List<FixedImageDrawer> fixDivided(float t){
        List<FixedImageDrawer> list = imageProviders.stream().map(x -> new SimpleImageDrawer(x).fix(t)).toList();
        for(var imageDrawer : list){
            if(!this.isGlobalAlpha())
                imageDrawer.setAlpha(this.opacity);
        }
        return list;
    }
}
