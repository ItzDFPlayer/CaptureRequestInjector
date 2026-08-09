package com.itzdfplayer.capturerequestinjector;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RuleProvider extends ContentProvider {
    private static final String TAG = "RuleProvider";
    private static final String AUTHORITY = "com.itzdfplayer.capturerequestinjector.provider";
    private static final String PATH_RULE = "rule";
    
    private static final int RULE = 1;
    
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    static {
        uriMatcher.addURI(AUTHORITY, PATH_RULE, RULE);
    }
    
    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {

        Log.i(TAG, "Query called with URI: " + uri);

        if (uriMatcher.match(uri) != RULE) {
            Log.e(TAG, "Unknown URI: " + uri);
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        String packageName = uri.getQueryParameter("pkg");

        Log.i(TAG, "Query for package: " + packageName);
        Log.i(TAG, "Context: " + (getContext() != null ? getContext().getPackageName() : "null"));

        if (packageName == null) {
            Log.w(TAG, "No package parameter");
            return null;
        }

        String json = RuleStore.loadRulesJson(getContext(), packageName);

        Log.i(TAG, "Stored JSON: " + json);

        MatrixCursor cursor = new MatrixCursor(new String[]{"json"});
        cursor.addRow(new Object[]{json == null ? "" : json});

        Log.i(TAG, "Returning cursor with " + cursor.getCount() + " rows");

        return cursor;
    }
    
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if (uriMatcher.match(uri) == RULE) {
            return "vnd.android.cursor.item/json";
        }
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported");
    }
    
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }
    
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                     @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }
}
