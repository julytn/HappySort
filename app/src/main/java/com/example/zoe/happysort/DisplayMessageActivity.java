package com.example.zoe.happysort;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class DisplayMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent intent = getIntent();

        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String suggested_items_message = intent.getStringExtra(MainActivity.EXTRA_ITEMS_MESSAGE);
//        Bitmap image = intent.getParcelableExtra(MainActivity.EXTRA_IMAGE);
        TextView textView = (TextView) findViewById(R.id.image_details2);
        textView.setText(Html.fromHtml(message));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        TextView suggestedItems = (TextView) findViewById(R.id.suggested_items);
        suggestedItems.setText(suggested_items_message);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
    }



}
