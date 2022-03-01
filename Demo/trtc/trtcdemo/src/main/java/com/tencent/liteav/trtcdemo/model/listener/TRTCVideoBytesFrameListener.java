package com.tencent.liteav.trtcdemo.model.listener;

import android.util.Log;

import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudDef.TRTCVideoFrame;
import com.tencent.trtc.TRTCCloudListener.TRTCVideoFrameListener;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

/**
 * @author : xander
 * @date : 2021/5/25
 */
public class TRTCVideoBytesFrameListener implements TRTCVideoFrameListener {
    private static final String TAG = "TRTCVideoBytesFrameListener";

    private Random     mRandom = new Random();
    private byte[]     mBytes;
    private ByteBuffer mByteBuffers;

    @Override
    public void onGLContextCreated() {
        Log.i(TAG, "onGLContextCreated");
    }

    @Override
    public int onProcessVideoFrame(TRTCVideoFrame inputFrame, TRTCVideoFrame outputFrame) {
        boolean useObjectInOutputFrame = (Math.abs(mRandom.nextInt()) % 2 == 0);

        if (inputFrame.bufferType == TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY) {
            if (mBytes == null || mBytes.length != inputFrame.data.length) {
                mBytes = new byte[inputFrame.data.length];
            }
            if (!useObjectInOutputFrame) {
                outputFrame.data = mBytes;
            }

            if (inputFrame.pixelFormat == TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_RGBA) {
                byte[] from = inputFrame.data, to = outputFrame.data;
                for (int i = 0; i < from.length; i += 4) {
                    int value = ((0xFF & from[i]) + (0xFF & from[i + 1]) + (0xFF & from[i + 2])) / 3;
                    to[i] = to[i + 1] = to[i + 2] = (byte) value;
                    to[i + 3] = from[i + 3];
                }
            } else {
                System.arraycopy(inputFrame.data, 0, outputFrame.data, 0, inputFrame.width * inputFrame.height);
                Arrays.fill(outputFrame.data, inputFrame.width * inputFrame.height, outputFrame.data.length, (byte) 128);
            }
        } else if (inputFrame.bufferType == TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_BUFFER) {
            if (mByteBuffers == null || mByteBuffers.capacity() != inputFrame.buffer.capacity()) {
                mByteBuffers = ByteBuffer.allocateDirect(inputFrame.buffer.capacity());
            }
            if (!useObjectInOutputFrame) {
                outputFrame.buffer = mByteBuffers;
            }

            if (inputFrame.pixelFormat == TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_RGBA) {
                byte[] data = new byte[inputFrame.width * inputFrame.height * 4];
                inputFrame.buffer.position(0);
                inputFrame.buffer.get(data);
                for (int i = 0; i < data.length; i += 4) {
                    int value = ((0xFF & data[i]) + (0xFF & data[i + 1]) + (0xFF & data[i + 2])) / 3;
                    data[i] = data[i + 1] = data[i + 2] = (byte) value;
                }
                outputFrame.buffer.position(0);
                outputFrame.buffer.put(data);
            } else {
                inputFrame.buffer.limit(inputFrame.width * inputFrame.height);
                inputFrame.buffer.position(0);
                outputFrame.buffer.position(0);
                outputFrame.buffer.put(inputFrame.buffer);
                byte[] uv = new byte[inputFrame.width * inputFrame.height / 2];
                Arrays.fill(uv, (byte) 128);
                outputFrame.buffer.put(uv);
            }
        }
        return 0;
    }

    @Override
    public void onGLContextDestory() {
        Log.i(TAG, "onGLContextDestory");
    }
}
