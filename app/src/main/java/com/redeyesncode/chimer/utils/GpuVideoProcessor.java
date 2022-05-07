package com.redeyesncode.chimer.utils;

import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoContentHint;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoFrame;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoSink;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoSource;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCore;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCoreFactory;
import com.amazonaws.services.chime.sdk.meetings.internal.video.gl.DefaultGlVideoFrameDrawer;
import com.amazonaws.services.chime.sdk.meetings.internal.video.gl.GlUtil;
import com.amazonaws.services.chime.sdk.meetings.utils.logger.Logger;

import java.util.ArrayList;

public class GpuVideoProcessor implements VideoSink, VideoSource {

    Logger logger;
    EglCoreFactory eglCoreFactory;
    public GpuVideoProcessor(Logger logger, EglCoreFactory eglCoreFactory){
        this.logger =logger;
        this.eglCoreFactory = eglCoreFactory;
    }

    VideoFrame pendingFrame;

    Object pendingFrameLock;

    BlackAndWhiteGlVideoFrameDrawer bwDrawer = new BlackAndWhiteGlVideoFrameDrawer();
    DefaultGlVideoFrameDrawer rectDrawer = new DefaultGlVideoFrameDrawer();

    GlTextureFrameBufferHelper textureFrameBuffer;
    EglCore eglCore;
    public HandlerThread thread = new HandlerThread("DemoGpuVideoProcessor");
    public Handler handler;

    private boolean textureInUse = false;
    private boolean released = false;
    private ArrayList<VideoSink> sinks = new ArrayList<>();

    private final int DUMMY_PBUFFER_OFFSET = 0;
    private final String TAG = "DemoGpuVideoProcessor";

    public void init(){
        thread.start();
        handler = new Handler(thread.getLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                eglCore = eglCoreFactory.createEglCore();
                int[] surfaceAttribs = new int[]{EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE};
                EGLSurface eglSurface = eglCore.getEglSurface();
                eglSurface = EGL14.eglCreatePbufferSurface(
                        eglCore.getEglDisplay(),
                        eglCore.getEglConfig(),
                        surfaceAttribs,
                        DUMMY_PBUFFER_OFFSET
                );
                EGL14.eglMakeCurrent(eglCore.getEglDisplay(),eglCore.getEglSurface(),eglCore.getEglSurface(),eglCore.getEglContext());
                textureFrameBuffer = new GlTextureFrameBufferHelper(GLES20.GL_RGBA);
                GlUtil.INSTANCE.checkGlError("Failed to set dummy surface to intialize surface texture video source");
                logger.info(TAG,"Created demo GPU video processor");
            }
        });




    }
    private void release(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                logger.info(TAG, "Releasing GPU video processor source")
                released = true;
                if(!textureInUse){
                    completeRelease();
                }
            }
        });


    }
    private void completeRelease(){
        if(Looper.myLooper()==handler.getLooper()){
            rectDrawer.release();
            bwDrawer.release();
            textureFrameBuffer.
            eglCore.release();



        }





    }








    @Override
    public void onVideoFrameReceived(VideoFrame videoFrame) {

    }

    @Override
    public VideoContentHint getContentHint() {

        return null;
    }

    @Override
    public void addVideoSink(VideoSink videoSink) {

    }

    @Override
    public void removeVideoSink(VideoSink videoSink) {

    }
}
