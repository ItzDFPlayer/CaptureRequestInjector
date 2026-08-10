package com.itzdfplayer.capturerequestinjector;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Rules are stored in both SharedPreferences (for UI) and a JSON file
 * (for Xposed hook access across process boundaries).
 */
public class RuleStore {

    public static final String PREFS_NAME = "camtags_rules";
    private static final String RULES_FILE = "camtags_rules.json";
    private static OnRulesSavedListener onRulesSavedListener;

    public interface OnRulesSavedListener {
        void onRulesSaved(Context context);
    }

    public static void setOnRulesSavedListener(OnRulesSavedListener listener) {
        onRulesSavedListener = listener;
    }

    public static void saveRules(Context context, String targetPackage, List<Rule> rules) {
        // Save to SharedPreferences for UI
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(targetPackage, Rule.listToJson(rules)).commit();

        // Save to JSON file for Xposed hook
        saveRulesToFile(context);
    }

    public static List<Rule> loadRules(Context context, String targetPackage) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return Rule.listFromJson(prefs.getString(targetPackage, null));
    }

    public static String loadRulesJson(Context context, String targetPackage) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(targetPackage, null);
    }

    public static void saveRulesToFile(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences settingsPrefs = context.getSharedPreferences("camtags_settings", Context.MODE_PRIVATE);
            Map<String, ?> all = prefs.getAll();

            // Build a JSON object with all package rules and settings
            StringBuilder json = new StringBuilder("{");
            boolean first = true;

            // Add package rules
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":");
                json.append(entry.getValue());
                first = false;
            }

            // Add settings
            if (!first) json.append(",");
            json.append("\"settings\":{");
            json.append("\"disable_global_rules\":").append(settingsPrefs.getBoolean("disable_global_rules", false));
            json.append(",\"disable_all_rules\":").append(settingsPrefs.getBoolean("disable_all_rules", false));
            json.append("}");

            json.append("}");

            // Write to external storage for cross-process access
            File rulesFile = getRulesFile(context);
            FileOutputStream fos = new FileOutputStream(rulesFile);
            fos.write(json.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();

            // Make file readable by all
            rulesFile.setReadable(true, false);

            // Notify listener that rules were saved
            if (onRulesSavedListener != null) {
                onRulesSavedListener.onRulesSaved(context);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getRulesFile(Context context) {
        return new File(context.getExternalFilesDir(null), RULES_FILE);
    }
}
