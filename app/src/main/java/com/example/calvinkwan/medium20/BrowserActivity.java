package com.example.calvinkwan.medium20;


import android.app.FragmentManager;
//import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import static com.example.calvinkwan.medium20.R.id.myPostFragment;

public class BrowserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    View myView;
    private FirebaseAuth Auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String postKey = null;
    private TextView nameView;
    private String userKey = null;
    private DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        FragmentManager manager = getFragmentManager();
        userKey = getIntent().getStringExtra("UID");
        if(userKey == null)
        {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content_frame, new myPostFragment());
            transaction.commit();
        } else {
            profileFragment profile = new profileFragment();
            profile.setUser(userKey);
            manager.beginTransaction().replace(R.id.content_frame, profile).commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
        Auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    Intent loginIntent = new Intent(BrowserActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        Auth.addAuthStateListener(authStateListener);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(Auth.getInstance().getCurrentUser() != null) {
            users = FirebaseDatabase.getInstance().getReference().child("Users");
            String user_key = FirebaseAuth.getInstance().getCurrentUser().getUid();
            View header = navigationView.getHeaderView(0);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String emailString = user.getEmail();
            TextView email = header.findViewById(R.id.email);
            email.setText(emailString);
            final TextView username = header.findViewById(R.id.userName);
            users.child(user_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = (String) dataSnapshot.child("name").getValue();
                    username.setText(name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
    public static class BlogViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public BlogViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        private void setTitle(String title)
        {
            TextView postTitle = mView.findViewById(R.id.postTitle);
            postTitle.setText(title);
        }

        private void setDesc(String desc)
        {
            TextView postDesc = mView.findViewById(R.id.postDesc);
            postDesc.setText(desc);
        }

        private void setImage(Context ctx, String image)
        {
            ImageView postImage = mView.findViewById(R.id.postImage);
            Picasso.with(ctx).load(image).into(postImage);

        }

        private void setUser(String user)
        {
            TextView postUser = mView.findViewById(R.id.postUser);
            postUser.setText(user);
        }

        private void setCateg(String categ)
        {
            Spinner postCateg = mView.findViewById(R.id.spinner1);
            postCateg.getSelectedItem();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.actionMessage)
        {
            startActivity(new Intent(BrowserActivity.this, MessageActivity.class));
        }

        if(item.getItemId() == R.id.action_add)
        {
            startActivity(new Intent(BrowserActivity.this, PostActivity.class));
            Bundle extras = getIntent().getExtras();
            if(extras != null){
                postKey = extras.getString("blog_id");
                Log.d("Test","yeah" + postKey);
            }
        }

        if(item.getItemId() == R.id.action_logout)
        {
            System.out.println("esketit");
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();
        if (id == R.id.nav_home) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new myPostFragment()).commit();
        }
        else if (id == R.id.nav_myPost) {
            profileFragment profile = new profileFragment();
            profile.setUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
            fragmentManager.beginTransaction().replace(R.id.content_frame, profile).commit();
        }
        else if (id == R.id.nav_bookmark) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new BookmarksFragment()).commit();
        }else if (id == R.id.nav_categories) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new categoriesFragment()).commit();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout()
    {
        Auth.signOut();
        Intent intent = new Intent(BrowserActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

}
