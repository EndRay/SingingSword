package com.example.singingsword.game.graphics.images;

import com.example.singingsword.GameController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Objects;

import static java.lang.Math.*;

public class TextDrawer implements ImageDrawer {
    private String text;
    private final Color color;
    private final int fontSize;
    private final float sizeChangePeriod = 1.5f;
    private final float sizeChangeAmplitude = 0.05f;

    private boolean firstDraw = true;
    private float startT;

    private float opacity = 1f;

    public TextDrawer(String text, Color color, int fontSize) {
        this.text = text;
        this.color = color;
        this.fontSize = fontSize;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void drawImage(GraphicsContext gc, float x, float y, float t) {
        if (firstDraw) {
            startT = t;
            firstDraw = false;
        }

        float oldOpacity = (float) gc.getGlobalAlpha();
        gc.setGlobalAlpha(opacity * oldOpacity);

        gc.setFill(color);
        gc.setFont(
            Font.loadFont(Objects.requireNonNull(GameController.class.getResource("fonts/pixeloid-font/PixeloidMono.ttf")).toString(),
                    fontSize + cos((t - startT) * 2 * PI / sizeChangePeriod) * sizeChangeAmplitude * fontSize));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, x, y);

        gc.setGlobalAlpha(oldOpacity);
    }

    @Override
    public int getWidth() {
        return fontSize * text.length();
    }

    @Override
    public int getHeight() {
        return fontSize;
    }

    @Override
    public void setAlpha(float opacity) {
        this.opacity = opacity;
    }

    @Override
    public ImageDrawer fix(float t) {
        return this;
    }

    @Override
    public List<? extends ImageDrawer> fixDivided(float t) {
        return List.of(this);
    }
}
