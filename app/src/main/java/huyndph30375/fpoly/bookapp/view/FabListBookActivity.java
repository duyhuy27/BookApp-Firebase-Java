package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.R;
import huyndph30375.fpoly.bookapp.adapter.FavoritesBookAdapter;
import huyndph30375.fpoly.bookapp.databinding.ActivityFabListBookBinding;
import huyndph30375.fpoly.bookapp.global.MyApplication;
import huyndph30375.fpoly.bookapp.models.BookPdf;

public class FabListBookActivity extends AppCompatActivity {

    private ActivityFabListBookBinding binding;

    private FirebaseAuth mAuth;

    private ArrayList<BookPdf> arrayList = new ArrayList<>();

    private FavoritesBookAdapter favoritesBookAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFabListBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        loadUser();

        loadFavoritesBook();

        listener();
    }

    private void listener() {
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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

                        favoritesBookAdapter = new FavoritesBookAdapter(arrayList, FabListBookActivity.this);
                        binding.recyclerviewBookFab.setAdapter(favoritesBookAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}