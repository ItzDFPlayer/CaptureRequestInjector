package com.itzdfplayer.capturerequestinjector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PackageAdapter adapter;
    private List<String> packageList = new ArrayList<>();
    private SharedPreferences prefs;
    private SharedPreferences settingsPrefs;
    
    private static final String PREFS_SETTINGS_NAME = "camtags_settings";
    private static final String KEY_DISABLE_GLOBAL_RULES = "disable_global_rules";
    private static final String KEY_DISABLE_ALL_RULES = "disable_all_rules";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(RuleStore.PREFS_NAME, MODE_PRIVATE);
        settingsPrefs = getSharedPreferences(PREFS_SETTINGS_NAME, MODE_PRIVATE);

        // Set status bar based on system theme
        setStatusBarBasedOnTheme();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this::onToolbarMenuItemClick);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PackageAdapter(this, packageList, this::onPackageClicked);
        adapter.setOnPackageDeleteListener(this::onPackageDelete);
        recyclerView.setAdapter(adapter);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddPackageDialog());

        ExtendedFloatingActionButton fabGlobal = findViewById(R.id.fabGlobal);
        fabGlobal.setOnClickListener(v -> {
            Intent intent = new Intent(this, RuleEditorActivity.class);
            intent.putExtra("packageName", "global");
            startActivity(intent);
        });

        FloatingActionButton fabSettings = findViewById(R.id.fabSettings);
        fabSettings.setOnClickListener(v -> showSettingsDialog());

        loadPackageList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh adapter to update rule counts when returning from RuleEditorActivity
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private boolean onToolbarMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            showInfoDialog();
            return true;
        }
        return false;
    }

    private void showInfoDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_info, null);
        
        TextView messageText = dialogView.findViewById(R.id.messageText);
        TextView githubLink = dialogView.findViewById(R.id.githubLink);
        
        messageText.setText(R.string.app_info_text);
        githubLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url)));
            startActivity(intent);
        });
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_info_title)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void setStatusBarBasedOnTheme() {
        int nightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                isDarkMode ? 0 : android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (isDarkMode) {
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void onPackageDelete(int position, String packageName) {
        if (packageName.equals("global")) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.cannot_delete_global)
                    .setMessage(R.string.cannot_delete_global_message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_package)
                .setMessage(getString(R.string.delete_package_message, packageName))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    prefs.edit().remove(packageName).apply();
                    packageList.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadPackageList() {
        Map<String, ?> all = prefs.getAll();
        packageList.clear();
        for (String key : all.keySet()) {
            // Skip "global" since it has its own dedicated button
            if (!key.equals("global")) {
                packageList.add(key);
            }
        }
        Collections.sort(packageList);
        adapter.notifyDataSetChanged();
    }

    private void onPackageClicked(String packageName) {
        Intent intent = new Intent(this, RuleEditorActivity.class);
        intent.putExtra("packageName", packageName);
        startActivity(intent);
    }

    private void showAddPackageDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_app_list, null);
        TextInputEditText searchInput = dialogView.findViewById(R.id.searchInput);
        MaterialSwitch showSystemApps = dialogView.findViewById(R.id.showSystemApps);
        RecyclerView appRecyclerView = dialogView.findViewById(R.id.appRecyclerView);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_package)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .create();
        
        PackageManager pm = getPackageManager();
        AppListAdapter adapter = new AppListAdapter(this, pm, packageName -> {
            RuleStore.saveRules(this, packageName, new ArrayList<>());
            loadPackageList();
            dialog.dismiss();
        });
        
        appRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appRecyclerView.setAdapter(adapter);
        
        // Load initial apps (non-system only)
        loadApps(adapter, showSystemApps.isChecked());
        
        showSystemApps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadApps(adapter, isChecked);
            adapter.filter(searchInput.getText().toString());
        });
        
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        dialog.show();
    }
    
    private void loadApps(AppListAdapter adapter, boolean showSystemApps) {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        adapter.setApps(apps, showSystemApps);
    }

    private void scanCameraApps() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (PackageInfo info : packages) {
            if (info.requestedPermissions != null) {
                for (String perm : info.requestedPermissions) {
                    if (perm.equals(android.Manifest.permission.CAMERA)) {
                        if (!packageList.contains(info.packageName)) {
                            RuleStore.saveRules(this, info.packageName, new ArrayList<>());
                        }
                        break;
                    }
                }
            }
        }
        loadPackageList();
    }

    private void showSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        
        MaterialSwitch switchDisableGlobalRules = dialogView.findViewById(R.id.switchDisableGlobalRules);
        MaterialSwitch switchDisableAllRules = dialogView.findViewById(R.id.switchDisableAllRules);
        
        // Load current settings
        switchDisableGlobalRules.setChecked(settingsPrefs.getBoolean(KEY_DISABLE_GLOBAL_RULES, false));
        switchDisableAllRules.setChecked(settingsPrefs.getBoolean(KEY_DISABLE_ALL_RULES, false));
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    // Save settings
                    settingsPrefs.edit()
                            .putBoolean(KEY_DISABLE_GLOBAL_RULES, switchDisableGlobalRules.isChecked())
                            .putBoolean(KEY_DISABLE_ALL_RULES, switchDisableAllRules.isChecked())
                            .apply();
                    // Trigger JSON file update
                    RuleStore.saveRulesToFile(this);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}