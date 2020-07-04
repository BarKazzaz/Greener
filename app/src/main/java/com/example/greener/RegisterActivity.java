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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText email, password, confirmationPassword;
    private Button createAccountButton;
    private Model modal = Model.getInstance();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = (EditText) findViewById(R.id.register_email);
        password = (EditText) findViewById(R.id.register_password);
        confirmationPassword = (EditText) findViewById(R.id.register_confirm_password);
        createAccountButton = (Button) findViewById(R.id.register_create_account_button);
        progressDialog = new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("clicked");
                if((TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(password.getText().toString()) || TextUtils.isEmpty(confirmationPassword.getText().toString()))){
                    Toast.makeText(RegisterActivity.this, "Please make sure to fill the form", Toast.LENGTH_SHORT).show();
                    return ;
                }if(!password.getText().toString().equals(confirmationPassword.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Incorrect confirmation password", Toast.LENGTH_SHORT).show();
                    return ;
                }
                progressDialog.setTitle("Creating new account");
                progressDialog.setMessage("Please wait while account is being created");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);
                createNewUser(email.getText().toString(), password.getText().toString());
            }
        });
    }

    private void createNewUser(String email, String pass) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            redirectToSetupActivity();
                            Toast.makeText(RegisterActivity.this, "You are now registered", Toast.LENGTH_SHORT).show();
                        }else{
                            String msg = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void redirectToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, MainActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
