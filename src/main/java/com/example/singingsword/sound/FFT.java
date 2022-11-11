package com.example.singingsword.sound;

import static java.lang.Math.*;

public class FFT {
    public static void fft(Complex[] a){
        int N = a.length;
        if (N == 1) return;
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }
        // compute the FFT of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = a[2*k];
        }
        fft(even);
        // compute the FFT of odd terms
        Complex[] odd  = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            odd[k] = a[2*k + 1];
        }
        fft(odd);
        // combine
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * PI / N;
            Complex wk = new Complex(cos(kth), sin(kth));
            a[k]       = even[k].plus(wk.times(odd[k]));
            a[k + N/2] = even[k].minus(wk.times(odd[k]));
        }
    }

    // reverse fft
    public static void ifft(Complex[] a){
        int N = a.length;
        Complex[] b = new Complex[N];
        for (int i = 0; i < N; i++) {
            b[i] = a[i].conjugate();
        }
        fft(b);
        for (int i = 0; i < N; i++) {
            a[i] = b[i].conjugate();
        }
        for(int i = 0; i < N; i++){
            a[i] = a[i].scale(1.0/N);
        }
    }
}
