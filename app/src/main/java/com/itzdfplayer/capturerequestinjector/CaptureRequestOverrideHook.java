package com.itzdfplayer.capturerequestinjector;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.hardware.camera2.CaptureRequest;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.List;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * Applies user-configured Rule overrides to every CaptureRequest built in
 * the current process, for whichever rules were saved for this package
 * name from the settings Activity.
 *
 * IMPORTANT: this class runs inside the TARGET app's process (e.g. the
 * camera app), not inside this module's own app process. It reads config
 * via file pointed at this module's own preferences file.
 */
@SuppressLint({"PrivateApi", "BlockedPrivateApi"})
public class CaptureRequestOverrideHook extends XposedModule {

    private static final String TAG = "CamTags";
    private static final String MODULE_PACKAGE = "com.itzdfplayer.capturerequestinjector";

    @Override
    public void onModuleLoaded(@NonNull XposedModuleInterface.ModuleLoadedParam param) {
        super.onModuleLoaded(param);
        android.util.Log.i(TAG, "Module loaded successfully");
    }

    @Override
    public void onSystemServerStarting(@NonNull SystemServerStartingParam param) {
        // No system server hooks needed
    }

    @Override
    public void onPackageLoaded(@NonNull PackageLoadedParam param) {
        // No hooks needed here
    }

    @Override
    public void onPackageReady(@NonNull PackageReadyParam param) {
        String packageName = param.getPackageName();
        
        android.util.Log.i(TAG, "onPackageReady called for: " + packageName);
        
        // Hook CaptureRequest.Builder.build for all packages
        try {
            Class<?> builderClass = param.getClassLoader().loadClass("android.hardware.camera2.CaptureRequest$Builder");
            var buildMethod = builderClass.getDeclaredMethod("build");
            
            hook(buildMethod).intercept(new CaptureRequestHooker(packageName));
            
            android.util.Log.i(TAG, "Successfully hooked CaptureRequest.Builder.build for " + packageName);
        } catch (Throwable t) {
            android.util.Log.e(TAG, "Failed to hook CaptureRequest.Builder.build for " + packageName, t);
        }
    }

    private static class CaptureRequestHooker implements XposedInterface.Hooker {
        private final String targetPackage;

        CaptureRequestHooker(String targetPackage) {
            this.targetPackage = targetPackage;
        }

        @Override
        public Object intercept(@NonNull XposedInterface.Chain chain) throws Throwable {
            try {
                Object thisObject = chain.getThisObject();
                if (thisObject instanceof CaptureRequest.Builder) {
                    applyRules(targetPackage, (CaptureRequest.Builder) thisObject);
                }
            } catch (Throwable t) {
                android.util.Log.e(TAG, "Error applying rules for " + targetPackage, t);
            }
            return chain.proceed();
        }
    }

    private static void applyRules(String targetPackage, CaptureRequest.Builder builder) {
        try {
            JSONObject allRules = loadRulesFromFile();
            if (allRules == null) {
                android.util.Log.d(TAG, "No rules file found");
                return;
            }

            // Check if all rules are disabled
            boolean disableAllRules = false;
            boolean disableGlobalRules = false;
            
            if (allRules.has("settings")) {
                JSONObject settings = allRules.getJSONObject("settings");
                disableAllRules = settings.optBoolean("disable_all_rules", false);
                disableGlobalRules = settings.optBoolean("disable_global_rules", false);
                android.util.Log.d(TAG, "Settings - disableAllRules: " + disableAllRules + ", disableGlobalRules: " + disableGlobalRules);
            }
            
            if (disableAllRules) {
                android.util.Log.d(TAG, "All rules are disabled, skipping rule application");
                return;
            }

            // Apply global rules first (baseline) - unless disabled
            if (!disableGlobalRules) {
                applyRulesForPackage(allRules, "global", builder, targetPackage);
            } else {
                android.util.Log.d(TAG, "Global rules are disabled, skipping global rules");
            }
            // Apply package-specific rules second (overrides global for same keys)
            applyRulesForPackage(allRules, targetPackage, builder, targetPackage);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error loading rules: " + e);
            e.printStackTrace();
        }
    }

