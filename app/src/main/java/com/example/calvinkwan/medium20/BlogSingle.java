package com.example.calvinkwan.medium20;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class BlogSingle extends AppCompatActivity {
    private String postKey = null;

    private StorageReference storage;
    private DatabaseReference mDatabase;
    private DatabaseReference bookmarks;
    private DatabaseReference users;
    private DatabaseReference temp;
    private DatabaseReference commentDatabase;
    private DatabaseReference like;



    private ImageView singleImage;
    private TextView singleTitle;
    private TextView singleDesc;
    private TextView singleName;
    private TextView singleCateg;

    private ImageButton pass_Key;
    private ImageButton bookmarkButton;
    private ImageButton likeButton;
    private Button addComment;

    private Uri imageUri = null;
    private TextView likecounter;

    private String post_title;
    private String post_desc;
    private String post_image;
    private String post_name;
    private String post_categ;

    private int flag;
    private boolean bkbut = false;

    // private RecyclerView comments;
    int count = 0;

    boolean mProcessLike = false;

    String curr_user = "";
    String poster = "";

    private FirebaseAuth Auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);
        storage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        users = FirebaseDatabase.getInstance().getReference().child("Users");

        likecounter = findViewById(R.id.likecounter);

        postKey = getIntent().getExtras().getString("blog_id");
        temp = users.child("Posts");
        final DatabaseReference totally = temp.child(postKey);
        Log.d("Test", "this is blogsingle test 1 " + totally.toString());

        temp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mProcessLike) {
                    Log.d("Test",postKey);
                    System.out.println(dataSnapshot.getKey());
                    if(dataSnapshot.hasChild(postKey)) {
                        mProcessLike = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        flag = 0;
        flag = getIntent().getExtras().getInt("flag");

        // change mdatabase
        if (flag == 1) {
            mDatabase = users;
            String user_key = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mDatabase = mDatabase.child(user_key);
            mDatabase = mDatabase.child("Bookmarks");

//            users.child("Posts").setValue(postKey);
        }

        // Toast.makeText(BlogSingle.this, postKey, Toast.LENGTH_LONG).show();

        singleImage = findViewById(R.id.imageSingle);
        singleTitle = findViewById(R.id.postTitle);
        singleDesc = findViewById(R.id.postDescription);
        singleName = findViewById(R.id.postUser);
        singleCateg = findViewById(R.id.display_result);
        bookmarkButton = findViewById(R.id.bookmark);
        likeButton = findViewById(R.id.likebtn);
        addComment = findViewById(R.id.addComment);
        pass_Key = findViewById(R.id.userPage);

//        comments = findViewById(R.id.my_post_recycler);
//        comments.setHasFixedSize(true);
//        comments.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
//        comments.setLayoutManager(layoutManager);
//        comments.setNestedScrollingEnabled(false);

        commentDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        commentDatabase = commentDatabase.child(postKey);
        commentDatabase = commentDatabase.child("Comments");
        Auth = FirebaseAuth.getInstance();

        //FOR LIKES:::
       // likes = users.child(Auth.getCurrentUser().getUid()).child("Likes");
        like = FirebaseDatabase.getInstance().getReference().child("Blog").child(postKey);
        Log.d("postKey2:", postKey);
        like.child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
                if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    likeButton.setImageResource(R.drawable.whitethumb);
                    likecounter.setText(Long.toString(dataSnapshot.getChildrenCount()));
                }
                else {
                    likeButton.setImageResource(R.drawable.thumbup);
                    likecounter.setText(Long.toString(dataSnapshot.getChildrenCount()));
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userkey = FirebaseAuth.getInstance().getCurrentUser().getUid();

//                DatabaseReference addLike = users.child(userkey);
//                DatabaseReference blah = addLike.child("Likes");
//                final DatabaseReference newLike = blah.child(postKey);

                DatabaseReference addLike = like;
                DatabaseReference blah = addLike.child("Likes");
                final DatabaseReference newLike = blah.child(userkey);

                mProcessLike = true;

                blah.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(mProcessLike)
                        {
                            System.out.println(dataSnapshot.getKey());
                            if(dataSnapshot.hasChild(userkey)) {
                                Toast.makeText(BlogSingle.this, "Unliked", Toast.LENGTH_LONG).show();
                                delLike();
                                likeButton.setImageResource(R.drawable.thumbup);
                                mProcessLike = false;
                            }

                        else {
                                Toast.makeText(BlogSingle.this, "Liked", Toast.LENGTH_LONG).show();
                                newLike.child("title").setValue(post_title);
                                //newLike.child("desc").setValue(post_desc);
                                newLike.child("userkey").setValue(userkey);
                                likeButton.setImageResource(R.drawable.whitethumb);
//                                pass_Key.setImage
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            }
        });

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookmark();
            }
        });

        String temp;

        pass_Key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                DatabaseReference getcode = FirebaseDatabase.getInstance().getReference().child("Blog").child(postKey);
