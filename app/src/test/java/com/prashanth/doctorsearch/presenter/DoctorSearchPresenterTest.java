package com.prashanth.doctorsearch.presenter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.Doctor;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Map;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import retrofit2.Response;

public class DoctorSearchPresenterTest {

    @Mock
    private APIContract.DoctorSearchView view;

    private String DUMMY_NAME = "dummy";

    private String DUMMY_LATITUDE = "dummy";

    private String DUMMY_LONGITUDE = "dummy";

    private String DUMMY_LAST_KEY = "dummy";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setupClass() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerCallable -> Schedulers.trampoline());
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Test
    public void getDataAndLoadViewTest() {
        DoctorSearchPresenter presenter = new DoctorSearchPresenter(provideDoctorSearchAPI(true), view);
        presenter.fetchData(DUMMY_NAME, DUMMY_LATITUDE, DUMMY_LONGITUDE, DUMMY_LAST_KEY);
        Mockito.verify(view, times(1)).callStarted();
        Mockito.verify(view, times(1)).onDataRetrievedSuccessfully(any());
        Mockito.verify(view, times(1)).callComplete();
    }

    @Test
    public void getDataAndLoadViewNoDoctorTest() {
        DoctorSearchPresenter presenter = new DoctorSearchPresenter(new MockDoctorSearchSuccessDoctorListEmpty(), view);
        presenter.fetchData(DUMMY_NAME, DUMMY_LATITUDE, DUMMY_LONGITUDE, DUMMY_LAST_KEY);
        Mockito.verify(view, times(1)).callStarted();
        Mockito.verify(view, times(1)).noDoctorsFound();
        Mockito.verify(view, times(1)).callComplete();
    }

    @Test
    public void getDataAndLoadViewFailTest() {
        DoctorSearchPresenter presenter = new DoctorSearchPresenter(provideDoctorSearchAPI(false), view);
        presenter.fetchData(DUMMY_NAME, DUMMY_LATITUDE, DUMMY_LONGITUDE, DUMMY_LAST_KEY);
        Mockito.verify(view, times(1)).callFailed(any(Throwable.class),any(Integer.class));
    }

    @Test
    public void subscribeTest() {
        DoctorSearchPresenter presenter = new DoctorSearchPresenter(provideDoctorSearchAPI(true), view);
        presenter.subscribe();
        Mockito.verify(view, times(1)).callStarted();
        Mockito.verify(view, times(1)).onDataRetrievedSuccessfully(any());
        Mockito.verify(view, times(1)).callComplete();
    }

    @Test
    public void unsubscribeTest() {
        DoctorSearchPresenter presenter = new DoctorSearchPresenter(provideDoctorSearchAPI(true), view);
        presenter.unsubscribe();
        Mockito.verifyZeroInteractions(view);
    }

    @Test
    public void onDestroyTest() {
        DoctorSearchPresenter presenter = new DoctorSearchPresenter(provideDoctorSearchAPI(true), view);
        presenter.onDestroy();
        Mockito.verifyZeroInteractions(view);
    }

    private DoctorSearchAPI provideDoctorSearchAPI(boolean success) {
        if (success) {
            return new MockDoctorSearchSuccess();
        }
        return new MockDoctorSearchFailure();
    }

    private class MockDoctorSearchSuccess implements DoctorSearchAPI {

        @Override
        public Observable<LoginResponse> login(Map<String, String> doctorSearchLoginModel) {
            return null;
        }

        @Override
        public Observable<DoctorSearchResponse> getDoctors(String doctorName, String latitude, String longitude, String lastKey) {
            DoctorSearchResponse doctorSearchResponse = new DoctorSearchResponse();
            ArrayList<Doctor> doctors = new ArrayList<>();
            Doctor doctor = new Doctor();
            doctor.setName("DUMMY");
            doctor.setAddress("DUMMY");
            doctor.setId("DUMMY");
            doctor.setPhotoId("DUMMY");
            doctors.add(doctor);
            doctorSearchResponse.setDoctors(doctors);
            return Observable.just(doctorSearchResponse);
        }

        @Override
        public Observable<Response<ResponseBody>> getProfilePicture(String doctorId) {
            return null;
        }
    }

    private class MockDoctorSearchSuccessDoctorListEmpty implements DoctorSearchAPI {

        @Override
        public Observable<LoginResponse> login(Map<String, String> doctorSearchLoginModel) {
            return null;
        }

        @Override
        public Observable<DoctorSearchResponse> getDoctors(String doctorName, String latitude, String longitude, String lastKey) {
            return Observable.just(new DoctorSearchResponse());
        }

        @Override
        public Observable<Response<ResponseBody>> getProfilePicture(String doctorId) {
            return null;
        }
    }

    private class MockDoctorSearchFailure implements DoctorSearchAPI {

        @Override
        public Observable<LoginResponse> login(Map<String, String> doctorSearchLoginModel) {
            return null;
        }

        @Override
        public Observable<DoctorSearchResponse> getDoctors(String doctorName, String latitude, String longitude, String lastKey) {
            return Observable.error(Throwable::new);
        }

        @Override
        public Observable<Response<ResponseBody>> getProfilePicture(String doctorId) {
            return null;
        }
    }

}