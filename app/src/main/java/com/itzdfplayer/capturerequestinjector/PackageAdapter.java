package com.itzdfplayer.capturerequestinjector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
    private List<String> packages;
    private OnPackageClickListener listener;
    private OnPackageDeleteListener deleteListener;
    private Context context;
    private PackageManager packageManager;

    public PackageAdapter(Context context, List<String> packages, OnPackageClickListener listener) {
        this.context = context;
        this.packages = packages;
        this.listener = listener;
        this.packageManager = context.getPackageManager();
    }

    public void setOnPackageDeleteListener(OnPackageDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String pkg = packages.get(position);
        
        // Load app info
        String appName = pkg;
        Drawable icon = null;
        
        if (pkg.equals("global")) {
            appName = "Global Rules";
            try {
                icon = context.getDrawable(R.drawable.globe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(pkg, 0);
                appName = packageManager.getApplicationLabel(appInfo).toString();
                icon = packageManager.getApplicationIcon(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                // App not installed, use package name as fallback
                appName = pkg;
            }
        }
        
        holder.appName.setText(appName);
        holder.packageName.setText(pkg);
        holder.appIcon.setImageDrawable(icon);
        
        List<Rule> rules = RuleStore.loadRules(context, pkg);
        int enabled = 0;
        for (Rule r : rules) if (r.enabled) enabled++;
        holder.ruleCount.setText(context.getString(R.string.rules_enabled, enabled));
        holder.itemView.setOnClickListener(v -> listener.onClick(pkg));
        
        // Hide delete button for global package
        if (pkg.equals("global")) {
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(position, pkg);
                }
            });
        }
    }

    @Override
    public int getItemCount() { return packages.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName, packageName, ruleCount;
        MaterialButton deleteButton;

        ViewHolder(View v) {
            super(v);
            appIcon = v.findViewById(R.id.appIcon);
            appName = v.findViewById(R.id.appName);
            packageName = v.findViewById(R.id.packageName);
            ruleCount = v.findViewById(R.id.ruleCount);
            deleteButton = v.findViewById(R.id.deleteButton);
        }
    }

    interface OnPackageClickListener {
        void onClick(String packageName);
    }

    interface OnPackageDeleteListener {
        void onDelete(int position, String packageName);
    }
}
