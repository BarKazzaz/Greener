package com.example.greener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText email, password;
    private TextView dontHaveAccount;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dontHaveAccount = (TextView) findViewById(R.id.dontHaveAccount);
        email = (EditText) findViewById(R.id.login_email);
        password = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_button);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        dontHaveAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                redirectToRegister();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        String mail = email.getText().toString();
        String pass = password.getText().toString();

        if(TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please fill the form", Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.setTitle("Logging you in");
            progressDialog.setMessage("Please wait while we verify the login request");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            auth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                redirectToMain();
                            }else {
                                String msg = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: "+msg, Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    private void redirectToMain() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void redirectToRegister() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
