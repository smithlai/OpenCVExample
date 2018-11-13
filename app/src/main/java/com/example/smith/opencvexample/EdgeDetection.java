package com.example.smith.opencvexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EdgeDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    /*JNI Function*/
    ////Java_com_example_smith_opencvexample_EdgeDetection_detectEdges
    public native void detectEdges(long matPtr);
    public native void detectSize(long matPtr);
    public native void markObjectRect(long matPtr, double threshhold,int padding, Rect rect);


    private static final String TAG = "EdgeDetection";
    private CameraBridgeViewBase cameraBridgeViewBase;
    private RelativeLayout mOverlay;
    private String mClassifyName = "Unknown";
    private ToggleButton mToggleCapture;
    private EditText mPaddingCapture;
    private EditText mThreshold;

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
        Intent i = getIntent();
        mClassifyName = i.getStringExtra("classifyname");
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_view);
//        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this); ////CameraBridgeViewBase.CvCameraViewListener2
        mOverlay = (RelativeLayout) findViewById(R.id.overlay);

        mToggleCapture = (ToggleButton) findViewById(R.id.toggle_capture);
        mPaddingCapture = (EditText) findViewById(R.id.padding_capture);
        mThreshold = (EditText) findViewById(R.id.threshold);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Get the preview size
        int previewWidth = cameraBridgeViewBase.getMeasuredWidth(),
                previewHeight = cameraBridgeViewBase.getMeasuredHeight();

        // Set the height of the overlay so that it makes the preview a square
        RelativeLayout.LayoutParams overlayParams = (RelativeLayout.LayoutParams) mOverlay.getLayoutParams();
        int x = previewHeight - previewWidth;
        if(x>0) //potrait
            overlayParams.height = x;
        else//landscape
            overlayParams.width = Math.abs(x);
        mOverlay.setLayoutParams(overlayParams);
        int size = Math.min(previewHeight , previewWidth);
        cameraBridgeViewBase.setMaxFrameSize(size ,size);
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

        //crop center
        Mat cropped = null;
        int shift =  origin.height() - origin.width();
        if( shift>0 ) { //potrait
            Rect roi = new Rect( 0 , shift/2, origin.width(),origin.width());
            cropped = new Mat(origin, roi);
        }else{ //landscape
            Rect roi = new Rect( -shift/2 , 0, origin.height(), origin.height());
            cropped = new Mat(origin, roi);
        }
        Mat resized = new Mat(600,600, CvType.CV_8UC3);
        Imgproc.resize(cropped, resized, resized.size()); //resize image

        Mat process = resized.clone();
        //detectSize(edges.getNativeObjAddr());

        Rect rect = new Rect(); //Rect object with data
        int padding = 0;
        try {
            padding = Math.abs(Integer.parseInt(mPaddingCapture.getText().toString()));
        }catch(Exception e){

        }
        double threshold = 60.0d;
        try {
            threshold = Math.abs(Double.parseDouble(mThreshold.getText().toString()));
        }catch(Exception e){

        }
        markObjectRect(process.getNativeObjAddr(), threshold,padding, rect);


        if(mToggleCapture.isChecked()) {
            if (rect.area() > 1000) {

                Bitmap bmp = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resized, bmp);


                Bitmap bmp2 = Bitmap.createBitmap(process.cols(), process.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(process, bmp2);

                savePicAndXml(mClassifyName, bmp, rect.x, rect.y, rect.width, rect.height, bmp2);
            }
        }
        Imgproc.resize(process, cropped, cropped.size()); //resize image to original center crop size and paste to frame
        cropped.copyTo(origin.rowRange(0, cropped.height()).colRange(0, cropped.width()));
        return origin;
    }

    private  void savePicAndXml(String name,Bitmap bmp,int x,int y,int w, int h, Bitmap bmp2){
        try {
            long time= System.currentTimeMillis();
            String filename_base = name+Long.toString(time);
            String picname = filename_base + ".jpg";
            String picname2 = filename_base + ".jpg";
            String xmlname = filename_base + ".xml";
            File filexml = new File(getPublicDownloadStorageDir(name), xmlname);
            File filepic = new File(getPublicDownloadStorageDir(name), picname);
            File checkfolder = new File(getPublicDownloadStorageDir(name), name+"_check");
            checkfolder.mkdirs();
            File filepic2 = new File(checkfolder, picname2);

//            FileWriter fw = new FileWriter(filexml);
//            StringWriter writer = new StringWriter();
            FileOutputStream fos_xml = new FileOutputStream(filexml);
            FileOutputStream fos_pic = new FileOutputStream(filepic);
            FileOutputStream fos_pic2 = new FileOutputStream(filepic2);

            XmlSerializer xmlSerializer = Xml.newSerializer();

            xmlSerializer.setOutput(fos_xml,"UTF-8");

//            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "annotation");
            xmlSerializer.startTag(null, "folder");
            xmlSerializer.text(name);
            xmlSerializer.endTag(null, "folder");
            xmlSerializer.startTag(null,"filename");
            xmlSerializer.text(picname);
            xmlSerializer.endTag(null, "filename");


            xmlSerializer.startTag(null,"path");
            xmlSerializer.text(filepic.getPath());
            xmlSerializer.endTag(null, "path");

            xmlSerializer.startTag(null,"source");
            xmlSerializer.startTag(null,"database");
            xmlSerializer.text("Unknown");
            xmlSerializer.endTag(null, "database");
            xmlSerializer.endTag(null, "source");

            xmlSerializer.startTag(null,"size");
            xmlSerializer.startTag(null,"width");
            xmlSerializer.text(Integer.toString(bmp.getWidth()));
            xmlSerializer.endTag(null, "width");
            xmlSerializer.startTag(null,"height");
            xmlSerializer.text(Integer.toString(bmp.getHeight()));
            xmlSerializer.endTag(null, "height");
            xmlSerializer.startTag(null,"depth");
            xmlSerializer.text("3");
            xmlSerializer.endTag(null, "depth");
            xmlSerializer.endTag(null, "size");

            xmlSerializer.startTag(null,"segmented");
            xmlSerializer.text("0");
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
            xmlSerializer.text(Integer.toString(x));
            xmlSerializer.endTag(null, "xmin");
            xmlSerializer.startTag(null,"ymin");
            xmlSerializer.text(Integer.toString(y));
            xmlSerializer.endTag(null, "ymin");
            xmlSerializer.startTag(null,"xmax");
            xmlSerializer.text(Integer.toString(x+w));
            xmlSerializer.endTag(null, "xmax");
            xmlSerializer.startTag(null,"ymax");
            xmlSerializer.text(Integer.toString(y+h));
            xmlSerializer.endTag(null, "ymax");
            xmlSerializer.endTag(null, "bndbox");

            xmlSerializer.endTag(null, "object");





            xmlSerializer.endTag(null, "annotation");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            fos_xml.flush();
            fos_xml.close();

//            String dataWrite = writer.toString();
//            fw.write(dataWrite.toString());
//            fw.close();

            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos_pic);
            fos_pic.flush();
            fos_pic.close();

            bmp2.compress(Bitmap.CompressFormat.JPEG, 50, fos_pic2);
            fos_pic2.flush();
            fos_pic2.close();

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