package com.example.zoe.happysort;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.happysort.MESSAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Firebase.setAndroidContext(this);
//        Firebase myFirebaseRef = new Firebase("https://happysort-e1592.firebaseio.com/");
//
//        myFirebaseRef.child("Awning").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                System.out.println("~~~ SOMETHING HERE");
//                System.out.println(snapshot.getValue());  //prints "Do you have data? You'll love Firebase."
//                Log.d("MyApp","I am here");
//            }
//
//            @Override
//            public void onCancelled(FirebaseError error) {
//            }
//        });

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
//                System.out.println("~~~ SOMETHING HERE");
                String instructions = (String) snapshot.getValue();
                newItem.instructions = instructions;

                if (instructions == null) {
                    newItem.instructions = "Sorry, we couldn't find disposal instructions for this item.";
                }

                intent.putExtra(EXTRA_MESSAGE, newItem.getInstructions());
                startActivity(intent);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });


    }
}
