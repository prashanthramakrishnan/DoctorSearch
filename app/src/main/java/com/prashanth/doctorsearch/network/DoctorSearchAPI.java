package com.prashanth.doctorsearch.network;

import androidx.annotation.Keep;
import com.prashanth.doctorsearch.network.model.DoctorSearchResponse;
import com.prashanth.doctorsearch.network.model.LoginResponse;
import io.reactivex.Observable;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DoctorSearchAPI {

    String AUTHORIZATION = "Basic " + "aXBob25lOmlwaG9uZXdpbGxub3RiZXRoZXJlYW55bW9yZQ==";

    @Keep
    @FormUrlEncoded
    @POST("/oauth/token")
    Call<LoginResponse> login(@Header("Content-Type") String contentType,
                              @Header("Accept") String contentTypeAccept,
                              @Header("Authorization") String authorization,
                              @FieldMap Map<String, String> doctorSearchLoginModel);

    @Keep
    @GET("/api/users/me/doctors")
    Observable<DoctorSearchResponse> getDoctors(@Query("search") String location,
                                                @Query("lat") String latitude,
                                                @Query("lng") String longitude,
                                                @Query("lastKey") String lastKey,
                                                @Header("Accept") String contentType,
                                                @Header("Authorization") String accessToken);

    @Keep
    @GET("/api/doctors/{doctorId}/keys/profilepictures")
    Observable<Response<ResponseBody>> getProfilePicture(@Path("doctorId") String doctorId,
                                               @Header("Accept") String contentType,
                                               @Header("Authorization") String accessToken);



}
