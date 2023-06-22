package huyndph30375.fpoly.bookapp.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.view.DetailsBookActivity;
import huyndph30375.fpoly.bookapp.global.MyApplication;
import huyndph30375.fpoly.bookapp.view.UpdateBookPdfAdminActivity;
import huyndph30375.fpoly.bookapp.databinding.ItemBookListBinding;
import huyndph30375.fpoly.bookapp.filter.FilterSearchBook;
import huyndph30375.fpoly.bookapp.models.BookPdf;

public class BookAdminAdapter extends RecyclerView.Adapter<BookAdminAdapter.ViewHolder> implements Filterable {
    private Context context;
    public ArrayList<BookPdf> arrayList, filterList;
    private FilterSearchBook filterSearchBook;
    private ProgressDialog progressDialog;

    public BookAdminAdapter(Context context, ArrayList<BookPdf> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.filterList = arrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public BookAdminAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookListBinding binding = ItemBookListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookAdminAdapter.ViewHolder holder, int position) {
        BookPdf object = arrayList.get(position);
        String title = object.getTitle();
        String description = object.getDes();
        long timestamp = object.getTimestamp();
        String formatDateTime = MyApplication.formatTime(timestamp);


        //set data vao item trong recycler
        holder.binding.title.setText(title);
        holder.binding.description.setText(description);
        holder.binding.time.setText(formatDateTime);

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


        //handler click to delete or update
        holder.binding.moreClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOption(object, holder);
            }
        });

        //handler when user click item to see details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailsBookActivity.class);
                intent.putExtra("idBook", object.getId());
                context.startActivity(intent);
            }
        });

    }

    private void moreOption(BookPdf object, ViewHolder holder) {
        String[] optionSelect = {"Delete", "Update"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option You Want...")
                .setItems(optionSelect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            // position = 0 (1) thi nguoi dung chon delete
                            //delete function
                            MyApplication.deleteBookPdf(context,
                                    "" + object.getId(),
                                    "" + object.getTitle(),
                                    "" + object.getUrl());
                        } else if (i == 1) {
                            //position == 1 (2) thi nguoi dung chon update thong tin cua sach
                            // update function
                            updateBookPdf(object, holder);
                        }

                    }
                })
                .show();
    }

    private void updateBookPdf(BookPdf object, ViewHolder holder) {
        Intent intent = new Intent(context, UpdateBookPdfAdminActivity.class);
        intent.putExtra("id", object.getId());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public Filter getFilter() {

        if (filterSearchBook == null) {
            filterSearchBook = new FilterSearchBook(filterList, this);
        }
        return filterSearchBook;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemBookListBinding binding;

        public ViewHolder(@NonNull ItemBookListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
