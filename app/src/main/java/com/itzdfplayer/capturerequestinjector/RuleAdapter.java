package com.itzdfplayer.capturerequestinjector;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.ViewHolder> {
    private List<Rule> rules;
    private OnRuleChangeListener listener;
    private OnRuleClickListener clickListener;

    public RuleAdapter(List<Rule> rules, OnRuleChangeListener listener) {
        this.rules = rules;
        this.listener = listener;
    }

    public void setOnRuleClickListener(OnRuleClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Rule rule = rules.get(position);
        holder.keyName.setText(rule.keyName);
        holder.details.setText(rule.type + " = " + rule.value);
        holder.enabledSwitch.setChecked(rule.enabled);
        holder.enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rule.enabled = isChecked;
            listener.onRuleChanged();
        });
        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                rules.remove(adapterPosition);
                listener.onRuleChanged();
                notifyItemRemoved(adapterPosition);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRuleClick(position, rule);
            }
        });
    }

    @Override
    public int getItemCount() { return rules.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView keyName, details;
        MaterialSwitch enabledSwitch;
        MaterialButton deleteButton;

        ViewHolder(View v) {
            super(v);
            keyName = v.findViewById(R.id.keyName);
            details = v.findViewById(R.id.details);
            enabledSwitch = v.findViewById(R.id.enabledSwitch);
            deleteButton = v.findViewById(R.id.deleteButton);
        }
    }

    interface OnRuleChangeListener {
        void onRuleChanged();
    }

    interface OnRuleClickListener {
        void onRuleClick(int position, Rule rule);
    }
}