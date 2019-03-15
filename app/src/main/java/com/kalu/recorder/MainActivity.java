package com.kalu.recorder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.kalu.recorder.Bean.CameraSetting;
import com.kalu.recorder.Bean.RecordSetting;
import com.kalu.recorder.Bean.RenderSetting;
import com.kalu.recorder.GlRender.GlRenderImg;
import com.kalu.recorder.GlRender.GlRenderImgList;
import com.kalu.recorder.MediaRecord.RecordVideoAndAudioManager;
import com.kalu.recorder.RecorderHelper.RecordManageBase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements Handler.Callback {

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;
    private Button btn6;

    private static final int RequestCameraPermission = 12304;

    //view
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private RecordVideoAndAudioManager recorder;

    private final static int TargetLongWidth = 1920;
    private int TargetShortWidth = 1080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_live_text);
        this.btn3 = (Button) findViewById(R.id.btn3);
        this.btn2 = (Button) findViewById(R.id.btn2);
        this.btn1 = (Button) findViewById(R.id.btn1);
        this.btn4 = (Button) findViewById(R.id.btn4);
        this.btn5 = (Button) findViewById(R.id.btn5);
        this.btn6 = (Button) findViewById(R.id.btn6);
        this.surfaceView = findViewById(R.id.surface_view);
        RecordSetting recordSetting = new RecordSetting();
        CameraSetting cameraSetting = new CameraSetting();
        cameraSetting.fps = 30;
        cameraSetting.cameraPosition = 0;
        RenderSetting renderSetting = new RenderSetting();

        String s = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";
        File file = new File(s);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder = new RecordVideoAndAudioManager(this, file, recordSetting, cameraSetting, renderSetting, surfaceView);
        recorder.setCallBackEvent(new RecordManageBase.CallBackEvent() {
            @Override
            public void startRecordSuccess() {
                Log.e("main", "startRecordSuccess");
            }

            @Override
            public void onDuringUpdate(float time) {
                Log.e("main", "onDuringUpdate => time = " + time);
            }

            @Override
            public void stopRecordFinish(File file) {
                Log.e("main", "stopRecordFinish => path = " + file.getPath());
            }

            @Override
            public void recordError(String errorMsg) {
                Log.e("main", "recordError => msg = " + errorMsg);
            }

            @Override
            public void openCameraSuccess(int cameraPosition) {

                recorder.getRecordSetting().setVideoSetting(TargetShortWidth, TargetLongWidth,
                        recorder.getCameraManager().getRealFps() / 1000, RecordSetting.ColorFormatDefault);
                recorder.getRecordSetting().setVideoBitRate(3000 * 1024);
                recorder.switchOnBeauty(cameraPosition == 1);

                Log.e("main", "openCameraSuccess => cameraPosition = " + cameraPosition);
            }

            @Override
            public void openCameraFailure(int cameraPosition) {
                Log.e("main", "openCameraFailure => cameraPosition = " + cameraPosition);
            }

            @Override
            public void onVideoSizeChange(int width, int height) {
                Log.e("main", "onVideoSizeChange => width = " + width + ", height = " + height);
            }

            @Override
            public void onPhotoSizeChange(int width, int height) {
                Log.e("main", "onPhotoSizeChange => width = " + width + ", height = " + height);
            }
        });

        holder = surfaceView.getHolder();
        holder.addCallback(new CustomCallBack());

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    recorder.startRecord();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder.stopRecord();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn3.getText().equals("继续")) {
                    btn3.setText("暂停");
                    recorder.init();
                } else {
                    btn3.setText("继续");
                    recorder.cancelRecord();
                }

            }
        });


        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder.getCameraManager().switchCamera();
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean off = recorder.isSoundOff();
                recorder.soundOff(!off);
                if (recorder.isSoundOff())
                    btn5.setText("开启声音");
                else
                    btn5.setText("静音");
            }
        });

        Message obtain = Message.obtain();
        obtain.obj = System.currentTimeMillis();
        h.sendMessageDelayed(obtain, 1000);

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlRenderImgList renderImgList = recorder.getGlRenderManager().getRenderList();
                if (renderImgList.contains("logo"))
                    return;

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.match_water_img);
                GlRenderImg glRenderImg = new GlRenderImg(bitmap);
                glRenderImg.initVerticalPosition(1000, 400, 200, 200);
                glRenderImg.initHorizontalPosition(1000, 400, 200, 200);
                renderImgList.add("logo", glRenderImg);
            }
        });
    }

    protected void onPermissionBack(int requestCode, boolean succeed) {
        if (requestCode == RequestCameraPermission) {
            if (succeed)
                recorder.init();
            else {
                recorder.getCameraManager().getEvent().openCameraFailure(0);
            }

        }
    }

    final Handler h = new Handler(this::handleMessage);

    boolean ok = true;

    boolean isPause = false;

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
        h.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isPause) {
            isPause = false;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());
            String str = formatter.format(curDate);
            Message obtain = Message.obtain();
            obtain.obj = str;
            h.sendMessageDelayed(obtain, 1000);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        GlRenderImgList renderImgList = recorder.getGlRenderManager().getRenderList();
        renderImgList.clear("time");

        if (ok) {
            ok = false;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.match_water_img);
            GlRenderImg glRenderImg = new GlRenderImg(bitmap);
            glRenderImg.initVerticalPosition(bitmap.getWidth() / (float) surfaceView.getWidth(),
                    bitmap.getHeight() / (float) surfaceView.getHeight(), 0.05f, 0.05f);
