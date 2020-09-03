package com.example.greener;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView mainFeed;
    private ImageButton addPostButton;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String userId;
    private TextView nav_userName;
    private ImageView nav_profilePicture;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private String userName;
    private String profilePicUri;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = Model.getInstance().getAuth();
        usersRef = Model.getInstance().getUsers();
        FirebaseUser currentUser = Model.getInstance().getAuth().getCurrentUser();
        if (currentUser != null)
            userId = currentUser.getUid();

        addPostButton = (ImageButton) findViewById(R.id.addPostButton);
        toolbar = findViewById(R.id.main_app_bar);
//        setActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Greener");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_name); // TODO: make the button open drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerView = navigationView.inflateHeaderView(R.layout.navigation_header);

        nav_userName = (TextView) headerView.findViewById(R.id.nav_username);
        nav_profilePicture = (ImageView) headerView.findViewById(R.id.nav_profile);

        mainFeed = (RecyclerView) findViewById(R.id.main_feed);
        mainFeed.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mainFeed.setLayoutManager(layoutManager);

        if (userId != null)
            usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("username")) {
                            String name = dataSnapshot.child("username").getValue().toString();
                            nav_userName.setText(name);
                            userName = name;
                        }

                        if (dataSnapshot.hasChild("profilePicture")) {
                            String imgUrl = dataSnapshot.child("profilePicture").getValue().toString();
                            Picasso.get().load(imgUrl).into(nav_profilePicture);
                            profilePicUri = imgUrl;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                navigateToItem(menuItem);
                return false;
            }
        });

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToPostActivity();
            }
        });

        showAllPosts();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView username, content, date, time;
        CircleImageView profilePic;
        ImageView postImg;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.post_username);
            profilePic = itemView.findViewById(R.id.post_profile_image);
            postImg = itemView.findViewById(R.id.post_image);
            content = itemView.findViewById(R.id.post_content);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
        }
    }

    private void showAllPosts() {
        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(Model.getInstance().getPostsRef(), Post.class)
                .build();
        FirebaseRecyclerAdapter<Post, FeedViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, FeedViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FeedViewHolder holder, int position, @NonNull final Post model) {
                holder.username.setText(model.getUsername());
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());
                holder.content.setText(model.getPostContent());
                Picasso.get().load(Uri.parse(model.profilePic)).into(holder.profilePic);
                holder.profilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "GoTo profile", Toast.LENGTH_LONG).show();
                    }
                });
                Picasso.get().load(Uri.parse(model.postImage)).into(holder.postImg);
                holder.postImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (model.username.equals(userName)){
                            editPost(model.postImage, model.postContent, model.id);
                        }else {
                            viewPost(model.postImage, model.postContent);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
                FeedViewHolder feedViewHolder = new FeedViewHolder(view);
                return feedViewHolder;
            }
        };
        mainFeed.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void viewPost(String picUrl, String content) {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        postIntent.putExtra("PIC_URL", picUrl);
        postIntent.putExtra("POST_CONTENT", content);
        startActivity(postIntent);
    }


    private void editPost(String picUrl, String content, String postId){
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        postIntent.putExtra("EDIT_MODE", true);
        postIntent.putExtra("POST_ID", postId);
        postIntent.putExtra("USER_NAME", userName);
        postIntent.putExtra("PROFILE_PICTURE", profilePicUri);
        postIntent.putExtra("PIC_URL", picUrl);
        postIntent.putExtra("POST_CONTENT", content);
        startActivity(postIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        } else {
            profileIsSet();
        }
    }

    private void profileIsSet() {
        final String userId = auth.getCurrentUser().getUid();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(userId)) {
                    redirectToProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void redirectToProfile() {
        Intent profileIntent = new Intent(MainActivity.this, UpdateProfileActivity.class);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        startActivity(profileIntent);
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void redirectToPostActivity() {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        postIntent.putExtra("USER_NAME", userName);
        postIntent.putExtra("PROFILE_PICTURE", profilePicUri);
        postIntent.putExtra("IS_NEW", true);
        startActivity(postIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
//            return true;
//        }
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.openDrawer(GravityCompat.START);
                else
                    drawerLayout.closeDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                redirectToProfile();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_Friends:
                Toast.makeText(this, "Friends", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_search_friends:
                Toast.makeText(this, "Find Friends", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_post:
                redirectToPostActivity();
                break;
            case R.id.nav_logout:
                auth.signOut();
                redirectToLogin();
                break;
        }
    }
}
