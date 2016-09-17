package com.example.zoe.happysort;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class AnalyzedImageActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyzed_image);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_IMAGE_DETAILS);
        Bitmap image = intent.getParcelableExtra(MainActivity.EXTRA_UPLOADED_IMAGE);
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setText(message);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_analyzed_image);
        layout.addView(textView);
    }
}
