package project.note;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import Model.User;

public class UserActivity extends AppCompatActivity {
  private User userCurrent;

  private SharedPreferences sharedPreferences;

  private TextView displayTextView;
  private Button verifyButton,
          passwordChangeButton,
          signOutButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user);

    sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

    userCurrent = getUser();

    if (userCurrent == null) {
      startActivity(new Intent(UserActivity.this, SignInActivity.class));
      finish();
    }

    displayTextView = findViewById(R.id.displayTextView);
    verifyButton = findViewById(R.id.verifyButton);
    passwordChangeButton = findViewById(R.id.passwordChangeButton);
    signOutButton = findViewById(R.id.signOutButton);

    displayTextView.setText(
            userCurrent.getUsername() + ", " +
                    userCurrent.getPassword() + ", " +
                    userCurrent.getType() + ", " +
                    userCurrent.isActivated()
    );

    verifyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent;
        if ( userCurrent.getType().equals("phone") ) {
          intent = new Intent(UserActivity.this, VerifyPhoneActivity.class);
        }
        else {
          intent = new Intent(UserActivity.this, VerifyEmailActivity.class);
        }
        intent.putExtra("MODE", "activated");
        startActivity(intent);
      }
    });

    passwordChangeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(UserActivity.this, PasswordChangeActivity.class));
      }
    });

    signOutButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(UserActivity.this, SignOutActivity.class));
      }
    });
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