package com.prashanth.doctorsearch.presenter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import com.prashanth.doctorsearch.Utils;
import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import java.util.HashMap;
import java.util.Map;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import retrofit2.Response;

public class LoginPresenterTest {

    @Mock
    private APIContract.LoginView view;

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
        LoginAPIPresenter presenter = new LoginAPIPresenter(provideLoginAPI(true), view);
        presenter.fetchData(initLoginModel());
        Mockito.verify(view, times(1)).callStarted();
        Mockito.verify(view, times(1)).onDataRetrievedSuccessfully(any());
        Mockito.verify(view, times(1)).callComplete();
    }

    @Test
    public void getDataAndLoadViewFailTest() {
        LoginAPIPresenter presenter = new LoginAPIPresenter(provideLoginAPI(false), view);
        presenter.fetchData(initLoginModel());
        Mockito.verify(view, times(1)).callFailed(any(Throwable.class), any(Integer.class));
    }

    @Test
    public void subscribeTest() {
        LoginAPIPresenter presenter = new LoginAPIPresenter(provideLoginAPI(true), view);
        presenter.subscribe();
        Mockito.verify(view, times(1)).callStarted();
        Mockito.verify(view, times(1)).onDataRetrievedSuccessfully(any());
        Mockito.verify(view, times(1)).callComplete();
    }

    @Test
    public void unsubscribeTest() {
        LoginAPIPresenter presenter = new LoginAPIPresenter(provideLoginAPI(true), view);
        presenter.unsubscribe();
        Mockito.verifyZeroInteractions(view);
    }

    @Test
    public void onDestroyTest() {
        LoginAPIPresenter presenter = new LoginAPIPresenter(provideLoginAPI(true), view);
        presenter.onDestroy();
        Mockito.verifyZeroInteractions(view);
    }

    private Map<String, String> initLoginModel() {
        final Map<String, String> fields = new HashMap<>();
        fields.put(Utils.USERNAME_KEY, Utils.USERNAME_LOGIN);
        fields.put(Utils.PASSWORD_KEY, Utils.PASSWORD_LOGIN);
        fields.put(Utils.GRANT_TYPE_KEY, Utils.GRANT_TYPE_VALUE);
        return fields;
    }

    private DoctorSearchAPI provideLoginAPI(boolean success) {
        if (success) {
            return new MockLoginSucess();
        }
        return new MockLoginFailure();
    }

    private class MockLoginSucess implements DoctorSearchAPI {

        @Override
        public Observable<LoginResponse> login(Map<String, String> doctorSearchLoginModel) {
            return Observable.just(new LoginResponse());
        }

        @Override
        public Observable<DoctorSearchResponse> getDoctors(String doctorName, String latitude, String longitude, String lastKey) {
            return null;
        }

        @Override
        public Observable<Response<ResponseBody>> getProfilePicture(String doctorId) {
            return null;
        }
    }

    private class MockLoginFailure implements DoctorSearchAPI {

        @Override
        public Observable<LoginResponse> login(Map<String, String> doctorSearchLoginModel) {
            return Observable.error(Throwable::new);
        }

        @Override
        public Observable<DoctorSearchResponse> getDoctors(String doctorName, String latitude, String longitude, String lastKey) {
            return null;
        }

        @Override
        public Observable<Response<ResponseBody>> getProfilePicture(String doctorId) {
            return null;
        }
    }

}