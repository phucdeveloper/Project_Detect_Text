package com.philipstudio.projectdetecttext.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.philipstudio.projectdetecttext.R;

import java.io.File;
import java.util.ArrayList;

public class FilePDFAdapter extends RecyclerView.Adapter<FilePDFAdapter.ViewHolder> {

    ArrayList<File> arrayList;

    public FilePDFAdapter(ArrayList<File> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_pdf, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtNameFile.setText(arrayList.get(position).getName());
        holder.txtNamePath.setText(arrayList.get(position).getPath());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtNameFile, txtNamePath;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNameFile = itemView.findViewById(R.id.item_textview_name_file);
            txtNamePath = itemView.findViewById(R.id.item_textview_name_path);

            itemView.setOnClickListener(view -> {

            });
        }
    }
}
