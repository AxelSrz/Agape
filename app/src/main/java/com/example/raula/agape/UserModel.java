package com.example.raula.agape;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by raula on 2/21/2017.
 */

public class UserModel {
    String id;
    String name;
    String email;
    String room_id;
    String partner_id;
    String invitation_sent;
    String invitation_received;


    static UserModel currentUser;

    static UserModel currentPartner;

    public UserModel(){
        id = "";
        name = "";
        email = "";
        room_id = "none";
        partner_id = "none";
        invitation_sent = "none";
        invitation_received = "none";
    }

    public UserModel(String inputId, String inputName, String inputEmail){
        id = inputId;
        name = inputName;
        email = inputEmail;
        room_id = "none";
        partner_id = "none";
        invitation_sent = "none";
        invitation_received = "none";
    }

    public void copy(UserModel toCopy){
        id = toCopy.id;
        name = toCopy.name;
        email = toCopy.email;
        room_id = toCopy.room_id;
        partner_id = toCopy.partner_id;
        invitation_sent = toCopy.invitation_sent;
        invitation_received = toCopy.invitation_received;
    }

    public void resetStatus(){
        switch (getStatus()){
            case INVITATION_RECEIVED:
                invitation_received = "none";
                break;
            case INVITATION_SENT:
                invitation_sent = "none";
                break;
        }

    }

    public static void pairCurrentCandidates(){
        currentUser.resetStatus();
        currentUser.partner_id = currentPartner.id;
        currentUser.room_id = FirebaseHelper.currentRoomReference.getKey();
        currentPartner.resetStatus();
        currentPartner.partner_id = currentUser.id;
        currentPartner.room_id = FirebaseHelper.currentRoomReference.getKey();
    }

    static void setCurrentUser(UserModel user){
        currentUser = user;
    }

    public UserStatus getStatus(){
        if(!partner_id.equals("none")){
            return UserStatus.PAIRED;
        }
        else if(!invitation_sent.equals("none")){
            return  UserStatus.INVITATION_SENT;
        }
        else if(!invitation_received.equals("none")){
            return  UserStatus.INVITATION_RECEIVED;
        }
        return UserStatus.NOT_PAIRED;
    }


}
