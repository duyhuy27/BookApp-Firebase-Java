package huyndph30375.fpoly.bookapp.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.adapter.BookUserAdapter;
import huyndph30375.fpoly.bookapp.databinding.FragmentUserBookPdfBinding;
import huyndph30375.fpoly.bookapp.models.BookPdf;


public class UserBookPdfFragment extends Fragment{

    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<BookPdf> bookPdfArrayList;
    private BookUserAdapter adapter;

    //binding in fragment
    private FragmentUserBookPdfBinding binding;

    public static final String TAG = "Book_Fragment";


    public UserBookPdfFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static UserBookPdfFragment newInstance(String categoryId, String category, String uid) {
        UserBookPdfFragment fragment = new UserBookPdfFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBookPdfBinding.inflate(LayoutInflater.from(getContext()), container, false);

        setUpToLoadBook();
        listener();

        return binding.getRoot();
    }

    private void listener() {
        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapter.getFilter().filter(charSequence);
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setUpToLoadBook() {
        if (category.equals("All")) {
            loadAllBookCategory();
        } else if (category.equals("Most View")) {
            loadMostBookCategory("viewerCount");
        } else if (category.equals("Most Favorite")) {
            loadFavoriteBookCategory("fabCount");
        } else {
            //load muc duoc category duoc chon
            loadBookSelector();
        }
    }

    private void loadBookSelector() {
        bookPdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookPdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            BookPdf object = ds.getValue(BookPdf.class);
                            bookPdfArrayList.add(object);
                        }
                        adapter = new BookUserAdapter(getContext(), bookPdfArrayList);
                        binding.recyclerviewBookUser.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFavoriteBookCategory(String fabCount) {
        bookPdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(fabCount).limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookPdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            BookPdf object = ds.getValue(BookPdf.class);
                            bookPdfArrayList.add(object);
                        }
                        adapter = new BookUserAdapter(getContext(), bookPdfArrayList);
                        binding.recyclerviewBookUser.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMostBookCategory(String viewerCount) {
        bookPdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(viewerCount).limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookPdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            BookPdf object = ds.getValue(BookPdf.class);
                            bookPdfArrayList.add(object);
                        }
                        adapter = new BookUserAdapter(getContext(), bookPdfArrayList);
                        binding.recyclerviewBookUser.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllBookCategory() {
        bookPdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookPdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    BookPdf object = ds.getValue(BookPdf.class);
                    bookPdfArrayList.add(object);
                }
                adapter = new BookUserAdapter(getContext(), bookPdfArrayList);
                binding.recyclerviewBookUser.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}