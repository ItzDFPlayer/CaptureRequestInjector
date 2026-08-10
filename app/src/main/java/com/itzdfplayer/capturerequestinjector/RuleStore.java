package com.itzdfplayer.capturerequestinjector;

import android.content.Context;
import android.content.SharedPreferences;

import io.github.libxposed.api.XposedInterface;

import java.util.List;

public class RuleStore {

    public static final String PREFS_NAME = "camtags_rules";
    public static final String SETTINGS_PREFS_NAME = "camtags_settings";

    // Call inside UI / Activity using Context
    public static void saveRules(Context context, String targetPackage, List<Rule> rules) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(targetPackage, Rule.listToJson(rules)).apply();
    }

    // Call inside UI / Activity using Context
    public static List<Rule> loadRules(Context context, String targetPackage) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return Rule.listFromJson(prefs.getString(targetPackage, null));
    }

    // Call inside UI / Activity using Context
    public static String loadRulesJson(Context context, String targetPackage) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(targetPackage, null);
    }

    // Call inside Hooked Process using your XposedInterface / XposedModule instance
    public static List<Rule> loadRulesInHook(XposedInterface moduleInterface, String targetPackage) {
        SharedPreferences prefs = moduleInterface.getRemotePreferences(PREFS_NAME);
        return Rule.listFromJson(prefs.getString(targetPackage, null));
    }

    // Call inside Hooked Process using your XposedInterface / XposedModule instance
    public static boolean getBooleanSettingInHook(XposedInterface moduleInterface, String key, boolean defaultValue) {
        SharedPreferences settingsPrefs = moduleInterface.getRemotePreferences(SETTINGS_PREFS_NAME);
        return settingsPrefs.getBoolean(key, defaultValue);
    }
}
