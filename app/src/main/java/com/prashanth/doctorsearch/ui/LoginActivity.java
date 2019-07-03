package com.prashanth.doctorsearch.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.prashanth.doctorsearch.Constants;
import com.prashanth.doctorsearch.DoctorSearchApplication;
import com.prashanth.doctorsearch.MainActivity;
import com.prashanth.doctorsearch.R;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username)
    EditText usernameField;

    @BindView(R.id.password)
    EditText passwordField;

    @Inject
    LoginSharedPreferences loginSharedPreferences;

    @Inject
    @Named(NetworkDaggerModule.LOGIN)
    DoctorSearchAPI loginApi;

    private ProgressDialog progressDialog;

    private RxPermissions rxPermissions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DoctorSearchApplication.component.inject(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        rxPermissions = new RxPermissions(this);

        progressDialog = new ProgressDialog(this);

        usernameField.setText(Constants.USERNAME_LOGIN);
        passwordField.setText(Constants.PASSWORD_LOGIN);
    }

    @OnClick(R.id.login_button)
    void onButtonClicked() {
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            performLoginAPICall();
        } else {
            Toast.makeText(LoginActivity.this, R.string.location_permission_missing, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("CheckResult")
    private void performLoginAPICall() {

        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(getString(R.string.logging_in));
            progressDialog.show();
            progressDialog.setCancelable(false);

            final Map<String, String> fields = new HashMap<>();
            fields.put(Constants.USERNAME_KEY, usernameField.getText().toString());
            fields.put(Constants.PASSWORD_KEY, passwordField.getText().toString());
            fields.put(Constants.GRANT_TYPE_KEY, Constants.GRANT_TYPE_VALUE);

            loginApi.login(fields)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<LoginResponse>() {
                        @Override
                        public void onNext(LoginResponse loginResponse) {
                            if (loginResponse != null) {
                                Timber.d("Access token set");
                                loginSharedPreferences.setAccessToken(loginResponse.getAccess_token());
                                startMainActivity();
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e, "Error logging in");
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, R.string.error_logging_in, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }
                    });
        }

    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onResume() {
        super.onResume();
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        if (loginSharedPreferences.getAccessToken() != null) {
                            startMainActivity();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.location_permission_missing, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
