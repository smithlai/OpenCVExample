package com.example.smith.opencvexample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static int EDGE_DETECT_REQUEST_CODE = 1;
    private static int CAMERA_TEST_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        // Button to call OpenCV Camera Activity
        Button cameraInit = (Button) findViewById(R.id.cameraInit);
        Button edgedetection = (Button) findViewById(R.id.edgedetection);

        cameraInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission(CAMERA_TEST_REQUEST_CODE);

            }
        });
        edgedetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission(EDGE_DETECT_REQUEST_CODE);
            }
        });
    }

    private void checkCameraPermission(final int requestcode){
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);

        //沒有權限時
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //如果使用者第二次點擊功能呼叫權限視窗，就會跳出shouldShowRequestPermissionRationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                //創建Dialog解釋視窗
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("單純使用在拍照功能，如果您不給我相機的權限，您將無法使用此功能")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, requestcode);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, requestcode);
            }
        } else {
            Toast.makeText(MainActivity.this, "已經拿到權限囉!" , Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(),EdgeDetection.class);
            startActivity(i);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //假如允許了
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //do something
            Toast.makeText(this, "已經拿到CAMERA權限囉!", Toast.LENGTH_SHORT).show();
            if (requestCode == EDGE_DETECT_REQUEST_CODE) {
                Intent i = new Intent(getApplicationContext(),EdgeDetection.class);
                startActivity(i);
            }
        }
        //假如拒絕了
        else {
            //do something
            Toast.makeText(this, "CAMERA權限FAIL", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
