package com.pointer.wave.easyship.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


public class ROTAUpdateManager {
    private String TAG = "ROTAUpdateManager";
    //UpdateEngine mUpdateEngine;
    private final UpdateParser.ParsedUpdate parsedUpdate;
    private static String updateFilePath = "/sdcard/update.zip";
    private static final String REBOOT_REASON = "reboot-ab-update";

    private Context mContext;
    private Handler mHandler;
    private PowerManager mPowerManager;

    public ROTAUpdateManager(Context context, Handler handler, String path) throws IOException {
        //mUpdateEngine = new UpdateEngine();
        this.mContext = context;
        this.mHandler = handler;
        updateFilePath = path;

        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        parsedUpdate = UpdateParser.parse(new File(updateFilePath));

    }

    public void startUpdateSystem() throws Exception {
        Class<?> aClass = Class.forName("android.os.UpdateEngine");
        @SuppressLint("PrivateApi") Class<?> callClass = Class.forName("android.osnouse.UpdateEngineCallback");
        Constructor<?> constructor = aClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object mUpdateEngine = constructor.newInstance();//UpdateEngine mUpdateEngine = new UpdateEngine();
        //mUpdateEngine.bind(mUpdateEngineCallback);
        Method bindMethod = aClass.getDeclaredMethod("bind", callClass, Handler.class);
        bindMethod.setAccessible(true);// 绑定callback
        bindMethod.invoke(mUpdateEngine, new UpdateEngineCallback() {
            @Override
            public void onStatusUpdate(int status, float percent) {
                Log.d(TAG, "onStatusUpdate status = " + status + "; percent = " + percent);
                handleStatusUpdate(status, percent);
            }

            @Override
            public void onPayloadApplicationComplete(int errorCode) {
                Log.e(TAG, "onPayloadApplicationComplete errorCode = " + errorCode);
                handlePayloadApplicationComplete(errorCode);
            }
        }, mHandler);
        //mUpdateEngine.applyPayload(String url, long offset, long size, String[] headerKeyValuePairs)
        Method payloadMethod = aClass.getDeclaredMethod("applyPayload", String.class,
                long.class, long.class, String[].class);// 进行升级
        payloadMethod.setAccessible(true);
        payloadMethod.invoke(mUpdateEngine, parsedUpdate.mUrl,
                parsedUpdate.mOffset, parsedUpdate.mSize, parsedUpdate.mProps);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private void handleStatusUpdate(int status, float percent) {
        switch (status) {
            case UpdateStatusConstants.UPDATE_AVAILABLE:
                break;
            case UpdateStatusConstants.VERIFYING:
                showToast("update engine is verifying an update");
                break;
            case UpdateStatusConstants.DOWNLOADING:
            case UpdateStatusConstants.FINALIZING:
                int progress = Float.valueOf(percent * 100).intValue();
                break;
            case UpdateStatusConstants.UPDATED_NEED_REBOOT:
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPowerManager.reboot(REBOOT_REASON);
                    }
                }, 2000);
                break;
        }
    }

    private void handlePayloadApplicationComplete(int errorCode) {
        switch (errorCode) {
            case ErrorCodeConstants.SUCCESS:

                break;
            case ErrorCodeConstants.FILESYSTEM_COPIER_ERROR:
                showToast("filesystem copier error");
                break;
            case ErrorCodeConstants.POST_INSTALL_RUNNER_ERROR:
                showToast("an error in running post-install hooks.");
                break;
            case ErrorCodeConstants.PAYLOAD_MISMATCHED_TYPE_ERROR:
                showToast("a mismatching payload");
                break;
            case ErrorCodeConstants.INSTALL_DEVICE_OPEN_ERROR:
                showToast("an error in opening devices");
                break;
            case ErrorCodeConstants.KERNEL_DEVICE_OPEN_ERROR:
                showToast("an error in opening kernel device");
                break;
            case ErrorCodeConstants.DOWNLOAD_TRANSFER_ERROR:
                showToast("an error in fetching the payload");
                break;
            case ErrorCodeConstants.PAYLOAD_HASH_MISMATCH_ERROR:
                showToast(" a mismatch in payload hash");
                break;
            case ErrorCodeConstants.PAYLOAD_SIZE_MISMATCH_ERROR:
                showToast("a mismatch in payload size");
                break;
            case ErrorCodeConstants.DOWNLOAD_PAYLOAD_VERIFICATION_ERROR:
                showToast("failing to verify payload signatures");
                break;
            case ErrorCodeConstants.NOT_ENOUGH_SPACE:
                showToast("there is not enough space on the device to apply the update");
                break;
        }
    }

    public static final class UpdateStatusConstants {
        /**
         * Update status code: update engine is in idle state.
         */
        public static final int IDLE = 0;

        /**
         * Update status code: update engine is checking for update.
         */
        public static final int CHECKING_FOR_UPDATE = 1;

        /**
         * Update status code: an update is available.
         */
        public static final int UPDATE_AVAILABLE = 2;

        /**
         * Update status code: update engine is downloading an update.
         */
        public static final int DOWNLOADING = 3;

        /**
         * Update status code: update engine is verifying an update.
         */
        public static final int VERIFYING = 4;

        /**
         * Update status code: update engine is finalizing an update.
         */
        public static final int FINALIZING = 5;

        /**
         * Update status code: an update has been applied and is pending for
         * reboot.
         */
        public static final int UPDATED_NEED_REBOOT = 6;

        /**
         * Update status code: update engine is reporting an error event.
         */
        public static final int REPORTING_ERROR_EVENT = 7;

        /**
         * Update status code: update engine is attempting to rollback an
         * update.
         */
        public static final int ATTEMPTING_ROLLBACK = 8;

        /**
         * Update status code: update engine is in disabled state.
         */
        public static final int DISABLED = 9;
    }

    public static final class ErrorCodeConstants {
        /**
         * Error code: a request finished successfully.
         */
        public static final int SUCCESS = 0;
        /**
         * Error code: a request failed due to a generic error.
         */
        public static final int ERROR = 1;
        /**
         * Error code: an update failed to apply due to filesystem copier
         * error.
         */
        public static final int FILESYSTEM_COPIER_ERROR = 4;
        /**
         * Error code: an update failed to apply due to an error in running
         * post-install hooks.
         */
        public static final int POST_INSTALL_RUNNER_ERROR = 5;
        /**
         * Error code: an update failed to apply due to a mismatching payload.
         */
        public static final int PAYLOAD_MISMATCHED_TYPE_ERROR = 6;
        /**
         * Error code: an update failed to apply due to an error in opening devices.
         */
        public static final int INSTALL_DEVICE_OPEN_ERROR = 7;
        /**
         * Error code: an update failed to apply due to an error in opening kernel device.
         */
        public static final int KERNEL_DEVICE_OPEN_ERROR = 8;
        /**
         * Error code: an update failed to apply due to an error in fetching the payload.
         */
        public static final int DOWNLOAD_TRANSFER_ERROR = 9;
        /**
         * Error code: an update failed to apply due to a mismatch in payload hash.
         */
        public static final int PAYLOAD_HASH_MISMATCH_ERROR = 10;

        /**
         * Error code: an update failed to apply due to a mismatch in payload size.
         */
        public static final int PAYLOAD_SIZE_MISMATCH_ERROR = 11;

        /**
         * Error code: an update failed to apply due to failing to verify payload signatures.
         */
        public static final int DOWNLOAD_PAYLOAD_VERIFICATION_ERROR = 12;

        /**
         * Error code: an update failed to apply due to a downgrade in payload timestamp.
         */
        public static final int PAYLOAD_TIMESTAMP_ERROR = 51;

        /**
         * Error code: an update has been applied successfully but the new slot
         * hasn't been set to active.
         */
        public static final int UPDATED_BUT_NOT_ACTIVE = 52;

        /**
         * Error code: there is not enough space on the device to apply the update. User should
         * be prompted to free up space and re-try the update.
         */
        public static final int NOT_ENOUGH_SPACE = 60;

        /**
         * Error code: the device is corrupted and no further updates may be applied.
         */
        public static final int DEVICE_CORRUPTED = 61;
    }
}