package com.prashanth.doctorsearch;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.EditText;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import com.prashanth.doctorsearch.ui.LoginActivity;
import com.prashanth.doctorsearch.ui.MainActivity;
import com.robotium.solo.Solo;
import java.io.IOException;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import timber.log.Timber;

@RunWith(AndroidJUnit4.class)
public class FlowTest {

    private MockWebServer server;

    private Solo solo;

    @Rule
    public ActivityTestRule<LoginActivity> rule = new ActivityTestRule<>(LoginActivity.class, false, false);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void setUp() throws Exception {
        clearPreferences();
        setupServer();
        solo = new Solo(getInstrumentation(), rule.getActivity());
    }

    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        clearPreferences();
        server.shutdown();
    }

    private void clearPreferences() {
        SharedPreferences prefs =
                getInstrumentation().getTargetContext().getSharedPreferences("doctorsearch", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    @Test()
    public void startLoginFlowTest() {

        //Launch app with the MockServer running
        rule.launchActivity(new Intent());

        introduceDelay(2000L);

        //click login button
        solo.clickOnView(solo.getView(R.id.login_button));

        introduceDelay(2000L);

        assertTrue(solo.waitForActivity(MainActivity.class.getSimpleName()));

        introduceDelay(1000L);

        EditText editTextUsername = (EditText) solo.getView(R.id.doctor_search_vew);

        //search for doctors
        solo.enterText(editTextUsername, "Doctor");

        introduceDelay(2000L);

    }

    private void setupServer() throws Exception {

        server = new MockWebServer();
        Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getMethod().equals("POST") && "/oauth/token".equals(request.getPath())) {

                    String loginResponse;
                    try {
                        loginResponse = IOUtils.toString(getInstrumentation().getContext().getResources().getAssets().open("json/login_response.json"));
                        return new MockResponse().setResponseCode(200).setBody(loginResponse);
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }

                if (request.getMethod().equals("GET") && (request.getPath().contains("/api/users/me/"))) {
                    String searchResponse;
                    try {
                        searchResponse = IOUtils.toString(getInstrumentation().getContext().getResources().getAssets().open("json/search_response.json"));
                        return new MockResponse().setResponseCode(200).setBody(searchResponse);
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
                return null;
            }
        };

        server.setDispatcher(dispatcher);
        server.start(8080);

    }

    private void introduceDelay(long timeout) {
        synchronized (this) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                Timber.e(e);
            }
        }
    }

}