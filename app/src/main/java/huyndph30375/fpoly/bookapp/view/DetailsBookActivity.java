package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import huyndph30375.fpoly.bookapp.R;
import huyndph30375.fpoly.bookapp.adapter.CommentAdapter;
import huyndph30375.fpoly.bookapp.adapter.FavoritesBookAdapter;
import huyndph30375.fpoly.bookapp.global.MyApplication;
import huyndph30375.fpoly.bookapp.databinding.ActivityDetailsBookBinding;
import huyndph30375.fpoly.bookapp.models.BookPdf;
import huyndph30375.fpoly.bookapp.models.CommentsModel;

public class DetailsBookActivity extends AppCompatActivity {
    private ActivityDetailsBookBinding binding;

    //id book
    private String idBook;

    private boolean existFab = false;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    public static final String TAG = "Add Comment Tag";

    private ArrayList<CommentsModel> commentsModelArrayList;
    private CommentAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(DetailsBookActivity.this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        getDataIntent();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            checkExistFab();
        }

        // cong viewer when activity bat dau mo ra
        MyApplication.calculatorViewCount(idBook);

        loadBookDetailsToUi();

        loadCommentOfUser();

        listener();
    }

    private void loadCommentOfUser() {
        commentsModelArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(idBook).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentsModelArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            CommentsModel model = ds.getValue(CommentsModel.class);
                            commentsModelArrayList.add(model);
                        }

                        binding.tvComment.setText("Comments (" + commentsModelArrayList.size() + ")" );

                        adapter = new CommentAdapter(DetailsBookActivity.this, commentsModelArrayList);
                        binding.recyclerviewComments.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private String comment = "";

    private void showDialogComment(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bottom_send_comment);

        //init view UI
        CircleImageView profile_image = dialog.findViewById(R.id.profile_image);
        TextView nameUser = dialog.findViewById(R.id.nameUser);
        TextView timeUserComment = dialog.findViewById(R.id.timeUserComment);
        EditText inputComment = dialog.findViewById(R.id.inputComment);

        RelativeLayout clickSend = dialog.findViewById(R.id.clickSend);

        clickSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment = inputComment.getText().toString().trim();
                if (TextUtils.isEmpty(comment)) {
                    Toast.makeText(context, " Your Comment Is Not Empty", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                    dialog.dismiss();
                }
            }
        });


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
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
                        nameUser.setText(name);
                        timeUserComment.setText(date);
                        //load image
                        try {
                            if (!isDestroyed()) {
                                Glide.with(DetailsBookActivity.this)
                                        .load(imageProfile)
                                        .placeholder(R.drawable.ic_person)
                                        .into(profile_image);
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "onDataChange: " + e.getMessage());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void addComment() {
        progressDialog.setMessage("Sending Your Comment To Author...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("bookId", "" + idBook);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("comment", "" + comment);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(idBook).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(DetailsBookActivity.this, "Sent Comment To Author", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    private void showDialogAddToCart(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bottom_add_cart);

        PDFView pdfView = dialog.findViewById(R.id.pdfView);
        ProgressBar progress = dialog.findViewById(R.id.progress);
        TextView titleTv = dialog.findViewById(R.id.title);
        TextView categoryTv = dialog.findViewById(R.id.categoryTv);
        TextView pageTv = dialog.findViewById(R.id.pageTv);
        TextView sizeTv = dialog.findViewById(R.id.sizeTv);
        TextView priceTv = dialog.findViewById(R.id.priceTv);
        TextView countTv = dialog.findViewById(R.id.countTv);
        TextView timeTv = dialog.findViewById(R.id.timeTv);

        AppCompatButton decreaseButton = dialog.findViewById(R.id.decreaseButton);
        AppCompatButton increaseButton = dialog.findViewById(R.id.increaseButton);
        AppCompatButton addToCart = dialog.findViewById(R.id.addToCart);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(idBook)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = "" + snapshot.child("title").getValue();
                        String des = "" + snapshot.child("des").getValue();
                        String category = "" + snapshot.child("categoryId").getValue();
                        String viewerCount = "" + snapshot.child("viewerCount").getValue();
                        String url = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String id = "" + snapshot.child("id").getValue();


                        String date = MyApplication.formatTime(Long.parseLong(timestamp));

                        titleTv.setText(title);
                        timeTv.setText(date);

                        MyApplication.loadCategory("" + category, categoryTv);
                        MyApplication.loadPdfUrl("" + url, pdfView, DetailsBookActivity.this, progress, pageTv);
                        MyApplication.loadPdfSize("" + url, title, sizeTv, DetailsBookActivity.this);

                        MyApplication.loadPrice(
                                "" + id,
                                priceTv);

                        MyApplication.loadPdfUrl("" + url, pdfView, context, progress, pageTv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void listener() {
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //user click like or don't like
        binding.fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(DetailsBookActivity.this, "You need loggin to add fab", Toast.LENGTH_SHORT).show();
                } else {
                    if (existFab) {
                        // da thich -> remove
                        MyApplication.deleteFab(DetailsBookActivity.this, idBook);
                    } else {
                        // chua thich -> add
                        MyApplication.calculatorFabCount(DetailsBookActivity.this, idBook);
                    }
                }
            }
        });

        binding.redAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailsBookActivity.this, ReadBookActivity.class);
                intent.putExtra("idBook", idBook);
                startActivity(intent);
            }
        });

        binding.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(DetailsBookActivity.this, "You need login to buy this book", Toast.LENGTH_SHORT).show();

                } else {
                    showDialogAddToCart(DetailsBookActivity.this);
                }
            }
        });

        binding.clickShowDialogComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(DetailsBookActivity.this, "You need login to comment", Toast.LENGTH_SHORT).show();
                } else {
                    showDialogComment(DetailsBookActivity.this);
                }
            }
        });
    }

    private void loadBookDetailsToUi() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(idBook)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = "" + snapshot.child("title").getValue();
                        String des = "" + snapshot.child("des").getValue();
                        String category = "" + snapshot.child("categoryId").getValue();
                        String viewerCount = "" + snapshot.child("viewerCount").getValue();
                        String url = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String date = MyApplication.formatTime(Long.parseLong(timestamp));

                        MyApplication.loadCategory("" + category, binding.categoryTv);
                        MyApplication.loadPdfUrl("" + url, binding.pdfView, DetailsBookActivity.this, binding.progress, binding.pageTv);
                        MyApplication.loadPdfSize("" + url, title, binding.downloadCount, DetailsBookActivity.this);

                        //set data
                        binding.titleTv.setText(title);
                        binding.dateTv.setText(date);
                        binding.viewer.setText(viewerCount.replace("null", "N/A"));
                        binding.desTv.setText(des);
                        binding.rate.setText("5.0");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getDataIntent() {
        idBook = getIntent().getStringExtra("idBook");
    }

    private void checkExistFab() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + firebaseAuth.getUid()).child("Fab").child(idBook)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        existFab = snapshot.exists();
                        if (existFab) {
                            // da thich quyen sach do
                            binding.fabAction.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_fab_red, 0, 0);

                        } else {
                            //chua thich
                            binding.fabAction.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_fab_black, 0, 0);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}