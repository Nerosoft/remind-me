package com.remind.me.firebaseapp;


import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class RM_Login extends AppCompatActivity {

    RM_SharedPreference preference;
    private EditText Name, Password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rm_login);
        preference = new RM_SharedPreference(this);

        if (preference.checkUserLoginState() && NetworkAvailable.isNetworkAvailable(this)) {
            startActivity(new Intent(RM_Login.this, RM_MainActivity.class));
            finish();
        }


    }

    public void singUp(View view) {
        startActivity(new Intent(RM_Login.this, RM_SignUp.class));
    }

    public void login(View view) {
        AppCompatButton butLogin=(AppCompatButton) view;
        butLogin.setEnabled(false);
        if (NetworkAvailable.isNetworkAvailable(this)) {
            this.Name = findViewById(R.id.input_name);
            this.Password = findViewById(R.id.input_password);

            if (this.validate(this.Name, this.Password)) {
                // Toast.makeText(this, "sucsscefuly", Toast.LENGTH_SHORT).show();
                this.singUpUser(butLogin,this.Name, this.Password);
            }else
                butLogin.setEnabled(true);


        } else {
            Toast.makeText(this, "Please Check Your Network Connection"
                    , Toast.LENGTH_LONG).show();
            butLogin.setEnabled(true);
        }
    }

    private void singUpUser(final  AppCompatButton butLogin ,
                            final EditText name, final EditText password) {
        final ProgressDialog dialog= new ProgressDialog();
            dialog .show(getFragmentManager(),"");
        new Firebase("https://yourfirebase/user").
                addValueEventListener(new com.firebase.client.ValueEventListener() {
                    @Override
                    public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(name.getText().toString()).exists()) {
                            try {
                                final JSONObject object = new JSONObject(dataSnapshot.child(name.getText().toString()).getValue().toString());
                                if (object.getString("password").equals(password.getText().toString())) {
                                    /*** To keep it Login Until press Logout**/
                                    final JSONObject OjectUserName = new JSONObject();
                                    final String UserName = object.getString("name");
                                    OjectUserName.put("name", object.getString("name"));
                                    OjectUserName.put("gender", object.getString("gender"));
                                    OjectUserName.put("birthday", object.getString("birthday"));
                                    OjectUserName.put("number", object.getString("number"));
                                    RM_SharedPreference.loadUserPhoto(UserName, new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            preference.seveUserState(OjectUserName.toString());
                                            preference.saveLoginImageInPreferences(bytes);
                                            startActivity(new Intent(getBaseContext(), RM_MainActivity.class));
                                            finish();
                                        }
                                    });


                                } else {
                                    name.setError("Wrong Name");
                                    password.setError("Wrong Password");
                                    butLogin.setEnabled(true);
                                    dialog.dismiss();//------------------------------------
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                butLogin.setEnabled(true);
                                dialog.dismiss();//------------------------------------
                            }

                        } else {
                            name.setError("Wrong Name");
                            password.setError("Wrong Password");
                            butLogin.setEnabled(true);
                            dialog.dismiss();//------------------------------------
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Toast.makeText(getApplicationContext(), "Please Check Your Network Connection"
                                , Toast.LENGTH_LONG).show();
                        butLogin.setEnabled(true);
                        dialog.dismiss();//------------------------------------
                    }
                });
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

    public boolean validate(EditText name, EditText password) {
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

        return valid;
    }


    public void forGetPass(View view) {
        if (!NetworkAvailable.isNetworkAvailable(this)) {
            Toast.makeText(getApplicationContext(), "Please Connect Your Device With Internet", Toast.LENGTH_LONG).show();
            return;
        }
        final dialog.DialogRestPass dialogRestPass = new dialog.DialogRestPass().getDialogRestPass();

        dialogRestPass.setclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //      Toast.makeText(getApplicationContext(),dialogRestPass.option.getText(),Toast.LENGTH_LONG).show();
                final String user = dialogRestPass.option.getText().toString();


                new Firebase("https://yourfirebase/user")
                        .addValueEventListener(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(final com.firebase.client.DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(user).exists()) {
                                    dialogRestPass.option.setHint("Please Key");
                                    dialogRestPass.option.setText("");
                                    dialogRestPass.buttonO.setText("Step2");
                                    dialogRestPass.setclickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            RM_User chilvalue = dataSnapshot.child(user).getValue(RM_User.class);
                                            if (chilvalue.getKey().equals(dialogRestPass.option.getText().toString())) {
                                                /*** To keep it Login Until press Logout**/
                                                dialogRestPass.option.setEnabled(false);
                                                dialogRestPass.option.setHint("Password Is");
                                                dialogRestPass.option.setText(chilvalue.getPassword());
                                                dialogRestPass.buttonO.setText("Finsh");
                                                dialogRestPass.setclickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        dialogRestPass.getDialog().cancel();
                                                    }
                                                });

                                            } else {
                                                dialogRestPass.option.setError("Wrong Key");
                                            }
                                        }
                                    });

                                } else {
                                    dialogRestPass.option.setError("This email is not found");
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
            }
        });
        dialogRestPass.show(getFragmentManager(), "");
    }

}
