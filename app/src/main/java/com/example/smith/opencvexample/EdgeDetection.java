package com.example.smith.opencvexample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

public class EdgeDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    /*JNI Function*/
    ////Java_com_example_smith_opencvexample_EdgeDetection_detectEdges
    public native void detectEdges(long matPtr);
    public native void detectSize(long matPtr);
    public native void markObjectRect(long matPtr, double threshhold, Rect rect);


    private static final String TAG = "EdgeDetection";
    private CameraBridgeViewBase cameraBridgeViewBase;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    cameraBridgeViewBase.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_opencv_camera);
        aaaa("Falcon",1,2,3,4);
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this); ////CameraBridgeViewBase.CvCameraViewListener2

    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Mat edges = inputFrame.gray();
        //detectEdges(edges.getNativeObjAddr());
        Mat origin = inputFrame.rgba();
        Mat copy = origin.clone();
        //detectSize(edges.getNativeObjAddr());

        Rect rect = new Rect(); //Rect object with data
        markObjectRect(origin.getNativeObjAddr(),60.0d, rect);

        long time= System.currentTimeMillis();


        return origin;
    }
    private  void aaaa(String name,int x,int y,int w, int h){
        try {
            long time= System.currentTimeMillis();
            String filename_base = name+Long.toString(time);
            String photoname = filename_base + ".jpg";
            String xmlname = filename_base + ".xml";
            File filexml = new File(getPublicDownloadStorageDir(name), xmlname);
            File filephoto = new File(getPublicDownloadStorageDir(name), photoname);
            FileWriter fw = new FileWriter(filexml);

            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);

//            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "annotation");
            xmlSerializer.startTag(null, "folder");
            xmlSerializer.text(name);
            xmlSerializer.endTag(null, "folder");
            xmlSerializer.startTag(null,"filename");
            xmlSerializer.text(photoname);
            xmlSerializer.endTag(null, "filename");


            xmlSerializer.startTag(null,"path");
            xmlSerializer.text(filephoto.getPath());
            xmlSerializer.endTag(null, "path");

            xmlSerializer.startTag(null,"source");
            xmlSerializer.startTag(null,"database");
            xmlSerializer.text("Unknown");
            xmlSerializer.endTag(null, "database");
            xmlSerializer.endTag(null, "source");

            xmlSerializer.startTag(null,"size");
            xmlSerializer.startTag(null,"width");
            xmlSerializer.text("222222222");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.startTag(null,"height");
            xmlSerializer.text("222222222");
            xmlSerializer.endTag(null, "height");
            xmlSerializer.startTag(null,"depth");
            xmlSerializer.text("3");
            xmlSerializer.endTag(null, "depth");
            xmlSerializer.endTag(null, "size");

            xmlSerializer.startTag(null,"segmented");
            xmlSerializer.text("222222222");
            xmlSerializer.endTag(null, "segmented");

            xmlSerializer.startTag(null,"object");
            xmlSerializer.startTag(null,"name");
            xmlSerializer.text(name);
            xmlSerializer.endTag(null, "name");

            xmlSerializer.startTag(null,"pose");
            xmlSerializer.text("Unspecified");
            xmlSerializer.endTag(null, "pose");
            xmlSerializer.startTag(null,"truncated");
            xmlSerializer.text("0");
            xmlSerializer.endTag(null, "truncated");
            xmlSerializer.startTag(null,"difficult");
            xmlSerializer.text("0");
            xmlSerializer.endTag(null, "difficult");
            xmlSerializer.startTag(null,"bndbox");
            xmlSerializer.startTag(null,"xmin");
            xmlSerializer.text("222222222");
            xmlSerializer.endTag(null, "xmin");
            xmlSerializer.startTag(null,"ymin");
            xmlSerializer.text("222222222");
            xmlSerializer.endTag(null, "ymin");
            xmlSerializer.startTag(null,"xmax");
            xmlSerializer.text("222222222");
            xmlSerializer.endTag(null, "xmax");
            xmlSerializer.startTag(null,"ymax");
            xmlSerializer.text("222222222");
            xmlSerializer.endTag(null, "ymax");
            xmlSerializer.endTag(null, "bndbox");

            xmlSerializer.endTag(null, "object");





            xmlSerializer.endTag(null, "annotation");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            String dataWrite = writer.toString();

            fw.write(dataWrite.toString());
            fw.close();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPublicDownloadStorageDir(String folder) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), folder);
        if (!file.mkdirs()) {
            Log.e("XXXX", "Directory not created");
        }
        return file;
    }
}