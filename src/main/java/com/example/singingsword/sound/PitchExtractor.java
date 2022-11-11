package com.example.singingsword.sound;

import java.util.ArrayList;
import java.util.List;

import static com.example.singingsword.sound.FFT.ifft;
import static com.example.singingsword.sound.Sound.SAMPLE_RATE;

public class PitchExtractor {
    private final static int blockSize = 16384;
    private final static int windowSize = 2048;
    private List<Float> samples = new ArrayList<>();
    private final Complex[] complex = new Complex[blockSize];
    private float pitch = 0;
    private boolean recognizable = false;

    public PitchExtractor(){}

    private void extractPitch(){
        while(samples.size() >= blockSize){
            for(int i = 0; i < blockSize; i++){
                complex[i] = new Complex(samples.get(i), 0);
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
            if(max > 0.3) {
                pitch = (float) maxIndex * SAMPLE_RATE / blockSize;
                recognizable = true;
            }
            else{
                recognizable = false;
            }
            samples = samples.subList(windowSize, samples.size());
        }
    }

    public void feedRawData(byte[] data, int numBytesRead) {
        for (int i = 0; i < numBytesRead; i += 2) {
            samples.add((float) ((data[i + 1] & 0xff) | (data[i] << 8)) / (1 << 8));
        }
        extractPitch();
    }

    public boolean isRecognizable(){
        return recognizable;
    }

    public float getPitch(){
        return pitch;
    }
}
