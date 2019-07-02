package com.prashanth.doctorsearch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.jakewharton.rxbinding3.widget.TextViewTextChangeEvent;
import com.prashanth.doctorsearch.adapter.DoctorSearchRecyclerViewAdapter;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.Doctor;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.network.networkwrapper.DoctorSearchRetrofitWrapper;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import com.prashanth.doctorsearch.ui.LoginActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements EditText.OnEditorActionListener {

    @BindView(R.id.doctor_search_vew)
    EditText searchEditText;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private LoginSharedPreferences loginSharedPreferences;

    private DoctorSearchAPI doctorSearchAPI;

    private Disposable disposable;

    private boolean loading = true;

    private DoctorSearchRecyclerViewAdapter adapter;

    ArrayList<Doctor> doctors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //use dagger
        loginSharedPreferences = new LoginSharedPreferences(this);
        Retrofit retrofitForDoctorSearch = DoctorSearchRetrofitWrapper.retrofitClient(Constants.LOGGED_IN_URL);
        doctorSearchAPI = retrofitForDoctorSearch.create(DoctorSearchAPI.class);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        adapter = new DoctorSearchRecyclerViewAdapter(MainActivity.this, doctors);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
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

    @SuppressLint("CheckResult")
    private void doctorSearchAPICall(String queryName, String lastKey) {
        doctorSearchAPI.getDoctors(queryName,
                "52.534709",
                "13.3976972",
                lastKey,
                Constants.CONTENT_TYPE_ACCEPT_VALUE,
                "Bearer " + loginSharedPreferences.getAccessToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DoctorSearchResponse>() {
                    @Override
                    public void onNext(DoctorSearchResponse doctorSearchResponse) {
                        Timber.d("Search response %s", doctorSearchResponse.getDoctors().size());
                        if (lastKey == null) {
                            if (!doctorSearchResponse.getDoctors().isEmpty()) {
                                doctors = doctorSearchResponse.getDoctors();
                                adapter.update(doctors);
                            }
                        }
                        loginSharedPreferences.setLastKey(doctorSearchResponse.getLastKey());

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Exception");
                        if (e instanceof HttpException) {
                            loginSharedPreferences.clear();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onComplete() {
                        //ignore
                    }
                });
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
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(TextViewTextChangeEvent::getText)
                .map(charSequence -> charSequence)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        queryString -> {
                            final int length = queryString.length();
                            if (length > 0 && length >= 4) {
                                Timber.d("String second %s", queryString.toString());
                                doctorSearchAPICall(queryString.toString(), null);
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
