package com.example.a;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_ON_CREATE = 1;
    private static final int REQ_CODE_AFTER_SIGN_OUT = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                /*
        Check if user is signed-in. if not - prompt user to sign-in/sign-up
         */
        if (FirebaseAuth.getInstance().getCurrentUser() == null){  //user is not signed-in
            //prompt user to sign-in/sign-up
            signIn(REQ_CODE_ON_CREATE);
        }
        else{ // user is signed in
            showDetails(true);
            enterPlayerNameActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        // if app doesn't crash, return true
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // only one menu item can be selected at a time
        if (item.getItemId() == R.id.log_out_item) signOut();
        return true;
    }
    public void signIn(int requestCode){
        Intent signInUp = AuthUI.getInstance().createSignInIntentBuilder().build();
        startActivityForResult(signInUp, requestCode);
    }
    private void signOut(){
        Task<Void> signOutTask = AuthUI.getInstance().signOut(this);
        signOutTask.addOnCompleteListener(task ->{
            Toast.makeText(MainActivity.this,
                    "Signed out successfully",
                    Toast.LENGTH_SHORT).show();
            signIn(REQ_CODE_AFTER_SIGN_OUT);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_ON_CREATE){
            //
            if (resultCode == RESULT_OK){
                //user successfully signed in
                showDetails(true);
                enterPlayerNameActivity();
            }else{
                showDetails(false);
                //if MainActivity finishes, the application will close
                finish();
            }
        }
    }
    /**
     * This method shows a pop-up on MainActivity with either the user details in case
     * of success login or a termination error in case the user didn't login successfully
     * @param success
     */
    public void showDetails(boolean success){
        if(success){
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userDetails = "Your display name: " + currentUser.getDisplayName()
                    + ", your ID: "
                    + currentUser.getUid()
                    + ", provider: "
                    + currentUser.getProviderId();
            Toast.makeText(this, userDetails, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, R.string.failed_login, Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * This method opens a new activity representing 2 authenticated users that can chat
     */
    public void enterPlayerNameActivity(){
        Intent intent = new Intent(this, PlayerName.class);
        startActivity(intent);
    }
}