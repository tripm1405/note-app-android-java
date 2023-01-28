package project.note;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Calendar;

import Model.User;
import project.note.databinding.AddNoteBinding;
import Model.Note;

public class AddNote extends AppCompatActivity {
    private User userCurrent;

    private SharedPreferences sharedPreferences;
    private Uri uri;

    private AddNoteBinding addNoteBinding;
    private static final int MY_REQUEST_CODE = 1000;

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult (
            new ActivityResultContracts.StartActivityForResult (),
            new ActivityResultCallback<ActivityResult> () {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode () == Activity.RESULT_OK){
                        Intent data = result.getData ();
                        if(data == null)
                            return;
                        uri = data.getData ();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap (getContentResolver (), uri);
                            addNoteBinding.addIvPicture.setImageBitmap (bitmap);
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                    }
                }
            }
    );
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        addNoteBinding = AddNoteBinding.inflate (getLayoutInflater ());
        setContentView (addNoteBinding.getRoot ());

        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

        userCurrent = getUser();

        if (userCurrent == null) {
            startActivity(new Intent(AddNote.this, SignInActivity.class));
        }
        
        setDay();
        setTime();
        setButton();
        addNoteBinding.btnChoseImage.setOnClickListener (view -> onClickRequesPermission());
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

    private void setDay() {
        addNoteBinding.addEtDay.setOnFocusChangeListener((view, b) -> {
            if (b) {
                addNoteBinding.addEtDay.clearFocus();

                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog (AddNote.this, (datePicker, year, month, day) ->
                    addNoteBinding.addEtDay.setText( day + "/" + (month + 1) + "/" + year), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void setTime() {
        addNoteBinding.addEtTime.setOnFocusChangeListener((view, b) -> {
            if (b) {
                addNoteBinding.addEtTime.clearFocus();

                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog (AddNote.this, (timePicker, selectedHour, selectedMinute) -> {
                    String hour = String.valueOf(selectedHour);
                    if (selectedHour < 10) {
                        hour = '0' + hour;
                    }

                    String minute = String.valueOf(selectedMinute);
                    if (selectedMinute < 10) {
                        minute = '0' + minute;
                    }
                    addNoteBinding.addEtTime.setText( hour + ":" + minute);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });
    }

    private void setButton() {
        if(getIntent ().getStringExtra ("Function").equals ("SAVE")){
            if(addNoteBinding.btnUpdate.getVisibility () == View.VISIBLE)
                addNoteBinding.btnUpdate.setVisibility (View.GONE);

            addNoteBinding.btnSave.setVisibility (View.VISIBLE);
            addNoteBinding.btnSave.setOnClickListener (view -> buttonSave ());
        }
        if(getIntent ().getStringExtra ("Function").equals ("UPDATE")){
            Note oldNote = getIntent ().getParcelableExtra ("OldNote");

            if(addNoteBinding.btnSave.getVisibility () == View.VISIBLE)
                addNoteBinding.btnSave.setVisibility (View.GONE);

            addNoteBinding.btnUpdate.setVisibility (View.VISIBLE);

            addNoteBinding.addEtTitle.setText (oldNote.getTitle ());
            addNoteBinding.addEtDay.setText (oldNote.getDay ());
            addNoteBinding.addEtTime.setText (oldNote.getTime ());
            addNoteBinding.addEtContent.setText (oldNote.getContent ());
            //ImageView

            addNoteBinding.btnUpdate.setOnClickListener (view -> buttonUpdate(oldNote.getId ()));
        }
    }

    private void buttonSave() {
        String title = addNoteBinding.addEtTitle.getText ().toString ();
        String day = addNoteBinding.addEtDay.getText ().toString ();
        String time = addNoteBinding.addEtTime.getText ().toString ();
        String content = addNoteBinding.addEtContent.getText ().toString ();
        Note note;
        if(uri == null)
            note = new Note (title, day, time, content);
        else{
            String image = uri.toString ();
            note = new Note (title, day, time, content, "", image, false, false);
        }


        Intent intent = new Intent ();
        intent.putExtra ("NOTE", note);
        setResult (RESULT_OK, intent);
        finish ();
    }

    private void buttonUpdate(String ID) {
        Note newNote = new Note ();
        newNote.setTitle (addNoteBinding.addEtTitle.getText ().toString ());
        newNote.setDay (addNoteBinding.addEtDay.getText ().toString ());
        newNote.setTime (addNoteBinding.addEtTime.getText ().toString ());
        newNote.setContent (addNoteBinding.addEtContent.getText ().toString ());
        newNote.setId (ID);
        //ImageView

        Intent intent = new Intent ();
        intent.putExtra ("NOTE", newNote);
        setResult (RESULT_OK, intent);
        finish ();
    }

    private void onClickRequesPermission() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            openGallery ();
            return;
        }else{
            if(checkSelfPermission (Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                openGallery();
            }else{
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions (permission, MY_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        if(requestCode == MY_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery ();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent ();
        intent.setType ("image/*");
        intent.setAction (Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser (intent, "Select Picture"));
    }
}
