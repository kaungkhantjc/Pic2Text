package com.jcoder.picsms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.jcoder.picsms.R;
import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.Constants;

import java.util.Locale;

public class TextAdapter extends RecyclerView.Adapter<TextAdapter.PlaceHolder> {

    private OnItemLongClickedListener onItemLongClickedListener;
    private OnCopyClickedListener onCopyClickedListener;

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {

        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvChunk.setText(String.format(Locale.ENGLISH, "Chunks : %d", ChunkUtils.getChunkCount(Constants.list[position])));

        if (onCopyClickedListener != null)
            holder.ivCopy.setOnClickListener(v -> {
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
        return Constants.list.length;
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        MaterialTextView tvNumber, tvChunk;
        AppCompatImageView ivCopy;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.item_text_tv_number);
            tvChunk = itemView.findViewById(R.id.item_text_tv_chunk);
            ivCopy = itemView.findViewById(R.id.item_text_iv_copy);
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
