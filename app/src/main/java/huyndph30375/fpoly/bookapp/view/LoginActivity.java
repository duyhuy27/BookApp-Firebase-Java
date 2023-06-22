package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import huyndph30375.fpoly.bookapp.databinding.ActivitySigninBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivitySigninBinding binding; // binding
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        mAuth = FirebaseAuth.getInstance();

        listener();
    }

    private void listener() {
        binding.backPress.setOnClickListener(view -> onBackPressed());
        binding.noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        binding.signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData(); // ham validate du lieu nhap vao
            }
        });
    }

    // khoi tao cac bien
    private String email = "", password = "";

    private void validateData() {
        email = binding.inputEmail.getText().toString();
        password = binding.inputpassword.getText().toString();

        //validate cac bien
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.setError("Wrong email format");
        } else if (TextUtils.isEmpty(password)) {
            binding.inputpassword.setError("Enter your password");
        } else {
            loginClick(); //ham login cua user
        }

    }

    private void loginClick() {
        progressDialog.setMessage("Logging...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // dang nhap thanh cong
                        checkDataOfUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // dang nhap that bai
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkDataOfUser() {
        //check neu nguoi dung la user thi se vao man hinh khac va nguoi dung la admin thi se vao man hinh khac
        //get du lieu cua account
        progressDialog.setMessage("Check your role...");

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //lay vai tro cua nguoi dung
                        String userType = "" + snapshot.child("userType").getValue();
                        if (userType.equals("user")) {
                            // neu dung thi nguoi dung chi la user binh thuong, nen se vao man hinh cua user
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                        } else if (userType.equals("admin")) {
                            //neu dung nguoi dung la admin va co the chinh sua nen se vao man hinh khac
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}