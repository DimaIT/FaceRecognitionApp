package by.tolpekin.recognition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.RadioButton;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static by.tolpekin.recognition.Utils.resizeTwiceSmaller;
import static org.opencv.videoio.Videoio.CAP_PROP_FPS;

public class IndexController {

    private static final int CAMERA_ID = 0;

    @FXML
    private Button button;

    @FXML
    private ImageView mainFrame;

    @FXML
    private ImageView grayFxFrame;

    @FXML
    private ImageView redFxFrame;

    @FXML
    private RadioButton radioRed;
    @FXML
    private RadioButton radioBlue;
    @FXML
    private RadioButton radioGreen;

    private DetectionService detectionService;


    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;

    public void init() {
        detectionService = new DetectionService();
    }

    @FXML
    protected void toggleCamera(ActionEvent event) {
        if (!this.cameraActive) {
            startCamera();
        } else {
            stopCamera();
        }
    }

    private void startCamera() {
        this.capture.open(CAMERA_ID);

        if (this.capture.isOpened()) {
            this.cameraActive = true;

            double fps = this.capture.get(CAP_PROP_FPS);
            long interval = (long) Math.ceil(1000 / fps);

            Runnable frameGrabber = () -> {
                Mat frame = grabFrame();
                Image imageToShow = Utils.mat2Image(frame);
                updateImageView(mainFrame, imageToShow);
            };

            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(frameGrabber, 0, interval, TimeUnit.MILLISECONDS);

            this.button.setText("Stop Camera");
        } else {
            System.err.println("Impossible to open the camera connection...");
        }
    }

    private void stopCamera() {
        this.cameraActive = false;
        this.button.setText("Start Camera");
        this.stopStreaming();
    }

    private Mat grabFrame() {
        Mat frame = new Mat();
        if (!this.capture.isOpened()) {
            return frame;
        }

        try {
            this.capture.read(frame);

            Mat redFrame = getThirdFrame(frame);
            detectionService.detectAndDisplay(redFrame);
            Image redImageToShow = Utils.mat2Image(resizeTwiceSmaller(redFrame));
            updateImageView(redFxFrame, redImageToShow);

            Mat grayFrame = detectionService.destratureAndEqualize(frame);
            detectionService.detectAndDisplay(grayFrame);
            Image grayImageToShow = Utils.mat2Image(resizeTwiceSmaller(grayFrame));
            updateImageView(grayFxFrame, grayImageToShow);

            detectionService.detectAndDisplay(frame);

        } catch (Exception e) {
            System.err.println("Error during the image elaboration: " + e);
        }

        return frame;
    }

    private Mat getThirdFrame(Mat frame) {
        List<Mat> images = new ArrayList<>();
        Core.split(frame, images);

        if (radioRed.isSelected()) {
            return images.get(2);
        } else if (radioGreen.isSelected()) {
            return images.get(1);
        } else {
            return images.get(0);
        }
    }

    private void stopStreaming() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture: " + e);
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed() {
        this.stopStreaming();
    }

}