package com.dds.core.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dds.core.util.ActivityStackManager;

public class BaseActivity extends AppCompatActivity {

    private Activity               activity;
    private MediaProjectionManager mMediaProjectionManage;
    public static final int    RECORD_REQUEST_CODE = 109;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 添加Activity到堆栈
        ActivityStackManager.getInstance().onCreated(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        ActivityStackManager.getInstance().onDestroyed(this);
        super.onDestroy();
    }


    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                    Manifest.permission.RECORD_AUDIO
            }, 1);

        }
        return false;
    }

    public void requestRecording(Activity activity, int requestCode) {

        this.activity = activity;
        mMediaProjectionManage = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mMediaProjectionManage.createScreenCaptureIntent();
        activity.startActivityForResult(captureIntent, requestCode);

    }
}
