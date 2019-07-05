package com.prashanth.doctorsearch.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.prashanth.doctorsearch.DoctorSearchApplication;
import com.prashanth.doctorsearch.R;
import com.prashanth.doctorsearch.Utils;
import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import com.prashanth.doctorsearch.presenter.LoginAPIPresenter;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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

    private CompositeDisposable compositeDisposable;

    private LoginAPIPresenter loginAPIPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DoctorSearchApplication.component.inject(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        compositeDisposable = new CompositeDisposable();
        rxPermissions = new RxPermissions(this);

        progressDialog = new ProgressDialog(this);

        usernameField.setText(Utils.USERNAME_LOGIN);
        passwordField.setText(Utils.PASSWORD_LOGIN);
    }

    @OnClick(R.id.login_button)
    void onButtonClicked() {
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            performLoginAPICall();
        } else {
            Toast.makeText(LoginActivity.this, R.string.location_permission_missing, Toast.LENGTH_SHORT).show();
        }
    }

    private void performLoginAPICall() {
        final Map<String, String> fields = new HashMap<>();
        fields.put(Utils.USERNAME_KEY, Utils.USERNAME_LOGIN);
        fields.put(Utils.PASSWORD_KEY, Utils.PASSWORD_LOGIN);
        fields.put(Utils.GRANT_TYPE_KEY, Utils.GRANT_TYPE_VALUE);

        loginAPIPresenter = new LoginAPIPresenter(loginApi, new APIContract.LoginView() {
            @Override
            public void callStarted() {
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.setMessage(getString(R.string.logging_in));
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                }
            }

            @Override
            public void callComplete() {
                //do nothing
            }

            @Override
            public void callFailed(Throwable throwable, int statusCode) {
                Timber.e(throwable, "Error logging in");
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, R.string.error_logging_in, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataRetrievedSuccessfully(LoginResponse loginResponse) {
                if (loginResponse != null) {
                    Timber.d("Access token set");
                    loginSharedPreferences.setAccessToken(loginResponse.getAccess_token());
                    startMainActivity();
                }
                progressDialog.dismiss();
            }
        });
        loginAPIPresenter.fetchData(fields);
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Disposable disposable = rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        if (loginSharedPreferences.getAccessToken() != null) {
                            startMainActivity();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.location_permission_missing, Toast.LENGTH_SHORT).show();
                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (loginAPIPresenter != null) {
            loginAPIPresenter.unsubscribe();
            loginAPIPresenter.onDestroy();
        }
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