//        glRenderImg.initVerticalPosition(0.5f,
//                0.5f, 0, 0);
            glRenderImg.initHorizontalPosition(bitmap.getWidth() / (float) TargetLongWidth,
                    bitmap.getHeight() / (float) TargetShortWidth, 0.05f, 0.05f);
            renderImgList.add("logo", glRenderImg);
        }

        TextView tv = findViewById(R.id.text);
        tv.setText(msg.obj.toString());
        tv.setDrawingCacheEnabled(true);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
        tv.destroyDrawingCache();

        GlRenderImg glRenderImg = new GlRenderImg(bitmap);
        glRenderImg.initVerticalPosition(bitmap.getWidth() / (float) surfaceView.getWidth(),
                bitmap.getHeight() / (float) surfaceView.getHeight(), 0.05f, renderImgList.getSize() == 0 ? 0.05f : 0.5f);
        glRenderImg.initHorizontalPosition(bitmap.getWidth() / (float) TargetLongWidth,
                bitmap.getHeight() / (float) TargetShortWidth, 0.05f, renderImgList.getSize() == 0 ? 0.05f : 0.5f);

        renderImgList.replace("time", glRenderImg);

        Message obtain = Message.obtain();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        obtain.obj = str;
        h.sendMessageDelayed(obtain, 1000);

        return false;
    }

    private class CustomCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            requestPermission(RequestCameraPermission, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            recorder.getRenderSetting().setRenderSize(width < height ? TargetShortWidth : TargetLongWidth, width < height ? TargetLongWidth : TargetShortWidth);
            recorder.getRenderSetting().setDisplaySize(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            recorder.destroy();
        }

    }

    @Override
    protected void onDestroy() {
        if (recorder != null)
            recorder.destroy();
        super.onDestroy();
    }

    protected void requestPermission(int requestCode, String permission[]) {
        List<String> requestStr = new ArrayList<>();
        for (String s : permission) {
            if (ContextCompat.checkSelfPermission(this, s)
                    != PackageManager.PERMISSION_GRANTED) {
                requestStr.add(s);
            }
        }
        if (requestStr.size() == 0) {
            onPermissionBack(requestCode, true);
            return;
        }
        String realPermission[] = new String[requestStr.size()];
        requestStr.toArray(realPermission);
        ActivityCompat.requestPermissions(this, realPermission, requestCode);
    }
}
