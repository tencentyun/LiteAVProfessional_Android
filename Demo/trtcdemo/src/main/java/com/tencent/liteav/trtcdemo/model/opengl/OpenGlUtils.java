package com.tencent.liteav.trtcdemo.model.opengl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Pair;

import com.tencent.liteav.basic.log.TXCLog;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.opengles.GL10;


public class OpenGlUtils {
    private static final String TAG = "OpenGlUtils";
    private static final boolean DEBUG = false;

    public static final int NO_TEXTURE = -1;
    public static final int NOT_INIT = -1;
    public static final int ON_DRAWN = 1;

    public static void bindTexture(int target, int texture) {
        GLES20.glBindTexture(target, texture);
        checkGlError("bindTexture(" + texture + ")");
    }

    public static void bindFramebuffer(int target, int framebuffer) {
        GLES20.glBindFramebuffer(target, framebuffer);
        checkGlError("bindTexture(" + framebuffer + ")");
    }

    public static int generateTextureOES() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    public static int createTexture(int width, int height, int internalFormat, int format) {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        if (DEBUG) {
            TXCLog.d(TAG, "glGenTextures textureId: " + textureIds[0]);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, null);
        return textureIds[0];
    }

    public static int loadTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        int[] textures = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            if (DEBUG) {
                TXCLog.d(TAG, "glGenTextures textureId: " + textures[0]);
            }

            bindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        } else {
            bindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        return textures[0];
    }

    public static int loadTexture(int format, Buffer data, int width, int height, int usedTexId) {
        int[] textures = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            if (DEBUG) {
                TXCLog.d(TAG, "glGenTextures textureId: " + textures[0]);
            }

            bindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, data);
        } else {
            bindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height, format, GLES20.GL_UNSIGNED_BYTE, data);
            textures[0] = usedTexId;
        }
        return textures[0];
    }

    public static int generateFrameBufferId() {
        int[] ids = new int[1];
        GLES20.glGenFramebuffers(1, ids, 0);
        return ids[0];
    }

    public static void deleteTexture(int textureId) {
        if (textureId != NO_TEXTURE) {
            GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
            if (DEBUG) {
                TXCLog.d(TAG, "delete textureId " + textureId);
            }
        }
    }

    public static void deleteFrameBuffer(int frameBufferId) {
        if (frameBufferId != NO_TEXTURE) {
            GLES20.glDeleteFramebuffers(1, new int[]{frameBufferId}, 0);
            TXCLog.d(TAG, "delete frame buffer id: " + frameBufferId);
        }
    }

    public static FloatBuffer createNormalCubeVerticesBuffer() {
        return (FloatBuffer) ByteBuffer.allocateDirect(GLConstants.CUBE_VERTICES_ARRAYS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(GLConstants.CUBE_VERTICES_ARRAYS)
                .position(0);
    }

    public static FloatBuffer createTextureCoordsBuffer(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        float[] temp = new float[GLConstants.TEXTURE_COORDS_NO_ROTATION.length];
        initTextureCoordsBuffer(temp, rotation, flipHorizontal, flipVertical);

        FloatBuffer buffer = ByteBuffer.allocateDirect(GLConstants.TEXTURE_COORDS_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(temp).position(0);
        return buffer;
    }

    /**
     * 通过输入和输出的宽高，计算顶点数组和纹理数组
     * @param scaleType          缩放方式
     * @param inputRotation      输入纹理的旋转角度
     * @param needFlipHorizontal 是否进行镜面映射处理
     * @param inputWith          输入纹理的宽（未经处理的）
     * @param inputHeight        输入纹理的高（未经处理的）
     * @param outputWidth        绘制目标的宽
     * @param outputHeight       绘制目标的高
     * @return 返回顶点数组和纹理数组
     */
    public static Pair<float[], float[]> calcCubeAndTextureBuffer(GLConstants.GLScaleType scaleType,
                                                                  Rotation inputRotation,
                                                                  boolean needFlipHorizontal,
                                                                  int inputWith,
                                                                  int inputHeight,
                                                                  int outputWidth,
                                                                  int outputHeight) {

        boolean needRotate = (inputRotation == Rotation.ROTATION_90 || inputRotation == Rotation.ROTATION_270);
        int rotatedWidth = needRotate ? inputHeight : inputWith;
        int rotatedHeight = needRotate ? inputWith : inputHeight;
        float maxRratio = Math.max(1.0f * outputWidth / rotatedWidth, 1.0f * outputHeight / rotatedHeight);
        float ratioWidth = 1.0f * Math.round(rotatedWidth * maxRratio) / outputWidth;
        float ratioHeight = 1.0f * Math.round(rotatedHeight * maxRratio) / outputHeight;

        float[] cube = GLConstants.CUBE_VERTICES_ARRAYS;
        float[] textureCoords = new float[GLConstants.TEXTURE_COORDS_ROTATE_RIGHT.length];
        initTextureCoordsBuffer(textureCoords, inputRotation, needFlipHorizontal, true);
        if (scaleType == GLConstants.GLScaleType.CENTER_CROP) {
            float distHorizontal = needRotate ? ((1 - 1 / ratioHeight) / 2) : ((1 - 1 / ratioWidth) / 2);
            float distVertical = needRotate ? ((1 - 1 / ratioWidth) / 2) : ((1 - 1 / ratioHeight) / 2);
            textureCoords = new float[] {
                    addDistance(textureCoords[0], distHorizontal),
                    addDistance(textureCoords[1], distVertical),
                    addDistance(textureCoords[2], distHorizontal),
                    addDistance(textureCoords[3], distVertical),
                    addDistance(textureCoords[4], distHorizontal),
                    addDistance(textureCoords[5], distVertical),
                    addDistance(textureCoords[6], distHorizontal),
                    addDistance(textureCoords[7], distVertical), };
        } else {
            cube = new float[] { cube[0] / ratioHeight, cube[1] / ratioWidth,
                    cube[2] / ratioHeight, cube[3] / ratioWidth,
                    cube[4] / ratioHeight, cube[5] / ratioWidth,
                    cube[6] / ratioHeight, cube[7] / ratioWidth, };
        }
        return new Pair<>(cube, textureCoords);
    }

    private static float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public static void initTextureCoordsBuffer(float[] textureCoords, Rotation rotation,
                                               boolean flipHorizontal, boolean flipVertical) {
        float[] initRotation;
        switch (rotation) {
            case ROTATION_90:
                initRotation = GLConstants.TEXTURE_COORDS_ROTATE_RIGHT;
                break;
            case ROTATION_180:
                initRotation = GLConstants.TEXTURE_COORDS_ROTATED_180;
                break;
            case ROTATION_270:
                initRotation = GLConstants.TEXTURE_COORDS_ROTATE_LEFT;
                break;
            case NORMAL:
            default:
                initRotation = GLConstants.TEXTURE_COORDS_NO_ROTATION;
                break;
        }

        System.arraycopy(initRotation, 0, textureCoords, 0, initRotation.length);
        if (flipHorizontal) {
            textureCoords[0] = flip(textureCoords[0]);
            textureCoords[2] = flip(textureCoords[2]);
            textureCoords[4] = flip(textureCoords[4]);
            textureCoords[6] = flip(textureCoords[6]);
        }

        if (flipVertical) {
            textureCoords[1] = flip(textureCoords[1]);
            textureCoords[3] = flip(textureCoords[3]);
            textureCoords[5] = flip(textureCoords[5]);
            textureCoords[7] = flip(textureCoords[7]);
        }
    }

    private static float flip(final float i) {
        return i == 0.0f ? 1.0f : 0.0f;
    }


    public static int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(iType);
        GLES20.glShaderSource(iShader, strSource);
        GLES20.glCompileShader(iShader);
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            TXCLog.w("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
            return 0;
        }
        return iShader;
    }

    public static int loadProgram(final String strVSource, final String strFSource) {
        int iVShader;
        int iFShader;
        int iProgId;
        int[] link = new int[1];
        iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            TXCLog.w("Load Program", "Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            TXCLog.w("Load Program", "Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES20.glCreateProgram();

        GLES20.glAttachShader(iProgId, iVShader);
        GLES20.glAttachShader(iProgId, iFShader);

        GLES20.glLinkProgram(iProgId);

        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            TXCLog.w("Load Program", "Linking Failed");
            return 0;
        }
        GLES20.glDeleteShader(iVShader);
        GLES20.glDeleteShader(iFShader);
        return iProgId;
    }

    public static Bitmap createTimeBitmap(long time, int width, int height) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String timeStr = String.format("%s", dateFormat.format(new Date(time)));
        Paint paint = new Paint();
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);

        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        String[] arr = timeStr.split("\n");
        int maxWidth = 0;
        for (String t : arr) {
            maxWidth = (int) Math.max(maxWidth, paint.measureText(t));
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (int i = 0; i < arr.length; i++) {
            canvas.drawText(arr[i], 130, (i + 1)* (fm.descent - fm.ascent) +300, paint);
        }
        canvas.save();
        return bitmap;
    }


    public static void checkGlError(String op) {
        if (!DEBUG) {
            return;
        }

        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            TXCLog.e(TAG, String.format("%s: glError %s", op, GLUtils.getEGLErrorString(error)));
        }
    }
}
