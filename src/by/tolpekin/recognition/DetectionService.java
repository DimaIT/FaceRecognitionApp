package by.tolpekin.recognition;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class DetectionService {
    private CascadeClassifier faceCascade = new CascadeClassifier();
    private CascadeClassifier eyesCascade = new CascadeClassifier();
    private long counter = 0;

    public DetectionService() {
        this.faceCascade.load("src/resources/lbpcascades/lbpcascade_frontalface.xml");
//        this.eyesCascade.load("src/resources/haarcascades/haarcascade_eye_tree_eyeglasses.xml");
        this.eyesCascade.load("src/resources/haarcascades/haarcascade_eye.xml");
//        this.eyesCascade.load("src/resources/haarcascades/haarcascade_frontalface_default.xml");
    }

    public Mat destratureAndEqualize(Mat frame) {
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        return grayFrame;
    }

    public void detectAndDisplay(Mat frame) {
        long startTime = System.currentTimeMillis();
        detectAndDisplayFace(frame);
        printTime(startTime, "First:");

        startTime = System.currentTimeMillis();
        detectAndDisplayEyes(frame);
        printTime(startTime, "Second:");
        counter++;
    }

    private void printTime(long start, String key) {
        if (counter % 50 == 0) {
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - start;
            System.out.println(key + " " + elapsedTime);
        }
    }

    public void detectAndDisplayFace(Mat frame) {
        MatOfRect faces = new MatOfRect();

        // compute minimum face size (20% of the frame height, in our case)
        int height = frame.rows();
        int faceHeight = Math.round(height * 0.2f);

        // detect faces
        this.faceCascade.detectMultiScale(frame, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                new Size(faceHeight, faceHeight), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
    }

    public void detectAndDisplayEyes(Mat frame) {
        MatOfRect eyes = new MatOfRect();

        // compute minimum face size (20% of the frame height, in our case)
        int height = frame.rows();
        int minHeight = Math.round(height * 0.15f);

        // detect faces
        this.eyesCascade.detectMultiScale(frame, eyes, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                new Size(minHeight, minHeight), new Size());

        eyes.toList().forEach(eyeRect ->
                Imgproc.rectangle(frame, eyeRect.tl(), eyeRect.br(), new Scalar(0, 0, 100), 3));

    }

    public void load(String classifierPath) {
        this.faceCascade.load(classifierPath);
    }
}
