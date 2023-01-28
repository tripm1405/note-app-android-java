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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

import Model.User;

public class SignUpActivity extends AppCompatActivity {
  private User userCurrent;

  private SharedPreferences sharedPreferences;

  private EditText usernameEditText, passwordEditText, rePasswordEditText;
  private Button signUpButton;
  private TextView passwordForgotA, signInA, notifyTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);

    sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

    userCurrent = getUser();

    if (userCurrent != null) {
      startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
    }

    createUI();

    signUpButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        notifyTextView.setText(R.string.loading);

        signUp();
      }
    });

    passwordForgotA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(SignUpActivity.this, PasswordForgotActivity.class));
      }
    });

    signInA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
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
    rePasswordEditText = findViewById(R.id.rePasswordEditText);
    signUpButton = findViewById(R.id.signUpButton);
    passwordForgotA = findViewById(R.id.passwordForgotA);
    signInA = findViewById(R.id.signInA);
    notifyTextView = findViewById(R.id.notifyTextView);
  }

  private void signUp() {
    String username = usernameEditText.getText().toString().trim();
    String password = passwordEditText.getText().toString().trim();
    String rePassword = rePasswordEditText.getText().toString().trim();

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

    if (password.length() < 5) {
      notifyTextView.setText(R.string.password_length_require);
      return;
    }

    if (type == null) {
      notifyTextView.setText(R.string.username_type_require);
      return;
    }

    if ( !password.equals(rePassword) ) {
      notifyTextView.setText(R.string.re_password_match_require);
      return;
    }

    if ( type.equals("email") ) {
      FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

      firebaseAuth.createUserWithEmailAndPassword(username, password)
              .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                  SharedPreferences.Editor editor = sharedPreferences.edit();
                  editor.putString("username", username);
                  editor.putString("password", password);
                  editor.putString("type", type);
                  editor.putBoolean("activated", false);
                  editor.apply();

                  Intent intent = new Intent(SignUpActivity.this, VerifyEmailActivity.class);
                  intent.putExtra("MODE", "activated");
                  startActivity(intent);
                  finish();
                }
              })
              .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  notifyTextView.setText(R.string.Sign_up_failed);
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
                    if ( username.equals( queryDocumentSnapshot.toObject(User.class).getUsername() ) ) {
                      notifyTextView.setText(R.string.exists_username);
                      return;
                    }
                  }

                  User user = new User(username, password, type, false);
                  collectionReference.add(user)
                          .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                              SharedPreferences.Editor editor = sharedPreferences.edit();
                              editor.putString("username", username);
                              editor.putString("password", password);
                              editor.putString("type", type);
                              editor.putBoolean("activated", false);
                              editor.apply();

                              Intent intent = new Intent(SignUpActivity.this, VerifyPhoneActivity.class);
                              intent.putExtra("MODE", "activated");
                              startActivity(intent);
                              finish();
                            }
                          })
                          .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              notifyTextView.setText(R.string.Sign_up_failed);
                            }
                          });
                }
              })
              .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  notifyTextView.setText(R.string.restart_please);
                }
              });
    }
  }
}