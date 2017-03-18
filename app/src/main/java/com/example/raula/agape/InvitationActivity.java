package com.example.raula.agape;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class InvitationActivity extends AppCompatActivity implements InvitationNotifier {

    private FirebaseAuth auth;
    private ValueEventListener userListener;

    private UserStatus previousStatus;

    private TextView notPairedTitle, inviteReceivedTitle, waitingTitle;
    private EditText inviteEmailField;
    private Button sendInviteBtn, acceptInviteBtn, declineInviteBtn, cancelInviteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        auth = FirebaseHelper.getAuthenticator();
        previousStatus = UserModel.currentUser.getStatus();
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(UserModel.class).getStatus() != previousStatus){
                    startActivity(new Intent(InvitationActivity.this, InvitationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        switch (UserModel.currentUser.getStatus()){
            case NOT_PAIRED:
                setContentView(R.layout.no_invite);
                notPairedTitle = (TextView) findViewById(R.id.txtv_invite);
                inviteEmailField = (EditText) findViewById(R.id.txtf_invite_mail);
                sendInviteBtn = (Button) findViewById(R.id.btn_send_invite);
                sendInviteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!inviteEmailField.getText().toString().trim().equals("")) {
                            FirebaseHelper.sendInvitationTo(inviteEmailField.getText().toString().trim(), InvitationActivity.this);
                        } else if (inviteEmailField.getText().toString().trim().equals("")) {
                            inviteEmailField.setError("Enter email");
                        }
                    }
                });
                break;
            case INVITATION_SENT:
                setContentView(R.layout.waiting_response);
                waitingTitle = (TextView) findViewById(R.id.txtv_waiting_invite);
                waitingTitle.setText(String.format(res.getString(R.string.waiting_title), UserModel.currentPartner.name));
                cancelInviteBtn = (Button) findViewById(R.id.cancel_button);
                cancelInviteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseHelper.cancelInvitation();
                    }
                });
                break;
            case INVITATION_RECEIVED:
                setContentView(R.layout.invite);
                inviteReceivedTitle = (TextView) findViewById(R.id.txtv_received_invite);
                inviteReceivedTitle.setText(String.format(res.getString(R.string.invitation_title),
                                                          UserModel.currentPartner.name,
                                                          UserModel.currentPartner.email));
                acceptInviteBtn = (Button) findViewById(R.id.btn_accept_invite);
                declineInviteBtn = (Button) findViewById(R.id.btn_decline_invite);
                declineInviteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseHelper.declineInvitation();
                    }
                });

                acceptInviteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseHelper.acceptInvitation();
                    }
                });
                break;
            case PAIRED:
                FirebaseHelper.setCurrentRoomReference();
                startActivity(new Intent(InvitationActivity.this, AgapeActivity.class));
                finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseHelper.getCurrentUserReference().addValueEventListener(userListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (userListener != null) {
            FirebaseHelper.getCurrentUserReference().removeEventListener(userListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, AccountSettingsActivity.class));
                return true;
            case R.id.action_logout:
                auth.signOut();
                startActivity(new Intent(InvitationActivity.this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void sendInvitationFinished(String status) {
        Toast.makeText(InvitationActivity.this, status, Toast.LENGTH_LONG).show();
    }
}
