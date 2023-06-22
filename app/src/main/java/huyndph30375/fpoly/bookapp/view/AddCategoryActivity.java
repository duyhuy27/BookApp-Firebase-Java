package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import huyndph30375.fpoly.bookapp.databinding.ActivityAddCategoryBinding;

public class AddCategoryActivity extends AppCompatActivity {

    private ActivityAddCategoryBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(AddCategoryActivity.this); //set up dialog
        progressDialog.setTitle("Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();

        listener();
    }

    private void listener() {
        binding.backPress.setOnClickListener(view -> onBackPressed());
        //add category book
        binding.submitNewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateDataInput();
            }
        });
    }

    //khoi tao bien de validate
    String nameCategory = "";
    private void validateDataInput() {
        nameCategory = binding.inputCategory.getText().toString().trim();
        if (TextUtils.isEmpty(nameCategory)){
            binding.inputCategory.setError("Please input name category");
        }
        else {
            submitCategoryToFirebase();
        }
    }

    private void submitCategoryToFirebase() {
        //show dialog de nguoi dung doi xu ly day du lieu
        progressDialog.setMessage("Add Category ! Please wait");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        String uid = mAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+ timestamp);
        hashMap.put("uid", uid);
        hashMap.put("category", ""+nameCategory);
        hashMap.put("timestamp", timestamp);

        //submit to firebase realtime db > id > information
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Categories");
        database.child(""+ timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //add thanh cong len firebase
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, "Add Category Successfully", Toast.LENGTH_SHORT).show();
                        binding.inputCategory.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //add that bai
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}