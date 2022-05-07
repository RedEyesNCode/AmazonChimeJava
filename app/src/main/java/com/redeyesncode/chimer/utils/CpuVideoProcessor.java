package com.redeyesncode.chimer.utils;


import android.graphics.Matrix;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoContentHint;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoFrame;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoSink;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoSource;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.buffer.VideoFrameRGBABuffer;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCore;
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCoreFactory;
import com.amazonaws.services.chime.sdk.meetings.internal.video.gl.DefaultGlVideoFrameDrawer;
import com.amazonaws.services.chime.sdk.meetings.internal.video.gl.GlUtil;
import com.amazonaws.services.chime.sdk.meetings.utils.logger.Logger;
import com.xodee.client.video.ContentHint;
import com.xodee.client.video.JniUtil;

import org.amazon.chime.webrtc.GlTextureFrameBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CpuVideoProcessor  implements VideoSink,  VideoSource {



    private Logger logger;
    private EglCoreFactory eglCoreFactory;
    public  DefaultGlVideoFrameDrawer reactDrawer = new DefaultGlVideoFrameDrawer();
    private GlTextureFrameBuffer textureFrameBuffer = new GlTextureFrameBuffer(GLES20.GL_RGBA);
    private VideoContentHint contentHint = VideoContentHint.Motion;
    public EglCore eglCore;
    //making this static first , let's see what happens.
    public static HandlerThread thread = new HandlerThread("DemoCpuVideoProcessor");
    public static Handler handler;
    private ArrayList<VideoSink> sinks = new ArrayList<>();
    private final int DUMMY_PBUFFER_OFFSET=0;
    private final String TAG = "DemoCpuVideoProcessor";


    //Make a constructor for this class as defined in CpuVideoProcessor.kt class
    public CpuVideoProcessor(Logger logger,EglCoreFactory eglCoreFactory){

        this.logger=logger;
        this.eglCoreFactory=eglCoreFactory;
    }

    public void init(){
        handler= new Handler(thread.getLooper());
        thread.start();

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
                GlUtil.INSTANCE.checkGlError("Failed to set dummy surface to intialize surface texture video source");
                logger.info(TAG,"Created demo CPU video processor");

            }
        });

    }

    @Override
    public void onVideoFrameReceived(VideoFrame videoFrame) {
        videoFrame.retain();
        handler.post(new Runnable() {
            @Override
            public void run() {
                textureFrameBuffer.setSize(videoFrame.getRotatedWidth(), videoFrame.getRotatedHeight());
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,textureFrameBuffer.getFrameBufferId());

                Matrix matrix = new Matrix();
                // Shift before flipping
                matrix.preTranslate(0.5f, 0.5f);
                // RGBA frames are upside down relative to texture coordinates
                matrix.preScale(1f, -1f);
                // Unshift following flip;
                matrix.preTranslate(-0.5f, -0.5f);
                // Note the draw call will account for any rotation, so we need to account for that in viewport width/height
                reactDrawer.drawFrame(
                        videoFrame,
                        0,
                        0,
                        videoFrame.getRotatedWidth(),
                        videoFrame.getRotatedHeight(),
                        matrix
                );

               ByteBuffer rgbaData =  JniUtil.nativeAllocateByteBuffer(videoFrame.getWidth() * videoFrame.getHeight() * 4);
                GLES20.glReadPixels(
                        0,
                        0,
                        videoFrame.getRotatedWidth(),
                        videoFrame.getRotatedHeight(),
                        GLES20.GL_RGBA,
                        GLES20.GL_UNSIGNED_BYTE,
                        rgbaData
                );

                GlUtil.INSTANCE.checkGlError("glReadPixels");
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        JniUtil.nativeFreeByteBuffer(rgbaData);
                    }
                };

                VideoFrameRGBABuffer rgbaBuffer = new VideoFrameRGBABuffer(
                        videoFrame.getRotatedWidth(),
                        videoFrame.getRotatedHeight(),
                        rgbaData, videoFrame.getRotatedWidth() * 4,
                        runnable
                );
                convertToBlackAndWhite(rgbaBuffer);
                VideoFrame processedFrame = new  VideoFrame(videoFrame.getTimestampNs(),rgbaBuffer,videoFrame.getRotation());
//                            sinks.forEach { it.onVideoFrameReceived(processedFrame) }
                videoFrame.release();
                for (int i = 0; i < sinks.size(); i++) {
                    onVideoFrameReceived(videoFrame);
                }
                processedFrame.release();

            }
        });



    }

    private void release(){
        handler.post(new Runnable() {
            @Override
            public void run() {

                logger.info(TAG, "Releasing CPU video processor source");

                reactDrawer.release();
                textureFrameBuffer.release();
                eglCore.release();

                handler.getLooper().quit();
            }
        });


    }
    private void convertToBlackAndWhite(VideoFrameRGBABuffer rgbaBuffer){

//        fun Byte.toPositiveInt() = toInt() and 0XFF
//        x in 0 until rgbaBuffer.width
        for (int x =0; x< rgbaBuffer.getWidth();x++) {
            for (int y=0;y<rgbaBuffer.getHeight();y++) {
                int rLocation = y * rgbaBuffer.getStride() + x * 4;
                int gLocation = rLocation + 1;
                int bLocation = rLocation + 2;

                ByteBuffer rvalueBuffer = rgbaBuffer.getData();
                int rValue = rvalueBuffer.asIntBuffer().get(rLocation);

                ByteBuffer gvalueBuffer = rgbaBuffer.getData();
                int gValue = gvalueBuffer.asIntBuffer().get(gLocation);

                ByteBuffer bValueBuffer = rgbaBuffer.getData();
                int bValue = bValueBuffer.asIntBuffer().get(bLocation);


                Double newValueDouble =  ((rValue + gValue + bValue) / (3.0));

                rgbaBuffer.getData().put(rLocation, newValueDouble.byteValue());
                rgbaBuffer.getData().put(rLocation, newValueDouble.byteValue());
                rgbaBuffer.getData().put(rLocation, newValueDouble.byteValue());

/*
                rgbaBuffer.data.put(rLocation, newValue);
                rgbaBuffer.data.put(gLocation, newValue);
                rgbaBuffer.data.put(bLocation, newValue);*/
            }
        }




    }
    @Override
    public VideoContentHint getContentHint() {
        return null;
    }

    @Override
    public void addVideoSink(VideoSink videoSink) {
        sinks.add(videoSink);

    }

    @Override
    public void removeVideoSink(VideoSink videoSink) {
        sinks.remove(videoSink);

    }
}