    private static JSONObject loadRulesFromFile() {
        try {
            File externalDir = android.os.Environment.getExternalStorageDirectory();
            File rulesFile = new File(externalDir, "Android/data/" + MODULE_PACKAGE + "/files/camtags_rules.json");
            
            android.util.Log.d(TAG, "Rules file path: " + rulesFile.getAbsolutePath());
            android.util.Log.d(TAG, "Rules file exists: " + rulesFile.exists());
            
            if (!rulesFile.exists()) {
                return null;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(rulesFile));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            
            android.util.Log.d(TAG, "Rules file content: " + json.toString());
            return new JSONObject(json.toString());
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error reading rules file: " + e);
            e.printStackTrace();
            return null;
        }
    }

    private static void applyRulesForPackage(JSONObject allRules, String rulePackage,
                                      CaptureRequest.Builder builder, String logPackage) {
        try {
            if (!allRules.has(rulePackage)) {
                android.util.Log.d(TAG, "No rules found for " + rulePackage);
                return;
            }

            String json = allRules.getString(rulePackage);
            android.util.Log.d(TAG, "JSON for " + rulePackage + ": " + json);
            
            if (json == null || json.isEmpty()) {
                android.util.Log.d(TAG, "Empty JSON for " + rulePackage);
                return;
            }

            List<Rule> rules = Rule.listFromJson(json);
            android.util.Log.d(TAG, "Loaded " + rules.size() + " rules for " + rulePackage);
            for (Rule rule : rules) {
                if (!rule.enabled) continue;
                try {
                    applyRule(builder, rule);
                    android.util.Log.d(TAG, "Applied " + rule.keyName + " from " + rulePackage + " to " + logPackage);
                } catch (Throwable t) {
                    android.util.Log.e(TAG, "failed to apply rule " + rule.keyName
                            + " from " + rulePackage + " to " + logPackage + " - " + t);
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error applying rules for " + rulePackage + ": " + e);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void applyRule(CaptureRequest.Builder builder, Rule rule) throws Exception {
        Class<?> valueClass = classForType(rule.type);
        Object value = parseValue(rule.type, rule.value);

        CaptureRequest.Key<Object> key;
        if (rule.vendorKey) {
            key = new CaptureRequest.Key<>(rule.keyName, (Class<Object>) valueClass);
        } else {
            Field field = CaptureRequest.class.getField(rule.keyName);
            key = (CaptureRequest.Key<Object>) field.get(null);
        }

        builder.set(key, value);
    }

    private static Class<?> classForType(String type) {
        switch (type) {
            case Rule.TYPE_INT: return Integer.class;
            case Rule.TYPE_FLOAT: return Float.class;
            case Rule.TYPE_LONG: return Long.class;
            case Rule.TYPE_BYTE: return Byte.class;
            case Rule.TYPE_BOOLEAN: return Boolean.class;
            case Rule.TYPE_RECT: return Rect.class;
            case Rule.TYPE_INT_ARRAY: return int[].class;
            default: throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private static Object parseValue(String type, String raw) {
        raw = raw.trim();
        switch (type) {
            case Rule.TYPE_INT: return Integer.parseInt(raw);
            case Rule.TYPE_FLOAT: return Float.parseFloat(raw);
            case Rule.TYPE_LONG: return Long.parseLong(raw);
            case Rule.TYPE_BYTE: return Byte.parseByte(raw);
            case Rule.TYPE_BOOLEAN: return Boolean.parseBoolean(raw);
            case Rule.TYPE_RECT: {
                String[] parts = raw.split(",");
                return new Rect(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()),
                        Integer.parseInt(parts[3].trim()));
            }
            case Rule.TYPE_INT_ARRAY: {
                String[] parts = raw.split(",");
                int[] out = new int[parts.length];
                for (int i = 0; i < parts.length; i++) out[i] = Integer.parseInt(parts[i].trim());
                return out;
            }
            default: throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}
