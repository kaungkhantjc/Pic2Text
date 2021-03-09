package com.jcoder.picsms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.jcoder.picsms.R;
import com.jcoder.picsms.models.Conversation;
import com.jcoder.picsms.models.Message;

import java.util.ArrayList;

public class PickSmsHomeAdapter extends RecyclerView.Adapter<PickSmsHomeAdapter.PlaceHolder> {

    private final ArrayList<Conversation> conversations;
    private OnItemClickedListener onItemClickedListener;

    public PickSmsHomeAdapter(ArrayList<Conversation> conversations) {
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pick_sms_home, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.tvAddress.setText(conversation.getAddress());
        holder.tvBody.setText(conversation.getBody());
        holder.tvDate.setText(conversation.getDate());

        if (onItemClickedListener != null)
            holder.itemView.setOnClickListener(v -> {
                Conversation conversation1 = conversations.get(holder.getAdapterPosition());
                onItemClickedListener.onItemClicked(conversation1.getAddress(), conversation1.getMessages());
            });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        MaterialTextView tvAddress, tvBody, tvDate;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.item_pick_sms_home_tv_address);
            tvBody = itemView.findViewById(R.id.item_pick_sms_home_tv_body);
            tvDate = itemView.findViewById(R.id.item_pick_sms_home_tv_date);
        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(String address, ArrayList<Message> messages);
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }
}
