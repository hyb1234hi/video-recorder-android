package com.kalu.recorder.MediaRecord;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.support.annotation.Nullable;
import android.view.SurfaceView;

import com.kalu.recorder.Bean.CameraSetting;
import com.kalu.recorder.Bean.MediaFrameData;
import com.kalu.recorder.Bean.RecordSetting;
import com.kalu.recorder.Bean.RenderSetting;
import com.kalu.recorder.RecorderHelper.RecordManageBase;
import com.kalu.recorder.Utils.FileUntil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Lzc on 2018/3/22 0022.
 */

public class RecordVideoAndAudioManager extends RecordManageBase {
    //合并音频和视频
    private VideoAudioMerger videoAudioMerger;

    private int formatConfirmSucceedCount = 0;

    private File file;

    public RecordVideoAndAudioManager(Activity context, File file,
                                      @Nullable RecordSetting recordSetting,
                                      @Nullable CameraSetting cameraSetting,
                                      @Nullable RenderSetting renderSetting,
                                      SurfaceView surfaceView) {
        super(context, recordSetting, cameraSetting, renderSetting, surfaceView);

        this.file = file;
        videoAudioMerger = new VideoAudioMerger(file);
        initCallBack();
        resetFile();
    }

    private void initCallBack() {
        videoAudioMerger.setCallBack(new VideoAudioMerger.CallBack() {

            @Override
            public void compoundFail(String msg) {
                cancelRecord();
                callBackEvent.recordError(msg);

            }

            @Override
            public void compoundSuccess(File file) {
                cancelRecord();

                // TODO: 2019/03/15
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(file.getPath());
                Bitmap bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                // FileUntil.saveImageAndGetFile(bmp, videoImg, -1, Bitmap.CompressFormat.JPEG);

                callBackEvent.stopRecordFinish(file);
            }
        });
    }

    @Override
    protected void onRecordStop() {
        videoAudioMerger.shutdownCompoundVideo();
    }

    @Override
    public void startRecord() {
        super.startRecord();
        onRecordStart();
    }

    private void onRecordStart() {
        formatConfirmSucceedCount = 0;
        if (!videoAudioMerger.initCompoundVideo()) {
            callBackEvent.recordError("开始合成器失败!");
            destroy();
        }
    }

    @Override
    protected void onFrameAvailable(DataType type, MediaFrameData frameData) {
        if (type == DataType.Type_Video) {
            videoAudioMerger.frameAvailable(frameData, recordVideoManager.getTrack());
        } else {
            videoAudioMerger.frameAvailable(frameData, recordAudioManager.getTrack());
        }
    }

    @Override
    protected int onFormatConfirm(DataType type, MediaFormat mediaFormat) {
        int trace = -1;
        try {
            if (type == DataType.Type_Video) {
                if (needVideo)
                    trace = videoAudioMerger.addTrack(mediaFormat);
            } else if (type == DataType.Type_Audio) {
                if (needAudio) {
                    trace = videoAudioMerger.addTrack(mediaFormat);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            cancelRecord();
            callBackEvent.recordError("添加合并轨道失败！");
        }
        formatConfirmSucceedCount++;
        if (formatConfirmSucceedCount == MaxCount())
            videoAudioMerger.start();
        return trace;
    }


    //重置文件
    private void resetFile() {
        if (file != null) {
            try {
                FileUntil.clearFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        if (videoImg != null)
//            try {
//                FileUntil.clearFile(videoImg);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
    }

    @Override
    public void cancelRecord() {
        super.cancelRecord();
        if (videoAudioMerger != null)
            videoAudioMerger.cancelCompoundVideo();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (videoAudioMerger != null)
            videoAudioMerger.cancelCompoundVideo();
    }

    public File getFile() {
        return file;
    }

//    public File getVideoImg() {
//        return videoImg;
//    }
}
