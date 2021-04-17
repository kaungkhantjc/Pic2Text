package com.jcoder.picsms.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.material.textview.MaterialTextView;
import com.jcoder.picsms.R;
import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.models.Encoding;

import java.util.ArrayList;

public class EncodingAdapter extends BaseAdapter implements Filterable {
    private final ArrayList<Encoding> encodings;

    private EncodingFilter nameFilter;
    private int position = 0;

    public EncodingAdapter(ArrayList<Encoding> encodings) {
        this.encodings = encodings;
    }

    @Override
    public int getCount() {
        return encodings.size();
    }

    @Override
    public Object getItem(int position) {
        return encodings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_encoding, parent, false);

        Encoding encoding = encodings.get(position);
        MaterialTextView tvEncodingName = convertView.findViewById(R.id.tvEncodingName);
        MaterialTextView tvEncodingMessage = convertView.findViewById(R.id.tvEncodingMessage);

        tvEncodingName.setText(encoding.getEncodingName());
        tvEncodingMessage.setText(encoding.getEncodingMessage());

        convertView.setOnTouchListener((View v, @SuppressLint("ClickableViewAccessibility") MotionEvent event) -> {
            this.position = position;
            return false;
        });
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (nameFilter == null)
            nameFilter = new EncodingFilter(encodings);
        return nameFilter;
    }

    public EncodingType getEncodingType() {
        return encodings.get(position).getEncodingType();
    }

    private class EncodingFilter extends Filter {
        private final ArrayList<Encoding> encodingsOriginal;
        private final ArrayList<Encoding> encodingsFilter = new ArrayList<>();

        public EncodingFilter(ArrayList<Encoding> encodingsOriginal) {
            this.encodingsOriginal = encodingsOriginal;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            encodingsFilter.clear();

            final FilterResults results = new FilterResults();
            String query = constraint.toString().toLowerCase().trim();

            if (query.isEmpty()) {
                encodingsFilter.addAll(encodingsOriginal);
            } else {
                for (int i = 0; i < encodingsOriginal.size(); i++) {
                    if (encodingsOriginal.get(i).getEncodingName().toLowerCase().contains(query)) {
                        encodingsFilter.add(encodingsOriginal.get(i));
                    }
                }
            }

            results.count = encodingsFilter.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            encodings.clear();
            encodings.addAll(encodingsFilter);
        }
    }

}
