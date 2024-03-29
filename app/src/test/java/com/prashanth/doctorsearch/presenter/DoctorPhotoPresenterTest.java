package com.prashanth.doctorsearch.presenter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import java.util.Map;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import retrofit2.Response;

public class DoctorPhotoPresenterTest {

    @Mock
    private APIContract.DoctorPhotoView view;

    private static final String DUMMY_ID = "dummy";

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
        DoctorPhotoPresenter presenter = new DoctorPhotoPresenter(provideDoctorSearchAPI(true), view);
        presenter.fetchData(DUMMY_ID);
        Mockito.verify(view, times(1)).callStarted();
        Mockito.verify(view, times(1)).onDataRetrievedSuccessfully(any());
        Mockito.verify(view, times(1)).callComplete();
    }

    @Test
    public void getDataAndLoadViewFailTest() {
        DoctorPhotoPresenter presenter = new DoctorPhotoPresenter(provideDoctorSearchAPI(false), view);
        presenter.fetchData(DUMMY_ID);
        Mockito.verify(view, times(1)).callFailed(any(Throwable.class), any(Integer.class));
    }

    @Test
    public void subscribeTest() {
        DoctorPhotoPresenter presenter = new DoctorPhotoPresenter(provideDoctorSearchAPI(true), view);
        presenter.subscribe();
        Mockito.verify(view, times(1)).callStarted();
        Mockito.verify(view, times(1)).onDataRetrievedSuccessfully(any());
        Mockito.verify(view, times(1)).callComplete();
    }

    @Test
    public void unsubscribeTest() {
        DoctorPhotoPresenter presenter = new DoctorPhotoPresenter(provideDoctorSearchAPI(true), view);
        presenter.unsubscribe();
        Mockito.verifyZeroInteractions(view);
    }

    @Test
    public void onDestroyTest() {
        DoctorPhotoPresenter presenter = new DoctorPhotoPresenter(provideDoctorSearchAPI(true), view);
        presenter.onDestroy();
        Mockito.verifyZeroInteractions(view);
    }

    private DoctorSearchAPI provideDoctorSearchAPI(boolean success) {
        if (success) {
            return new MockDoctorPhotoSuccess();
        }
        return new MockDoctorPhotoFailure();
    }

    private class MockDoctorPhotoSuccess implements DoctorSearchAPI {

        @Override
        public Observable<LoginResponse> login(Map<String, String> doctorSearchLoginModel) {
            return null;
        }

        @Override
        public Observable<DoctorSearchResponse> getDoctors(String doctorName, String latitude, String longitude, String lastKey) {
            return null;
        }

        @Override
        public Observable<Response<ResponseBody>> getProfilePicture(String doctorId) {
            Response<ResponseBody> response = mock(Response.class);
            return Observable.just(response);
        }
    }

    private class MockDoctorPhotoFailure implements DoctorSearchAPI {

        @Override
        public Observable<LoginResponse> login(Map<String, String> doctorSearchLoginModel) {
            return null;
        }

        @Override
        public Observable<DoctorSearchResponse> getDoctors(String doctorName, String latitude, String longitude, String lastKey) {
            return null;
        }

        @Override
        public Observable<Response<ResponseBody>> getProfilePicture(String doctorId) {
            return Observable.error(Throwable::new);
        }
    }

}