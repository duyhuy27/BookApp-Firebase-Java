package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import huyndph30375.fpoly.bookapp.R;
import huyndph30375.fpoly.bookapp.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    private FirebaseAuth mAuth; // firebase
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase
        mAuth = FirebaseAuth.getInstance();

        //create progress dialog
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        listener(); // ham xu ly cac nut trong layout
    }

    private void listener() {
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.register.setOnClickListener(new View.OnClickListener() { //button dang ky
            @Override
            public void onClick(View view) {
                validateData(); // ham validate inphut
            }
        });

        binding.inputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String passwordInput = charSequence.toString();
                if (passwordInput.length() >= 6) {
                    Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher = pattern.matcher(passwordInput);
                    boolean passwordsMatch = matcher.find();
                    if (passwordsMatch) {
                        binding.passwordTil.setHelperText("Your Password are Strong");
                        binding.passwordTil.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                        binding.passwordTil.setError("");
                    } else {
                        binding.passwordTil.setError("Please make sure your password includes at least one non-capital letter, one uppercase letter, and one special character for increased security");
                    }
                } else {
                    binding.passwordTil.setHelperText("Password must 6 Characters long");
                    binding.passwordTil.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //khai bao cac bien de su dung
    private String name = "", email = "", password = "", passwordCf = "";

    private void validateData() {
        name = binding.inputUsername.getText().toString();
        email = binding.inputEmail.getText().toString();
        password = binding.inputPassword.getText().toString();
        passwordCf = binding.inputPasswordCf.getText().toString();

        //validate cac bien
        if (TextUtils.isEmpty(name)) {
            binding.inputUsername.setError("Enter your name");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputPassword.setError("Wrong email format");
        } else if (TextUtils.isEmpty(password)) {
            binding.inputPassword.setError("Enter your password");
        } else if (TextUtils.isEmpty(passwordCf)) {
            binding.inputPasswordCf.setError("Enter your password confirm");
        } else if (!password.equals(passwordCf)) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
        } else {
            registerAccount();
        }
    }

    private void registerAccount() {
        progressDialog.setMessage("Creating New Account...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // create user thanh cong va luu vao database realtime
                        updateUIofUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateUIofUser() {
        progressDialog.setMessage("Saved Information of User");

        //time real
        long timestamp = System.currentTimeMillis();

        //get uid cua user tren database de co the get ve moi khi dang nhau ( gan nhu giong luu mat khau )
        String uid = mAuth.getUid();

        //
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("password", password);
        hashMap.put("name", name);
        hashMap.put("imageProfile", ""); // day len firebase truoc, vi sau nay se co chuc nang update giao dien nguoi dung
        hashMap.put("userType", "user"); // module user
        hashMap.put("timestamp", timestamp);

        // day dta len firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data da add thanh cong len firebase
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //add data khong thanh cong
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}