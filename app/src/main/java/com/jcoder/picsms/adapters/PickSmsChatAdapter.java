package com.jcoder.picsms.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jcoder.picsms.R;
import com.jcoder.picsms.databinding.ItemSmsReceivedBinding;
import com.jcoder.picsms.databinding.ItemSmsSentBinding;
import com.jcoder.picsms.models.Message;
import com.jcoder.picsms.utils.PreciseTime;

import java.util.ArrayList;

public class PickSmsChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<Message> messages;
    private final ArrayList<Boolean> checkedStates;
    private final PreciseTime preciseTime;

    private OnItemCheckChangedListener onItemCheckChangedListener;
    private OnItemLongClickListener onItemLongClickListener;

    public PickSmsChatAdapter(Context context, ArrayList<Message> messages, ArrayList<Boolean> checkedStates) {
        this.context = context;
        this.preciseTime = new PreciseTime(context);
        this.inflater = LayoutInflater.from(context);
        this.messages = messages;
        this.checkedStates = checkedStates;
    }

    private View inflate(ViewGroup parent, int res) {
        return inflater.inflate(res, parent, false);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case 0:
                holder = new SenderViewHolder(inflate(parent, R.layout.item_sms_sent));
                break;

            case 1:
                holder = new ReceiverViewHolder(inflate(parent, R.layout.item_sms_received));
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType() == 1 ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean checkedState = checkedStates.get(position);

        String dateStr = preciseTime.preciseFormat(message.getDate());

        if (holder instanceof SenderViewHolder) {
            SenderViewHolder senderViewHolder = (SenderViewHolder) holder;

            senderViewHolder.binding.tvBody.setText(message.getBody());
            senderViewHolder.binding.tvDate.setText(dateStr);
            senderViewHolder.binding.checkbox.setChecked(checkedState);

            if (checkedState)
                senderViewHolder.binding.card.setCardBackgroundColor(getColor(R.color.number_badge_color));
            else
                senderViewHolder.binding.card.setCardBackgroundColor(getColor(R.color.colorControlNormal));

            if (onItemCheckChangedListener != null)
                senderViewHolder.binding.card.setOnClickListener(v -> {
                    int pos = holder.getAdapterPosition();
                    if (checkedStates.get(pos))
                        onItemCheckChangedListener.onItemUnchecked(pos, messages.get(pos));
                    else onItemCheckChangedListener.onItemChecked(pos, messages.get(pos));
                });

        } else {
            ReceiverViewHolder senderViewHolder = (ReceiverViewHolder) holder;

            senderViewHolder.binding.tvBody.setText(message.getBody());
            senderViewHolder.binding.tvDate.setText(dateStr);
            senderViewHolder.binding.checkbox.setChecked(checkedState);

            if (checkedState)
                senderViewHolder.binding.card.setCardBackgroundColor(getColor(R.color.number_badge_color));
            else
                senderViewHolder.binding.card.setCardBackgroundColor(getColor(R.color.colorControlNormal));

            if (onItemCheckChangedListener != null)
                senderViewHolder.binding.card.setOnClickListener(v -> {
                    int pos = holder.getAdapterPosition();
                    if (checkedStates.get(pos))
                        onItemCheckChangedListener.onItemUnchecked(pos, messages.get(pos));
                    else onItemCheckChangedListener.onItemChecked(pos, messages.get(pos));
                });
        }
    }

    private int getColor(int res) {
        return ResourcesCompat.getColor(context.getResources(), res, null);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        ItemSmsSentBinding binding;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSmsSentBinding.bind(itemView);

            binding.card.setOnLongClickListener(view -> {
                if (onItemLongClickListener != null)
                    onItemLongClickListener.onLongClicked(messages.get(getAdapterPosition()).getBody());
                return true;
            });
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemSmsReceivedBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSmsReceivedBinding.bind(itemView);
            binding.card.setOnLongClickListener(view -> {
                if (onItemLongClickListener != null)
                    onItemLongClickListener.onLongClicked(messages.get(getAdapterPosition()).getBody());
                return true;
            });
        }
    }

    public interface OnItemCheckChangedListener {
        void onItemChecked(int position, Message message);

        void onItemUnchecked(int position, Message message);
    }

    public interface OnItemLongClickListener {
        void onLongClicked(String body);
    }

    public void setOnItemCheckChangedListener(OnItemCheckChangedListener onItemCheckChangedListener) {
        this.onItemCheckChangedListener = onItemCheckChangedListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
}
