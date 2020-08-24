package com.philipstudio.projectdetecttext.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.philipstudio.projectdetecttext.R;
import com.philipstudio.projectdetecttext.adapter.FilePDFAdapter;

import java.io.File;
import java.util.ArrayList;

public class ListFilePdfActivity extends AppCompatActivity {
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pdf);

        initView();

        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        setUpRecyclerViewListFilePDF(fileDir);

    }

    public void OnNavigationToolbarClick(View view) {
        finish();
    }

    private ArrayList<File> getListFilePDF(File directory) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] listFile = directory.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                if (file.getName().endsWith(".pdf")) {
                    arrayList.add(file);
                }
            }
        }
        return arrayList;
    }

    private void setUpRecyclerViewListFilePDF(File file) {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ListFilePdfActivity.this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<File> fileArrayList = getListFilePDF(file);

        FilePDFAdapter pdfAdapter = new FilePDFAdapter(fileArrayList);
        recyclerView.setAdapter(pdfAdapter);
    }

    private void initView() {
        recyclerView = findViewById(R.id.recyclerview_list_file_pdf);
    }
}