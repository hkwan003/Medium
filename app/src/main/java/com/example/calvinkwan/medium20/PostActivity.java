package com.example.calvinkwan.medium20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class PostActivity extends AppCompatActivity {

    private ImageButton selectImage;
    private EditText postTitle;
    private EditText postDescription;
    private EditText postName;
    private AdapterView.OnItemSelectedListener postCateg;

    private Button submitButton;
    private StorageReference storage;
    private DatabaseReference database;
    private DatabaseReference users;
    private DatabaseReference likes;
    private ProgressDialog progress;

    private Uri imageUri = null;

    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        storage = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference().child("Blog");
        //likes = FirebaseDatabase.getInstance().getReference().child("Likes");

        selectImage = findViewById(R.id.likebutton);
        postTitle = findViewById(R.id.postTitle);
        postDescription = findViewById(R.id.postDescription);

        submitButton = findViewById(R.id.submitPost);
        progress = new ProgressDialog(this);

        // Category drop down list
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(PostActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Category));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);
        // End category drop down lost


        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent, GALLERY_REQUEST);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPost();
            }
        });
    }

    private void sendPost() {
            progress.setMessage("Posting to blog...");
            final String titleText = postTitle.getText().toString().trim();
            final String descText = postDescription.getText().toString().trim();

            if (!TextUtils.isEmpty(titleText) && !TextUtils.isEmpty(descText) && imageUri != null) {
                progress.show();
                StorageReference filePath = storage.child("Blog_Images").child(imageUri.getLastPathSegment());
                filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        final DatabaseReference newPost = database.push();
                        newPost.child("title").setValue(titleText);
                        newPost.child("desc").setValue(descText);
                        newPost.child("image").setValue(downloadUri.toString());

                        String user_key = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        users = FirebaseDatabase.getInstance().getReference().child("Users");
                        users.child(user_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String u  = (String) dataSnapshot.child("name").getValue();
                                newPost.child("name").setValue(u);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        progress.dismiss();

                        startActivity(new Intent(PostActivity.this, MainActivity.class));       //return to timeline
                    }
                });
            }
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
                imageUri = data.getData();
                selectImage.setImageURI(imageUri);
            }
        }
    }

