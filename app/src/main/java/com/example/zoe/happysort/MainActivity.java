package com.example.zoe.happysort;

import android.*;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.utilities.Base64;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.json.JSONException;
import org.json.JSONObject;
//import org.json.simple.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_ITEM_NAME = "com.example.happysort.EXTRA_ITEM_NAME";
    public final static String EXTRA_MESSAGE = "com.example.happysort.MESSAGE";
    public final static String EXTRA_ITEMS_MESSAGE = "com.example.happysort.ITEMS_MESSAGE";
    public final static String EXTRA_ML_DESCRIPTION = "com.example.happysort.BLAH";
    public final static String EXTRA_ML_KEYWORDS = "com.example.happysort.BLAsdfH";

    public final static String EXTRA_IMAGE = "com.example.happysort.IMAGE";
    public final static String EXTRA_IMAGE_DETAILS = "com.example.happysort.MESSAGE";
    public final static String EXTRA_UPLOADED_IMAGE = "com.example.happysort.IMAGE";
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAReud2l-64PugD0KW0gDlHWIhfCOyvhcM";
    public static final String FILE_NAME = "temp.jpg";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    private TextView mImageDetails;
    private TextView mImageDetailsMain;
    private ImageView mMainImage;


    static final int REQUEST_IMAGE_CAPTURE = 4;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
        Button btnCapture = (Button) findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage("Choose a picture")
                        .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
            }
        });

        mImageDetailsMain = (TextView) findViewById(R.id.image_details);
    }

    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA) || true) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCameraFile()));
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
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
        intent.putExtra(EXTRA_ITEM_NAME, message);

        final WasteItem newItem = new WasteItem();

        Firebase.setAndroidContext(this);
        final Firebase myFirebaseRef = new Firebase("https://happysort-e1592.firebaseio.com/");
        System.out.println("Got here");
        myFirebaseRef.child(message + "/INSTRUCTIONS").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String rui_instructions = "";
                String rui_suggested_items = "";
                newItem.name = message;
                String instructions = (String) snapshot.getValue();
                newItem.instructions = instructions;

                if (instructions == null) {
                    newItem.instructions = "Sorry, we couldn't find disposal instructions for this item.";
                }
                try {
                    String parsed_message = parseInput(message);

                    String ruinstructions = getRuinfo(parsed_message);
                    JSONObject obj = new JSONObject(ruinstructions);
                    rui_instructions = obj.getString("instructions");
                    rui_suggested_items = obj.getString("similar_item");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra(EXTRA_IMAGE, mBitmap);



//                JSONObject jsonObj = new JSONObject(ruinstructions);
                intent.putExtra(EXTRA_MESSAGE, rui_instructions);
                intent.putExtra(EXTRA_ITEMS_MESSAGE, rui_suggested_items.substring(1, rui_suggested_items.length() - 1));
                startActivity(intent);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    public String getRuinfo(String item) throws IOException {
        String ruinfoString = "";
        String urlString = "http://104.155.23.57:5000/get_similar_items?item=" + item;
        System.out.println(urlString);
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            ruinfoString = readStream(in);
        } finally {
            urlConnection.disconnect();
        }
        return ruinfoString;
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }


    public void sendPhoto(View view) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        System.out.println("Made new intent");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            System.out.println("Resolved activity");
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCameraFile()));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            System.out.println("Finished starting activity");

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK) {
            System.out.println("Gallery");
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            System.out.println("CAMERA IMAGE REQUEST");
            uploadImage(Uri.fromFile(getCameraFile()));
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            System.out.println("BARCODE TIME");
            Bundle extras = data.getExtras();
            Bitmap barcodeBitmap = (Bitmap) extras.get("data");
            BarcodeDetector detector =
                    new BarcodeDetector.Builder(getApplicationContext())
                            .setBarcodeFormats(Barcode.ALL_FORMATS)
                            .build();
            if(!detector.isOperational()){
                System.out.println("Could not set up the detector!");
                return;
            }
            Frame frame = new Frame.Builder().setBitmap(barcodeBitmap).build();
            SparseArray<Barcode> barcodes = detector.detect(frame);
            if (barcodes.size() != 0) {
                Barcode thisCode = barcodes.valueAt(0);
                TextView txtView = (TextView) findViewById(R.id.image_details);
                txtView.setText(thisCode.displayValue);
                System.out.println("@@@@@@@@@@@");
                System.out.println(thisCode.rawValue);
            }

        }

    }

    public void uploadImage(Uri uri) {

        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                System.out.println("About to scale the bitmap down");
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);
                System.out.println("About to call cloud vision");
                callCloudVision(bitmap);

                System.out.println("I have put extra");

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, "Something went wrong, please choose another picture.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, "Something went wrong, please choose another picture.", Toast.LENGTH_LONG).show();
        }

    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {

        final Intent intent = new Intent(this, DisplayMessageActivity.class);
        mImageDetailsMain.setText("Uploading image, please wait...");
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});
                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                System.out.println("About to execute image details");

                String lines[] = result.split("[\\r\\n]+");
                String[] matches = Arrays.copyOfRange(lines, 1, lines.length);
                String items = "";
                for (String match : matches ) {
                    String[] parts = match.split(": ");
                    System.out.println(parts[1]);
                    items += parts[1];
                    items += " ";
                }
                System.out.println("33333333");
                System.out.println(items);
                String rui_item = parseInput(items);
                String rui_instructions = "";
                String rui_suggested_items = "";
                String ruinstructions = "";
                try {
                    ruinstructions = getRuinfo(rui_item);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject obj = null;
                try {
                    obj = new JSONObject(ruinstructions);
                    rui_instructions = obj.getString("instructions");
                    rui_suggested_items = obj.getString("similar_item");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                intent.putExtra(EXTRA_MESSAGE, rui_instructions);
                intent.putExtra(EXTRA_ITEMS_MESSAGE, rui_suggested_items);
                startActivity(intent);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                mImageDetailsMain.setText("");

            }
        }.execute();

    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "Automatically generated keywords:\n\n";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format("%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
            }
        } else {
            message += "nothing";
        }

        return message;
    }


    public String parseInput(String input) {
        String newInput = input;
        newInput = newInput.toLowerCase();
        newInput = newInput.replace('-', ' ');
        String[] inputWords = newInput.replaceAll("^[,\\s]+", "").split("[,\\s]+");

        StringBuilder builder = new StringBuilder();
        for(String s : inputWords) {
            builder.append(s);
            builder.append("%20");
        }
        System.out.println("1234567");
        System.out.println(builder.toString());
        return builder.toString();
    }

}
