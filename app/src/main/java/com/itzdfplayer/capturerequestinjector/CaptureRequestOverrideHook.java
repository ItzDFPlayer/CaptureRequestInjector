package com.itzdfplayer.capturerequestinjector;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.hardware.camera2.CaptureRequest;
import android.util.Log;

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
@SuppressLint({ "PrivateApi", "BlockedPrivateApi" })
public class CaptureRequestOverrideHook extends XposedModule {

    private static final String TAG = "CamTags";
    private static final String MODULE_PACKAGE = "com.itzdfplayer.capturerequestinjector";

    @Override
    public void onModuleLoaded(@NonNull XposedModuleInterface.ModuleLoadedParam param) {
        super.onModuleLoaded(param);
        log(Log.INFO, TAG, "Module loaded successfully");
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

        log(Log.INFO, TAG, "onPackageReady called for: " + packageName);

        // Hook CaptureRequest.Builder.build for all packages
        try {
            Class<?> builderClass = param.getClassLoader().loadClass("android.hardware.camera2.CaptureRequest$Builder");
            var buildMethod = builderClass.getDeclaredMethod("build");

            hook(buildMethod).intercept(new CaptureRequestHooker(packageName));

            log(Log.INFO, TAG, "Successfully hooked CaptureRequest.Builder.build for " + packageName);
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "Failed to hook CaptureRequest.Builder.build for " + packageName, t);
        }
    }

    private class CaptureRequestHooker implements XposedInterface.Hooker {
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
                log(Log.ERROR, TAG, "Error applying rules for " + targetPackage, t);
            }
            return chain.proceed();
        }
    }

    private void applyRules(String targetPackage, CaptureRequest.Builder builder) {
        try {
            JSONObject allRules = loadRulesFromFile(targetPackage);
            if (allRules == null) {
                log(Log.DEBUG, TAG, "No rules file found");
                return;
            }

            // Check if all rules are disabled
            boolean disableAllRules = false;
            boolean disableGlobalRules = false;

            if (allRules.has("settings")) {
                JSONObject settings = allRules.getJSONObject("settings");
                disableAllRules = settings.optBoolean("disable_all_rules", false);
                disableGlobalRules = settings.optBoolean("disable_global_rules", false);
                log(Log.DEBUG, TAG, "Settings - disableAllRules: " + disableAllRules + ", disableGlobalRules: " + disableGlobalRules);
            }

            if (disableAllRules) {
                log(Log.DEBUG, TAG, "All rules are disabled, skipping rule application");
                return;
            }

            // Apply global rules first (baseline) - unless disabled
            if (!disableGlobalRules) {
                applyRulesForPackage(allRules, "global", builder, targetPackage);
            } else {
                log(Log.DEBUG, TAG, "Global rules are disabled, skipping global rules");
            }
            // Apply package-specific rules second (overrides global for same keys)
            applyRulesForPackage(allRules, targetPackage, builder, targetPackage);
        } catch (Exception e) {
            log(Log.ERROR, TAG, "Error loading rules: " + e, e);
        }
    }

    private JSONObject loadRulesFromFile(String targetPackage) {
        try {
            // Read from the app's data directory (copied via root)
            File rulesFile = new File("/data/data/" + targetPackage + "/files/camtags_rules.json");

            log(Log.DEBUG, TAG, "Rules file path: " + rulesFile.getAbsolutePath());
            log(Log.DEBUG, TAG, "Rules file exists: " + rulesFile.exists());

            if (!rulesFile.exists()) {
                log(Log.DEBUG, TAG, "No rules file found");
                return null;
            }

            BufferedReader reader = new BufferedReader(new FileReader(rulesFile));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            log(Log.DEBUG, TAG, "Rules file content: " + json.toString());
            return new JSONObject(json.toString());
        } catch (Exception e) {
            log(Log.ERROR, TAG, "Error reading rules file: " + e, e);
            return null;
        }
    }

    private void applyRulesForPackage(JSONObject allRules, String rulePackage,
            CaptureRequest.Builder builder, String logPackage) {
        try {
            if (!allRules.has(rulePackage)) {
                log(Log.DEBUG, TAG, "No rules found for " + rulePackage);
                return;
            }

            String json = allRules.getString(rulePackage);
            log(Log.DEBUG, TAG, "JSON for " + rulePackage + ": " + json);

            if (json == null || json.isEmpty()) {
                log(Log.DEBUG, TAG, "Empty JSON for " + rulePackage);
                return;
            }

            List<Rule> rules = Rule.listFromJson(json);
            log(Log.DEBUG, TAG, "Loaded " + rules.size() + " rules for " + rulePackage);
            for (Rule rule : rules) {
                if (!rule.enabled)
                    continue;
                try {
                    applyRule(builder, rule);
                    log(Log.DEBUG, TAG, "Applied " + rule.keyName + " from " + rulePackage + " to " + logPackage);
                } catch (Throwable t) {
                    log(Log.ERROR, TAG, "failed to apply rule " + rule.keyName
                            + " from " + rulePackage + " to " + logPackage, t);
                }
            }
        } catch (Exception e) {
            log(Log.ERROR, TAG, "Error applying rules for " + rulePackage + ": " + e, e);
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
            case Rule.TYPE_INT:
                return Integer.class;
            case Rule.TYPE_FLOAT:
                return Float.class;
            case Rule.TYPE_LONG:
                return Long.class;
            case Rule.TYPE_BYTE:
                return Byte.class;
            case Rule.TYPE_BOOLEAN:
                return Boolean.class;
            case Rule.TYPE_RECT:
                return Rect.class;
            case Rule.TYPE_INT_ARRAY:
                return int[].class;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private static Object parseValue(String type, String raw) {
        raw = raw.trim();
        switch (type) {
            case Rule.TYPE_INT:
                return Integer.parseInt(raw);
            case Rule.TYPE_FLOAT:
                return Float.parseFloat(raw);
            case Rule.TYPE_LONG:
                return Long.parseLong(raw);
            case Rule.TYPE_BYTE:
                return Byte.parseByte(raw);
            case Rule.TYPE_BOOLEAN:
                return Boolean.parseBoolean(raw);
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
                for (int i = 0; i < parts.length; i++)
                    out[i] = Integer.parseInt(parts[i].trim());
                return out;
            }
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}
