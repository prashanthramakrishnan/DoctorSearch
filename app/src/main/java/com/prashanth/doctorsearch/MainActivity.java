package com.prashanth.doctorsearch;

import android.Manifest;
import android.content.Intent;
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
import com.prashanth.doctorsearch.adapter.DoctorSearchRecyclerViewAdapter;
import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.Doctor;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.presenter.DoctorPhotoPresenter;
import com.prashanth.doctorsearch.presenter.DoctorSearchPresenter;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import com.prashanth.doctorsearch.ui.LoginActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import retrofit2.HttpException;
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

    Set<Doctor> doctors = new HashSet<>();

    HashMap<String, InputStream> photoStream = new HashMap<>();

    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DoctorSearchApplication.component.inject(this);
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
        adapter = new DoctorSearchRecyclerViewAdapter(MainActivity.this, doctors, photoStream);
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
                        doctorSearchAPICall(searchEditText.getText().toString(), loginSharedPreferences.getLastKey());
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
    private void doctorSearchAPICall(String queryName, String lastKey) {

        DoctorSearchPresenter doctorSearchPresenter = new DoctorSearchPresenter(doctorSearchAPI, new APIContract.DoctorSearchView() {
            @Override
            public void onDataRetrievedSuccessfully(DoctorSearchResponse doctorSearchResponse) {
                Timber.d("Search response %s", doctorSearchResponse.getDoctors().size());

                if (!doctorSearchResponse.getDoctors().isEmpty()) {
                    //query for the photos
                    doctors.addAll(doctorSearchResponse.getDoctors());
                    DoctorPhotoPresenter doctorPhotoPresenter = new DoctorPhotoPresenter(doctorSearchAPI, new APIContract.DoctorPhotoView() {
                        @Override
                        public void onDataRetrievedSuccessfully(HashMap<String, InputStream> response) {
                            photoStream.putAll(response);
                            if (lastKey == null) {
                                adapter.update(doctors, photoStream);
                            } else {
                                updateRecycleView(doctors, photoStream);
                            }
                            loginSharedPreferences.setLastKey(doctorSearchResponse.getLastKey());
                            Timber.d("Last key %s", loginSharedPreferences.getLastKey());

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
                        public void callFailed(Throwable throwable) {
                            if (throwable instanceof HttpException) {
                                int code = ((HttpException) throwable).response().code();
                                if (code == 404) {
                                    Timber.d("Photo doesn't exist");
                                }
                                if (code == 401) {
                                    Timber.d("Logging out because of some other error");
                                    loginSharedPreferences.clear();
                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            } else {
                                Timber.e(throwable, "Handle this!");
                            }
                        }
                    });
                    doctorPhotoPresenter.fetchData(doctorSearchResponse.getDoctors());
                }
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
            public void callFailed(Throwable throwable) {
                Timber.e(throwable, "Exception");
                if (throwable instanceof HttpException) {
                    loginSharedPreferences.clear();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, R.string.internet_not_available, Toast.LENGTH_SHORT).show();
                }

            }
        });
        doctorSearchPresenter.fetchData(queryName, String.valueOf(latitude), String.valueOf(longitude), lastKey);
    }

    private void updateRecycleView(Set<Doctor> doctorArrayList, HashMap<String, InputStream> photoStream) {
        this.doctors.addAll(doctorArrayList);
        this.photoStream.putAll(photoStream);
        adapter.update(doctors, this.photoStream);
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
                            if (length > 0 && length >= 4) {
                                Timber.d("String second %s", queryString.toString());
                                doctorSearchAPICall(queryString.toString(), null);
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
}