package com.philipstudio.projectdetecttext.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.philipstudio.projectdetecttext.R;
import com.philipstudio.projectdetecttext.adapter.FilePDFAdapter;
import com.philipstudio.projectdetecttext.util.LayoutUtil;

import java.io.File;
import java.util.ArrayList;

public class ListFilePdfActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView txtNamePath;
    ImageView imgChangeLayout, imgMenu;
    RelativeLayout relativeLayout;
    EditText editText;
    Button btnOk, btnCancel;

    BottomSheetBehavior<RelativeLayout> bottomSheetBehavior;
    LayoutUtil layoutUtil;
    boolean isChangeLayout;
    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFilePDF";
    FilePDFAdapter pdfAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pdf);

        initView();

        txtNamePath.setText(path);

        File fileDir = new File(path);

        setUpRecyclerViewListFilePDF(fileDir);

        imgChangeLayout.setOnClickListener(view -> {
            isChangeLayout = layoutUtil.getChangeLayout();
            if (isChangeLayout) {
                layoutUtil.setChangeLayout(false);
            } else {
                layoutUtil.setChangeLayout(true);
            }
            setUpRecyclerViewListFilePDF(fileDir);
        });

        imgMenu.setOnClickListener(view -> showPopupMenu());
    }

    public void OnImageViewComeBackClick(View view) {
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
        isChangeLayout = layoutUtil.getChangeLayout();
        recyclerView.setHasFixedSize(true);
        if (!isChangeLayout) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ListFilePdfActivity.this, RecyclerView.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(ListFilePdfActivity.this, 5);
            recyclerView.setLayoutManager(gridLayoutManager);
        }

        ArrayList<File> fileArrayList = getListFilePDF(file);

        pdfAdapter = new FilePDFAdapter(fileArrayList, ListFilePdfActivity.this);
        recyclerView.setAdapter(pdfAdapter);
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, imgMenu);
        popupMenu.getMenuInflater().inflate(R.menu.my_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.sorted_by:
                    break;
                case R.id.create_folder:
                    showBottomSheetCreateFolder();
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showBottomSheetCreateFolder() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        btnOk.setOnClickListener(view -> {
            String nameFolder = editText.getText().toString();
            if (TextUtils.isEmpty(nameFolder)) {
                File dir = new File(path + "/" + nameFolder);
                if (!dir.exists()) {
                    dir.mkdirs();
                    Toast.makeText(ListFilePdfActivity.this, "You created new folder", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ListFilePdfActivity.this, "You have not entered a folder name", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(view -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
    }

    private void initView() {
        imgChangeLayout = findViewById(R.id.image_view_change_layout);
        imgMenu = findViewById(R.id.image_view_menu);
        txtNamePath = findViewById(R.id.text_view_name_path);
        recyclerView = findViewById(R.id.recyclerview_list_file_pdf);
        relativeLayout = findViewById(R.id.layout_bottom_sheet);
        btnOk = findViewById(R.id.button_ok);
        btnCancel = findViewById(R.id.button_cancel);
        editText = findViewById(R.id.edit_text_input_name_folder);

        layoutUtil = new LayoutUtil(ListFilePdfActivity.this);
        bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);
    }
}