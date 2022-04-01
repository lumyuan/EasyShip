package com.pointer.wave.easyship.core;

public abstract class UpdateEngineCallback {

    public abstract void onStatusUpdate(int status, float percent);

    public abstract void onPayloadApplicationComplete(int errorCode);
}