package pl.skompilowani.core.service;

public interface ProgressListener {
    void onProgress(int current, int total, String message);
    void onProgressComplete();
}