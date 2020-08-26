package com.philipstudio.projectdetecttext.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.philipstudio.projectdetecttext.BuildConfig;
import com.philipstudio.projectdetecttext.R;
import com.philipstudio.projectdetecttext.util.LayoutUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FilePDFAdapter extends RecyclerView.Adapter<FilePDFAdapter.ViewHolder> {

    ArrayList<File> arrayList;
    Context context;
    boolean isChangeLayout;

    LayoutUtil layoutUtil;

    public FilePDFAdapter(ArrayList<File> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutUtil = new LayoutUtil(context);
        isChangeLayout = layoutUtil.getChangeLayout();
        View view;
        if (!isChangeLayout) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_pdf_vertical, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_pdf_horizontal, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        layoutUtil = new LayoutUtil(context);
        isChangeLayout = layoutUtil.getChangeLayout();
        if (!isChangeLayout) {
            String name = arrayList.get(position).getName();
            holder.txtNameFile.setText(name);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = new Date();
            String dateTime = simpleDateFormat.format(date);
            holder.txtTime.setText(dateTime);

            long size = arrayList.get(position).length();
            holder.txtSizeFile.setText(formatFileSize(size));

            holder.cBClear.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    boolean isDelete = arrayList.get(position).delete();
                    if (isDelete) {
                        showAlertDialog(context, position);
                    }
                }
            });
        } else {
            String name = arrayList.get(position).getName();
            holder.txtNameFile.setText(name);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNameFile, txtTime, txtSizeFile;
        CheckBox cBClear;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNameFile = itemView.findViewById(R.id.item_textview_name_file);
            txtTime = itemView.findViewById(R.id.item_textview_time);
            txtSizeFile = itemView.findViewById(R.id.item_textview_size_file);
            cBClear = itemView.findViewById(R.id.item_checkbox_clear);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                openPdf(context, arrayList, position);
            });
        }
    }

    private void openPdf(Context context, ArrayList<File> arrayList, int position) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFilePDF";
        File file = new File(path, arrayList.get(position).getName());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
        intent.setDataAndType(uri, "application/pdf");

        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
        }
    }

    private String formatFileSize(long size) {
        String hrSize;

        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else {
            hrSize = dec.format(k).concat(" KB");
        }

        return hrSize;
    }

    private void showAlertDialog(Context context, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Notification");
        builder.setMessage("Do you really want to delete this item?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            arrayList.remove(position);
            notifyItemRemoved(position);
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
