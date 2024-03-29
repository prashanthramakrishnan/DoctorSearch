package com.prashanth.doctorsearch.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.jakewharton.rxbinding3.widget.TextViewTextChangeEvent;
import com.prashanth.doctorsearch.BuildConfig;
import com.prashanth.doctorsearch.DoctorSearchBaseApplication;
import com.prashanth.doctorsearch.R;
import com.prashanth.doctorsearch.adapter.DoctorSearchRecyclerViewAdapter;
import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.Doctor;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.presenter.DoctorSearchPresenter;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements EditText.OnEditorActionListener {

    @BindView(R.id.doctor_search_vew)
    EditText searchEditText;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Inject
    LoginSharedPreferences loginSharedPreferences;

    @Inject
    @Named(NetworkDaggerModule.AUTHENTICATED)
    DoctorSearchAPI doctorSearchAPI;

    private Disposable disposable;

    private DoctorSearchRecyclerViewAdapter adapter;

    DoctorSearchPresenter doctorSearchPresenter;

    ArrayList<Doctor> doctors = new ArrayList<>();

    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DoctorSearchBaseApplication.component.inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Timber.d("Location %s %s", location.getLatitude(), location.getLongitude());
                        }
                    });
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        adapter = new DoctorSearchRecyclerViewAdapter(MainActivity.this, doctors);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        initScrollListener();
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == doctors.size() - 1) {
                    //bottom of list!
                    if (loginSharedPreferences.getLastKey() != null) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.more_doctors), Toast.LENGTH_LONG).show();
                        searchForDoctors(searchEditText.getText().toString(), loginSharedPreferences.getLastKey());
                    } else {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.end), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        disposable.dispose();
        hideKeyboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachEditTextBindings();
    }

    //    "52.534709",
    //    "13.3976972",
    private void searchForDoctors(String queryName, String lastKey) {
        doctorSearchPresenter = new DoctorSearchPresenter(doctorSearchAPI, new APIContract.DoctorSearchView() {
            @Override
            public void onDataRetrievedSuccessfully(DoctorSearchResponse doctorSearchResponse) {
                if (lastKey == null) {
                    doctors = doctorSearchResponse.getDoctors();
                    adapter.update(doctors);
                } else {
                    updateRecycleView(doctorSearchResponse.getDoctors());
                }
                loginSharedPreferences.setLastKey(doctorSearchResponse.getLastKey());
            }

            @Override
            public void noDoctorsFound() {
                Toast.makeText(MainActivity.this, R.string.no_doctors, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void callStarted() {
                //no op
            }

            @Override
            public void callComplete() {
                //no op
            }

            @Override
            public void callFailed(Throwable throwable, int statusCode) {
                if (statusCode == 401) {
                    loginSharedPreferences.clear();
                    Toast.makeText(MainActivity.this, R.string.logging_out, Toast.LENGTH_SHORT).show();
                    LoginActivity.startActivity(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, R.string.internet_not_available, Toast.LENGTH_SHORT).show();
                }

            }
        });
        doctorSearchPresenter.fetchData(queryName, String.valueOf(latitude), String.valueOf(longitude), lastKey);
    }

    private void updateRecycleView(ArrayList<Doctor> doctorArrayList) {
        doctors.addAll(doctorArrayList);
        adapter.update(doctors);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String searchString = v.getText().toString().trim();
            if (searchString != null && !searchString.isEmpty()) {
                onSearchClicked(searchString);
            }

            return true;
        }
        return false;
    }

    private void onSearchClicked(String searchString) {
        disposable.dispose();
        hideKeyboard();
        searchEditText.setText(searchString);
        attachEditTextBindings();
    }

    private void attachEditTextBindings() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = RxTextView.textChangeEvents(searchEditText)
                .doOnNext(onTextChangeEvent -> onTextChangeEvent.getText().toString())
                .debounce(700, TimeUnit.MILLISECONDS)
                .map(TextViewTextChangeEvent::getText)
                .map(charSequence -> charSequence)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        queryString -> {
                            final int length = queryString.length();
                            if (length > 0 && length >= Integer.valueOf(BuildConfig.MIN_CHARACTER_SEARCH_COUNT)) {
                                Timber.d("String query %s", queryString.toString());
                                searchForDoctors(queryString.toString(), null);
                                hideKeyboard();
                            }
                        }
                );
    }

    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (doctorSearchPresenter != null) {
            doctorSearchPresenter.unsubscribe();
            doctorSearchPresenter.onDestroy();
        }
    }
}