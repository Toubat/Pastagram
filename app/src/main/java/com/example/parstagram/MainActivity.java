package com.example.parstagram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private EditText etDescription;
    private ImageView ivPostImage;
    private ImageView ivTakePicture;
    private ImageView ivPost;
    private Button btnSubmit;
    private FloatingActionButton floatingActionButton;
    private ProgressBar progressBar;
    public String photoFileName = "photo.jpg";
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDescription = findViewById(R.id.etDescription);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        floatingActionButton = findViewById(R.id.floatingButton);
        progressBar = findViewById(R.id.pbLoading);
        ivTakePicture = findViewById(R.id.ivTakePicture);
        ivPost = findViewById(R.id.ivPost);
        setVisibility(View.INVISIBLE);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
        ivTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Description cannot be empty!", Toast.LENGTH_SHORT).show();
                } else if (photoFile == null || ivPostImage.getDrawable() == null) {
                    Toast.makeText(MainActivity.this, "There is no image!", Toast.LENGTH_SHORT).show();
                } else {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    savePost(description, currentUser, photoFile);
                }
            }
        };
        btnSubmit.setOnClickListener(clickListener);
        ivPost.setOnClickListener(clickListener);
    }

    private void setVisibility(int visibility) {
        etDescription.setVisibility(visibility);
        btnSubmit.setVisibility(visibility);
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(MainActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivPostImage.setImageBitmap(takenImage);
                ivPostImage.setImageBitmap(takenImage);
                setVisibility(View.VISIBLE);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void savePost(String description, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(currentUser);
        post.setImage(new ParseFile(photoFile));
        ivPostImage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(MainActivity.this, "Error while saving", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Post save was successful!!");
                    etDescription.setText("");
                    ivPostImage.setImageResource(0);
                    Toast.makeText(MainActivity.this, "Image posted!", Toast.LENGTH_LONG).show();
                    setVisibility(View.INVISIBLE);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void queryPosts() {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    Toast.makeText(MainActivity.this, "Issue with getting posts!", Toast.LENGTH_SHORT).show();
                } else {
                    for (Post post : objects) {
                        Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                    }
                }
            }
        });
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.parstagram_bar, menu);
        return true;
    }


}