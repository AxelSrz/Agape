package com.example.raula.agape;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raula on 3/2/2017.
 */

public abstract class FirebaseHelper {


    static DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");

    static DatabaseReference roomsReference = FirebaseDatabase.getInstance().getReference("chat_rooms");

    static DatabaseReference currentRoomReference = null;

    static StorageReference photoStorageRef = FirebaseStorage.getInstance().getReference();

    static StorageReference getPhotoFolderRef() {
        return photoStorageRef.child(currentRoomReference.getKey());
    }

    static DatabaseReference getMessagesReference() {
        return currentRoomReference.child("/mesagges");
    }

    static DatabaseReference getTodoReference() {
        return currentRoomReference.child("/todos");
    }

    static DatabaseReference getPhotoLinkReference() {
        return currentRoomReference.child("/photo_links");
    }

    static void setCurrentRoomReference(){
        currentRoomReference = roomsReference.child(UserModel.currentUser.room_id);
    }

    static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    static DatabaseReference getCurrentUserReference() {
        return usersReference.child(UserModel.currentUser.id);
    }

    static void saveNewUser(UserModel user) {
        usersReference.child(user.id).setValue(user);
    }

    static void updateUser(UserModel user) {
        Map<String, Object> userToUpdate = new HashMap<String, Object>();
        userToUpdate.put(user.id, user);
        usersReference.updateChildren(userToUpdate);
    }

    static FirebaseAuth getAuthenticator() {
        return FirebaseAuth.getInstance();
    }

    static void setCurrentUserFromFirebase(String id, final UserNotifier userActivity) {
        usersReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserModel.setCurrentUser(snapshot.getValue(UserModel.class));
                userActivity.currentUserRetrieved();
            }
            @Override public void onCancelled(DatabaseError error) {
            }
        });
    }

    static void setPartnerFromFirebase(String id, final UserNotifier userActivity) {
        usersReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserModel.currentPartner = snapshot.getValue(UserModel.class);
                userActivity.partnerRetrieved();
            }
            @Override public void onCancelled(DatabaseError error) {
            }
        });
    }

    static void readUserFromDB(String id, final FirebaseDataListener listener){
        listener.onStart();
        usersReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listener.onSuccess(snapshot);
            }
            @Override public void onCancelled(DatabaseError error) {
                listener.onFailed(error);
            }
        });
    }

    static void sendInvitationTo(final String email, final InvitationNotifier invitationActivity){
        usersReference.orderByChild("email").equalTo(email).limitToFirst(1)
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserModel userToInvite = null;
                for (DataSnapshot snap: snapshot.getChildren()) {
                    userToInvite = snap.getValue(UserModel.class);
                }
                String message = "";
                if (userToInvite != null){
                    switch (userToInvite.getStatus()){
                        case PAIRED:
                            message = "Sorry but that user is paired with someone else";
                            break;
                        case INVITATION_SENT:
                            message = "Sorry but that user already sent an invitation to another user";
                            break;
                        case INVITATION_RECEIVED:
                            message = "Sorry but that user received another invitation first";
                            break;
                        case NOT_PAIRED:
                            userToInvite.invitation_received = UserModel.currentUser.id;
                            UserModel.currentUser.invitation_sent = userToInvite.id;
                            updateUser(userToInvite);
                            updateUser(UserModel.currentUser);
                            UserModel.currentPartner = userToInvite;
                            message = "Invitation sent successfully!!";
                            break;
                    }
                } else {
                    message = "Sorry but there is no user with that email";
                }

                invitationActivity.sendInvitationFinished(message);
            }
            @Override public void onCancelled(DatabaseError error){
                invitationActivity.sendInvitationFinished("There was a problem accessing the database, try later");
            }
        });
    }

    static void acceptInvitation(){
        Map<String, String> newRoom = new HashMap<String, String>();
        newRoom.put("idUser1", UserModel.currentPartner.id);
        newRoom.put("idUser2", UserModel.currentUser.id);

        currentRoomReference = roomsReference.push();
        currentRoomReference.setValue(newRoom);
        UserModel.pairCurrentCandidates();
        photoStorageRef.child(currentRoomReference.getKey());


        updateUser(UserModel.currentUser);
        updateUser(UserModel.currentPartner);
    }

    static void cancelInvitation(){
        UserModel.currentUser.invitation_sent = "none";
        UserModel.currentPartner.invitation_received = "none";

        updateUser(UserModel.currentUser);
        updateUser(UserModel.currentPartner);

        UserModel.currentPartner = null;
    }

    static void declineInvitation() {
        UserModel.currentUser.invitation_received = "none";
        UserModel.currentPartner.invitation_sent = "none";

        updateUser(UserModel.currentUser);
        updateUser(UserModel.currentPartner);

        UserModel.currentPartner = null;
    }
}
