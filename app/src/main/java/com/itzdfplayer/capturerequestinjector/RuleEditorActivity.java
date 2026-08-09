package com.itzdfplayer.capturerequestinjector;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class RuleEditorActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RuleAdapter adapter;
    private List<Rule> rules = new ArrayList<>();
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_editor);
        packageName = getIntent().getStringExtra("packageName");
        setTitle(packageName);

        // Set status bar based on system theme
        setStatusBarBasedOnTheme();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.rules);
        toolbar.setSubtitle(packageName);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RuleAdapter(rules, this::onRuleChanged);
        adapter.setOnRuleClickListener(this::onRuleClicked);
        recyclerView.setAdapter(adapter);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddRuleDialog());

        loadRules();
    }

    private void loadRules() {
        rules.clear();
        rules.addAll(RuleStore.loadRules(this, packageName));
        adapter.notifyDataSetChanged();
    }

    private void saveRules() {
        RuleStore.saveRules(this, packageName, rules);
    }

    private void onRuleChanged() {
        saveRules();
    }

    private void onRuleClicked(int position, Rule rule) {
        showEditRuleDialog(position, rule);
    }

    private void showAddRuleDialog() {
        showRuleDialog(-1, null);
    }

    private void showEditRuleDialog(int position, Rule rule) {
        showRuleDialog(position, rule);
    }

    private void showRuleDialog(int position, Rule existingRule) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_rule, null);

        MaterialSwitch vendorKey = view.findViewById(R.id.vendorKey);
        AutoCompleteTextView tagSpinner = view.findViewById(R.id.tagSpinner);
        com.google.android.material.textfield.TextInputLayout keyNameLayout = view.findViewById(R.id.keyNameLayout);
        TextInputEditText keyName = view.findViewById(R.id.keyName);
        AutoCompleteTextView typeSpinner = view.findViewById(R.id.typeSpinner);
        TextInputEditText value = view.findViewById(R.id.value);
        MaterialSwitch enabled = view.findViewById(R.id.enabled);

        // Setup tag spinner
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                TagPresets.getAllTags(this));
        tagSpinner.setAdapter(tagAdapter);

        // Setup type spinner
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{Rule.TYPE_INT, Rule.TYPE_FLOAT, Rule.TYPE_LONG,
                        Rule.TYPE_BYTE, Rule.TYPE_BOOLEAN, Rule.TYPE_RECT, Rule.TYPE_INT_ARRAY});
        typeSpinner.setAdapter(typeAdapter);

        // Tag selection listener
        tagSpinner.setOnItemClickListener((parent, view1, pos, id) -> {
            String selectedTag = tagSpinner.getText().toString();
            if (TagPresets.isCustomTag(selectedTag, this)) {
                // CUSTOM selected - show key name field
                keyNameLayout.setVisibility(View.VISIBLE);
                keyName.setEnabled(true);
            } else {
                // Preset tag selected - auto-fill and disable key name field
                keyNameLayout.setVisibility(View.VISIBLE);
                keyName.setEnabled(false);
                keyName.setText(selectedTag);
                
                // Auto-select type
                String type = TagPresets.getTypeForTag(selectedTag);
                if (type != null) {
                    typeSpinner.setText(type, false);
                }
            }
        });

        // Pre-fill if editing
        if (existingRule != null) {
            vendorKey.setChecked(existingRule.vendorKey);
            keyName.setText(existingRule.keyName);
            typeSpinner.setText(existingRule.type, false);
            value.setText(existingRule.value);
            enabled.setChecked(existingRule.enabled);
            
            // Try to match existing key name to a preset
            if (!TagPresets.isCustomTag(existingRule.keyName, this)) {
                tagSpinner.setText(existingRule.keyName, false);
                keyName.setEnabled(false);
            } else {
                tagSpinner.setText(getString(R.string.custom), false);
                keyName.setEnabled(true);
            }
            
            builder.setTitle(R.string.edit_rule);
        } else {
            // Default to CUSTOM for new rules
            tagSpinner.setText(getString(R.string.custom), false);
            keyName.setEnabled(true);
            builder.setTitle(R.string.add_rule);
        }

        builder.setView(view);
        builder.setPositiveButton(position >= 0 ? R.string.save : R.string.add, (dialog, which) -> {
            Rule rule = existingRule != null ? existingRule : new Rule();
            rule.vendorKey = vendorKey.isChecked();
            rule.keyName = keyName.getText().toString().trim();
            rule.type = typeSpinner.getText().toString().trim();
            rule.value = value.getText().toString().trim();
            rule.enabled = enabled.isChecked();

            if (position >= 0) {
                rules.set(position, rule);
            } else {
                rules.add(rule);
            }
            saveRules();
            adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton(R.string.cancel, null);
        
        AlertDialog dialog = builder.show();
        
        // Prevent dropdown flicker by setting touch listener on dialog window
        dialog.getWindow().getDecorView().setOnTouchListener((v, event) -> {
            tagSpinner.dismissDropDown();
            typeSpinner.dismissDropDown();
            return false;
        });
    }

    private void setStatusBarBasedOnTheme() {
        int nightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                isDarkMode ? 0 : android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (isDarkMode) {
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }
}