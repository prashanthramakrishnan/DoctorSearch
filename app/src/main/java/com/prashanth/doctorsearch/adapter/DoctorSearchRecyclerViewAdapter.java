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
import com.prashanth.doctorsearch.DoctorSearchApplication;
import com.prashanth.doctorsearch.R;
import com.prashanth.doctorsearch.dependencyInjection.NetworkDaggerModule;
import com.prashanth.doctorsearch.network.DoctorSearchAPI;
import com.prashanth.doctorsearch.network.model.Doctor;
import com.prashanth.doctorsearch.storage.LoginSharedPreferences;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.jetbrains.annotations.NotNull;

public class DoctorSearchRecyclerViewAdapter extends RecyclerView.Adapter<DoctorSearchRecyclerViewAdapter.ViewHolder> {

    private Set<Doctor> doctorList;

    private HashMap<String, InputStream> photoStream;

    private Context context;

    @Inject
    LoginSharedPreferences loginSharedPreferences;

    @Inject
    @Named(NetworkDaggerModule.AUTHENTICATED)
    DoctorSearchAPI doctorSearchAPI;

    public DoctorSearchRecyclerViewAdapter(Context context, Set<Doctor> doctorList, HashMap<String, InputStream> photoStream) {
        this.context = context;
        this.doctorList = doctorList;
        this.photoStream = photoStream;
        DoctorSearchApplication.component.inject(this);
    }

    @NotNull
    @Override
    public DoctorSearchRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_results, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull DoctorSearchRecyclerViewAdapter.ViewHolder holder, int position) {

        List<Doctor> tempArray  = new ArrayList<>();
        tempArray.addAll(doctorList);

        tempArray.get(position).getAddress();

        String address = tempArray.get(position).getAddress();
        String doctorName = tempArray.get(position).getName();
        String photoId = tempArray.get(position).getPhotoId();
        String doctorId = tempArray.get(position).getId();
        InputStream photo = photoStream.get(doctorId);

        holder.address.setText(address);
        holder.name.setText(doctorName);

        if (photoId == null) {
            holder.photo.setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder));
        } else {
            Bitmap bitmap = BitmapFactory.decodeStream(photo);
            Glide.with(context.getApplicationContext())
                    .load(bitmap)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.photo);
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

    public void update(Set<Doctor> doctors, HashMap<String, InputStream> photoBytesMap) {
        this.photoStream.putAll(photoBytesMap);
        doctorList.addAll(doctors);
        notifyDataSetChanged();
    }

}