package com.example.chatapp.login;

import static android.graphics.Color.RED;
import static com.example.chatapp.Globals.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InputDevice;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.ConversationMainActivityLists;
import com.example.chatapp.FirebaseHelper;
import com.example.chatapp.Globals;
import com.example.chatapp.R;
import com.example.chatapp.signUp.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText loginPhoneNumber, loginPassword;
    TextView popUpLoginNumber, popUpLoginPassword;
    ProgressBar progressBar;
    Button loginVerify, signUp;
    FirebaseAuth firebaseAuth;
    boolean isPhoneNumberSatisfyFormat = false;
    boolean isPasswordSatisfyFormat = false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===== FIREBASE VERIFICATION =====
        // Verify we're connected to the CORRECT Firebase project at startup
        try {
            FirebaseHelper.verifyProjectConfiguration();
        } catch (RuntimeException e) {
            Log.e("Login", "CRITICAL: Firebase config error - " + e.getMessage());
            Toast.makeText(this, "Firebase configuration error. Check logs.", Toast.LENGTH_LONG).show();
        }
        
        setContentView(R.layout.activity_login2);
        Log.d("FIREBASE_CHECK",
                FirebaseApp.getInstance().getOptions().getProjectId());


        loginPhoneNumber = findViewById(R.id.loginPhoneNumber);
        loginPassword = findViewById(R.id.loginPassword);
        loginVerify = findViewById(R.id.loginVerify);
        signUp = findViewById(R.id.signUp);
        popUpLoginNumber = findViewById(R.id.popUpLoginNumber);
        popUpLoginPassword = findViewById(R.id.popUpLoginPassword);
        progressBar = findViewById(R.id.Login_progress);

        firebaseAuth = FirebaseAuth.getInstance();

        //verify phone number and password on text change
        verifyNumberOnTextChange();
        verifyPasswordOnTextChange();

        //login user to main activity
        loginUser();
        //start sign up activity
        switchActivityOnSignUp();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            //switch to main activity
            Intent intent = new Intent(LoginActivity.this, ConversationMainActivityLists.class);
            startActivity(intent);
            finish();
        }
    }

    private void verifyNumberOnTextChange(){
        this.loginPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                popUpLoginNumber.setVisibility(View.VISIBLE);
                ColorStateList colorStateList;
                if(Globals.verifyPhoneNumber(charSequence.toString().trim())){
                    popUpLoginNumber.setText("Format Verified. ");
                    int greenColor = getResources().getColor(R.color.ForestGreen,null);
                    colorStateList = ColorStateList.valueOf(greenColor);
                    isPhoneNumberSatisfyFormat = true;
                    if(isPasswordSatisfyFormat){
                        loginVerify.setEnabled(true);
                    }
                }else{
                    popUpLoginNumber.setText("Phone must start with 0 and contain 10-11 digits");
                    colorStateList = ColorStateList.valueOf(RED);
                    loginVerify.setEnabled(false);
                    isPhoneNumberSatisfyFormat = false;
                }
                popUpLoginNumber.setBackgroundTintList(colorStateList);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void verifyPasswordOnTextChange(){
        loginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                popUpLoginPassword.setVisibility(View.VISIBLE);
                ColorStateList colorStateList;
                if(charSequence.toString().trim().length() > 5){
                    popUpLoginPassword.setText("Format Verified. ");
                    int greenColor = getResources().getColor(R.color.ForestGreen,null);
                    colorStateList = ColorStateList.valueOf(greenColor);
                    isPasswordSatisfyFormat = true;
                    if(isPhoneNumberSatisfyFormat){
                        loginVerify.setEnabled(true);
                    }
                }else{
                    popUpLoginPassword.setText("Min 6 letters");
                    colorStateList = ColorStateList.valueOf(RED);
                    loginVerify.setEnabled(false);
                    isPasswordSatisfyFormat = false;
                }
                popUpLoginPassword.setBackgroundTintList(colorStateList);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void loginUser(){
        loginVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String phoneNumber = Globals.formatPhoneNumber(loginPhoneNumber.getText().toString().trim());
                String password = loginPassword.getText().toString().trim();
                if(phoneNumber.equals("-1") || password.equals("")){
                    Toast.makeText(LoginActivity.this, "Field is Empty", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }else{
                    String email = phoneNumber + Email_Extension;
                    
                    // ===== DEBUG LOGGING =====
                    Log.w("Login_DEBUG", "========== LOGIN ATTEMPT ==========");
                    Log.w("Login_DEBUG", "Email: " + email);
                    Log.w("Login_DEBUG", "Phone Number: " + phoneNumber);
                    Log.w("Login_DEBUG", "Firebase Project: " + 
                        com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());
                    Log.w("Login_DEBUG", "Firebase DB URL: " + 
                        com.google.firebase.FirebaseApp.getInstance().getOptions().getDatabaseUrl());
                    Log.w("Login_DEBUG", "====================================");
                    
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        
                                        Log.w("Login_SUCCESS", "✓ Login successful");
                                        Log.w("Login_SUCCESS", "User UID: " + (user != null ? user.getUid() : "null"));
                                        Log.w("Login_SUCCESS", "User Email: " + (user != null ? user.getEmail() : "null"));

                                        //switch to main activity
                                        Intent intent = new Intent(LoginActivity.this, ConversationMainActivityLists.class);
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        // ===== DETAILED ERROR LOGGING =====
                                        Exception exception = task.getException();
                                        String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                                        
                                        Log.e("Login_ERROR", "========== LOGIN FAILED ==========");
                                        Log.e("Login_ERROR", "Error: " + errorMsg);
                                        Log.e("Login_ERROR", "Exception: " + (exception != null ? exception.getClass().getSimpleName() : "Unknown"));
                                        Log.e("Login_ERROR", "Firebase Project: " + 
                                            com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());
                                        Log.e("Login_ERROR", "==================================");
                                        
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });
    }
    private void switchActivityOnSignUp(){
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}