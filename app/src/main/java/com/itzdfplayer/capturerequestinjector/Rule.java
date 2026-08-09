package com.itzdfplayer.capturerequestinjector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A single "force this capture request key to this value" rule.
 *
 * keyName:
 *   - for a standard key, the exact public static field name on
 *     android.hardware.camera2.CaptureRequest, e.g. "SHADING_MODE"
 *   - for a vendor tag, the fully-qualified tag name as the HAL
 *     exposes it, e.g. "org.codeaurora.qcamera3.some_tag" or
 *     "com.xiaomi.something.enable"
 *
 * type: one of the ValueType constants below - determines both how
 * the stored string value is parsed AND (for vendor keys) what class
 * is passed to the CaptureRequest.Key(String, Class) constructor.
 */
public class Rule {

    public static final String TYPE_INT = "INT";
    public static final String TYPE_FLOAT = "FLOAT";
    public static final String TYPE_LONG = "LONG";
    public static final String TYPE_BYTE = "BYTE";
    public static final String TYPE_BOOLEAN = "BOOLEAN";
    public static final String TYPE_RECT = "RECT";          // "left,top,right,bottom"
    public static final String TYPE_INT_ARRAY = "INT_ARRAY"; // "1,2,3"

    public boolean vendorKey;
    public String keyName;
    public String type;
    public String value;   // raw string form, parsed at apply time
    public boolean enabled = true;

    public Rule() {}

    public Rule(boolean vendorKey, String keyName, String type, String value) {
        this.vendorKey = vendorKey;
        this.keyName = keyName;
        this.type = type;
        this.value = value;
    }

    JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("vendorKey", vendorKey);
        o.put("keyName", keyName);
        o.put("type", type);
        o.put("value", value);
        o.put("enabled", enabled);
        return o;
    }

    static Rule fromJson(JSONObject o) throws JSONException {
        Rule r = new Rule();
        r.vendorKey = o.optBoolean("vendorKey", false);
        r.keyName = o.getString("keyName");
        r.type = o.getString("type");
        r.value = o.getString("value");
        r.enabled = o.optBoolean("enabled", true);
        return r;
    }

    public static String listToJson(List<Rule> rules) {
        JSONArray arr = new JSONArray();
        try {
            for (Rule r : rules) arr.put(r.toJson());
        } catch (JSONException e) {
            // shouldn't happen with our own fields
        }
        return arr.toString();
    }

    public static List<Rule> listFromJson(String json) {
        List<Rule> out = new ArrayList<>();
        if (json == null || json.isEmpty()) return out;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                out.add(fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            // corrupt/old data - just return what we parsed so far (empty)
        }
        return out;
    }
}
