package com.jcoder.picsms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.jcoder.picsms.R;
import com.jcoder.picsms.models.SmsDetailModel;

import java.util.List;

public class SmsDetailAdapter extends RecyclerView.Adapter<SmsDetailAdapter.PlaceHolder> {

    private final List<SmsDetailModel> smsDetails;

    public SmsDetailAdapter(List<SmsDetailModel> smsDetails) {
        this.smsDetails = smsDetails;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms_detail, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        SmsDetailModel model = smsDetails.get(position);
        holder.tvKey.setText(model.getKey());
        holder.tvValue.setText(model.getValue());
        if (model.getColor() != -1)
            holder.cardView.setCardBackgroundColor(model.getColor());
    }

    @Override
    public int getItemCount() {
        return smsDetails.size();
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        MaterialTextView tvKey, tvValue;
        MaterialCardView cardView;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            tvKey = itemView.findViewById(R.id.item_sms_detail_tv_key);
            tvValue = itemView.findViewById(R.id.item_sms_detail_tv_value);
            cardView = (MaterialCardView) itemView;
        }
    }

}
