package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.R;
import huyndph30375.fpoly.bookapp.databinding.ActivityProfileBinding;
import huyndph30375.fpoly.bookapp.global.MyApplication;
import huyndph30375.fpoly.bookapp.models.BookPdf;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseAuth mAuth;

    private ArrayList<BookPdf> arrayList;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Check the permission

        mAuth = FirebaseAuth.getInstance();

        loadUser();

        loadFavoritesBook();

        listener();

    }

    private void loadFavoritesBook() {

        arrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(mAuth.getUid()).child("Fab")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()){
                            String bookId = "" + ds.child("bookId").getValue();

                            BookPdf object = new BookPdf();
                            object.setId(bookId);

                            arrayList.add(object);

                        }

                        binding.fabCount.setText("" + arrayList.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void listener() {
        binding.linerEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });

        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.fabLiner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, FabListBookActivity.class));
            }
        });
    }

    private void loadUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get infor of user from database
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String imageProfile = "" + snapshot.child("imageProfile").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String userType = "" + snapshot.child("userType").getValue();
                        String uid = "" + snapshot.child("uid").getValue();

                        //

                        String date = MyApplication.formatTime(Long.parseLong(timestamp));

                        //set data to ui
                        binding.emailProfile.setText(email);
                        binding.nameProfile.setText(name);
                        binding.memberDateTv.setText(date);
                        binding.accountTv.setText(userType);
                        try {
                            if (!isDestroyed()){
                                Glide.with(ProfileActivity.this)
                                        .load(imageProfile)
                                        .placeholder(R.drawable.ic_person)
                                        .into(binding.profileImage);
                            }
                        }
                        catch (Exception e){
                            e.getMessage();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}