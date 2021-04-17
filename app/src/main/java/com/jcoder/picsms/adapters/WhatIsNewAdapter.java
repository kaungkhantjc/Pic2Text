package com.jcoder.picsms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcoder.picsms.R;
import com.jcoder.picsms.databinding.ItemWhatIsNewBinding;
import com.jcoder.picsms.models.WhatIsNewModel;

import java.util.ArrayList;

public class WhatIsNewAdapter extends RecyclerView.Adapter<WhatIsNewAdapter.PlaceHolder> {
    private final ArrayList<WhatIsNewModel> whatIsNewModels;

    public WhatIsNewAdapter(ArrayList<WhatIsNewModel> whatIsNewModels) {
        this.whatIsNewModels = whatIsNewModels;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_what_is_new, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        WhatIsNewModel model = whatIsNewModels.get(position);
        holder.binding.tvVersion.setText(model.getVersionName());
        holder.binding.tvChanges.setText(model.getChanges());
    }

    @Override
    public int getItemCount() {
        return whatIsNewModels.size();
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        ItemWhatIsNewBinding binding;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemWhatIsNewBinding.bind(itemView);
        }
    }
}
