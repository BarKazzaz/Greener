package com.example.greener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class UpdateProfileActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private ImageView profilePicture;
    private EditText userName;
    private Button saveButton;
    private static final int GALLERY_REQUEST = 1;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        progressDialog = new ProgressDialog(this);
        userId = Model.getInstance().getAuth().getCurrentUser().getUid();
        profilePicture = (ImageView) findViewById(R.id.profile_picture);
        userName = (EditText) findViewById(R.id.profile_name);
        saveButton = (Button) findViewById(R.id.profile_save);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null){
            Uri imgUri = data.getData();
            CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult res = CropImage.getActivityResult(data);
            if(requestCode == RESULT_OK){
                Uri resUri = res.getUri();
            }
        }
    }

    private void saveProfile() {
        progressDialog.setTitle("Saving information");
        progressDialog.setMessage("Please wait while information is being saved");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);

        String user = userName.getText().toString();
        DatabaseReference usersRef = Model.getInstance().getUsers().child(Model.getInstance().getAuth().getCurrentUser().getUid());
        if(TextUtils.isEmpty(user)){
            Toast.makeText(this, "Please make sure to have username and profile picture filled", Toast.LENGTH_LONG);
        }else{
            HashMap usersMap = new HashMap();
            usersMap.put("username", user);
            usersRef.updateChildren(usersMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(UpdateProfileActivity.this, "Updated profile!", Toast.LENGTH_SHORT).show();
                                redirectToMain();
                            }else{
                                String msg = task.getException().getMessage();
                                Toast.makeText(UpdateProfileActivity.this, "Error: "+msg, Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    private void redirectToMain() {
        Intent mainIntent = new Intent(UpdateProfileActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}