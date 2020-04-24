package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    enum State {
        LOGIN, SIGNUP;
    }

    private State currentState;
    private String as;

    private Button btnSignUpLogIn, btnOneTimeLogIn;
    private EditText edtUsername, edtPassword, edtOneTimeDriverPass;
    private RadioButton radioDriver, radioPassenger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("as").toString().toLowerCase()
                    .equals("passenger")) {
                Intent intent = new Intent(MainActivity.this, PassengerMap.class);
                startActivity(intent);
                finish();
            }
            else if (ParseUser.getCurrentUser().get("as").toString().toLowerCase()
                    .equals("driver")) {
                Intent intent = new Intent(MainActivity.this, DriversActivity.class);
                startActivity(intent);
                finish();
            }
        }


        btnSignUpLogIn= findViewById(R.id.btnSignUp);
        btnSignUpLogIn.setOnClickListener(this);
        edtUsername = findViewById(R.id.edtSignUpUserName);
        edtPassword = findViewById(R.id.edtSignUpPass);
        edtOneTimeDriverPass = findViewById(R.id.edtSingleLogInDrivePass);
        btnOneTimeLogIn = findViewById(R.id.btnOneTimeLogIn);
        btnOneTimeLogIn.setOnClickListener(this);

        radioDriver = findViewById(R.id.radioDriver);
        radioPassenger = findViewById(R.id.radioPass);

        currentState = State.SIGNUP;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnSignUp:
                if(currentState==State.SIGNUP){
                    Toast.makeText(this, "SignUp", Toast.LENGTH_SHORT).show();



                    if(as==null){
                        Toast.makeText(this, "Please choose from Driver or Passenger", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        final ParseUser newUser = new ParseUser();
                        newUser.setUsername(edtUsername.getText().toString());
                        newUser.setPassword(edtPassword.getText().toString());
                        newUser.put("as",as);

                        newUser.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(MainActivity.this, "Signed Up!", Toast.LENGTH_SHORT).show();
                                    if(as.toLowerCase().equals("passenger")==true){
                                        Intent intent = new Intent(MainActivity.this,PassengerMap.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else if(as.toLowerCase().equals("driver")==true){
                                        Intent intent = new Intent(MainActivity.this,DriversActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }


                }

                else if(currentState ==State.LOGIN){
                    Toast.makeText(this, "LogIn", Toast.LENGTH_SHORT).show();

                    ParseUser.logInInBackground(
                            edtUsername.getText().toString(), edtPassword.getText().toString(),
                            new LogInCallback() {
                                @Override
                                public void done(ParseUser user, ParseException e) {

                                    if(user!=null && e==null){
                                     if(user.get("as").toString().toLowerCase().equals(as)==true){
                                         Toast.makeText(MainActivity.this, "Logged In!", Toast.LENGTH_SHORT).show();
                                         if(as.toLowerCase().equals("passenger")==true){
                                             Intent intent = new Intent(MainActivity.this,PassengerMap.class);
                                             startActivity(intent);
                                             finish();
                                         }
                                         else if(as.toLowerCase().equals("driver")){
                                             Intent intent = new Intent(MainActivity.this,DriversActivity.class);
                                             startActivity(intent);
                                             finish();
                                         }

                                         else{
                                             Toast.makeText(MainActivity.this, "Loookkk", Toast.LENGTH_SHORT).show();
                                         }

                                     }
                                     else{
                                         Toast.makeText(MainActivity.this, "Selected correct user type", Toast.LENGTH_SHORT).show();
                                         user.logOut();
                                     }
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    );
                }
                break;

            case R.id.btnOneTimeLogIn:

                if(edtOneTimeDriverPass.getText().toString().toLowerCase()
                .equals("driver") ||
                edtOneTimeDriverPass.getText().toString().toLowerCase()
                .equals("passenger")){

                    if(ParseUser.getCurrentUser() == null){
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(user!=null && e==null){
                                    user.put("as", edtOneTimeDriverPass
                                            .getText().toString());
                                    if(as.toLowerCase().equals("passenger")==true){
                                        Intent intent = new Intent(MainActivity.this,PassengerMap.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                   else if(as.toLowerCase().equals("driver")==true){
                                        Intent intent = new Intent(MainActivity.this,DriversActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                }

                                else{
                                    Toast.makeText(MainActivity.this, ""
                                            +e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    else{
                        Toast.makeText(this, "Some user already logged in!", Toast.LENGTH_SHORT).show();
                    }
                }

                else if (edtOneTimeDriverPass.getText().toString().equals("")!=true){
                    Toast.makeText(this, "Incorrectly spelt !", Toast.LENGTH_SHORT).show();}

                else{
                    Toast.makeText(this, "Are you a driver or a passenger?", Toast.LENGTH_SHORT).show();
                }
                break;
        }



        }



    public void radioButtonClicked(View v){

        switch (v.getId()){
            case R.id.radioDriver:
                as = "driver";
                break;

            case R.id.radioPass:
                as = "passenger";

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_login_menu,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.logInItem:
                if(currentState == State.LOGIN){

                    item.setTitle("LOG IN");
                    btnSignUpLogIn.setText("SIGN UP");
                    currentState = State.SIGNUP;
                    
                }

                else if(currentState == State.SIGNUP){
                    item.setTitle("SIGN UP");
                    btnSignUpLogIn.setText("LOG IN");
                    currentState = State.LOGIN;
                }
        }

        return super.onOptionsItemSelected(item);
    }
}
