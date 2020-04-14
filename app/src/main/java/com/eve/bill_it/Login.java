package com.eve.bill_it;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;


public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1234 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkLogin();
    }
     void login(){
         List<AuthUI.IdpConfig> providers = Arrays.asList(
                 new AuthUI.IdpConfig.GoogleBuilder().build()
         );
         startActivityForResult(
                 AuthUI.getInstance()
                         .createSignInIntentBuilder()
                         .setAvailableProviders(providers)
                         .build(),
                 RC_SIGN_IN);
     }

     void checkLogin(){
         FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
         if(user != null){
             startActivity(new Intent(this,MainActivity.class));
             finish();
         }else{
             login();
         }

     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                checkLogin();
                // ...
            } else {
                login();
                Toast.makeText(this, "Please Login to Continue", Toast.LENGTH_SHORT).show();
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
}
