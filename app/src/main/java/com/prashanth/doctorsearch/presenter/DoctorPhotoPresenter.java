package com.prashanth.doctorsearch.presenter;

import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.Doctor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class DoctorPhotoPresenter implements APIContract.DoctorPhotoPresenter {

    private DoctorSearchAPI api;

    private APIContract.DoctorPhotoView photoView;

    private List<Doctor> doctors;

    private HashMap<String, InputStream> photoStream;

    private CompositeDisposable compositeDisposable;

    public DoctorPhotoPresenter(DoctorSearchAPI api, APIContract.DoctorPhotoView doctorPhotoView) {
        this.api = api;
        this.photoView = doctorPhotoView;
        this.photoStream = new HashMap<>();
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void fetchData(List<Doctor> doctors) {
        this.doctors = doctors;
        Timber.d("Size %s", doctors.size());
        for (Doctor doctor : doctors) {
            Disposable disposable = api.getProfilePicture(doctor.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<Response<ResponseBody>>() {
                        @Override
                        public void onNext(Response<ResponseBody> response) {
                            Timber.d("Response code %s", response.code());
                            if (response.body() != null && response.body().byteStream() != null) {
                                photoStream.put(doctor.getId(), response.body().byteStream());
                            } else {
                                photoStream.put(doctor.getId(), null);
                            }
                            if(doctors.size() == photoStream.size()) {
                                photoView.onDataRetrievedSuccessfully(photoStream);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            photoView.callFailed(e);
                        }

                        @Override
                        public void onComplete() {
                            Timber.d("COmplete");
                            photoView.callComplete();
                        }
                    });
            compositeDisposable.add(disposable);
        }
        Timber.d("Send photstream back");
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
