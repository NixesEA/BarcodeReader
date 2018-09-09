package com.nixesea.barcodereader;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements Detector.Processor, View.OnClickListener {

    private TextView textView;
    private SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private ImageButton flash;
    private ImageButton save;

    private Boolean isStartCamera;
    private String mCameraId;
    private Boolean isTorchOn = false;

    final String name_DB = "history.db";
    String last_content = "";

    private CameraManager mCameraManager;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //make fullscreen mod
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        textView = this.findViewById(R.id.textView);
        surfaceView = this.findViewById(R.id.surfaceView);

        flash = findViewById(R.id.flashlightButton);
        save = findViewById(R.id.saveContentButton);

        save.setOnClickListener(this);
        flash.setOnClickListener(this);
        textView.setOnClickListener(this);

        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        barcodeDetector.setProcessor(this);

        cameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1024, 720)
                .setAutoFocusEnabled(true)
                .setRequestedFps(25)
                .build();

        final Activity activity = this;
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 1024);
                        return;
                    }
                    isStartCamera = true;
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException ie) {
                    ie.printStackTrace();
                    Log.e("Camera start problem", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(Detector.Detections detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();

        if (barcodes.size() != 0) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < barcodes.size(); ++i) {
                sb.append(barcodes.valueAt(i).rawValue).append("\n");
            }
            textView.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(sb.toString());
                }
            });

            if (!last_content.equals(sb.toString())){
                last_content = sb.toString();

                long[] pattern = {0, 250, 75, 250};
                vibrator.vibrate(pattern, -1);
            }
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.flashlightButton:
//                switchFlash();
                Toast.makeText(getApplicationContext(), "Soon", Toast.LENGTH_SHORT).show();
                break;
            case R.id.saveContentButton:
                saveText();
                Intent intent_history = new Intent(this, HistoryActivity.class);
                startActivity(intent_history);
                break;
            case R.id.textView:
                String str = (String) textView.getText();
                String[] subStr;
                subStr = str.split(" ");
                for (String aSubStr : subStr) {
                    if(validateUrl(aSubStr)){
                        Uri address;
                        address = Uri.parse(aSubStr);
                        if(!aSubStr.equals("http") && !aSubStr.equals("https")){
                            address = Uri.parse("http://" + aSubStr);
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(intent);
                        break;
                    }
                }
                saveText();
                break;
        }
    }

    public boolean validateUrl(String address){
        return android.util.Patterns.WEB_URL.matcher(address).matches();
    }

    private void switchFlash() {
        if (isStartCamera) {
            isStartCamera = false;
            cameraSource.stop();
        }

        Boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.show();
        }
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, !isTorchOn);
                isTorchOn = !isTorchOn;
                if (isTorchOn) {
                    flash.setImageResource(R.mipmap.flash_icon);
                } else {
                    flash.setImageResource(R.mipmap.flashoff_icon);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Activity activity = this;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 1024);
            return;
        }
        try {
            isStartCamera = true;
            cameraSource.start(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveText() {
        if (textView.getText().equals("")) {
            return;
        }
        SQLiteDatabase myDB =
                openOrCreateDatabase(name_DB, MODE_PRIVATE, null);
        myDB.execSQL(
                "CREATE TABLE IF NOT EXISTS user (URI VARCHAR(200), time VARCHAR(40))");
        ContentValues row1 = new ContentValues();
        row1.put("URI", textView.getText().toString());

        String s = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        row1.put("time", s);
        myDB.insert("user", null, row1);
        myDB.close();

        Toast.makeText(this, "Text saved", Toast.LENGTH_SHORT).show();
    }
}
