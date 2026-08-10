package com.itzdfplayer.capturerequestinjector;

import android.content.Context;
import android.content.SharedPreferences;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedInterface;

import java.util.List;

/**
 * Handles saving and loading module rules using libxposed RemotePreferences.
 * RemotePreferences works across process boundaries without requiring filesystem permissions.
 */
public class RuleStore {

    public static final String PREFS_NAME = "camtags_rules";
    public static final String SETTINGS_PREFS_NAME = "camtags_settings";

    // Save rules from UI / App Context using RemotePreferences
    public static void saveRules(Context context, String targetPackage, List<Rule> rules) {
        SharedPreferences prefs = XposedModule.getRemotePreferences(PREFS_NAME);
        prefs.edit().putString(targetPackage, Rule.listToJson(rules)).apply();
    }

    // Load rules in UI / App Context using RemotePreferences
    public static List<Rule> loadRules(Context context, String targetPackage) {
        SharedPreferences prefs = XposedModule.getRemotePreferences(PREFS_NAME);
        return Rule.listFromJson(prefs.getString(targetPackage, null));
    }

    // Load JSON string in UI / App Context
    public static String loadRulesJson(Context context, String targetPackage) {
        SharedPreferences prefs = XposedModule.getRemotePreferences(PREFS_NAME);
        return prefs.getString(targetPackage, null);
    }

    // Helper method to load rules inside your injected XposedModule class
    public static List<Rule> loadRulesInHook(XposedInterface moduleInterface, String targetPackage) {
        SharedPreferences prefs = moduleInterface.getRemotePreferences(PREFS_NAME);
        return Rule.listFromJson(prefs.getString(targetPackage, null));
    }

    // Helper method to check global settings inside your injected XposedModule class
    public static boolean getBooleanSettingInHook(XposedInterface moduleInterface, String key, boolean defaultValue) {
        SharedPreferences settingsPrefs = moduleInterface.getRemotePreferences(SETTINGS_PREFS_NAME);
        return settingsPrefs.getBoolean(key, defaultValue);
    }
}
