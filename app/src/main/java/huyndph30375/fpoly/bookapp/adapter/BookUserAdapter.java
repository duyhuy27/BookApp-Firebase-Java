package huyndph30375.fpoly.bookapp.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.R;
import huyndph30375.fpoly.bookapp.databinding.ItemBookUserBinding;
import huyndph30375.fpoly.bookapp.filter.FilterBookUser;
import huyndph30375.fpoly.bookapp.global.MyApplication;
import huyndph30375.fpoly.bookapp.models.BookPdf;
import huyndph30375.fpoly.bookapp.view.DetailsBookActivity;

public class BookUserAdapter extends RecyclerView.Adapter<BookUserAdapter.ViewHolder> implements Filterable {

    private Context context;
    public ArrayList<BookPdf> bookPdfArrayList, filterList;
    private FilterBookUser filter;

    public BookUserAdapter(Context context, ArrayList<BookPdf> bookPdfArrayList) {
        this.context = context;
        this.bookPdfArrayList = bookPdfArrayList;
        this.filterList = bookPdfArrayList;
    }

    @NonNull
    @Override
    public BookUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookUserBinding binding = ItemBookUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookUserAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        BookPdf object = bookPdfArrayList.get(position);
        String title = object.getTitle();
        String description = object.getDes();
        String pdfUrl = object.getUrl();
        String categoryId = object.getCategoryId();
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

        //handler when user click item to see details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailsBookActivity.class);
                intent.putExtra("idBook", object.getId());
                context.startActivity(intent);
            }
        });

        holder.binding.showDialogCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogAddToCart(context, position);
            }
        });
    }

    private int quantity = 1;
    private double cost = 0;
    private double finalCost = 0;
    private void showDialogAddToCart(Context context, int position) {
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

        BookPdf object = bookPdfArrayList.get(position);
        long timestamp = object.getTimestamp();
        String formatDateTime = MyApplication.formatTime(timestamp);
        String title = object.getTitle();

        timeTv.setText(formatDateTime);
        titleTv.setText(title);

        //load data detail pdf
        MyApplication.loadPdfUrl(
                "" + object.getUrl(),
                pdfView,
                context,
                progress,
                null);

        MyApplication.loadPdfSize(
                "" + object.getUrl(),
                "" + object.getTitle(),
                sizeTv,
                context);

        //loadCategory
        MyApplication.loadCategory(
                "" + object.getCategoryId(),
                categoryTv);

        //loadPrice
        MyApplication.loadPrice(
                "" + object.getId(),
                priceTv);

        MyApplication.loadPdfUrl("" + object.getUrl(), pdfView, context, progress, pageTv);

        String price = object.getPrice().replace(",", "");  // Remove commas from the price string
        quantity = 1;

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 1) {
                    quantity--;
                    double newPrice = quantity * Double.parseDouble(price);
                    String newFormattedPrice = MyApplication.formatVndEditText(String.valueOf(newPrice));
                    priceTv.setText(newFormattedPrice);
                    countTv.setText(String.valueOf(quantity));
                }
            }
        });


        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity++;
                double newPrice = quantity * Double.parseDouble(price);
                String newFormattedPrice = MyApplication.formatVndEditText(String.valueOf(newPrice));
                priceTv.setText(newFormattedPrice);
                countTv.setText(String.valueOf(quantity));
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    @Override
    public int getItemCount() {
        return bookPdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterBookUser(filterList, this);
        }
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemBookUserBinding binding;

        public ViewHolder(@NonNull ItemBookUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
