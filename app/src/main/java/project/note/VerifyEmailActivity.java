package project.note;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import Model.User;

public class VerifyEmailActivity extends AppCompatActivity {
  private User userCurrent;
  private String email;
  private String mode;

  FirebaseAuth firebaseAuth;
  private SharedPreferences sharedPreferences;

  private TextView notifyTextView, skipA;
  private Button verifyButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_verify_email);

    firebaseAuth = FirebaseAuth.getInstance();

    sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

    userCurrent = getUser();

    if (userCurrent == null) {
      startActivity(new Intent(VerifyEmailActivity.this, SignInActivity.class));
    }

    createUI();
    createIntent();

    if ( mode.equals("forgot") ) {
      email = getIntent().getStringExtra("USERNAME");
    }
    else {
      userCurrent = getUser();
      email = userCurrent.getPassword().substring(1);
    }

    send();

    verifyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        notifyTextView.setText(R.string.loading);

        verify();
      }
    });

    skipA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(VerifyEmailActivity.this, ListNote.class));
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

  private void createUI() {
    notifyTextView = findViewById(R.id.notifyTextView);
    verifyButton = findViewById(R.id.verifyButton);
    skipA = findViewById(R.id.skipA);
  }

  private void createIntent() {
    Intent intent = getIntent();
    mode = intent.getStringExtra("MODE");
  }

  private void send() {
    if ( mode.equals("forgot") ) {
      firebaseAuth.sendPasswordResetEmail(email);
    }
    else {
      Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification();
    }
  }

  private void verify() {
    if ( mode.equals("activated") ) {
      Handler handler = new Handler();

      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          firebaseAuth.signOut();

          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              firebaseAuth.signInWithEmailAndPassword(userCurrent.getUsername(), userCurrent.getPassword());

              handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                  if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("activated", true);
                    editor.apply();

                    startActivity(new Intent(VerifyEmailActivity.this, ListNote.class));
                  }
                  else {
                    notifyTextView.setText(R.string.verify_failed);
                  }
                }
              }, 1000);
            }
          }, 1000);
        }
      }, 1000);
    }
    else {
      startActivity(new Intent(VerifyEmailActivity.this, UserActivity.class));
    }
  }
}