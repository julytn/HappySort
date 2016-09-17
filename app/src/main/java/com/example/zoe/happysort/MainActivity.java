package com.example.zoe.happysort;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.happysort.MESSAGE";
    public final static String EXTRA_IMAGE = "com.example.happysort.IMAGE";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public class WasteItem {
        public String name = "defaultName";
        public String instructions = "defaultInstructions";

        public WasteItem() {

        }

        public String getName() {
            return name;
        }

        public String getInstructions() {
            return instructions;
        }

    }

    public void sendMessage(View view) {
        final Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        final String message = editText.getText().toString();
        final WasteItem newItem = new WasteItem();

        Firebase.setAndroidContext(this);
        final Firebase myFirebaseRef = new Firebase("https://happysort-e1592.firebaseio.com/");

        myFirebaseRef.child(message + "/INSTRUCTIONS").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                newItem.name = message;
                String instructions = (String) snapshot.getValue();
                newItem.instructions = instructions;

                if (instructions == null) {
                    newItem.instructions = "Sorry, we couldn't find disposal instructions for this item.";
                }

                intent.putExtra(EXTRA_MESSAGE, newItem.getInstructions());
                intent.putExtra(EXTRA_IMAGE, mBitmap);
                startActivity(intent);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }


    public void sendPhoto(View view) {
        /*
        System.out.println("Send Photo method &&&&&&&&&&&&&&");
        PhotoIntentActivity photoIntentActivity = new PhotoIntentActivity();
        System.out.println("Mae new photo intent activity");
        photoIntentActivity.dispatchTakePictureIntent();
        System.out.println("Dispatched take picture intent");*/

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        System.out.println("Made new intent");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            System.out.println("Resolved activity");
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mBitmap = (Bitmap) extras.get("data");
        }
    }
}
