package com.joppu.webcam;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class MultipleVideoCapture {

    private SimpleDateFormat simpleDateFormat;

    private static final MultipleVideoCapture multipleVideoCapture = new MultipleVideoCapture();

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            List<Integer> devices = new ArrayList<Integer>();

            for (String s : args) {
                devices.add(Integer.parseInt(s));
            }

            MultipleVideoCapture.getInstance().start(devices);
        }
    }

    private MultipleVideoCapture(){
        //For singleton. Effective Java.
    }

    public static MultipleVideoCapture getInstance(){
        return multipleVideoCapture;
    }

    private void start(List<Integer> devices){
        init();

        for(int device: devices){
            captureFrame(device);
            //sleep(1000);
        }

    }

    private void init() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        //TODO: setup directory strucute and other misc initialization tasks

        simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");


    }

    private Mat captureFrame(int device) {
        log("INFO Capturing frame...");
        VideoCapture camera = new VideoCapture(device);
        camera.open(device);

        //camera0.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 320);
        //camera0.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 240);

        if (!camera.isOpened()) {
            log("ERROR Could not open camera: " + device);
            return null;
        }

        log("INFO Opened camera: " + device);

        Mat frame = new Mat();
        //camera1.read(frame1);
        camera.retrieve(frame);
        camera.release();

        if (frame != null) {
            log(String.format("INFO Captured Frame from Camera%s: %sx%s", device, frame.width(), frame.height()));

            //Imgproc.cvtColor(frame, frameGS, Imgproc.COLOR_RGB2GRAY);

            writeFrameToImage(frame, device);

            log("INFO Done capturing frame.");
        }

        return frame;
    }

    private void sleep(long seconds){
        try{
            Thread.sleep(seconds);
        }catch(InterruptedException e){
            //Do Nothing
        }
    }

    private void log(String message){
        System.out.println(String.format("%s %s", simpleDateFormat.format(new Date()), message));
    }

    private void writeFrameToImage(Mat frame, int device) {
        String imagePath = getImagePath(device);
        Highgui.imwrite(imagePath, frame);
        log(String.format("INFO Wrote frame from camera%s to image: %s", device, imagePath));
    }

    private String getImagePath(int device) {
        return String.format("img/camera%s/%s.jpg", device, new Date().getTime());
    }
}
