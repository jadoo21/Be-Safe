package com.jadoo.risha.besafe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class Home extends AppCompatActivity {

    TextView textEmail, textName, textPassword;
    Button btsignOut, btChange;
    ImageView imgProfile;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    DocumentReference document;
    StorageReference storage;
    boolean google;
    GoogleSignInClient client;
    dialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        alertDialog = new dialog(this);

        textEmail = findViewById(R.id.text_show_email);
        textName = findViewById(R.id.text_show_name);
        textPassword = findViewById(R.id.text_show_pass);
        btsignOut = findViewById(R.id.button_sign_out);
        imgProfile = findViewById(R.id.image_profile);
        btChange = findViewById(R.id.button_change);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        client = GoogleSignIn.getClient(this, gso);

//        alertDialog.showDialog();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();

            Picasso.get().load(personPhoto).into(imgProfile);
            textName.setText(personName);
            textEmail.setText(personEmail);
            textPassword.setText(personGivenName+"\n"+personFamilyName+"\n"+personId);
            google = true;
        }
        else if(user!=null){
            String userId = user.getUid();
            document = firestore.collection("users").document(userId);
            document.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snap, @Nullable FirebaseFirestoreException e) {
                    if(snap!=null) {
                        textEmail.setText(snap.getString("email"));
                        textName.setText(snap.getString("name"));
                        textPassword.setText(snap.getString("password"));
                    }
                }
            });
            StorageReference child = storage.child("users/"+userId+"/profile.jpg");
            child.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    print("loading image...");
                    imgProfile.setImageResource(android.R.color.transparent);
                    Picasso.get().load(uri).into(imgProfile);
                }
            });
            google = false;
        }
//        alertDialog.removeDialog();

        btsignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        btChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(google)
                    print("This functionality is not available in Sign in with google");
                else
                    changePic();
            }
        });

    }

    void changePic(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1000 && resultCode== Activity.RESULT_OK && data!=null){
            final Uri uri = data.getData();
            StorageReference child = storage.child("users/"+user.getUid()+"/profile.jpg");
            child.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Picasso.get().load(uri).into(imgProfile);
                    print("Profile pic changed!!!");
                }
            });
        }
    }

    void signOut(){
        alertDialog.showDialog();
        if(google){
            client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                        print("Log out successfully!!");
                    else
                        print("Error!! occurred..");
                }
            });
        }
        else{
            auth.signOut();
            print("Log out successfully!!");
        }
        Intent intent = new Intent(Home.this, MainActivity.class);
        alertDialog.removeDialog();
        finish();
        startActivity(intent);
    }

    void print(String msg){
        Toast.makeText(Home.this, msg, Toast.LENGTH_LONG).show();
    }

}
