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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import Model.User;

public class PasswordChangeActivity extends AppCompatActivity {
  private User userCurrent;

  private SharedPreferences sharedPreferences;

  private EditText oldPasswordEditText, newPasswordEditText, reNewPasswordEditText;
  private Button changeButton;
  private TextView notifyTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_password_change);

    sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

    userCurrent = getUser();

    if (userCurrent == null) {
      startActivity(new Intent(PasswordChangeActivity.this, SignInActivity.class));
    }

    createUI();

    changeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        notifyTextView.setText(R.string.loading);

        changePassword();
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
    oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
    newPasswordEditText = findViewById(R.id.newPasswordEditText);
    reNewPasswordEditText = findViewById(R.id.reNewPasswordEditText);
    changeButton = findViewById(R.id.changeButton);
    notifyTextView = findViewById(R.id.notifyTextView);
  }

  private void changePassword() {
    String oldPassword = oldPasswordEditText.getText().toString().trim();
    String newPassword = newPasswordEditText.getText().toString().trim();
    String reNewPassword = reNewPasswordEditText.getText().toString().trim();

    if ( oldPassword.isEmpty() ) {
      notifyTextView.setText(R.string.old_password_require);
      return;
    }

    if ( newPassword.isEmpty() ) {
      notifyTextView.setText(R.string.new_password_require);
      return;
    }

    if ( newPassword.length() < 6 ) {
      notifyTextView.setText(R.string.new_password_length_require);
      return;
    }

    if ( !newPassword.equals(reNewPassword) ) {
      notifyTextView.setText(R.string.re_password_match_require);
      return;
    }

    if ( !oldPassword.equals( userCurrent.getPassword() ) ) {
      notifyTextView.setText(R.string.old_password_incorrect);
      return;
    }

    if ( userCurrent.getType().equals("phone") ) {
      CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");
      collectionReference.get()
              .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                  for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    if (!userCurrent.getUsername().equals(queryDocumentSnapshot.toObject(User.class).getUsername())) {
                      continue;
                    }

                    Map updateData = new HashMap();
                    updateData.put("password", newPassword);

                    collectionReference.document(queryDocumentSnapshot.getId()).update(updateData)
                            .addOnSuccessListener(new OnSuccessListener() {
                              @Override
                              public void onSuccess(Object o) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("password", newPassword);
                                editor.apply();


                                startActivity(new Intent(PasswordChangeActivity.this, UserActivity.class));
                                finish();
                              }
                            });

                    break;
                  }
                }
              });
    }
    else {
      Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).updatePassword(newPassword)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                  SharedPreferences.Editor editor = sharedPreferences.edit();
                  editor.putString("password", newPassword);
                  editor.apply();

                  startActivity(new Intent(PasswordChangeActivity.this, UserActivity.class));
                  finish();
                }
              });
    }
  }
}