package com.example.greener;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Model {

    private static Model instance = null;
    private FirebaseAuth fbAuth;
    private DatabaseReference dbRef;
    private StorageReference storageRef;

    private Model(){
        fbAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public static Model getInstance(){
        if(instance == null)
            instance = new Model();
        return instance;
    }

    private StorageReference getProfilePicturesStorage() {
        return storageRef.child("Profile Pictures");
    }
    public DatabaseReference getUsers() {
        return dbRef.child("Users");
    }

    public FirebaseAuth getAuth() {
        return fbAuth;
    }

    public UploadTask addProfilePicture(Uri pictureUri) {
        StorageReference imagePath = getProfilePicturesStorage().child(getAuth().getCurrentUser().getUid() + ".jpg");
        return imagePath.putFile(pictureUri);
    }

    public String getProfilePicture() {
        return getProfilePicturesStorage().child(getAuth().getCurrentUser().getUid() + ".jpg").getDownloadUrl().toString();
    }
}