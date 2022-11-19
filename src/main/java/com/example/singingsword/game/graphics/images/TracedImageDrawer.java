package com.example.singingsword.game.graphics.images;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.example.singingsword.game.graphics.GraphicsEngine.FPS;

// works very bad on fixing and when suddenly stopped drawing (remove trace)
public class TracedImageDrawer implements ImageDrawer {
    private final ImageDrawer imageDrawer;
    private final int traceLength;
    private final int traceDelay;
    private int traceDelayCounter = 0;
    private final Deque<TraceState> trace = new ArrayDeque<>();

    private static class TraceState {
        float x;
        float y;
        float t;
        float rotate;

        TraceState(float x, float y, float t, float rotate) {
            this.x = x;
            this.y = y;
            this.t = t;
            this.rotate = rotate;
        }
    }

    ;

    public TracedImageDrawer(ImageDrawer imageDrawer, float traceLength, float traceDelay) {
        this.imageDrawer = imageDrawer;
        this.traceDelay = (int) (traceDelay * FPS);
        this.traceLength = (int) (traceLength / this.traceDelay * FPS);

    }

    public TracedImageDrawer(ImageDrawer imageDrawer, float traceLength) {
        this(imageDrawer, traceLength, 1f / FPS);
    }


    @Override
    public void drawImage(GraphicsContext gc, float x, float y, float t) {
        this.drawImage(gc, x, y, t, 0);
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
    public void drawImage(GraphicsContext gc, float x, float y, float t, float rotate) {
        float oldOpacity = (float) gc.getGlobalAlpha();
        float opacity = (float) (traceDelay - traceDelayCounter - 1) / traceDelay / traceLength;
        for (var state : trace) {
            gc.setGlobalAlpha(opacity * oldOpacity);
            opacity += 1f / traceLength;
            imageDrawer.drawImage(gc, state.x, state.y, state.t, state.rotate);
        }
        gc.setGlobalAlpha(oldOpacity);
        if (++traceDelayCounter >= traceDelay) {
            traceDelayCounter = 0;
            trace.addLast(new TraceState(x, y, t, rotate));
            if (trace.size() > traceLength+1) {
                trace.removeFirst();
            }
        }
        imageDrawer.drawImage(gc, x, y, t, rotate);
    }

    @Override
    public ImageDrawer fix(float t) {
        return new TracedImageDrawer(imageDrawer.fix(t), (float) traceLength * traceDelay / FPS, (float) traceDelay / FPS);
    }

    @Override
    public List<? extends ImageDrawer> fixDivided(float t) {
        return imageDrawer.fixDivided(t).stream().map(x -> new TracedImageDrawer(x, (float) traceLength * traceDelay / FPS, (float) traceDelay / FPS)).toList();
    }
}
