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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import Model.User;

public class VerifyPhoneActivity extends AppCompatActivity {
  private User userCurrent;
  private String mode;
  private String phoneNumber;
  private String mVerificationId;
  PhoneAuthProvider.ForceResendingToken mForceResendingToken;

  private SharedPreferences sharedPreferences;
  private FirebaseAuth firebaseAuth;

  private EditText codeEditText;
  private Button verifyButton;
  private TextView sendAgainA, skipA, notifyTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_verify_phone);

    sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
    firebaseAuth = FirebaseAuth.getInstance();

    userCurrent = getUser();

    if (userCurrent == null) {
      startActivity(new Intent(VerifyPhoneActivity.this, SignInActivity.class));
    }

    createIntent();
    createUI();

    if ( mode.equals("forgot") ) {
      phoneNumber = getIntent().getStringExtra("USERNAME");
    }
    else {
      userCurrent = getUser();
      phoneNumber = userCurrent.getUsername();
    }
    send(phoneNumber.substring(1));

    verifyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        notifyTextView.setText(R.string.loading);

        String code = String.valueOf(codeEditText.getText()).trim();

        if ( code.isEmpty () ) {
            notifyTextView.setText ("OTP is requirement!");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
      }
    });

    sendAgainA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendAgain(phoneNumber);
      }
    });

    skipA.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(VerifyPhoneActivity.this, ListNote.class));
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

  private void createIntent() {
    Intent intent = getIntent();
    mode = intent.getStringExtra("MODE");
  }

  private void createUI() {
    codeEditText = findViewById(R.id.codeEditText);
    verifyButton = findViewById(R.id.verifyButton);
    sendAgainA = findViewById(R.id.sendAgainA);
    skipA = findViewById(R.id.skipA);
    notifyTextView = findViewById(R.id.notifyTextView);
  }

  private void send(String phoneNumber) {
    PhoneAuthOptions options =
            PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber("+84 " + phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                      @Override
                      public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                      }

                      @Override
                      public void onVerificationFailed(@NonNull FirebaseException e) {
                          notifyTextView.setText ("+84 " + phoneNumber);
                      }

                      @Override
                      public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        mVerificationId = verificationId;
                      }
                    })
                    .build();
    PhoneAuthProvider.verifyPhoneNumber(options);
  }

  private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
    firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
              @Override
              public void onSuccess(AuthResult authResult) {
                if ( mode.equals("activated") ) {
                  CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

                  collectionReference.get()
                          .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                              for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                if ( !userCurrent.getUsername().equals( queryDocumentSnapshot.toObject(User.class).getUsername() ) ) {
                                  continue;
                                }

                                Map updateData = new HashMap();
                                updateData.put("activated", true);

                                collectionReference.document(queryDocumentSnapshot.getId()).update(updateData)
                                        .addOnSuccessListener(new OnSuccessListener() {
                                          @Override
                                          public void onSuccess(Object o) {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean("activated", true);
                                            editor.apply();

                                            startActivity(new Intent(VerifyPhoneActivity.this, ListNote.class));
                                            finish();
                                          }
                                        });

                                break;
                              }
                            }
                          });
                }
                else {
                  Intent intent = new Intent(VerifyPhoneActivity.this, PasswordNewActivity.class);
                  intent.putExtra("USERNAME", phoneNumber);
                  startActivity(intent);
                  finish();
                }
              }
            });
  }

  private void sendAgain(String phoneNumber) {
    PhoneAuthOptions options =
            PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber("+84 " + phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setForceResendingToken(mForceResendingToken)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                      @Override
                      public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                      }

                      @Override
                      public void onVerificationFailed(@NonNull FirebaseException e) {
                      }

                      @Override
                      public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        mVerificationId = verificationId;
                        mForceResendingToken = forceResendingToken;
                      }
                    })
                    .build();
    PhoneAuthProvider.verifyPhoneNumber(options);
  }
}