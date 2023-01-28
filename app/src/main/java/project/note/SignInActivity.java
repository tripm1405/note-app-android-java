package project.note;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.regex.Pattern;

import Model.User;

public class SignInActivity extends AppCompatActivity {
  private User userCurrent;

  private SharedPreferences sharedPreferences;

  private EditText usernameEditText, passwordEditText;
  private Button signInButton;
  private TextView passwordForgotA, signUpA, notifyTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_in);

    sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

    userCurrent = getUser();

    if (userCurrent != null) {
      startActivity(new Intent(SignInActivity.this, SignInActivity.class));
    }

    createUI();

    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        notifyTextView.setText(R.string.loading);

        signIn();
      }
    });

    passwordForgotA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(SignInActivity.this, PasswordForgotActivity.class));
      }
    });

    signUpA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
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
    passwordEditText = findViewById(R.id.passwordEditText);
    signInButton = findViewById(R.id.signInButton);
    passwordForgotA = findViewById(R.id.passwordForgotA);
    signUpA = findViewById(R.id.signUpA);
    notifyTextView = findViewById(R.id.notifyTextView);
  }

  private void signIn() {
    String username = usernameEditText.getText().toString().trim();
    String password = passwordEditText.getText().toString().trim();

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

    if ( password.isEmpty() ) {
      notifyTextView.setText(R.string.password_require);
      return;
    }

    if (type == null) {
      notifyTextView.setText(R.string.username_type_require);
      return;
    }

    if ( type.equals("email") ) {
      FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

      firebaseAuth.signInWithEmailAndPassword(username, password)
              .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                  SharedPreferences.Editor editor = sharedPreferences.edit();

                  editor.putString("username", username);
                  editor.putString("password", password);
                  editor.putString("type", "email");
                  editor.putBoolean("activated", Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified());
                  editor.apply();

                  startActivity(new Intent(SignInActivity.this, ListNote.class));
                  finish();
                }
              })
              .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  notifyTextView.setText(R.string.Sign_in_failed);
                }
              });
    }
    else {
      CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

      collectionReference.get()
              .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                  for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    User user = queryDocumentSnapshot.toObject(User.class);

                    if ( !username.equals( user.getUsername() ) ) {
                      continue;
                    }

                    if ( !password.equals( user.getPassword() ) ) {
                      continue;
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.putString("type", "phone");
                    editor.putBoolean("activated", user.isActivated());
                    editor.apply();

                    startActivity(new Intent(SignInActivity.this, ListNote.class));
                    finish();
                  }
                }
              })
              .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  notifyTextView.setText(R.string.Sign_in_failed);
                }
              });
    }
  }
}