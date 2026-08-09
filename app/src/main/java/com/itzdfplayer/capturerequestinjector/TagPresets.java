package com.itzdfplayer.capturerequestinjector;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagPresets {
    private static final Map<String, String> TAG_TYPES = new HashMap<>();
    
    static {
        // android.colorCorrection
        TAG_TYPES.put("COLOR_CORRECTION_MODE", "INT");
        TAG_TYPES.put("COLOR_CORRECTION_ABERRATION_MODE", "INT");
        
        // android.control
        TAG_TYPES.put("CONTROL_MODE", "INT");
        TAG_TYPES.put("CONTROL_AE_MODE", "INT");
        TAG_TYPES.put("CONTROL_AE_LOCK", "BOOLEAN");
        TAG_TYPES.put("CONTROL_AE_ANTIBANDING_MODE", "INT");
        TAG_TYPES.put("CONTROL_AE_EXPOSURE_COMPENSATION", "INT");
        TAG_TYPES.put("CONTROL_AE_PRECAPTURE_TRIGGER", "INT");
        TAG_TYPES.put("CONTROL_AF_MODE", "INT");
        TAG_TYPES.put("CONTROL_AF_TRIGGER", "INT");
        TAG_TYPES.put("CONTROL_AWB_MODE", "INT");
        TAG_TYPES.put("CONTROL_AWB_LOCK", "BOOLEAN");
        TAG_TYPES.put("CONTROL_CAPTURE_INTENT", "INT");
        TAG_TYPES.put("CONTROL_EFFECT_MODE", "INT");
        TAG_TYPES.put("CONTROL_SCENE_MODE", "INT");
        TAG_TYPES.put("CONTROL_VIDEO_STABILIZATION_MODE", "INT");
        TAG_TYPES.put("CONTROL_POST_RAW_SENSITIVITY_BOOST", "INT");
        TAG_TYPES.put("CONTROL_ENABLE_ZSL", "BOOLEAN");
        TAG_TYPES.put("CONTROL_ZOOM_RATIO", "FLOAT");
        TAG_TYPES.put("CONTROL_EXTENDED_SCENE_MODE", "INT");
        TAG_TYPES.put("CONTROL_SETTINGS_OVERRIDE", "INT");
        TAG_TYPES.put("CONTROL_AUTOFRAMING", "INT");
        
        // android.edge
        TAG_TYPES.put("EDGE_MODE", "INT");
        
        // android.flash
        TAG_TYPES.put("FLASH_MODE", "INT");
        TAG_TYPES.put("FLASH_STRENGTH_LEVEL", "INT");
        
        // android.hotPixel
        TAG_TYPES.put("HOT_PIXEL_MODE", "INT");
        
        // android.jpeg
        TAG_TYPES.put("JPEG_ORIENTATION", "INT");
        TAG_TYPES.put("JPEG_QUALITY", "BYTE");
        TAG_TYPES.put("JPEG_THUMBNAIL_QUALITY", "BYTE");
        
        // android.lens
        TAG_TYPES.put("LENS_APERTURE", "FLOAT");
        TAG_TYPES.put("LENS_FILTER_DENSITY", "FLOAT");
        TAG_TYPES.put("LENS_FOCAL_LENGTH", "FLOAT");
        TAG_TYPES.put("LENS_FOCUS_DISTANCE", "FLOAT");
        TAG_TYPES.put("LENS_OPTICAL_STABILIZATION_MODE", "INT");
        
        // android.noiseReduction
        TAG_TYPES.put("NOISE_REDUCTION_MODE", "INT");
        
        // android.scaler
        TAG_TYPES.put("SCALER_CROP_REGION", "RECT");
        
        // android.sensor
        TAG_TYPES.put("SENSOR_EXPOSURE_TIME", "LONG");
        TAG_TYPES.put("SENSOR_FRAME_DURATION", "LONG");
        TAG_TYPES.put("SENSOR_SENSITIVITY", "INT");
        TAG_TYPES.put("SENSOR_TEST_PATTERN_MODE", "INT");
        TAG_TYPES.put("SENSOR_TEST_PATTERN_DATA", "INT_ARRAY");
        TAG_TYPES.put("SENSOR_PIXEL_MODE", "INT");
        
        // android.shading
        TAG_TYPES.put("SHADING_MODE", "INT");
        
        // android.statistics
        TAG_TYPES.put("STATISTICS_FACE_DETECT_MODE", "INT");
        TAG_TYPES.put("STATISTICS_HOT_PIXEL_MAP_MODE", "BOOLEAN");
        TAG_TYPES.put("STATISTICS_LENS_SHADING_MAP_MODE", "INT");
        TAG_TYPES.put("STATISTICS_OIS_DATA_MODE", "INT");
        
        // android.tonemap
        TAG_TYPES.put("TONEMAP_MODE", "INT");
        TAG_TYPES.put("TONEMAP_GAMMA", "FLOAT");
        TAG_TYPES.put("TONEMAP_PRESET_CURVE", "INT");
        
        // android.blackLevel
        TAG_TYPES.put("BLACK_LEVEL_LOCK", "BOOLEAN");
        
        // android.distortionCorrection
        TAG_TYPES.put("DISTORTION_CORRECTION_MODE", "INT");
        
        // android.reprocess
        TAG_TYPES.put("REPROCESS_EFFECTIVE_EXPOSURE_FACTOR", "FLOAT");
    }
    
    public static String getTypeForTag(String tagName) {
        return TAG_TYPES.get(tagName);
    }
    
    public static String[] getAllTags(Context context) {
        List<String> tagList = new ArrayList<>(TAG_TYPES.keySet());
        Collections.sort(tagList);
        
        String[] tags = new String[tagList.size() + 1];
        tags[0] = context.getString(R.string.custom);
        int i = 1;
        for (String tag : tagList) {
            tags[i++] = tag;
        }
        return tags;
    }
    
    public static boolean isCustomTag(String tagName, Context context) {
        return context.getString(R.string.custom).equals(tagName) || !TAG_TYPES.containsKey(tagName);
    }
}
