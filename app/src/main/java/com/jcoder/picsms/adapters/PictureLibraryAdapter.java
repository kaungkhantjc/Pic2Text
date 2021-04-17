package com.jcoder.picsms.adapters;

import android.content.Context;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jcoder.picsms.R;
import com.jcoder.picsms.databinding.ItemPictureLibraryBinding;
import com.jcoder.picsms.models.PictureLibraryItem;

import java.util.ArrayList;
import java.util.Locale;

public class PictureLibraryAdapter extends RecyclerView.Adapter<PictureLibraryAdapter.PlaceHolder> {

    private final Context context;
    private final ArrayList<PictureLibraryItem> codeLibraryItems;

    public static final int MENU_OPEN = 100;
    public static final int MENU_DELETE = 101;
    public static final int MENU_PATH = 102;

    private OnItemClickListener onItemClickListener;
    private int position;

    public PictureLibraryAdapter(Context context, ArrayList<PictureLibraryItem> codeLibraryItems) {
        this.context = context;
        this.codeLibraryItems = codeLibraryItems;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(context).inflate(R.layout.item_picture_library, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        PictureLibraryItem codeLibraryItem = codeLibraryItems.get(position);
        holder.binding.tvFileName.setText(codeLibraryItem.getFileName());

        int progress = codeLibraryItem.getSendingSmsPosition();
        int max = codeLibraryItem.getMax();

        holder.binding.progress.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            holder.binding.progress.setProgress(progress, true);
        else holder.binding.progress.setProgress(progress);

        String percent = String.format(Locale.getDefault(), "%d%%", (progress * 100) / max);
        String numberProgress = String.format(Locale.getDefault(), "%1$d/%2$d", progress, max);
        holder.binding.tvPercent.setText(percent);
        holder.binding.tvNumberProgress.setText(numberProgress);

        Glide.with(context)
                .load(codeLibraryItem.getBytes())
                .centerCrop()
                .into(holder.binding.iv);
    }

    @Override
    public int getItemCount() {
        return codeLibraryItems.size();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        ItemPictureLibraryBinding binding;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemPictureLibraryBinding.bind(itemView);
            binding.card.setOnCreateContextMenuListener(this);

            binding.card.setOnLongClickListener(v -> {
                PictureLibraryAdapter.this.position = getAdapterPosition();
                return false;
            });

            binding.card.setOnClickListener(v -> {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClicked(getAdapterPosition());
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(Menu.NONE, MENU_OPEN, Menu.NONE, R.string.menu_open);
            menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.menu_delete);
            menu.add(Menu.NONE, MENU_PATH, Menu.NONE, R.string.menu_path);
        }
    }

    public int getPosition() {
        return position;
    }

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
