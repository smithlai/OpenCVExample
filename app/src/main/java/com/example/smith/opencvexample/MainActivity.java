package com.example.smith.opencvexample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static int CAMERA_REQUEST_CODE = 3;
    private static int STORAGE_REQUEST_CODE = 4;

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
                Intent i = new Intent(getApplicationContext(),OpenCVCamera.class);
                startActivity(i);

            }
        });
        edgedetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = ((EditText)findViewById(R.id.classifyname));
                String classifyname = editText.getText().toString().trim();
                if(classifyname.length() == 0) {
                    Toast.makeText(MainActivity.this, "You did not enter a classifier name", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = new Intent(getApplicationContext(),EdgeDetection.class);
                i.putExtra("classifyname",classifyname);
                startActivity(i);
            }
        });
        checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_REQUEST_CODE);
        checkPermission(android.Manifest.permission.CAMERA, CAMERA_REQUEST_CODE);
    }

    private void checkPermission(final String manifest_permission_type, final int requestcode) {
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, manifest_permission_type);
        String camera_warning = "需要相機權限攝影";
        String storage_warning = "需要儲存權限紀錄圖檔以及xml";
        String warning = "";
        switch (manifest_permission_type){
            case Manifest.permission.CAMERA:
                warning = camera_warning;
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                warning = storage_warning;
                break;
            default:
                return;
        }
        //沒有權限時
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //如果使用者第二次點擊功能呼叫權限視窗，就會跳出shouldShowRequestPermissionRationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, manifest_permission_type)) {
                //創建Dialog解釋視窗
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(warning)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{manifest_permission_type}, requestcode);
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
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{manifest_permission_type}, requestcode);
            }
        } else {
            //Toast.makeText(MainActivity.this, "已經拿到權限囉!" , Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //假如允許了
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "已經拿到權限囉!", Toast.LENGTH_SHORT).show();
        }
        //假如拒絕了
        else {
            Toast.makeText(this, "取得權限FAIL", Toast.LENGTH_SHORT).show();
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
