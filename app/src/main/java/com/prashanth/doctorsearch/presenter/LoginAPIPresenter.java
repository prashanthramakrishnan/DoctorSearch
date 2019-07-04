package com.prashanth.doctorsearch.presenter;

import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.Map;

public class LoginAPIPresenter implements APIContract.LoginPresenter {

    private DoctorSearchAPI doctorSearchAPI;

    private CompositeDisposable compositeDisposable;

    private APIContract.LoginView view;

    private Map<String, String> fields;

    public LoginAPIPresenter(DoctorSearchAPI doctorSearchAPI, APIContract.LoginView view) {
        this.view = view;
        this.doctorSearchAPI = doctorSearchAPI;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void fetchData(Map<String, String> fields) {
        this.fields = fields;
        view.callStarted();
        compositeDisposable.clear();

        Disposable disposable = doctorSearchAPI.login(fields)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<LoginResponse>() {
                    @Override
                    public void onNext(LoginResponse loginResponse) {
                        view.onDataRetrievedSuccessfully(loginResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.callFailed(e);
                    }

                    @Override
                    public void onComplete() {
                        view.callComplete();
                    }
                });
        compositeDisposable.add(disposable);

    }

    @Override
    public void subscribe() {
        fetchData(fields);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    @Override
    public void onDestroy() {
        this.view = null;
    }
}