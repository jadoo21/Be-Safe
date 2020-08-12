package com.jadoo.risha.besafe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class signUp extends AppCompatActivity {

    EditText textEmail, textPassword, textName, textConfirmPassword;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    DocumentReference document;
    TextView textProfile;
    Button sign;
    ImageView imgProfile;
    StorageReference storage;
    Uri uri=null;
    dialog alertDialog;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        alertDialog = new dialog(this);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        textEmail = findViewById(R.id.text_email);
        textName = findViewById(R.id.text_name);
        textPassword = findViewById(R.id.text_pass);
        textConfirmPassword = findViewById(R.id.text_cpass);
        sign = findViewById(R.id.button_register);
        textProfile = findViewById(R.id.text_profile);
        imgProfile = findViewById(R.id.image_user);

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        textProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change();
            }
        });

    }

    void change(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1000 && resultCode== Activity.RESULT_OK && data!=null){
            uri = data.getData();
            imgProfile.setImageResource(android.R.color.transparent);
            imgProfile.setImageURI(uri);
            textProfile.setText("Uploaded!! Click to change");
        }
    }

    void register(){
        final String email = textEmail.getText().toString();
        final String name = textName.getText().toString();
        final String pass = textPassword.getText().toString();
        String cpass = textConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            textEmail.setError("Enter email-id");
            textEmail.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(name)){
            textName.setError("Enter Name");
            textName.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(pass)){
            textPassword.setError("Enter password");
            textPassword.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(cpass)){
            textConfirmPassword.setError("Enter password");
            textConfirmPassword.requestFocus();
            return;
        }
        if(!pass.equals(cpass)){
            textPassword.setError("Passwords not match");
            textPassword.requestFocus();
            textConfirmPassword.setError("Passwords not match");
            return;
        }
        alertDialog.showDialog();
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()) {
                    alertDialog.removeDialog();
                    print("Some Error Occurred!! fill your details again...");
                }
                else{
                    print("Registered Successfully!! verify your email..");
                    sendEmail(auth);

                    String userId = auth.getCurrentUser().getUid();
                    document = firestore.collection("users").document(userId);
                    Map<String, Object> map = new HashMap<>();
                    map.put("email", email);
                    map.put("name", name);
                    map.put("password", pass);
                    document.set(map);

                    if(uri!=null) {
                        StorageReference child = storage.child("users/" + userId + "/profile.jpg");
                        child.putFile(uri);
                    }

                    Intent intent = new Intent(signUp.this, MainActivity.class);
                    alertDialog.removeDialog();
                    finish();
                    startActivity(intent);
                }
            }
        });

    }

    void sendEmail(FirebaseAuth auth){
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    print("Email has been sent to your given email-address");
                }
            });
        }
        else{
            print("User is null");
        }

    }

    void print(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}



























