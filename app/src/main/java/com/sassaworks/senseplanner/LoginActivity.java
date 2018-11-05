package com.sassaworks.senseplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.User;
import com.sassaworks.senseplanner.ui.ActivityTypeFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {


  List<AuthUI.IdpConfig> providers;
  FirebaseUser user;

  private static final int RC_SIGN_IN = 123;
  public static boolean IS_FIRST_TIME = false;

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


    //((AnimationDrawable) getWindow().getDecorView().getBackground()).start();

    if (user == null)
    {
      startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
              .setAvailableProviders(providers).build(),RC_SIGN_IN);
    }
    else {
      IS_FIRST_TIME = false;
      startActivity(new Intent(this, MainActivity.class));
      //startActivity(new Intent(this,LoadingActivity.class));
      finish();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Context context = this;

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
            String curLocale = Locale.getDefault().getLanguage();
            curLocale = curLocale.equals("en") || curLocale.equals("ru") ? curLocale : "en";
            if (currentUser == null)
            {
              SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
              editor.putBoolean(ActivityTypeFragment.WELCOME_LABEL,false).apply();
              editor.putBoolean(EditCategoryActivity.ONBOARDING_SCREEN,false).apply();

              IS_FIRST_TIME = true;
              db.getReference(getString(R.string.ref_users)).child(user.getUid()).setValue(new User(user.getDisplayName(),user.getEmail(),user.getPhoneNumber()));
              copyDefaultCategories(db.getReference(getString(R.string.ref_defaultactivities)+"/"+curLocale),db.getReference(getString(R.string.ref_planner)),getString(R.string.ref_activities));
              copyDefaultCategories(db.getReference(getString(R.string.ref_defaultappealing)+"/"+curLocale),db.getReference(getString(R.string.ref_planner)),getString(R.string.ref_appealing));
              copyDefaultCategories(db.getReference(getString(R.string.ref_defaultmood)+"/"+curLocale),db.getReference(getString(R.string.ref_planner)),getString(R.string.ref_mood));
            }
            else
            {
              IS_FIRST_TIME = false;
            }
            startActivity(new Intent(context, MainActivity.class));
            finish();
            //move
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }

        });


      }
      else
      {
        finish();
      }
    }
  }


  private void copyDefaultCategories(DatabaseReference defaultactivities, DatabaseReference planner, String categoryType) {
    defaultactivities.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        //Category category =  dataSnapshot.getValue(Activity.class);
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