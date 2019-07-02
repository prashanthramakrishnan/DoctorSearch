package com.prashanth.doctorsearch.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.prashanth.doctorsearch.Constants;
import com.prashanth.doctorsearch.MainActivity;
import com.prashanth.doctorsearch.R;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import com.prashanth.doctorsearch.network.networkwrapper.DoctorSearchRetrofitWrapper;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username)
    EditText usernameField;

    @BindView(R.id.password)
    EditText passwordField;

    private LoginSharedPreferences loginSharedPreferences;

    private Retrofit retrofit;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //use dagger
        loginSharedPreferences = new LoginSharedPreferences(this);
        retrofit = DoctorSearchRetrofitWrapper.retrofitClient(Constants.LOGIN_URL);

        progressDialog = new ProgressDialog(this);

        usernameField.setText(Constants.USERNAME_LOGIN);
        passwordField.setText(Constants.PASSWORD_LOGIN);
    }

    @OnClick(R.id.login_button)
    void onButtonClicked() {
        performLoginAPICall();
    }

    private void performLoginAPICall() {

        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(getString(R.string.logging_in));
            progressDialog.show();
            progressDialog.setCancelable(false);

            final Map<String, String> fields = new HashMap<>();
            fields.put(Constants.USERNAME_KEY, usernameField.getText().toString());
            fields.put(Constants.PASSWORD_KEY, passwordField.getText().toString());
            fields.put(Constants.GRANT_TYPE_KEY, Constants.GRANT_TYPE_VALUE);

            final DoctorSearchAPI loginApi = retrofit.create(DoctorSearchAPI.class);
            Call<LoginResponse> responseCall = loginApi.login(Constants.CONTENT_TYPE,
                    Constants.CONTENT_TYPE_ACCEPT_VALUE,
                    DoctorSearchAPI.AUTHORIZATION, fields);

            responseCall.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.body() != null) {
                        Timber.d("Access token set %s", response.body().getAccess_token());
                        loginSharedPreferences.setAccessToken(response.body().getAccess_token());
                        startMainActivity();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    progressDialog.setMessage(getString(R.string.error_logging_in));
                }
            });

        }

    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loginSharedPreferences.getAccessToken() != null) {
            startMainActivity();
        }
    }
}
