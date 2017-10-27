#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

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