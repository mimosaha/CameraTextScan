package util.mimo.image.coupontextscan;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

import util.mimo.image.coupontextscan.camera.CameraSource;
import util.mimo.image.coupontextscan.camera.CameraSourcePreview;
import util.mimo.image.coupontextscan.camera.GraphicOverlay;
import util.mimo.image.coupontextscan.utilities.OcrDetectorProcessor;
import util.mimo.image.coupontextscan.utilities.OcrGraphic;

public class MainActivity extends AppCompatActivity implements
        OcrGraphic.ShowNumberCallback {

    private CameraSourcePreview cameraSourcePreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private CameraSource mCameraSource;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        cameraSourcePreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);

        createCameraSource();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    private void createCameraSource() {
        Context context = getApplicationContext();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new
                OcrDetectorProcessor(mGraphicOverlay, this));

        if (!textRecognizer.isOperational()) {

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show();
            }
        }

        mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024).setRequestedFps(2.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .build();
    }

    private void startCameraSource() throws SecurityException {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, code, 101);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                cameraSourcePreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void showNumber(String number) {
        Toast.makeText(this, number, Toast.LENGTH_SHORT).show();
    }
}
