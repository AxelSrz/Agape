package com.example.raula.agape;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class MainActivity extends AppCompatActivity implements UserNotifier {
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                }

                FirebaseHelper.setCurrentUserFromFirebase(user.getUid(), MainActivity.this);
            }
        };

    }


    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void currentUserRetrieved() {
        String id ="";
        switch (UserModel.currentUser.getStatus()){
            case PAIRED:
                id = UserModel.currentUser.partner_id;
                break;
            case INVITATION_RECEIVED:
                id = UserModel.currentUser.invitation_received;
                break;
            case INVITATION_SENT:
                id = UserModel.currentUser.invitation_sent;
                break;
            case NOT_PAIRED:
                startActivity(new Intent(MainActivity.this, InvitationActivity.class));
                finish();
                break;
        }

        FirebaseHelper.setPartnerFromFirebase(id, MainActivity.this);
    }

    @Override
    public void partnerRetrieved() {
        if (UserModel.currentUser.getStatus() == UserStatus.PAIRED){
            FirebaseHelper.setCurrentRoomReference();
            startActivity(new Intent(MainActivity.this, AgapeActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this, InvitationActivity.class));
        }
        finish();
    }
}
