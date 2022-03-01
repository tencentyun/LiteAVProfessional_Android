/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.liteav.trtcdemo.model.opengl;

import android.opengl.GLES20;

import com.tencent.liteav.trtcdemo.model.customcapture.structs.FrameBuffer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.tencent.liteav.trtcdemo.model.opengl.GLConstants.NO_TEXTURE;

public class GPUImageFilterGroup extends GPUImageFilter {
    private final FrameBuffer[]        mFrameBuffers = new FrameBuffer[2];
    private final FloatBuffer          mGLCubeBuffer;
    private final FloatBuffer          mGLTextureBuffer;
    private final List<GPUImageFilter> mFilters;
    private final List<GPUImageFilter> mMergedFilters;

    public GPUImageFilterGroup() {
        mGLCubeBuffer = OpenGlUtils.createNormalCubeVerticesBuffer();
        mGLTextureBuffer = OpenGlUtils.createTextureCoordsBuffer(Rotation.NORMAL, false, false);
        mFilters = new ArrayList<>();
        mMergedFilters = new ArrayList<>();
    }

    public void addFilter(GPUImageFilter filter) {
        if (filter == null) {
            return;
        }
        mFilters.add(filter);
        updateMergedFilters();
    }

    public List<GPUImageFilter> getMergedFilters() {
        return mMergedFilters;
    }

    public void updateMergedFilters() {
        if (mFilters == null) {
            return;
        }

        mMergedFilters.clear();

        List<GPUImageFilter> filters;
        for (GPUImageFilter filter : mFilters) {
            if (filter instanceof GPUImageFilterGroup) {
                ((GPUImageFilterGroup) filter).updateMergedFilters();
                filters = ((GPUImageFilterGroup) filter).getMergedFilters();
                if (filters != null && !filters.isEmpty()) {
                    mMergedFilters.addAll(filters);
                }
            } else {
                mMergedFilters.add(filter);
            }
        }
    }

    @Override
    protected void onInit() {
        super.onInit();
        for (int i = 0; i < mMergedFilters.size(); ++i) {
            mMergedFilters.get(i).init();
        }
    }

    @Override
    protected void onUninit() {
        destroyFramebuffers();
        for (GPUImageFilter filter : mMergedFilters) {
            filter.destroy();
        }
        super.onUninit();
    }

    private void destroyFramebuffers() {
        for (int i = 0; i < mFrameBuffers.length; ++i) {
            if (mFrameBuffers[i] != null) {
                mFrameBuffers[i].uninitialize();
                mFrameBuffers[i] = null;
            }
        }
    }

    @Override
    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        destroyFramebuffers();

        List<GPUImageFilter> renderFilters = getRenderFilters();
        int size = renderFilters.size();
        for (int i = 0; i < size; i++) {
            renderFilters.get(i).onOutputSizeChanged(width, height);
        }

        if (size > 0) {
            for (int i = 0; i < mFrameBuffers.length; i++) {
                mFrameBuffers[i] = new FrameBuffer(width, height);
                mFrameBuffers[i].initialize();
            }
        }
    }

    public List<GPUImageFilter> getRenderFilters() {
        return mMergedFilters;
    }

    @Override
    public void onDraw(final int textureId, final FloatBuffer cubeBuffer, final FloatBuffer textureBuffer) {
        throw new RuntimeException("this method should not been call!");
    }

    /**
     * 绘制当前特效
     *
     * @param textureId        图像输入
     * @param outFrameBufferId 需要绘制到哪里,如果为-1,表示需要绘制到屏幕
     * @param cubeBuffer       绘制的矩阵
     * @param textureBuffer    需要使用图像输入的哪一部分
     */
    public void draw(final int textureId,
                     final int outFrameBufferId,
                     final FloatBuffer cubeBuffer,
                     final FloatBuffer textureBuffer) {
        runPendingOnDrawTasks();
        if (!isInitialized() || null == getRenderFilters()) {
            return;
        }

        if (textureId == NO_TEXTURE) {
            return;
        }

        List<GPUImageFilter> filters = getRenderFilters();
        int size = filters.size();
        int previousTexture = textureId;
        for (int i = 0; i < size; i++) {
            GPUImageFilter filter = filters.get(i);
            boolean isNotLast = i < size - 1;
            if (isNotLast) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[i % 2].getFrameBufferId());
                GLES20.glClearColor(0, 0, 0, 0);
            } else if (NO_TEXTURE != outFrameBufferId) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outFrameBufferId);
                GLES20.glClearColor(0, 0, 0, 0);
            }

            if (i == 0) {
                filter.onDraw(previousTexture, cubeBuffer, textureBuffer);
            } else {
                filter.onDraw(previousTexture, mGLCubeBuffer, mGLTextureBuffer);
            }

            if (isNotLast) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                previousTexture = mFrameBuffers[i % 2].getTextureId();
            } else {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        }
    }
}
