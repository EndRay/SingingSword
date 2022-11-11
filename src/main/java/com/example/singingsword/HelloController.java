package com.example.singingsword;

import com.example.singingsword.sound.PitchExtractor;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javax.sound.sampled.*;

import static com.example.singingsword.sound.Sound.SAMPLE_RATE;

public class HelloController {
    @FXML
    private Label welcomeText;

    final private FloatProperty frequencyProperty = new SimpleFloatProperty();
    final private BooleanProperty isSingingProperty = new SimpleBooleanProperty();

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

        isSingingProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                welcomeText.setText("o={=====>");
            } else {
                welcomeText.setText(".______.");
                welcomeText.setTranslateY(0);
            }
        });
        frequencyProperty.addListener((observable, oldValue, newValue) -> {
            if (isSingingProperty.get()) {
                welcomeText.setTranslateY(-(newValue.floatValue() - 120) / 5);
            }
        });
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}