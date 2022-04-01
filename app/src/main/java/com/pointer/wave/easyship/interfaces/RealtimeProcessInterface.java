package com.pointer.wave.easyship.interfaces;

import com.pointer.wave.easyship.utils.RealtimeProcess;

public interface RealtimeProcessInterface {
    void onNewStdoutListener(RealtimeProcess.MSG msg);

    void onNewStderrListener(RealtimeProcess.ErrorMSG errorMSG);

    void onProcessFinish(int resultCode);
}
