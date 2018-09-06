package com.sassaworks.senseplanner;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sassaworks.senseplanner.data.User;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {


    List<AuthUI.IdpConfig> providers;
    FirebaseUser user;

    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //setting up providers for authorization
        providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());

        user = FirebaseAuth.getInstance().getCurrentUser();



        if (user == null)
        {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(providers).build(),RC_SIGN_IN);
            finish();
        }
        else {
            //copyDefaultCategories(db.getReference("defaultactivities"),db.getReference("planner"));
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK)
            {
                user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();
                FirebaseDatabase db = FirebaseDatabase.getInstance();

                DatabaseReference usersRef = db.getReference("users");

                String userId = user.getUid();
                boolean ver = user.isEmailVerified();

                Query userIsExist = usersRef.child(userId);


                userIsExist.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User currentUser;
                        currentUser = dataSnapshot.getValue(User.class);

                        Log.d("PLAN","onDataChange");
                        if (currentUser == null)
                        {

                            db.getReference("users").child(user.getUid()).setValue(new User(user.getDisplayName(),user.getEmail(),user.getPhoneNumber()));
                            copyDefaultCategories(db.getReference("defaultactivities"),db.getReference("planner"),"activities");
                            copyDefaultCategories(db.getReference("defaultappealing"),db.getReference("planner"),"appealing");
                            copyDefaultCategories(db.getReference("defaultmood"),db.getReference("planner"),"mood");

                        }
                        //move
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("PLAN","onCancelled");
                    }

                });


                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            else
            {
                finish();
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void copyDefaultCategories(DatabaseReference defaultactivities, DatabaseReference planner, String categoryType) {
        defaultactivities.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                planner.child(user.getUid()).child(categoryType).setValue(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
