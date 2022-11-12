package com.example.singingsword.sound;

import javafx.beans.property.FloatProperty;

import java.util.ArrayList;
import java.util.List;

import static com.example.singingsword.sound.FFT.ifft;
import static com.example.singingsword.sound.Sound.SAMPLE_RATE;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class PitchExtractor {
    private final static int blockSize = 16384;
    private final static int windowSize = 2048;
    private List<Float> samples = new ArrayList<>();
    private final Complex[] complex = new Complex[blockSize];
    private float pitch = 0;
    private float recognizability = 0;

    private final float recognizabilityDecreaseSpeed = 0.4f;

    private final float minParsableFrequency = 70;

    private final float minPeakAmplitude = 2f; // per second
    private final int minPeakCount = 1;
    private final int checkPeaks = 4;
    private final float maxDeviation = 0.2f;
    private final float minPeakDistance = 0.1f; // in coef of frequency

    public PitchExtractor() {
    }

    private static class Peak {
        public float frequency;
        public float amplitude;

        public Peak(float frequency, float amplitude) {
            this.frequency = frequency;
            this.amplitude = amplitude;
        }
    }

    private void extractPitch() {
        while (samples.size() >= blockSize) {
            for (int i = 0; i < blockSize; i++) {
                complex[i] = new Complex(samples.get(i), 0);
            }
            samples = samples.subList(windowSize, samples.size());
            ifft(complex);
            List<Peak> peaks = new ArrayList<>();
            for (int i = 1; i < blockSize / 2 - 1; ++i) {
                float freq = (float) i * SAMPLE_RATE / blockSize;
                if (freq < minParsableFrequency || !peaks.isEmpty() && peaks.get(peaks.size() - 1).frequency * (1 + minPeakDistance) > freq) {
                    continue;
                }
                if (complex[i].abs() > minPeakAmplitude && complex[i].abs() > complex[i - 1].abs() && complex[i].abs() > complex[i + 1].abs()) {
                    peaks.add(new Peak(freq, (float) complex[i].abs()));
                }
            }
            peaks = peaks.stream()
                    .sorted((x, y) -> Float.compare(y.amplitude, x.amplitude))
                    .filter(x -> x.amplitude > minPeakAmplitude).toList();
            peaks = peaks.stream()
                    .skip(max(0, peaks.size() - checkPeaks))
                    .sorted((x, y) -> Float.compare(x.frequency, y.frequency)).toList();
            System.out.println(peaks.stream().map(x -> "(" + x.frequency + ", " + x.amplitude + ")").toList());
            if (peaks.size() < minPeakCount) {
                recognizability = max(0, recognizability - recognizabilityDecreaseSpeed * windowSize / blockSize);
                continue;
            }
            float fundamental = peaks.get(0).frequency;
            boolean notHarmonic = false;
            for (Peak peak : peaks) {
                float deviation = min(peak.frequency % fundamental, fundamental - (peak.frequency % fundamental)) / fundamental;
                if(deviation > maxDeviation){
                    notHarmonic = true;
                    break;
                }
            }
            if(notHarmonic){
                System.out.println("BAD");
                recognizability = max(0, recognizability - recognizabilityDecreaseSpeed * windowSize / blockSize);
                continue;
            }
            System.out.println("GOOD" + fundamental);
            pitch = fundamental;
            recognizability = 1;
        }
    }

    public void feedRawData(byte[] data, int numBytesRead) {
        for (int i = 0; i < numBytesRead; i += 2) {
            samples.add((float) ((data[i + 1] & 0xff) | (data[i] << 8)) / (1 << 8));
        }
        extractPitch();
    }

    public boolean isRecognizable() {
        return recognizability > 0;
    }

    public float getRecognizability() {
        return recognizability;
    }

    public float getPitch() {
        return pitch;
    }
}
