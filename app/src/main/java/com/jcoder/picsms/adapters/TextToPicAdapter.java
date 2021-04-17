package com.jcoder.picsms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.jcoder.picsms.R;
import com.jcoder.picsms.models.CodePart;

import java.util.ArrayList;

public class TextToPicAdapter extends RecyclerView.Adapter<TextToPicAdapter.PlaceHolder> {

    private OnItemLongClickedListener onItemLongClickedListener;
    private OnDeleteClickedListener onDeleteClickedListener;

    private final ArrayList<CodePart> codeList;

    public TextToPicAdapter(ArrayList<CodePart> codeList) {
        this.codeList = codeList;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_to_pic, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        CodePart model = codeList.get(position);
        holder.tvNumber.setText(model.getFormattedOrder());
        holder.tvCode.setText(model.getCode());

        if (onDeleteClickedListener != null)
            holder.ivDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                onDeleteClickedListener.onDeleteClicked(pos, codeList.get(pos).getFormattedOrder());
            });

        if (onItemLongClickedListener != null)
            holder.itemView.setOnLongClickListener(v -> {
                onItemLongClickedListener.onItemLongClicked(codeList.get(holder.getAdapterPosition()).getCode());
                return true;
            });

    }

    @Override
    public int getItemCount() {
        return codeList.size();
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        MaterialTextView tvNumber, tvCode;
        AppCompatImageView ivDelete;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.item_text_to_pic_tv_number);
            tvCode = itemView.findViewById(R.id.item_text_to_pic_tv_code);
            ivDelete = itemView.findViewById(R.id.item_text_to_pic_iv_delete);
        }
    }

    public interface OnItemLongClickedListener {
        void onItemLongClicked(String text);
    }

    public interface OnDeleteClickedListener {
        void onDeleteClicked(int position, String orderName);
    }

    public void setOnItemLongClickedListener(OnItemLongClickedListener onItemLongClickedListener) {
        this.onItemLongClickedListener = onItemLongClickedListener;
    }

    public void setOnDeleteClickedListener(OnDeleteClickedListener onDeleteClickedListener) {
        this.onDeleteClickedListener = onDeleteClickedListener;
    }
}
