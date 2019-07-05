package com.prashanth.doctorsearch.contract;

import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Response;

public interface APIContract {

    interface View {

        void callStarted();

        void callComplete();

        void callFailed(Throwable throwable, int statusCode);
    }

    interface LoginView extends View {

        void onDataRetrievedSuccessfully(LoginResponse loginResponse);

    }

    interface DoctorSearchView extends View {

        void onDataRetrievedSuccessfully(DoctorSearchResponse doctorSearchResponse);

        void noDoctorsFound();
    }

    interface DoctorPhotoView extends View {

        void onDataRetrievedSuccessfully(Response<ResponseBody> response);
    }

    interface Presenter {

        void subscribe();

        void unsubscribe();

        void onDestroy();
    }

    interface LoginPresenter extends Presenter {

        void fetchData(Map<String, String> fields);
    }

    interface DoctorNameSearchPresenter extends Presenter {

        void fetchData(String location, String latitude, String longitude, String lastKey);
    }

    interface DoctorPhotoPresenter extends Presenter {

        void fetchData(String doctorId);
    }

}
