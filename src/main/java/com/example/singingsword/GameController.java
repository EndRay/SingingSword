package com.example.singingsword;

import com.example.singingsword.sound.PitchExtractor;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import javax.sound.sampled.*;

import static com.example.singingsword.sound.Sound.SAMPLE_RATE;
import static java.lang.Math.*;

public class GameController {
    @FXML
    private Canvas canvas;

    private static final Image swordImage = new Image(GameController.class.getResource("images/sword.png").toString(), 128, 128, true, false);
    private static final Image enemyImage = new Image(GameController.class.getResource("images/enemy.png").toString(), 128, 128, true, false);
    private static final Image backgroundImage = new Image(GameController.class.getResource("images/background.png").toString(), 960, 720, true, false);

    private static final float backgroundMovingSpeed = 40f;


    final private float minFrequency = 100;
    final private float maxFrequency = 800;

    final private FloatProperty frequencyProperty = new SimpleFloatProperty();
    final private BooleanProperty isSingingProperty = new SimpleBooleanProperty();

    final private FloatProperty swordPositionProperty = new SimpleFloatProperty();

    public void initialize() {
        Thread soundParsingThread = new Thread(() -> {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
            TargetDataLine line;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                    format); // format is an AudioFormat object
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
            }
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                PitchExtractor pitchExtractor = new PitchExtractor();
                while (true) {
                    byte[] data = new byte[1024];
                    int numBytesRead = line.read(data, 0, data.length);
                    pitchExtractor.feedRawData(data, numBytesRead);
                    if (pitchExtractor.isRecognizable()) {
                        Platform.runLater(() -> {
                            frequencyProperty.set(pitchExtractor.getPitch());
                            isSingingProperty.set(true);
                        });
                    } else {
                        Platform.runLater(() -> isSingingProperty.set(false));
                    }
                }
            } catch (LineUnavailableException ex) {
                System.out.println("Line unavailable");
            }
        });
        soundParsingThread.setDaemon(true);
        soundParsingThread.start();

        frequencyProperty.addListener(((observable, oldValue, newValue) -> {
            float value = (float) (log(newValue.floatValue() / minFrequency) / log(maxFrequency / minFrequency));
            value = max(0, min(1, value));
            swordPositionProperty.set(value);
        }));

        GraphicsContext gc = canvas.getGraphicsContext2D();

        final long startNanoTime = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                float t = (now - startNanoTime) / 1000000000f;
                float backgroundPos = (t * backgroundMovingSpeed)%((float)canvas.getWidth());
                gc.drawImage(backgroundImage, -backgroundPos, 0);
                gc.drawImage(backgroundImage, -backgroundPos + canvas.getWidth(), 0);
                gc.drawImage(swordImage, 0, (1-swordPositionProperty.floatValue()) * (canvas.getHeight() - swordImage.getHeight()));

            }
        };
        timer.start();
    }
}