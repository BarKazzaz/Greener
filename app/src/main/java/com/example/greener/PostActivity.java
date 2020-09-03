package com.example.greener;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class PostActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton postImage;
    private EditText postContent;
    private Button updatePostButton;
    private Button deletePostButton;
    private static final int GALLERY_REQUEST = 1;
    private Uri imageUrl = null;
    private String userName;
    private String profilePictureUrl;
    private Boolean canEdit;
    private boolean isNew;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        postImage = findViewById(R.id.postImageButton);
        postContent = findViewById(R.id.postContent);
        updatePostButton = findViewById(R.id.updatePostButton);
        deletePostButton = findViewById(R.id.deletePostButton);

        toolbar = findViewById(R.id.update_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Post");


        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isNew = extras.getBoolean("IS_NEW");
            canEdit = extras.getBoolean("EDIT_MODE");
            if (isNew) {
                canEdit = true;
                userName = extras.getString("USER_NAME");
                profilePictureUrl = extras.getString("PROFILE_PICTURE");
                deletePostButton.setVisibility(View.INVISIBLE);
            } else if (canEdit) {
                postId = extras.getString("POST_ID");
                userName = extras.getString("USER_NAME");
                profilePictureUrl = extras.getString("PROFILE_PICTURE");
                String pic = extras.getString("PIC_URL");
                String cont = extras.getString("POST_CONTENT");
                imageUrl = Uri.parse(pic);
                Picasso.get().load(imageUrl).into(postImage);
                postContent.setText(cont);
            } else {
                // view only
                String pic = extras.getString("PIC_URL");
                String cont = extras.getString("POST_CONTENT");
                imageUrl = Uri.parse(pic);
                Picasso.get().load(imageUrl).into(postImage);
                postContent.setText(cont);
                deletePostButton.setVisibility(View.INVISIBLE);
            }
        } else {
            // this means it's a new post
            isNew = true;
            deletePostButton.setVisibility(View.INVISIBLE);
        }
        if (!(canEdit || isNew)) {
            updatePostButton.setVisibility(View.INVISIBLE);
            postContent.setEnabled(false);
            postImage.setOnClickListener(null);
        } else {
            if (canEdit) {
                deletePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deletePost(postId);
                    }
                });
            }
            updatePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    validatePost();
                }
            });
        }
    }

    private void disableInteraction() {
        postImage.setEnabled(false);
        deletePostButton.setEnabled(false);
        updatePostButton.setEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
    }

    private void enableInteraction() {
        postImage.setEnabled(false);
        deletePostButton.setEnabled(true);
        updatePostButton.setEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void deletePost(final String postId) {
        disableInteraction();
        Model.getInstance().deletePost(postId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Model.getInstance().deleteStoragePost(postId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Model.getInstance().removeUserPost(postId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(PostActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                                            enableInteraction();
                                            finish();
                                        } else {
                                            Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            enableInteraction();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                enableInteraction();
                            }
                        }
                    });
                } else {
                    Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    enableInteraction();
                }
            }
        });
    }

    private void validatePost() {
        disableInteraction();
        if (!(canEdit || isNew)) {
            enableInteraction();
            return;
        }
        Boolean valid = true;
        if (TextUtils.isEmpty(postContent.getText())) {
            Toast.makeText(this, "Missing post content..", Toast.LENGTH_LONG).show();
            valid = false;
        }
        if (imageUrl == null) {
            Toast.makeText(this, "Missing post image..", Toast.LENGTH_LONG).show();
            valid = false;
        }
        if (valid) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
            final String date = dateFormat.format(new Date());
            final String time = getGMTtTime();
            if (postId == null) {
                postId = date + time + UUID.randomUUID().toString();
            }
            final String id = postId;
            final Uri picUri = imageUrl;
            final String content = this.postContent.getText().toString();
            final String user = userName;
            final String profilePicUrl = profilePictureUrl;
            if (canEdit) {
                Bundle extras = getIntent().getExtras();
                String originalPic = extras.getString("PIC_URL");
                String originalContent = extras.getString("POST_CONTENT");
                if (picUri.toString().equals(originalPic)) {
                    if (content.equals(originalContent)) {
                        Toast.makeText(PostActivity.this, "No updates made", Toast.LENGTH_SHORT).show();
                        enableInteraction();
                        finish();
                        return;
                    } else {
                        Post post = new Post(date, id, picUri.toString(), content, time, user, profilePicUrl);
                        updatePosts(post);
                        enableInteraction();
                        return;
                    }
                }
            }
            Model.getInstance().addPostImageToStorage(picUri, id)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String postPic = uri.toString();
                                                Post post = new Post(date, id, postPic, content, time, user, profilePicUrl);
                                                updatePosts(post);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                            } else {
                                Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
        enableInteraction();
    }

    private void updatePosts(final Post post) {
        // update db
        Model.getInstance().addPost(post)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Model.getInstance().addPostToUser(post.id).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(PostActivity.this, "post uploaded", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private String getGMTtTime() {
        DateFormat dateFormat = DateFormat.getTimeInstance();
        dateFormat.setTimeZone(TimeZone.getTimeZone("gmt")); //
        String time = dateFormat.format(new Date());
        return time;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUrl = data.getData();
            postImage.setImageURI(imageUrl);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            redirectToMain();
        }
        return super.onOptionsItemSelected(item);
    }

    private void redirectToMain() {
        finish();
    }
}
