package com.jcoder.picsms.ui.web;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.text.TextUtilsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jcoder.picsms.databinding.ActivityWebBinding;
import com.jcoder.picsms.ui.BaseActivity;
import com.jcoder.picsms.utils.Constants;

import java.util.Objects;

public class WebActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWebBinding binding = ActivityWebBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        new MaterialAlertDialogBuilder(this)
                .setMessage(TextUtilsCompat.htmlEncode(Constants.text))
                .show();
        String html = String.format(getBaseHtml(), Constants.text);
        binding.web.loadData(html, "text/html", "UTF-8");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private String getBaseHtml() {
        return "<!DOCTYPE html>\n" +
                " <html>\n" +
                " <head>\n" +
                "   <meta charset=\"UTF-8\">\n" +
                "   <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "   <style>\n" +
                "\n" +
                "    body {\n" +
                "        margin: 20px;\n" +
                "        padding: 0px;\n" +
                "        z-index: 1;\n" +
                "        background: white;\n" +
                "    }\n" +
                "\n" +
                "    p {\n" +
                "        color: black;\n" +
                "word-wrap: break-word;\n" +
                "    }\n" +
                "</style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "  <code>%s</code>\n" +
                "</body>\n" +
                "\n" +
                "</html>\n" +
                "\n";
    }
}