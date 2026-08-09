package com.itzdfplayer.capturerequestinjector;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class DisableGlobalRulesTileService extends TileService {
    private static final String PREFS_SETTINGS_NAME = "camtags_settings";
    private static final String KEY_DISABLE_GLOBAL_RULES = "disable_global_rules";
    
    @Override
    public void onClick() {
        super.onClick();
        
        SharedPreferences prefs = getSharedPreferences(PREFS_SETTINGS_NAME, MODE_PRIVATE);
        boolean currentState = prefs.getBoolean(KEY_DISABLE_GLOBAL_RULES, false);
        boolean newState = !currentState;
        
        prefs.edit().putBoolean(KEY_DISABLE_GLOBAL_RULES, newState).apply();
        updateTile();
    }
    
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }
    
    private void updateTile() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SETTINGS_NAME, MODE_PRIVATE);
        boolean isDisabled = prefs.getBoolean(KEY_DISABLE_GLOBAL_RULES, false);
        
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(isDisabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setLabel(getString(R.string.disable_global_rules));
            tile.updateTile();
        }
    }
}
