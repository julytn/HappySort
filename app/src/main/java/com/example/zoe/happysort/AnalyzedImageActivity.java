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
        String description = intent.getStringExtra(MainActivity.EXTRA_ML_DESCRIPTION);
        String keywords = intent.getParcelableExtra(MainActivity.EXTRA_ML_KEYWORDS);

        TextView descriptionTextView = (TextView) findViewById(R.id.image_details_ml);
        descriptionTextView.setText(description);

        TextView keywordTextView = (TextView) findViewById(R.id.suggested_items_ml);
        keywordTextView.setText(keywords);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_analyzed_image);

    }
}
