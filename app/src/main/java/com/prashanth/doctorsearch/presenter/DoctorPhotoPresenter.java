package com.prashanth.doctorsearch.presenter;

import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class DoctorPhotoPresenter implements APIContract.DoctorPhotoPresenter {

    private DoctorSearchAPI api;

    private APIContract.DoctorPhotoView photoView;

    private String doctorId;

    private CompositeDisposable compositeDisposable;

    public DoctorPhotoPresenter(DoctorSearchAPI api, APIContract.DoctorPhotoView doctorPhotoView) {
        this.api = api;
        this.photoView = doctorPhotoView;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void fetchData(String doctorId) {
        this.doctorId = doctorId;
        Disposable disposable = api.getProfilePicture(doctorId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<ResponseBody>>() {
                    @Override
                    public void onNext(Response<ResponseBody> response) {
                        photoView.onDataRetrievedSuccessfully(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        photoView.callFailed(e);
                    }

                    @Override
                    public void onComplete() {
                        photoView.callComplete();
                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    @Override
    public void onDestroy() {
        this.photoView = null;
    }

}
