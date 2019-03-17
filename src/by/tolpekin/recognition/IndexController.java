package by.tolpekin.recognition;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static org.opencv.videoio.Videoio.CAP_PROP_FPS;

public class IndexController {

    private static final int CAMERA_ID = 0;

    @FXML
    private Button button;

    @FXML
    private ImageView currentFrame;


    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;

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
                updateImageView(currentFrame, imageToShow);
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
        } catch (Exception e) {
            System.err.println("Error during the image elaboration: " + e);
        }

        return frame;
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