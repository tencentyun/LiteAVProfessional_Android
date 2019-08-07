package com.tencent.liteav.demo.videoediter.paster;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinsonswang on 2017/10/19.
 */

public class AnimatedPasterConfig {
    public static final String FILE_NAME = "config.json";

    public static final String CONFIG_NAME = "name";
    public static final String CONFIG_PERIOD = "period";
    public static final String CONFIG_COUNT = "count";
    public static final String CONFIG_WIDTH = "width";
    public static final String CONFIG_HEIGHT = "height";
    public static final String CONFIG_KEYFRAME = "keyframe";
    public static final String CONFIG_KEYFRAME_ARRAY = "frameArray";

    public String name;
    public int period;
    public int count;
    public int width;
    public int height;
    public int keyframe;
    public List<PasterPicture> frameArray = new ArrayList<>();

    static class PasterPicture {
        public static final String PICTURE_NAME = "picture";

        public String pictureName;
    }

}
