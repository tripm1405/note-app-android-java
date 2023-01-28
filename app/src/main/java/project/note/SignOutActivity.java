package project.note;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import Model.User;

public class SignOutActivity extends AppCompatActivity {
  private User userCurrent;

  private SharedPreferences sharedPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_out);

    sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

    userCurrent = getUser();

    if (userCurrent == null) {
      startActivity(new Intent(SignOutActivity.this, SignInActivity.class));
    }

    if ( Objects.requireNonNull(userCurrent).getType().equals("email") ) {
      FirebaseAuth.getInstance().signOut();
    }

    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.clear();
    editor.apply();

    startActivity(new Intent(SignOutActivity.this, SignInActivity.class));
    finish();
  }

  private User getUser() {
    if ( !sharedPreferences.contains("username") ) {
      return null;
    }

    String username = sharedPreferences.getString("username", "");
    String password = sharedPreferences.getString("password", "");
    String type = sharedPreferences.getString("type", "");
    boolean activated = sharedPreferences.getBoolean("activated", false);

    return new User(username, password, type, activated);
  }
}