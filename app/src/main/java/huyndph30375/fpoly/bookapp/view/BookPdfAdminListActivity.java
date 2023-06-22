package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.adapter.BookAdminAdapter;
import huyndph30375.fpoly.bookapp.databinding.ActivityBookPdfAdminListBinding;
import huyndph30375.fpoly.bookapp.models.BookPdf;

public class BookPdfAdminListActivity extends AppCompatActivity {

    private ActivityBookPdfAdminListBinding binding;
    private ArrayList<BookPdf> pdfList;
    private BookAdminAdapter adapter;
    private String categoryId, categoryTitle;

    public static final String TAG = "PDF_LIST";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookPdfAdminListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data intent
        getDataIntent();
        loadPdfBookList();
        listener();
    }

    private void listener() {
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //search title book
        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    //search hear function when user type
                try {
                    adapter.getFilter().filter(charSequence);
                }catch (Exception e){
                    Toast.makeText(BookPdfAdminListActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle= intent.getStringExtra("categoryTitle");
        binding.categoryTv.setText(categoryTitle);
    }

    private void loadPdfBookList() {
        pdfList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            BookPdf model = ds.getValue(BookPdf.class);
                            pdfList.add(model);
                        }
                        adapter = new BookAdminAdapter(BookPdfAdminListActivity.this, pdfList);
                        binding.bookRecyclerview.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}