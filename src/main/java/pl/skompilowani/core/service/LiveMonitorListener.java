package pl.skompilowani.core.service;

import pl.skompilowani.core.model.BlockDTO;

public interface LiveMonitorListener {
    void onMonitorStart();
    void onNewBlockProcessed(BlockDTO block);
    boolean shouldStop();
    void onMonitorStopped();
}