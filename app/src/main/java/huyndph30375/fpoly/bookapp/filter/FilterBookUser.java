package huyndph30375.fpoly.bookapp.filter;

import android.annotation.SuppressLint;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.Locale;

import huyndph30375.fpoly.bookapp.adapter.BookUserAdapter;
import huyndph30375.fpoly.bookapp.models.BookPdf;

public class FilterBookUser extends Filter {
    ArrayList<BookPdf> filterList;

    BookUserAdapter adapter;

    public FilterBookUser(ArrayList<BookPdf> filterList, BookUserAdapter adapter) {
        this.filterList = filterList;
        this.adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();

        if (charSequence != null || charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase(Locale.ROOT);
            ArrayList<BookPdf> filterObject = new ArrayList<>();

            for (int i = 0 ; i < filterList.size() ; i ++){
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence)) {
                    filterObject.add(filterList.get(i));
                }
            }
            results.count = filterObject.size();
            results.values = filterObject;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.bookPdfArrayList = (ArrayList<BookPdf>) filterResults.values;
        adapter.notifyDataSetChanged();
    }
}
