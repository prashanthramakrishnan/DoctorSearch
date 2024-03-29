package com.prashanth.doctorsearch.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prashanth.doctorsearch.DoctorSearchBaseApplication;
import com.prashanth.doctorsearch.R;
import com.prashanth.doctorsearch.contract.APIContract;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.Doctor;
import com.prashanth.doctorsearch.presenter.DoctorPhotoPresenter;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import com.prashanth.doctorsearch.ui.LoginActivity;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Named;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import retrofit2.Response;
import timber.log.Timber;

public class DoctorSearchRecyclerViewAdapter extends RecyclerView.Adapter<DoctorSearchRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Doctor> doctorList;

    private Context context;

    @Inject
    LoginSharedPreferences loginSharedPreferences;

    @Inject
    @Named(NetworkDaggerModule.AUTHENTICATED)
    DoctorSearchAPI doctorSearchAPI;

    public DoctorSearchRecyclerViewAdapter(Context context, ArrayList<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
        DoctorSearchBaseApplication.component.inject(this);
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
            getPhotoFromDoctorID(context, doctorId, holder);
        }
    }

    @Override
    public int getItemCount() {
        return doctorList == null ? 0 : doctorList.size();
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

    private void getPhotoFromDoctorID(Context context, String doctorId, @NotNull DoctorSearchRecyclerViewAdapter.ViewHolder holder) {

        DoctorPhotoPresenter doctorPhotoPresenter = new DoctorPhotoPresenter(doctorSearchAPI, new APIContract.DoctorPhotoView() {
            @Override
            public void onDataRetrievedSuccessfully(Response<ResponseBody> response) {
                if (response != null && response.body() != null && response.body().byteStream() != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    Glide.with(context.getApplicationContext())
                            .load(bitmap)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(holder.photo);
                }
            }

            @Override
            public void callStarted() {
                //no op
            }

            @Override
            public void callComplete() {
                //no op
            }

            @Override
            public void callFailed(Throwable throwable, int statusCode) {
                switch (statusCode) {
                    case 404:
                        Timber.d("Photo doesn't exist");
                        break;
                    case 401:
                        Timber.d("Logging out because of some other error");
                        loginSharedPreferences.clear();
                        LoginActivity.startActivity(context);
                        break;
                    case 0:
                        Timber.e(throwable, "Exception");
                        break;
                    default:
                        Timber.e(throwable, "Exception");
                        break;
                }
            }
        });
        doctorPhotoPresenter.fetchData(doctorId);
    }

    public void update(ArrayList<Doctor> doctors) {
        doctorList.clear();
        doctorList.addAll(doctors);
        notifyDataSetChanged();
    }

}