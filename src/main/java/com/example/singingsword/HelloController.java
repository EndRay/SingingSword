package com.example.singingsword;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ObservableFloatValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Pair;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

import static com.example.singingsword.FFT.ifft;
import static java.util.stream.Collectors.toList;

public class HelloController {
    @FXML
    private Label welcomeText;

    private Thread soundParsingThread;

    final static private int SAMPLE_RATE = 44100;
    final private FloatProperty frequencyProperty = new SimpleFloatProperty();
    final private BooleanProperty isSingingProperty = new SimpleBooleanProperty();

    public void initialize() {
        soundParsingThread = new Thread(() -> {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            TargetDataLine line;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                    format); // format is an AudioFormat object
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
            }
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                List<Float> all = new ArrayList<>();
                line.start();
                final int blockSize = 16384;
                final int windowSize = 2048;
                Complex[] complex = new Complex[blockSize];
                while(true){
                    // reading from line
                    byte[] data = new byte[1024];
                    int numBytesRead = line.read(data, 0, data.length);
                    // add bytes from data to all
//                    for (int i = 0; i < numBytesRead; i+=2) {
//                        all.add((float) ((data[i] & 0xff) | (data[i+1] << 8)));
//                    }
                    for(int i = 0; i < numBytesRead; ++i)
                        all.add((float) data[i]);
                    while(all.size() >= blockSize){
                        for(int i = 0; i < blockSize; i++){
                            complex[i] = new Complex(all.get(i), 0);
                        }
                        ifft(complex);
                        float max = 0;
                        int maxIndex = blockSize;
                        for(int i = 0; i < blockSize/2; ++i){
                            if(complex[i].abs() > max){
                                max = (float) complex[i].abs();
                                maxIndex = i;
                            }
                        }
                        if(max > 1) {
                            //System.out.println(maxIndex + " -> " + max);
                            int freq = maxIndex * SAMPLE_RATE / blockSize;
                            Platform.runLater(() -> {
                                frequencyProperty.set(freq);
                                isSingingProperty.set(true);
                            });
                        }
                        else{
                            Platform.runLater(() -> isSingingProperty.set(false));
                        }
                        all = all.subList(windowSize, all.size());

                    }
                }
            } catch (LineUnavailableException ex) {
                System.out.println("Line unavailable");
            }
        });
        soundParsingThread.setDaemon(true);
        soundParsingThread.start();

        isSingingProperty.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                welcomeText.setText("o={=====>");
            }
            else{
                welcomeText.setText(".______.");
                welcomeText.setTranslateY(0);
            }
        });
        frequencyProperty.addListener((observable, oldValue, newValue) -> {
            if(isSingingProperty.get()){
                welcomeText.setTranslateY(-(newValue.floatValue() - 120) / 5);
            }
        });
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}