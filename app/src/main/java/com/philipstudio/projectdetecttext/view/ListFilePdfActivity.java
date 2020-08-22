package com.philipstudio.projectdetecttext.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.philipstudio.projectdetecttext.R;

public class ListFilePdfActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pdf);


    }

    private void initView(){
        recyclerView = findViewById(R.id.recycletVire_list_dilpf);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(ListFilePdfActivity.this, RecyclerView.VERTICAL, false));


    }
}