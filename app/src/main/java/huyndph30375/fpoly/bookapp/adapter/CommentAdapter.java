package huyndph30375.fpoly.bookapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.R;
import huyndph30375.fpoly.bookapp.databinding.ItemCommentOfUserBinding;
import huyndph30375.fpoly.bookapp.global.MyApplication;
import huyndph30375.fpoly.bookapp.models.CommentsModel;
import huyndph30375.fpoly.bookapp.view.DetailsBookActivity;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private ArrayList<CommentsModel> commentsModelArrayList;
    public static final String TAG = "GLide With Image";
    private FirebaseAuth firebaseAuth;

    public CommentAdapter(Context context, ArrayList<CommentsModel> commentsModelArrayList) {
        this.context = context;
        this.commentsModelArrayList = commentsModelArrayList;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentOfUserBinding binding = ItemCommentOfUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        CommentsModel model = commentsModelArrayList.get(position);
        String id = model.getId();
        String bookId = model.getBookId();
        String uid = model.getUid();
        String timestamp = model.getTimestamp();
        String comment = model.getComment();

        String date = MyApplication.formatTime(Long.parseLong(timestamp));

        //set data to UI
        holder.binding.comment.setText(comment);
        holder.binding.dateTv.setText(date);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() != null && uid.equals(firebaseAuth.getUid())){
                    deleteComment(model, holder);
                }
            }
        });

        loadUserCommentDetails(model, holder);

    }

    private void deleteComment(CommentsModel model, ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete Comment")
                .setMessage("Are you sure to delete this comment")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
                        databaseReference
                                .child("Comments")
                                .child(model.getBookId())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialogInterface.dismiss();
                                        Toast.makeText(context, "Deleted Your Comment", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialogInterface.dismiss();
                                        Log.d(TAG, "onFailure: " + "delete failed" + e.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void loadUserCommentDetails(CommentsModel model, ViewHolder holder) {
        String uid = model.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nameUser = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("imageProfile").getValue();

                        holder.binding.nameUserComment.setText(nameUser);

                        try {
                                Glide.with(context)
                                        .load(profileImage)
                                        .placeholder(R.drawable.ic_person)
                                        .into(holder.binding.profileImage);
                        } catch (Exception e) {
                            Log.d(TAG, "onDataChange: " + e.getMessage());
                            holder.binding.profileImage.setImageResource(R.drawable.ic_person);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return commentsModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemCommentOfUserBinding binding;
        public ViewHolder(@NonNull ItemCommentOfUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
