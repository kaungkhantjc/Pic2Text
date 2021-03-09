package com.jcoder.picsms.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.jcoder.picsms.R;
import com.jcoder.picsms.models.Message;

import java.util.ArrayList;

public class PickSmsChatAdapter extends RecyclerView.Adapter<PickSmsChatAdapter.PlaceHolder> {

    private final Context context;
    private final ArrayList<Message> messages;
    private final ArrayList<Boolean> checkedStates;

    private OnItemCheckChangedListener onItemCheckChangedListener;

    public PickSmsChatAdapter(Context context, ArrayList<Message> messages, ArrayList<Boolean> checkedStates) {
        this.context = context;
        this.messages = messages;
        this.checkedStates = checkedStates;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pick_sms_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        Message message = messages.get(position);
        holder.tvBody.setText(message.getBody());
        holder.tvDate.setText(message.getDate());
        holder.checkBox.setChecked(checkedStates.get(position));

        if (message.getType() != 1)
            holder.cardView.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.number_badge_color, null));

        if (onItemCheckChangedListener != null)
            holder.cardView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                onItemCheckChangedListener.onItemCheckChanged(pos, messages.get(pos));
            });

    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        MaterialTextView tvBody, tvDate;
        MaterialCheckBox checkBox;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvBody = itemView.findViewById(R.id.item_pick_sms_chat_tvBody);
            tvDate = itemView.findViewById(R.id.item_pick_sms_chat_tvDate);
            checkBox = itemView.findViewById(R.id.item_pick_sms_chat_checkbox);
        }
    }

    public interface OnItemCheckChangedListener {
        void onItemCheckChanged(int position, Message message);
    }

    public void setOnItemCheckChangedListener(OnItemCheckChangedListener onItemCheckChangedListener) {
        this.onItemCheckChangedListener = onItemCheckChangedListener;
    }
}
