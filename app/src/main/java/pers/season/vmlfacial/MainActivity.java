package pers.season.vmlfacial;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pers.season.vml.asm.VmlAsmInterface;
import pers.season.vml.statistics.regressor.RegressorSet;
import pers.season.vml.statistics.shape.ShapeModel;
import pers.season.vml.util.FaceDetector;
import pers.season.vml.util.ImUtils;

public class MainActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener {

    public static final String KEY_CAMERA_ID = "CAMERA_ID";

    private static final int RESOLUTION_WIDTH = 1920;
    private static final int RESOLUTION_HEIGHT = 1080;

    private static ShapeModel sm;
    private static FaceDetector fd;
    private static RegressorSet rs;
    private static VmlAsmInterface vai;
    private static Mat overlay;
    private static Mat[][] facePics;
    private static Mat[][] facePicMasks;


    private static final double PROCESS_SCALE = 4.0;

    CameraBridgeViewBase openCvCameraView;

    boolean btnShot = false;
    boolean showPoints = false;
    int style = 0;
    int frameCounting = 0;

    Mat tempPicOverlay;
    Mat tempPicOverlayMask;

    int cameraId;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    private void initializeOpenCVDependencies() {
        try {
            tempPicOverlay = new Mat();
            tempPicOverlayMask = new Mat();
            if (vai == null) {
                // Copy the resource into a temp file so OpenCV can load it
                InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();

                // initialize models
                sm = ShapeModel.load(ImUtils.loadMat(getResources().openRawResource(R.raw.shape_v)), ImUtils.loadMat(getResources().openRawResource(R.raw.shape_z_e)));
                fd = FaceDetector.load(mCascadeFile.getAbsolutePath());
                rs = RegressorSet.load(ImUtils.loadMat(getResources().openRawResource(R.raw.patch_76_size61)),
                        ImUtils.loadMat(getResources().openRawResource(R.raw.reference_shape)), new Size(61, 61));
                vai = VmlAsmInterface.load(sm, fd, rs);


                // layer of control buttons
                overlay = Mat.zeros(new Size(RESOLUTION_WIDTH, RESOLUTION_HEIGHT), CvType.CV_8UC4);
                // shot button
                Imgproc.circle(overlay, new Point(overlay.width() * 0.9, overlay.height() * 0.5), (int) (overlay.width() * 0.05), new Scalar(255, 255, 255, 255), -1);
                Imgproc.circle(overlay, new Point(overlay.width() * 0.9, overlay.height() * 0.5), (int) (overlay.width() * 0.055), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                // account button
                Imgproc.circle(overlay, new Point(overlay.width() * 0.89, overlay.height() * 0.18), (int) (overlay.width() * 0.015), new Scalar(255, 255, 255, 255), -1);
                Imgproc.ellipse(overlay, new Point(overlay.width() * 0.93, overlay.height() * 0.18), new Size(overlay.width() * 0.0375, overlay.width() * 0.02), 0, 90, 270, new Scalar(255, 255, 255, 255), -1);
                Imgproc.circle(overlay, new Point(overlay.width() * 0.89, overlay.height() * 0.18), (int) (overlay.width() * 0.015), new Scalar(0, 0, 0, 0), (int) (overlay.width() * 0.0025));
                // album button
                Imgproc.circle(overlay, new Point(overlay.width() * 0.90, overlay.height() * 0.82), (int) (overlay.width() * 0.03), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                Imgproc.circle(overlay, new Point(overlay.width() * 0.90 - overlay.height() * 0.02, overlay.height() * 0.82 - overlay.height() * 0.02), (int) (overlay.width() * 0.003), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                Imgproc.line(overlay, new Point(overlay.width() * 0.90, overlay.height() * 0.82),
                        new Point(overlay.width() * 0.93, overlay.height() * 0.82), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                Imgproc.line(overlay, new Point(overlay.width() * 0.90, overlay.height() * 0.82 + overlay.width() * 0.03),
                        new Point(overlay.width() * 0.90, overlay.height() * 0.82), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                // switch button
                Imgproc.circle(overlay, new Point(overlay.width() * 0.05, overlay.height() * 0.5), (int) (overlay.width() * 0.025), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                Imgproc.circle(overlay, new Point(overlay.width() * 0.05, overlay.height() * 0.5), (int) (overlay.width() * 0.020), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                Imgproc.circle(overlay, new Point(overlay.width() * 0.05, overlay.height() * 0.5), (int) (overlay.width() * 0.015), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                Imgproc.circle(overlay, new Point(overlay.width() * 0.05, overlay.height() * 0.5), (int) (overlay.width() * 0.010), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));
                Imgproc.circle(overlay, new Point(overlay.width() * 0.05, overlay.height() * 0.5), (int) (overlay.width() * 0.005), new Scalar(255, 255, 255, 255), (int) (overlay.width() * 0.0025));

                // load stickers
                facePics = new Mat[3][2];
                facePicMasks = new Mat[3][2];
                facePics[0][0] = Utils.loadResource(this, R.drawable.face_pic_01_01);
                facePics[0][1] = Utils.loadResource(this, R.drawable.face_pic_01_02);
                facePics[1][0] = Utils.loadResource(this, R.drawable.face_pic_02_01);
                facePics[1][1] = Utils.loadResource(this, R.drawable.face_pic_02_02);
                facePics[2][0] = Utils.loadResource(this, R.drawable.face_pic_03_01);
                facePics[2][1] = Utils.loadResource(this, R.drawable.face_pic_03_02);

                // generate sticker-mat and sticker-mask-mat
                for (int i1 = 0; i1 < facePics.length; i1++) {
                    for (int i2 = 0; i2 < facePics[i1].length; i2++) {
                        Mat pic = facePics[i1][i2];
                        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGBA2BGRA);
                        List<Mat> chs = new ArrayList<Mat>();
                        Core.split(pic, chs);

                        Mat mask = chs.get(3).clone();
                        mask.convertTo(mask, CvType.CV_8U);
                        facePicMasks[i1][i2] = mask;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading model data", e);
        }

        // And we are ready to go
        openCvCameraView.enableView();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        cameraId = getIntent().getIntExtra(KEY_CAMERA_ID, CameraBridgeViewBase.CAMERA_ID_FRONT);
        openCvCameraView = new JavaCameraView(this, cameraId);
        openCvCameraView.setCvCameraViewListener(this);
        setContentView(openCvCameraView);
        mGestureDetector = new GestureDetector(this, mGestureListener);
    }


    @Override
    public void onCameraViewStarted(int width, int height) {


    }


    @Override
    public void onCameraViewStopped() {
    }


    private void copyFacePicToImage(Mat src, Mat srcMask, Mat dst, RotatedRect rr) {
        Mat R = new Mat(2, 3, CvType.CV_32F);
        double scale = (rr.size.height * PROCESS_SCALE / src.height() + rr.size.width * PROCESS_SCALE / src.width()) * 1.2;
        double transX = rr.center.y * PROCESS_SCALE - src.height() / 2;
        double transY = rr.center.x * PROCESS_SCALE - src.width() / 2;
        double centerX = src.height() / 2;
        double centerY = src.width() / 2;
        double theta = rr.angle / 180 * Math.PI;
        double alpha = scale * Math.cos(theta);
        double beta = scale * Math.sin(theta);
        R.put(0, 0, alpha, beta, transX + (1 - alpha) * centerX - beta * centerY,
                -beta, alpha, transY + beta * centerX + (1 - alpha) * centerX);


        Imgproc.warpAffine(srcMask.t(), tempPicOverlayMask, R, dst.size());
        tempPicOverlayMask.convertTo(tempPicOverlayMask, CvType.CV_8U);
        Imgproc.warpAffine(src.t(), tempPicOverlay, R, dst.size());
        tempPicOverlay.copyTo(dst, tempPicOverlayMask);

    }


    @Override
    public Mat onCameraFrame(Mat aInputFrame) {
        if (cameraId == CameraBridgeViewBase.CAMERA_ID_FRONT)
            Core.flip(aInputFrame, aInputFrame, 1);
        Mat pic = new Mat();

        Imgproc.resize(aInputFrame, pic, new Size(aInputFrame.cols() / PROCESS_SCALE, aInputFrame.rows() / PROCESS_SCALE));
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_BGRA2GRAY);
        pic = pic.t();

        frameCounting++;

        if (frameCounting % 10 == 0 || vai.getTrackingFaceCount() == 0)
            vai.searchFace(pic, new Size(75, 75), 0.5);
        // vai.removeOverlapTracking(0.8);

        Mat allFeaturePts = vai.track(pic, new Size(21, 21), 0.20, 3, 4);
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_GRAY2BGR);
        if (showPoints) {
            for (int i = 0; i < allFeaturePts.cols(); i++) {
                for (int ii = 0; ii < allFeaturePts.rows() / 2; ii++) {
                    Point p = new Point(allFeaturePts.get(ii * 2, i)[0], allFeaturePts.get(ii * 2 + 1, i)[0]);
                    Point np = new Point(p.y * PROCESS_SCALE, p.x * PROCESS_SCALE);
                    Imgproc.circle(aInputFrame, np, 3, new Scalar(0, 255, 0), 5);
                }
            }
        }

        RotatedRect[] locations = vai.getLocation(allFeaturePts);
        for (RotatedRect rr : locations) {
            if (Math.abs(style) % (facePics.length + 1) == facePics.length)
                break;
            int sNo = Math.abs(style) % (facePics.length + 1);
            copyFacePicToImage(facePics[sNo][frameCounting / 2 % 2], facePicMasks[sNo][frameCounting / 2 % 2], aInputFrame, rr);
        }

        if (btnShot) {
            Mat resizedPhoto = new Mat();
            Imgproc.resize(aInputFrame, resizedPhoto, new Size(1440, 810));
            resizedPhoto = resizedPhoto.t();
            Bitmap tbm = Bitmap.createBitmap(resizedPhoto.width(), resizedPhoto.height(), Bitmap.Config.RGB_565);

            Utils.matToBitmap(resizedPhoto, tbm);
            VfUtils.saveBitmap(tbm, System.currentTimeMillis());
            btnShot = false;
            aInputFrame.setTo(new Scalar(255, 255, 255, 255));
        } else {
            Core.addWeighted(aInputFrame, 1, overlay, 0.8, 0, aInputFrame);
        }

        return aInputFrame;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Point p = new Point(event.getX(), event.getY());
            Point pShot = new Point(openCvCameraView.getWidth() * 0.9, openCvCameraView.getHeight() * 0.5);
            Point pAlbum = new Point(openCvCameraView.getWidth() * 0.9, openCvCameraView.getHeight() * 0.82);
            Point pAccount = new Point(openCvCameraView.getWidth() * 0.9, openCvCameraView.getHeight() * 0.18);
            Point pSwitch = new Point(openCvCameraView.getWidth() * 0.05, openCvCameraView.getHeight() * 0.50);
            if (Math.sqrt((p.x - pShot.x) * (p.x - pShot.x) + (p.y - pShot.y) * (p.y - pShot.y)) < openCvCameraView.getWidth() * 0.05) {
                btnShot = true;
            } else if (Math.sqrt((p.x - pSwitch.x) * (p.x - pSwitch.x) + (p.y - pSwitch.y) * (p.y - pSwitch.y)) < openCvCameraView.getWidth() * 0.03) {
                MainActivity.this.finish();
                Intent it = new Intent();
                if (cameraId == CameraBridgeViewBase.CAMERA_ID_FRONT)
                    it.putExtra(KEY_CAMERA_ID, CameraBridgeViewBase.CAMERA_ID_BACK);
                else
                    it.putExtra(KEY_CAMERA_ID, CameraBridgeViewBase.CAMERA_ID_FRONT);
                it.setClass(MainActivity.this, MainActivity.class);
                startActivity(it);
            } else if (Math.sqrt((p.x - pAlbum.x) * (p.x - pAlbum.x) + (p.y - pAlbum.y) * (p.y - pAlbum.y)) < openCvCameraView.getWidth() * 0.03) {
                Intent it = new Intent();
                it.setClass(MainActivity.this, AlbumActivity.class);
                startActivity(it);
            } else if (Math.sqrt((p.x - pAccount.x) * (p.x - pAccount.x) + (p.y - pAccount.y) * (p.y - pAccount.y)) < openCvCameraView.getWidth() * 0.03) {
                if (WorldVar.userName == null) {
                    Intent it = new Intent();
                    it.setClass(MainActivity.this, LoginActivity.class);
                    startActivity(it);
                } else {
                    Intent it = new Intent();
                    it.setClass(MainActivity.this, LogoutActivity.class);
                    startActivity(it);
                }
            }


        }
        return mGestureDetector.onTouchEvent(event);
    }

    private GestureDetector mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > 5000) {
                showPoints = !showPoints;
            } else if (velocityY > 3000) {
                style++;
            } else if (velocityY < -3000) {
                style--;
            }
            return false;
        }


    };


    @Override
    public void onResume() {
        super.onResume();

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);


    }


}