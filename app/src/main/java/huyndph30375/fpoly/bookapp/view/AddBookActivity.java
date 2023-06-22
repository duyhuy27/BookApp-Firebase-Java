package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import huyndph30375.fpoly.bookapp.databinding.ActivityAddBookBinding;

public class AddBookActivity extends AppCompatActivity {
    private ActivityAddBookBinding binding;

    //firebase auth
    private FirebaseAuth mAuth;
    public static final int PDF_PICK_CODE = 1000;

    public static final String TAG = "PDF LOG ";

    private Uri pdfUri = null;

    private ArrayList<String> categoryTitleList, categoryIdList;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(AddBookActivity.this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        mAuth = FirebaseAuth.getInstance();
        listener();
        //load data from firebase
        loadCategoryPdf();
    }

    private void loadCategoryPdf() {
        categoryTitleList = new ArrayList<>();
        categoryIdList = new ArrayList<>();

        //ref to load database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleList.clear();
                categoryIdList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {

                    //get gia tri id va ten category
                    String categoryId = "" + ds.child("id").getValue();
                    String categoryTitle = "" + ds.child("category").getValue();

                    // arr
                    categoryTitleList.add(categoryTitle);
                    categoryIdList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void listener() {
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handler pick file pdf
        binding.uploadFilePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFileFormDevice();
            }
        });

        //handler pick category
        binding.categorySpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPicker();
            }
        });

        //handler click submit
        binding.submitNewBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        //handler khi nguoi dung nhap so tien se tu dong nhay dau , hoac dau .
        binding.inputPriceBook.addTextChangedListener(new TextWatcher() {
            private String setEditT = binding.inputPriceBook.getText().toString().trim();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(setEditT)) {
                    binding.inputPriceBook.removeTextChangedListener(this);
                    String replace = charSequence.toString().replaceAll("[₫,. ]", "");
                    setEditT = formatVndEditText(replace);
                    binding.inputPriceBook.setText(setEditT);
                    binding.inputPriceBook.setSelection(setEditT.length());
                    binding.inputPriceBook.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });    }

    private String formatVndEditText(String input) {
        // Remove non-numeric characters
        String cleanString = input.replaceAll("[^\\d]", "");

        // Insert commas for thousands, hundreds of thousands, millions, etc.
        if (!cleanString.isEmpty()) {
            StringBuilder formatted = new StringBuilder(cleanString);
            int length = formatted.length();

            for (int i = length - 3; i > 0; i -= 3) {
                formatted.insert(i, ",");
            }

            return formatted.toString();
        } else {
            return "";
        }
    }

    private String title = "", des = "", price = "", count = "";

    private void validateData() {
        title = binding.inputTitleBook.getText().toString().trim();
        des = binding.inputDesBook.getText().toString().trim();
        price = binding.inputPriceBook.getText().toString().trim();
        count = binding.inputCountBook.getText().toString().trim();

        //validate input
        if (TextUtils.isEmpty(title)) {
            binding.inputTitleBook.setError("Enter Title Book");
        } else if (TextUtils.isEmpty(des)) {
            binding.inputDesBook.setError("Enter Title Book");
        } else if (TextUtils.isEmpty(getSelectCategoryTitle)) {
            binding.categorySpinner.setError("Please Pick Category");
        } else if (TextUtils.isEmpty(price)) {
            binding.inputPriceBook.setError("Enter Price of Book");
        } else if (TextUtils.isEmpty(count)) {
            binding.inputCountBook.setError("Enter Quantity of Book");
        } else if (pdfUri == null) {
            Toast.makeText(this, "Upload your pdf file", Toast.LENGTH_SHORT).show();
        } else {
            submitPdfFileToStore();
        }
    }

    private void submitPdfFileToStore() {
        Log.d(TAG, "submitNewBook: uploading pdf to storage...");
        progressDialog.setMessage("Uploading PDF...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String filePath = "Books/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePath);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded");
                        Log.d(TAG, "onSuccess: getting pdf url");
                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadedFileUrl = "" + uriTask.getResult();

                        //upload to firebase
                        uploadInforBookToFirebase(uploadedFileUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        Toast.makeText(AddBookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadInforBookToFirebase(String uploadedFileUrl, long timestamp) {
        progressDialog.setMessage("Uploading pdf information");
        String uid = mAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("uid", "" + uid);
        hashMap.put("title", "" + title);
        hashMap.put("des", "" + des);
        hashMap.put("categoryId", selectCategoryId);
        hashMap.put("url", "" + uploadedFileUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("price", "" + price);
        hashMap.put("count", "" + count);
        hashMap.put("viewerCount", 0);
        hashMap.put("downloadCount", 0);
        hashMap.put("inventoryCount", 0);
        hashMap.put("fabCount", 0);
        hashMap.put("buyCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddBookActivity.this, "Upload successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddBookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private String selectCategoryId, getSelectCategoryTitle;


    private void categoryPicker() {
        String[] categoryArr = new String[categoryTitleList.size()];
        for (int i = 0; i < categoryTitleList.size(); i++) {
            categoryArr[i] = categoryTitleList.get(i);
        }

        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoryArr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        handler when user click
                        getSelectCategoryTitle = categoryTitleList.get(i);
                        selectCategoryId = categoryIdList.get(i);
                        binding.categorySpinner.setText(getSelectCategoryTitle);
                        Log.d(TAG, "onClick: " + selectCategoryId + getSelectCategoryTitle);
                    }
                }).show();
    }

    private void pickFileFormDevice() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pfd file"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: PDF PICKED");
                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI" + pdfUri);
            }
        } else {
            Log.d(TAG, "onActivityResult: failed to pick pdf");
            Toast.makeText(this, "Failed to pick", Toast.LENGTH_SHORT).show();
        }
    }
}