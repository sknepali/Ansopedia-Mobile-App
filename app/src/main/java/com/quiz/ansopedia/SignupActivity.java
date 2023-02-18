package com.quiz.ansopedia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.quiz.ansopedia.Utility.Constants;
import com.quiz.ansopedia.Utility.Utility;
import com.quiz.ansopedia.models.LoginModel;
import com.quiz.ansopedia.models.LoginRequestModel;
import com.quiz.ansopedia.retrofit.ContentApiImplementer;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    TextView signInTextBtn;
    MaterialButton signUpBtn;
    TextInputEditText userName, userEmail, password, confirmPassword;
    TextInputLayout userNameTextField, t3, passwordTextField, confirmPasswordTextField;
    String uName, uEmail, pass, confirmPass;
    SharedPreferences preferences;
    ScrollView svMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initView();
        preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        signInTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, SignInActivity.class));
                finish();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uName = userName.getText().toString().trim();
                uEmail = userEmail.getText().toString().trim();
                pass = password.getText().toString();
                confirmPass = confirmPassword.getText().toString();
                Utility.hideSoftKeyboard(SignupActivity.this);
                userNameTextField.setErrorEnabled(false);
                t3.setErrorEnabled(false);
                passwordTextField.setErrorEnabled(false);
                confirmPasswordTextField.setErrorEnabled(false);
                if (isValidateCredentials()) {
                    getRegister();
                } else {
                    if (uName.isEmpty()) {
                        userNameTextField.setErrorEnabled(true);
                        userNameTextField.setError("Please Enter Name");
                    } else if (uEmail.isEmpty()){
                        t3.setErrorEnabled(true);
                        t3.setError("Please Enter Email");
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(uEmail).matches()){
                        t3.setErrorEnabled(true);
                        t3.setError("Please Enter Valid Email");
                    }else if (pass.isEmpty()){
                        passwordTextField.setErrorEnabled(true);
                        passwordTextField.setError("Please Enter Password");
                    }else if(!pass.matches( ".{8,}")){
                        passwordTextField.setErrorEnabled(true);
                        passwordTextField.setError("Password must be of 8 digit");
                    }else if(!pass.matches(confirmPass)){
                        confirmPasswordTextField.setErrorEnabled(true);
                        confirmPasswordTextField.setError("Password must be same");
                    };
                }
            }
        });
        svMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Utility.hideSoftKeyboard(SignupActivity.this);
                return true;
            }
        });
    }

    private void initView() {
        signInTextBtn = findViewById(R.id.signInTextBtn);
        signUpBtn = findViewById(R.id.signUpBtn);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        userNameTextField = findViewById(R.id.userNameTextField);
        t3 = findViewById(R.id.t3);
        passwordTextField = findViewById(R.id.passwordTextField);
        confirmPasswordTextField = findViewById(R.id.confirmPasswordTextField);
        svMain = findViewById(R.id.svMain);
    }

    private void getRegister() {
        Utility.showProgress(this);
        LoginRequestModel loginRequestModel = new LoginRequestModel();
        loginRequestModel.setName(uName);
        loginRequestModel.setEmail(uEmail);
        loginRequestModel.setPassword(pass);
        loginRequestModel.setPassword_confirmation(confirmPass);
        loginRequestModel.setTc(true);
        if (Utility.isNetConnected(this)) {
            try {
                ContentApiImplementer.getRegister(loginRequestModel, new Callback<List<LoginModel>>() {
                    @Override
                    public void onResponse(Call<List<LoginModel>> call, Response<List<LoginModel>> response) {
                        Utility.dismissProgress(SignupActivity.this);
                        if (response.code() == 201) {
                            LoginModel loginModel = response.body().get(0);
                            if (loginModel.getStatus().equalsIgnoreCase("success")) {
                                Constants.TOKEN = loginModel.getToken();
                                preferences.edit().putString(Constants.token, loginModel.getToken()).apply();
                                preferences.edit().putString(Constants.username, uEmail).apply();
                                preferences.edit().putString(Constants.password, confirmPass).apply();
                                preferences.edit().putBoolean(Constants.isLogin, true).apply();
                                Toast.makeText(SignupActivity.this, "" + loginModel.getMessage(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Utility.showAlertDialog(SignupActivity.this, loginModel.getStatus(), loginModel.getMessage());
                            }
                        } else if (response.code() == 403) {
                            Utility.showAlertDialog(SignupActivity.this, "Failed", "Email already exists!!");
                        } else {
                            Utility.showAlertDialog(SignupActivity.this, "Error", "Something went wrong, Please Try Again");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LoginModel>> call, Throwable t) {
                        Utility.dismissProgress(SignupActivity.this);
                        Utility.showAlertDialog(SignupActivity.this, "Error", "Something went wrong, Please Try Again");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Utility.dismissProgress(this);
            }
        } else {
            Utility.dismissProgress(this);
            Utility.showAlertDialog(this, "Error", "Please connect to Internet");
        }
    }
    private boolean isValidateCredentials() {
        uName = userName.getText().toString().trim();
        uEmail = userEmail.getText().toString().trim();
        pass = password.getText().toString();
        confirmPass = confirmPassword.getText().toString();
        if (!uName.isEmpty() && !uEmail.isEmpty() && !pass.isEmpty() && !confirmPass.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(uEmail).matches() && pass.matches( ".{8,}")) {
            return true;
        } else {
            return false;
        }
    }
}