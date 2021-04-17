package com.jcoder.picsms.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcoder.picsms.R;
import com.jcoder.picsms.databinding.ItemTextBinding;
import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class TextAdapter extends RecyclerView.Adapter<TextAdapter.PlaceHolder> {

    private final Context context;
    private final ArrayList<Boolean> sentStates;
    private OnItemLongClickedListener onItemLongClickedListener;
    private OnCopyClickedListener onCopyClickedListener;

    public TextAdapter(Context context, ArrayList<Boolean> sentStates) {
        this.context = context;
        this.sentStates = sentStates;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        boolean smsSent = sentStates.get(position);

        holder.binding.tvSentBadge.setText(R.string.sms_sent);
        holder.binding.tvSentBadge.setVisibility(smsSent ? View.VISIBLE : View.INVISIBLE);

        holder.binding.tvNumber.setText(String.format(Locale.getDefault(), "%d", (position + 1)));
        if (position == Constants.list.length - 1) {
            // calculating chunk count is wasting memory
            // So I only count chunks of last item
            holder.binding.tvChunk.setText(String.format(Locale.getDefault(), context.getString(R.string.chunks), ChunkUtils.getChunkCount(Constants.list[position])));
        } else {
            holder.binding.tvChunk.setText(String.format(Locale.getDefault(), context.getString(R.string.chunks), 8));
        }

        if (onCopyClickedListener != null)
            holder.binding.ivCopy.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                onCopyClickedListener.onCopyClicked(ChunkUtils.getSortedText(pos, Constants.list[pos]));
            });

        if (onItemLongClickedListener != null)
            holder.itemView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                onItemLongClickedListener.onItemLongClicked(ChunkUtils.getSortedText(pos, Constants.list[pos]));
                return true;
            });
    }

    @Override
    public int getItemCount() {
        // Constants.list become null in SendSmsActivity @onDestroy method
        // When user closes activity while recycler is scrolling, getItemCount method is still calling
        // so we need to handle like this
        return Constants.list != null ? Constants.list.length : 0;
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        ItemTextBinding binding;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemTextBinding.bind(itemView);
            // just for ripple effect
            itemView.setOnClickListener(v -> {
            });
        }
    }

    public interface OnItemLongClickedListener {
        void onItemLongClicked(String text);
    }

    public interface OnCopyClickedListener {
        void onCopyClicked(String text);
    }

    public void setOnCopyClickedListener(OnCopyClickedListener onCopyClickedListener) {
        this.onCopyClickedListener = onCopyClickedListener;
    }

    public void setOnItemLongClickedListener(OnItemLongClickedListener onItemLongClickedListener) {
        this.onItemLongClickedListener = onItemLongClickedListener;
    }
}
