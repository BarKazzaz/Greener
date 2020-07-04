package com.example.greener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;

public class Model {

    private static Model instance = null;
    private FirebaseAuth fbAuth;
    private DatabaseReference dbRef;

    private Model(){
        fbAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public static Model getInstance(){
        if(instance == null)
            instance = new Model();
        return instance;
    }

    public DatabaseReference getUsers() {
        return dbRef.child("Users");
    }

    public FirebaseAuth getAuth() {
        return fbAuth;
    }
}