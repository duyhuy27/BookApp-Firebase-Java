package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import huyndph30375.fpoly.bookapp.databinding.ActivityUpdateBookPdfAdminBinding;

public class UpdateBookPdfAdminActivity extends AppCompatActivity {
    private ActivityUpdateBookPdfAdminBinding binding;

    private String idBook;

    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleList, categoryIdList;

    public static final String TAG = "UPDATE_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateBookPdfAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(UpdateBookPdfAdminActivity.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        getDataIntentFromItem();
        loadTitleCategory();
        loadDataBookPdf();
        listener();
    }

    private void loadDataBookPdf() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(idBook)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        idCategory = "" + snapshot.child("categoryId").getValue();

                        //set infor book pdf
                        String title = "" + snapshot.child("title").getValue();
                        String des = "" + snapshot.child("des").getValue();
                        String price = "" + snapshot.child("price").getValue();
                        String quantity = "" + snapshot.child("count").getValue();

                        //set data for view
                        binding.inputTitleBook.setText(title);
                        binding.inputDesBook.setText(des);
                        binding.inputPriceBook.setText(price);
                        binding.inputCountBook.setText(quantity);

                        //get value category
                        DatabaseReference refCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refCategory.child(idCategory)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = "" + snapshot.child("category").getValue();

                                        binding.categorySpinner.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(UpdateBookPdfAdminActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.categorySpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPickCategory();
            }
        });
    }

    // Khoi tao cac bien
    private String idCategory = "", titleCategory = "";

    private void dialogPickCategory() {
        String[] listTitleCategory = new String[categoryTitleList.size()];
        for (int i = 0; i < categoryTitleList.size(); i++) {
            listTitleCategory[i] = categoryTitleList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateBookPdfAdminActivity.this);
        builder.setTitle("Please Select Category")
                .setItems(listTitleCategory, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        idCategory = categoryIdList.get(i);
                        titleCategory = categoryTitleList.get(i);

                        binding.categorySpinner.setText(titleCategory);
                    }
                }).show();
    }

    private void loadTitleCategory() {
        categoryIdList = new ArrayList<>();
        categoryTitleList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdList.clear();
                categoryTitleList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = "" + ds.child("id").getValue();
                    String title = "" + ds.child("category").getValue();
                    categoryTitleList.add(title);
                    categoryIdList.add(id);
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

        binding.submitNewBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

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
        });

    }

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

    //khoi tao cac bien de validate
    String title = "", des = "", price = "", quantity = "";

    private void validateData() {
        title = binding.inputTitleBook.getText().toString().trim();
        des = binding.inputDesBook.getText().toString().trim();
        price = binding.inputPriceBook.getText().toString().trim();
        quantity = binding.inputCountBook.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.inputTitleBook.setError("Please Enter Title");
        } else if (TextUtils.isEmpty(des)) {
            binding.inputDesBook.setError("Please Enter Title");
        } else if (TextUtils.isEmpty(price)) {
            binding.inputPriceBook.setError("Please Enter Title");
        } else if (TextUtils.isEmpty(quantity)) {
            binding.inputCountBook.setError("Please Enter Title");
        } else if (TextUtils.isEmpty(idCategory)) {
            binding.categorySpinner.setError("Please Choose Category");
        } else {
            uploadPdf();
        }

    }

    private void uploadPdf() {
        progressDialog.setMessage("Update....");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("des", des);
        hashMap.put("price", price);
        hashMap.put("count", quantity);
        hashMap.put("categoryId", idCategory);

        //updating

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(idBook)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateBookPdfAdminActivity.this, "Update Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateBookPdfAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void getDataIntentFromItem() {
        idBook = getIntent().getStringExtra("id");
    }
}