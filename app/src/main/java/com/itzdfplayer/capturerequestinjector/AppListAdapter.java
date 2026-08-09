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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
    private List<AppInfo> appList = new ArrayList<>();
    private List<AppInfo> filteredList = new ArrayList<>();
    private PackageManager packageManager;
    private Context context;
    private OnAppClickListener listener;
    private Set<String> priorityPackages = new HashSet<>();

    public interface OnAppClickListener {
        void onAppClick(String packageName);
    }

    public AppListAdapter(Context context, PackageManager packageManager, OnAppClickListener listener) {
        this.context = context;
        this.packageManager = packageManager;
        this.listener = listener;
        loadPriorityPackages();
    }

    private void loadPriorityPackages() {
        // Hardcode priority packages from scope.list
        priorityPackages.add("com.android.MGC_9_6_080");
        priorityPackages.add("com.google.android.GoogleCameraEng");
        priorityPackages.add("com.android.camera");
        priorityPackages.add("org.lineageos.aperture");
        priorityPackages.add("com.meitu.meiyancamera");
        priorityPackages.add("com.samsung.android.scan3d");
        priorityPackages.add("com.android.MGC_9_7_047");
        priorityPackages.add("com.agc.gcamcom.qualcomm.qti.chromatixmobile");
        priorityPackages.add("com.ss.android.ugc.aweme");
        priorityPackages.add("com.motioncam");
        priorityPackages.add("com.riseupgames.proshot2");
        priorityPackages.add("com.niksoftware.snapseed");
        priorityPackages.add("com.vwfndr.mbl");
        priorityPackages.add("org.codeaurora.snapcam");
        priorityPackages.add("com.google.android.apps.googlecamera.fishfood");
        priorityPackages.add("com.android.MGC_9_4_103");
        priorityPackages.add("com.android.MGC_9_3_160");
        priorityPackages.add("com.android.MGC_9_2_113");
        priorityPackages.add("com.agc.gcam96");
        priorityPackages.add("com.samsung.agc.gcam96");
        priorityPackages.add("com.agc.gcam");
        priorityPackages.add("com.agc.gcam92");
        priorityPackages.add("com.agc.gcam88");
        priorityPackages.add("com.agc.gcam87");
        priorityPackages.add("com.agc.gcam85");
        priorityPackages.add("com.agc.gcam84");
        priorityPackages.add("org.codeaurora.qcamera3");
        priorityPackages.add("com.sec.factory.cameralyzer");
        priorityPackages.add("com.fintech.life");
        priorityPackages.add("com.lmc.fan.edition");
        priorityPackages.add("com.camera.LMC83_R3");
        priorityPackages.add("com.samsung.android.ruler");
        priorityPackages.add("com.motioncam");
        priorityPackages.add("com.motioncam.pro");
        android.util.Log.d("AppListAdapter", "Loaded " + priorityPackages.size() + " priority packages");
    }

    public void setApps(List<ApplicationInfo> apps, boolean showSystemApps) {
        appList.clear();
        for (ApplicationInfo app : apps) {
            boolean isSystem = (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isPriority = priorityPackages.contains(app.packageName);
            
            // Show if: showSystemApps is true, OR it's not a system app, OR it's in priority list
            if (showSystemApps || !isSystem || isPriority) {
                String appName = packageManager.getApplicationLabel(app).toString();
                String packageName = app.packageName;
                Drawable icon = packageManager.getApplicationIcon(app);
                appList.add(new AppInfo(appName, packageName, icon, isPriority));
            }
        }
        
        // Sort: priority apps first, then alphabetically
        Collections.sort(appList, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a1, AppInfo a2) {
                if (a1.isPriority && !a2.isPriority) return -1;
                if (!a1.isPriority && a2.isPriority) return 1;
                return a1.name.compareToIgnoreCase(a2.name);
            }
        });
        
        filteredList = new ArrayList<>(appList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(appList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (AppInfo app : appList) {
                if (app.name.toLowerCase().contains(lowerQuery) || 
                    app.packageName.toLowerCase().contains(lowerQuery)) {
                    filteredList.add(app);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_list, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = filteredList.get(position);
        holder.appName.setText(app.name);
        holder.appPackage.setText(app.packageName);
        holder.appIcon.setImageDrawable(app.icon);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppClick(app.packageName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView appPackage;

        AppViewHolder(View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            appPackage = itemView.findViewById(R.id.appPackage);
        }
    }

    static class AppInfo {
        String name;
        String packageName;
        Drawable icon;
        boolean isPriority;

        AppInfo(String name, String packageName, Drawable icon, boolean isPriority) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
            this.isPriority = isPriority;
        }
    }
}
