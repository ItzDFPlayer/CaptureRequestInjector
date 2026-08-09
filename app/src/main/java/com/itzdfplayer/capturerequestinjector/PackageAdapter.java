package com.itzdfplayer.capturerequestinjector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
    private List<String> packages;
    private OnPackageClickListener listener;
    private OnPackageDeleteListener deleteListener;

    public PackageAdapter(List<String> packages, OnPackageClickListener listener) {
        this.packages = packages;
        this.listener = listener;
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
        holder.packageName.setText(pkg);
        Context ctx = holder.itemView.getContext();
        List<Rule> rules = RuleStore.loadRules(ctx, pkg);
        int enabled = 0;
        for (Rule r : rules) if (r.enabled) enabled++;
        holder.ruleCount.setText("Rules: " + enabled + " enabled");
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
        TextView packageName, ruleCount;
        MaterialButton deleteButton;

        ViewHolder(View v) {
            super(v);
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
