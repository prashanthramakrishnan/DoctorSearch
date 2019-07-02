package com.prashanth.doctorsearch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.prashanth.doctorsearch.Constants;
import com.prashanth.doctorsearch.R;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.Doctor;
import com.prashanth.doctorsearch.network.networkwrapper.DoctorSearchRetrofitWrapper;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import com.prashanth.doctorsearch.ui.LoginActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

public class DoctorSearchRecyclerViewAdapter extends RecyclerView.Adapter<DoctorSearchRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Doctor> doctorList;

    private Context context;

    private LoginSharedPreferences loginSharedPreferences;

    public DoctorSearchRecyclerViewAdapter(Context context, ArrayList<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
        loginSharedPreferences = new LoginSharedPreferences(context);
    }

    @NotNull
    @Override
    public DoctorSearchRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_results, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull DoctorSearchRecyclerViewAdapter.ViewHolder holder, int position) {

        String address = doctorList.get(position).getAddress();
        String doctorName = doctorList.get(position).getName();
        String photoId = doctorList.get(position).getPhotoId();
        String doctorId = doctorList.get(position).getId();

        holder.address.setText(address);
        holder.name.setText(doctorName);

        if (photoId == null) {
            holder.photo.setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder));
        } else {
            getPictureFromDoctorIDCall(context, doctorId, holder);
        }
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView address;

        private ImageView photo;

        private TextView name;

        ViewHolder(View view) {
            super(view);
            address = view.findViewById(R.id.docotor_address);
            photo = view.findViewById(R.id.doctor_photo);
            name = view.findViewById(R.id.doctor_name);
        }
    }

    @SuppressLint("CheckResult")
    private void getPictureFromDoctorIDCall(Context context, String doctorId, @NotNull DoctorSearchRecyclerViewAdapter.ViewHolder holder) {
        Retrofit retrofitForDoctorSearch = DoctorSearchRetrofitWrapper.retrofitClient(Constants.LOGGED_IN_URL);
        DoctorSearchAPI doctorSearchAPI = retrofitForDoctorSearch.create(DoctorSearchAPI.class);
        doctorSearchAPI.getProfilePicture(doctorId,
                Constants.CONTENT_TYPE_ACCEPT_VALUE,
                "Bearer " + loginSharedPreferences.getAccessToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<ResponseBody>>() {
                    @Override
                    public void onNext(Response<ResponseBody> response) {
                        Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                        Glide.with(context)
                                .load(bitmap)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(holder.photo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            int code = ((HttpException) e).response().code();
                            if (code == 404) {
                                Timber.d("Photo doesn't exist");
                            }
                            if (code == 401) {
                                Timber.d("Logging out because of some other error");
                                loginSharedPreferences.clear();
                                Intent intent = new Intent(context, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        //do nothing
                    }
                });
    }

}