//                DatabaseReference tunnel = getcode.child("userKey");
                getcode.child("userKey").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        final String userkey = snapshot.getValue().toString();  //prints "Do you have data? You'll love Firebase."
                        Intent userIntent = new Intent(BlogSingle.this, BrowserActivity.class);
                        userIntent.putExtra("UID", userkey);
                        startActivity(userIntent);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
//                final String userkey =getcode.getKey();

//                Log.d("userPage",  "User Key " +  getcode.getKey());

//
//                fragmentManager.putExtra("user_id", );
//                fragmentManager.pu
//                startActivity(toComment);
            }
        });

        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment();
            }
        });

        mDatabase.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                post_title = (String) dataSnapshot.child("title").getValue();
                post_desc = (String) dataSnapshot.child("desc").getValue();
                post_image = (String) dataSnapshot.child("image").getValue();
                post_name = (String) dataSnapshot.child("name").getValue();
                post_categ = (String) dataSnapshot.child("categ").getValue();

                post_name = "By: " + post_name;
                post_categ = "Category: " + post_categ;

                singleTitle.setText(post_title);
                singleDesc.setText(post_desc);
                singleName.setText(post_name);
                singleCateg.setText(post_categ);
                Picasso.with(BlogSingle.this).load(post_image).into(singleImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void bookmark() {


        // NOTE: DOESN'T CHECK FOR DUPLICATE BOOKMARKS

        // String post_key = mDatabase.child
        String user_key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference addBookmark = users.child(user_key);
        DatabaseReference here = addBookmark.child("Bookmarks");
        final DatabaseReference newBookmark = here.child(postKey);

        bkbut = true;

        here.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (bkbut) {
                    if (dataSnapshot.hasChild(postKey)) {
                        Toast.makeText(BlogSingle.this, "Bookmark Removed", Toast.LENGTH_LONG).show();
                        delBookmark();
                        bkbut = false;
                    } else {
                        Toast.makeText(BlogSingle.this, "Bookmarked", Toast.LENGTH_LONG).show();
                        newBookmark.child("title").setValue(post_title);
                        newBookmark.child("desc").setValue(post_desc);
                        newBookmark.child("image").setValue(post_image);
                        newBookmark.child("name").setValue(post_name);
                        bkbut = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    void delBookmark() {
        mDatabase = users;
        String user_key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = mDatabase.child(user_key);
        mDatabase = mDatabase.child("Bookmarks");
        mDatabase.child(postKey).removeValue();
        finish();
    }

    void delLike() {
        //mDatabase = users;
        String user_key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mDatabase = mDatabase.child(postKey).child("Likes");
        like.child("Likes").child(user_key).removeValue();
        //finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.blog_single_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // get name of post
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        postKey = getIntent().getExtras().getString("blog_id");
        assert postKey != null;
        mDatabase.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String s = (String) dataSnapshot.child("name").getValue();
                if(s != null) poster = s;
            }
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        String user_key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.child(user_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String u  = (String) dataSnapshot.child("name").getValue();
                if(u != null) curr_user = u;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        boolean q = curr_user.equals(poster);
        if (q)
        {
            // Add delete option
            menu.findItem(R.id.del_post).setVisible(true);
            menu.findItem(R.id.edit).setVisible(true);
        }
        else
        {
            menu.findItem(R.id.del_post).setVisible(false);
            menu.findItem(R.id.edit).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.del_post) {
            AlertDialog alert = new AlertDialog.Builder(this).create();
            alert.setMessage("Delete post?");
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    delPost();
                    Intent toMain = new Intent(BlogSingle.this, BrowserActivity.class);
                    startActivity(toMain);
                }
            });
            alert.show();

        }
        else if(item.getItemId() == R.id.edit) {
            Intent editPost = new Intent(BlogSingle.this, EditPostActivity.class);
            editPost.putExtra("blog_id", postKey);
            startActivity(editPost);
        }

        return super.onOptionsItemSelected(item);
    }

    void delPost() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        postKey = getIntent().getExtras().getString("blog_id");
        if (postKey != null) mDatabase.child(postKey).removeValue();

        return;
    }

    void addComment() {
        Intent toComment = new Intent(this, ViewComments.class);
        toComment.putExtra("blog_id", postKey);
        startActivity(toComment);
    }


}
