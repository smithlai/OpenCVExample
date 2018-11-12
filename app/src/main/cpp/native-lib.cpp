#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
using namespace std;

////MainActivity.stringFromJNI
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_smith_opencvexample_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

////EdgeDetection.detectEdges
extern "C"
JNIEXPORT void JNICALL Java_com_example_smith_opencvexample_EdgeDetection_detectEdges(
        JNIEnv*, jobject /* this */,
        jlong gray) {
    cv::Mat& edges = *(cv::Mat *) gray;
    cv::Canny(edges, edges, 50, 250,3,false);
}

/// <summary>
/// </summary>
/// <param name="currentFrame"></param>
/// <returns></returns>
extern "C"
JNIEXPORT void JNICALL Java_com_example_smith_opencvexample_EdgeDetection_detectSize(
        JNIEnv*, jobject /* this */,
        jlong imgPtr
)

{
    cv::Mat& matImg = *(cv::Mat *) imgPtr;
    //__android_log_print(ANDROID_LOG_ERROR, "LOG_TAG", "Need to print : aaaaaaaaaaaa1");
    cv::Mat hsvImg;
    cv::cvtColor(matImg, hsvImg, CV_BGR2HSV);
    cv::Mat mask;
    cv::inRange(hsvImg,cv::Scalar(145, 0, 0),cv::Scalar(185, 255, 255),mask);

    std::vector< std::vector<cv::Point> > contours; // Vector for storing contour
    vector<cv::Vec4i> hierarchy;

    cv::findContours(mask, contours, hierarchy, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE); // Find the contours in the image


    for (int i = 0; i < contours.size(); i++)
        cv::drawContours(matImg, contours, i, cv::Scalar(255, 0, 0, 255), 1);
}
/// <summary>
/// </summary>
/// <param name="currentFrame"></param>
/// <returns></returns>
extern "C"
JNIEXPORT void JNICALL Java_com_example_smith_opencvexample_EdgeDetection_markObjectRect(
        JNIEnv *env, jobject /* this */,
        jlong imgPtr,
        jdouble jthreshold,
        jobject rect
)

{
    cv::RNG rng(12345);
    int largest_area=0;
    cv::Rect bounding_rect;
    cv::Mat& matImg = *(cv::Mat *) imgPtr;
    double threshold = jthreshold;
    //    cv::Mat grayImg;
    cv::Mat grayImg;
    cv::cvtColor(matImg, grayImg, CV_BGR2GRAY);

    cv::Mat& edges = grayImg;
    //高於50的通通設定為255
    cv::threshold(grayImg, edges, threshold, 255, cv::THRESH_BINARY_INV);//findContours通常以黑色為背景，白色為物體，我們用INV反過來以在白紙上偵測黑色手機


    std::vector< std::vector<cv::Point> > contours; // Vector for storing contour
    vector<cv::Vec4i> hierarchy;

    cv::findContours(edges, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE); // Find the contours in the image

    for( size_t i = 0; i< contours.size(); i++ ) // iterate through each contour.
    {
        double area = contourArea( contours[i] );  //  Find the area of contour

        if( area > largest_area )
        {
            largest_area = area;
            bounding_rect = boundingRect( contours[i] ); // Find the bounding rectangle for biggest contour
        }
    }
    // Get the class of the input object
    jclass nameCls = env->GetObjectClass(rect);

    //Z->boolean,B->byte,C->char,S->short,I->int,J->long,F->float,D->double,Ljava/lang/String->Java Lib String
    // Get Field references
    jfieldID width_id = env->GetFieldID(nameCls, "width", "I");
    jfieldID height_id = env->GetFieldID(nameCls, "height", "I");
    jfieldID x_id = env->GetFieldID(nameCls, "x", "I");
    jfieldID y_id = env->GetFieldID(nameCls, "y", "I");


    // Get Method references
    //jmethodID method_id = env->GetMethodID(nameCls, "methodname", "(II)V"); //(II)V = void methodname(int,int)
    //env->->CallVoidMethod(jniEnv, nameCls, method_id,1,2);

    // Set fields for object
    env->SetIntField( rect, width_id, bounding_rect.width);
    env->SetIntField( rect, height_id, bounding_rect.height);
    env->SetIntField( rect, x_id, bounding_rect.x);
    env->SetIntField( rect, y_id, bounding_rect.y);

//    for (int i = 0; i < contours.size(); i++)
//        cv::drawContours(matImg, contours, i, cv::Scalar(255, 0, 0, 255), 1);
    cv::Scalar color2 = cv::Scalar( rng.uniform(0, 255), rng.uniform(0,255), rng.uniform(0,255) );
    //cv::drawContours( matImg, contours,largest_contour_index, color2, 2 ); // Draw the largest contour using previously stored index.
    rectangle( matImg, bounding_rect.tl(), bounding_rect.br(), color2, 2, 8, 0 );
}