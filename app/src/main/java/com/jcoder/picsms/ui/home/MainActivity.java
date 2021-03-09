package com.jcoder.picsms.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jcoder.picsms.R;
import com.jcoder.picsms.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnPic2Text.setOnClickListener(v -> launchActivity(PicToTextActivity.class));
        binding.btnText2Pic.setOnClickListener(v -> launchActivity(TextToPicActivity.class));
    }

    private void launchActivity(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_menu_github) openGithub();
        return super.onOptionsItemSelected(item);
    }

    private void openGithub() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/kaungkhantjc/"));
        startActivity(intent);
    }
}