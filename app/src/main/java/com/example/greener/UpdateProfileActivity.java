package com.example.greener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

public class UpdateProfileActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private ImageView profilePicture;
    private EditText userName;
    private Button saveButton;
    private String pictureUrl;
    private ProgressBar progBar;
    private static final int GALLERY_REQUEST = 1;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        progressDialog = new ProgressDialog(this);
        progBar = new ProgressBar(this);
        userId = Model.getInstance().getAuth().getCurrentUser().getUid();
        profilePicture = (ImageView) findViewById(R.id.profile_picture);
        userName = (EditText) findViewById(R.id.profile_name);
        saveButton = (Button) findViewById(R.id.profile_save);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
                progressDialog.setTitle("Saving");
                progressDialog.setMessage("Please wait while information is saved");
                progressDialog.show();
            }
        });
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        Model.getInstance().getUsers().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.getKey().equals(userId)) {
                        HashMap me = (HashMap)snapshot.getValue();
                        if (me.get("username") != null)
                            userName.setText(me.get("username").toString());
                        if (me.get("profilePicture") != null) {
                            pictureUrl = me.get("profilePicture").toString();
                            Picasso.get().load(Uri.parse(pictureUrl)).into(profilePicture);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imgUri = data.getData();
            Picasso.get().load(imgUri).into(profilePicture);
            Model.getInstance().addProfilePicture(imgUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progBar.setVisibility(View.INVISIBLE);
                            progBar.setProgress(0);
                            Toast.makeText(UpdateProfileActivity.this, "Picture saved", Toast.LENGTH_LONG).show();
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    pictureUrl = uri.toString();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UpdateProfileActivity.this, "Failed to retrieve profile picture\nPlease try again.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progBar.setVisibility(View.INVISIBLE);
                            progBar.setProgress(0);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            if (progBar.getVisibility() != View.VISIBLE)
                                progBar.setVisibility(View.VISIBLE);
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progBar.setProgress((int) progress);
                        }
                    });
        }
    }

    private void saveProfile() {
        progressDialog.setTitle("Saving information");
        progressDialog.setMessage("Please wait while information is being saved");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);

        String user = userName.getText().toString();
        DatabaseReference usersRef = Model.getInstance().getUsers().child(userId);
        if (TextUtils.isEmpty(user)) {
            Toast.makeText(this, "Please make sure to have username and profile picture filled", Toast.LENGTH_LONG);
        } else {
            HashMap usersMap = new HashMap();
            usersMap.put("username", user);
            usersMap.put("profilePicture", pictureUrl);
            usersRef.updateChildren(usersMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpdateProfileActivity.this, "Updated profile!", Toast.LENGTH_SHORT).show();
                                redirectToMain();
                            } else {
                                String msg = task.getException().getMessage();
                                Toast.makeText(UpdateProfileActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void redirectToMain() {
        Intent mainIntent = new Intent(UpdateProfileActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}