package com.prashanth.doctorsearch.contract;

import com.prashanth.doctorsearch.network.model.Doctor;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface APIContract {

    interface View {

        void callStarted();

        void callComplete();

        void callFailed(Throwable throwable);
    }

    interface LoginView extends View {

        void onDataRetrievedSuccessfully(LoginResponse loginResponse);

    }

    interface DoctorSearchView extends View {

        void onDataRetrievedSuccessfully(DoctorSearchResponse doctorSearchResponse);

    }

    interface DoctorPhotoView extends View {

        void onDataRetrievedSuccessfully(HashMap<String, InputStream> response);
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

        void fetchData(List<Doctor> doctorId);
    }

}
