package com.prashanth.doctorsearch.presenter;

import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DoctorSearchPresenter implements APIContract.DoctorNameSearchPresenter {

    private String doctorName;

    private String latitude;

    private String longitude;

    private String lastkey;

    private DoctorSearchAPI doctorSearchAPI;

    private APIContract.DoctorSearchView doctorSearchView;

    private CompositeDisposable compositeDisposable;

    public DoctorSearchPresenter(DoctorSearchAPI doctorSearchAPI, APIContract.DoctorSearchView doctorSearchView) {
        this.doctorSearchAPI = doctorSearchAPI;
        this.doctorSearchView = doctorSearchView;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void fetchData(String doctorName, String latitude, String longitude, String lastKey) {
        this.doctorName = doctorName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastkey = lastKey;
        doctorSearchView.callStarted();
        Disposable disposable = doctorSearchAPI.getDoctors(doctorName,
                String.valueOf(latitude),
                String.valueOf(longitude), lastKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DoctorSearchResponse>() {
                    @Override
                    public void onNext(DoctorSearchResponse doctorSearchResponse) {
                        doctorSearchView.onDataRetrievedSuccessfully(doctorSearchResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        doctorSearchView.callFailed(e);
                    }

                    @Override
                    public void onComplete() {
                        doctorSearchView.callComplete();
                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void subscribe() {
        fetchData(doctorName, latitude, longitude, lastkey);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    @Override
    public void onDestroy() {
        this.doctorSearchView = null;
    }
}