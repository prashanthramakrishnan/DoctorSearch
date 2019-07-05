package com.prashanth.doctorsearch.storage;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoginSharedPreferencesTest {

    @Mock
    Context mockContext;

    @Mock
    SharedPreferences mockPrefs;

    @Mock
    SharedPreferences.Editor mockEditor;

    @InjectMocks
    LoginSharedPreferences loginSharedPreferences;

    final String DUMMY_STRING = "savedString";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
    }

    @Test
    public void testSetAccessToken() {
        loginSharedPreferences.setAccessToken(DUMMY_STRING);
        verify(mockEditor).putString("access_token", DUMMY_STRING);
    }

    @Test
    public void testSetLastKey() {
        loginSharedPreferences.setLastKey(DUMMY_STRING);
        verify(mockEditor).putString("last_key", DUMMY_STRING);
    }

}