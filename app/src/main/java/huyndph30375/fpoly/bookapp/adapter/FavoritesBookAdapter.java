package huyndph30375.fpoly.bookapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.databinding.ItemBookListFavoritesBinding;
import huyndph30375.fpoly.bookapp.global.MyApplication;
import huyndph30375.fpoly.bookapp.models.BookPdf;
import huyndph30375.fpoly.bookapp.view.DetailsBookActivity;

public class FavoritesBookAdapter extends RecyclerView.Adapter<FavoritesBookAdapter.ViewHolder> {
    private ArrayList<BookPdf> bookPdfArrayList;
    private Context context;

    public FavoritesBookAdapter(ArrayList<BookPdf> bookPdfArrayList, Context context) {
        this.bookPdfArrayList = bookPdfArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public FavoritesBookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookListFavoritesBinding binding = ItemBookListFavoritesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesBookAdapter.ViewHolder holder, int position) {
        BookPdf object = bookPdfArrayList.get(position);

        loadBookDetails(holder, object);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailsBookActivity.class);
                intent.putExtra("idBook", object.getId());
                context.startActivity(intent);
            }
        });

        holder.binding.removeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.deleteFab(context, object.getId());
            }
        });

    }

    private void loadBookDetails(ViewHolder holder, BookPdf object) {
        String bookId = object.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = "" + snapshot.child("title").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String price = "" + snapshot.child("price").getValue();
                        String url = "" + snapshot.child("url").getValue();
                        String viewerCount = "" + snapshot.child("viewerCount").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        String des = "" + snapshot.child("des").getValue();

                        //set data for object
                        object.setTitle(title);
                        object.setUid(uid);
                        object.setPrice(price);
                        object.setUrl(url);
                        object.setDes(des);
                        object.setTimestamp(Long.parseLong(timestamp));
                        object.setCheckFab(true);
                        object.setCategoryId(categoryId);

                        String formatDateTime = MyApplication.formatTime(Long.parseLong(timestamp));

                        //set data to ui

                        holder.binding.title.setText(title);
                        holder.binding.description.setText(des);
                        holder.binding.time.setText(formatDateTime);

                        //
                        //load data detail pdf
                        MyApplication.loadPdfUrl(
                                "" + object.getUrl(),
                                holder.binding.pdfView,
                                context,
                                holder.binding.progress,
                                null);

                        MyApplication.loadPdfSize(
                                "" + object.getUrl(),
                                "" + object.getTitle(),
                                holder.binding.size,
                                context);

                        //loadCategory
                        MyApplication.loadCategory(
                                "" + object.getCategoryId(),
                                holder.binding.category);

                        //loadPrice
                        MyApplication.loadPrice(
                                "" + object.getId(),
                                holder.binding.price);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return bookPdfArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemBookListFavoritesBinding binding;

        public ViewHolder(@NonNull ItemBookListFavoritesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
