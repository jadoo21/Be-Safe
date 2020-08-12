package com.jadoo.risha.besafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    Button login, signUp;
    SignInButton googleLogin;
    Firebase root;
    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN=0;
    dialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        alertDialog = new dialog(this);

        googleLogin = findViewById(R.id.sign_in_button);
        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        username = findViewById(R.id.text_user);
        password = findViewById(R.id.text_password);

        auth = FirebaseAuth.getInstance();

        login = findViewById(R.id.button_login);
        signUp = findViewById(R.id.button_signUp);
        root = new Firebase("https://besafe-55ea7.firebaseio.com/");

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, signUp.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check(username) && check(password)) {
                    alertDialog.showDialog();
                    String email = username.getText().toString();
                    String pass = password.getText().toString();
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                alertDialog.removeDialog();
                                if (user != null && user.isEmailVerified()) {
                                    Intent intent = new Intent(MainActivity.this, Home.class);
                                    finish();
                                    startActivity(intent);
                                }
                                else{
                                    if(user!=null)
                                        user.sendEmailVerification();
                                    username.getText().clear();
                                    password.getText().clear();
                                    print("Email not verified!! Email sent to your given email-id");
                                }
                            }
                            else{
                                print("Error Occurred!! Please fill your details again...");
                            }
                        }
                    });
                }
            }
        });

    }

    public boolean check(EditText text){
        String s = text.getText().toString();
        if(s.isEmpty()){
            text.setError("Fill this field!!");
            text.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser user = auth.getCurrentUser();
        if(account!=null){
            Intent intent = new Intent(MainActivity.this, Home.class);
            finish();
            startActivity(intent);
        }
        if(user!=null){
            if(user.isEmailVerified()){
                Intent intent = new Intent(MainActivity.this, Home.class);
                finish();
                startActivity(intent);
            }
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            alertDialog.showDialog();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Toast.makeText(MainActivity.this, "SignedIn Successfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, Home.class);
            alertDialog.removeDialog();
            finish();
            startActivity(intent);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            alertDialog.removeDialog();
            Toast.makeText(MainActivity.this,"signInResult:failed code=" + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    void print(String msg){
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }


}
