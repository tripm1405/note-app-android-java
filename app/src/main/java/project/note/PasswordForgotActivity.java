package project.note;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import Model.User;

public class PasswordForgotActivity extends AppCompatActivity {
  private User userCurrent;

  private SharedPreferences sharedPreferences;

  private EditText usernameEditText;
  private Button sendButton;
  private TextView signInA, signUpA, notifyTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_password_forgot);

    sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

    userCurrent = getUser();

    if (userCurrent != null) {
      startActivity(new Intent(PasswordForgotActivity.this, SignInActivity.class));
    }

    createUI();

    sendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        notifyTextView.setText(R.string.loading);

        send();
      }
    });

    signInA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(PasswordForgotActivity.this, SignInActivity.class));
      }
    });

    signUpA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(PasswordForgotActivity.this, SignUpActivity.class));
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
    usernameEditText = findViewById(R.id.usernameEditText);
    sendButton = findViewById(R.id.sendButton);
    signInA = findViewById(R.id.signInA);
    signUpA = findViewById(R.id.signUpA);
    notifyTextView = findViewById(R.id.notifyTextView);
  }

  private void send() {
    String username = usernameEditText.getText().toString().trim();

    String type;
    if ( Pattern.compile("0[0-9]{9}").matcher(username).find() ) {
      type = "phone";
    }
    else if ( Pattern.compile("^(.+)@(.+)$").matcher(username).find() ) {
      type = "email";
    }
    else {
      type = null;
    }

    if ( username.isEmpty() ) {
      notifyTextView.setText(R.string.username_require);
      return;
    }

    if (type == null) {
      notifyTextView.setText(R.string.username_type_require);
      return;
    }

    Intent intent;
    if ( type.equals("phone") ) {
      intent = new Intent(PasswordForgotActivity.this, VerifyPhoneActivity.class);
    }
    else {
      intent = new Intent(PasswordForgotActivity.this, VerifyEmailActivity.class);
    }
    intent.putExtra("USERNAME", username);
    intent.putExtra("MODE", "forgot");
    startActivity(intent);
  }
}