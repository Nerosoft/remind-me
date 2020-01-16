package com.remind.me.firebaseapp;

import android.app.DatePickerDialog;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.remind.me.firebaseapp.RM_Login.fireIDData;


public class RM_SignUp extends AppCompatActivity {

    private RM_SharedPreference preference;
    private CircleImageView Login_Photo;
    private EditText Name, Password, Number, Birthday, Key;
    private String Gender = "";
    private TextView TGender;
    private Firebase fireRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rm_signup);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        preference = new RM_SharedPreference(this);
        fireRef = new Firebase("https://"+fireIDData+".firebaseio.com/");
        this.setupView();
    }

    public void setDate(View view) {
        final EditText editText = (EditText) view;
        new DatePickerDialog(RM_SignUp.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                editText.setText(i + "-" + ++i1 + "-" + i2);
            }
        }, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                //      pickerDialog.setTitle("Select Date");
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(menuItem);
    }

    public void setImage(View view) {
        RM_SharedPreference.AddPhoto(this);
    }

    /*********  CALL METHOD TO SAVE & ADD NEW IMAGE VIEW SHAREDPREFRENCES  USING BASE64 ENCODE ********/
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {
            try {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(this.getResources(),
                        MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
                Bitmap bitmap = bitmapDrawable.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();
                long lengthbmp = imageInByte.length;
                if ((lengthbmp / 1024) < 1800)
                    RM_SharedPreference.setImage(this, this.Login_Photo, data);
                else
                    Toast.makeText(this, "The size of image is big than 1.5 mega", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public void setGender(View view) {
        this.Gender = (view.getTag().toString()
                .equals("male")) ? "male" : "female";
    }

    public void createAccount(View view) {
        AppCompatButton BuCreateUser = (AppCompatButton) view;
        BuCreateUser.setEnabled(false);
        if (NetworkAvailable.isNetworkAvailable(this)) {
            this.Name = findViewById(R.id.input_name);
            this.Password = findViewById(R.id.input_password);
            this.Number = findViewById(R.id.input_number);
            this.Birthday = findViewById(R.id.input_birthday);
            this.Key = findViewById(R.id.input_key);
            if (this.validate(this.Name, this.Password, this.Birthday, this.Key)) {
                // Toast.makeText(this, "sucsscefuly", Toast.LENGTH_SHORT).show();
                this.singUpUser(BuCreateUser, this.Name, this.Password.getText().toString(),
                        this.Gender, this.Birthday.getText().toString(),
                        this.Number.getText().toString(), this.Key.getText().toString());
            } else
                BuCreateUser.setEnabled(true);


        } else {
            Toast.makeText(this, "Please Check Your Network Connection"
                    , Toast.LENGTH_LONG).show();
            BuCreateUser.setEnabled(true);
        }
    }

    private void setupView() {
        this.Login_Photo = findViewById(R.id.login_photo);
        this.TGender = findViewById(R.id.input_gender);
    }

    public boolean isStringNullOrWhiteSpace(String value) {
        if (value == null) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i)))
                return true;

        }

        return false;
    }

    public boolean validate(EditText name, EditText password, EditText birthday,
                            EditText key) {
        boolean valid = true;

        if (isStringNullOrWhiteSpace(name.getText().toString())) {
            name.setError("the user name has a space");
            return false;
        } else
            name.setError(null);


        if (name.getText().toString().isEmpty() || name.length() < 7) {
            valid = false;
            name.setError("at least 7 characters");
        } else
            name.setError(null);

        if (password.getText().toString().isEmpty() || password.length() < 7
                || password.length() > 10) {
            valid = false;
            password.setError("between 7 and 10 alphanumeric characters");
        } else
            password.setError(null);

        if (birthday.getText().toString().isEmpty() ||
                !birthday.getText().toString().contains("-")) {
            valid = false;
            birthday.setError("enter a valid birthday.");
        } else {
            String[] date = (birthday.getText().toString() + "-").split("-");
            if (date.length < 4 && date[0].length() == 4 &&
                    Integer.parseInt(date[0]) > 1950 &&
                    //-------------------------------------------
                    date[1].length() < 3 &&
                    Integer.parseInt(date[1]) <= 12 &&
                    Integer.parseInt(date[1]) > 0 &&
                    //---------------------------------------------
                    date[2].length() < 3 &&
                    Integer.parseInt(date[2]) <= 31 &&
                    Integer.parseInt(date[2]) > 0) {
                birthday.setError(null);
            } else {
                valid = false;
                birthday.setError("enter a valid birthday.");
            }
        }

        if (key.getText().toString().isEmpty() || key.length() < 5) {
            valid = false;
            key.setError("at least 5 characters");
        } else
            key.setError(null);

        if (this.Gender.isEmpty()) {
            valid = false;
            this.TGender.setError("Please Enter The Gender");
        } else
            this.TGender.setError(null);


        return valid;
    }

    private void singUpUser(final AppCompatButton bu_create_user, final EditText name,
                            String password, String gender,
                            String birthday, String number, String key) {

        final ProgressDialog dialog = new ProgressDialog();
        dialog.show(getFragmentManager(), "");

        final Firebase childUser = fireRef.child("user");
        final RM_User user = new RM_User(name.getText().toString(), password, number, birthday, gender, key);

        //to add data to firebase
        childUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //check if the same user is found on firebase
                if (dataSnapshot.child(user.getName()).exists()) {
                    name.setError("This email is already exist");
                    Toast.makeText(getBaseContext(), "user is already exit", Toast.LENGTH_SHORT).show();
                    bu_create_user.setEnabled(true);
                    dialog.dismiss();//--------------------
                } else {
                    //add the user
                    childUser.child(user.getName()).setValue(user); //set all info
                    final JSONObject OjectUserName = new JSONObject();
                    final String UserName = user.getName();
                    try {
                        OjectUserName.put("name", user.getName());
                        OjectUserName.put("gender", user.getGender());
                        OjectUserName.put("birthday", user.getBirthday());
                        OjectUserName.put("number", user.getNumber());
                        //upload imag on firebase
                        uploadUserPhoto(UserName, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                preference.seveUserState(OjectUserName.toString());
                                preference.saveImageInPreferences(Login_Photo);
                                startActivity(new Intent(getBaseContext(), RM_MainActivity.class));
                                finish();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        bu_create_user.setEnabled(true);
                        dialog.dismiss();//----------------------------
                    }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                bu_create_user.setEnabled(true);
                dialog.dismiss();//------------------------------------
            }
        });
    }

    void uploadUserPhoto(String UserName, OnSuccessListener successListener) {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://"+fireIDData+".appspot.com/");
        StorageReference storageRef = storage.getReference();
        StorageReference mountainImagesRef = storageRef.child("user_photo/" + UserName + ".jpg");
        BitmapDrawable bitmapDrawable = (BitmapDrawable) Login_Photo.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] data = stream.toByteArray();
        UploadTask uploadTask = mountainImagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(successListener);

    }

}

//Toast.makeText(this,this.Gender,Toast.LENGTH_SHORT).show();
//Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);