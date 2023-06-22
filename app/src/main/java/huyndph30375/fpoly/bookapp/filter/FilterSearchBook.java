package huyndph30375.fpoly.bookapp.filter;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.Locale;

import huyndph30375.fpoly.bookapp.adapter.BookAdminAdapter;
import huyndph30375.fpoly.bookapp.adapter.CategoryAdapter;
import huyndph30375.fpoly.bookapp.models.BookPdf;
import huyndph30375.fpoly.bookapp.models.Category;

public class FilterSearchBook extends Filter {
    ArrayList<BookPdf> list;

    BookAdminAdapter adapter;

    //add contructor

    public FilterSearchBook(ArrayList<BookPdf> list, BookAdminAdapter adapter) {
        this.list = list;
        this.adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults = new FilterResults();
        //check gia tri co the null hoac empty
        if (charSequence != null && charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase(Locale.ROOT);
            ArrayList<BookPdf> filterObject = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getTitle().toUpperCase().contains(charSequence)) {
                    filterObject.add(list.get(i));
                }
            }

            filterResults.count = filterObject.size();
            filterResults.values = filterObject;
        }
        else {
            filterResults.count = list.size();
            filterResults.values = list;
        }

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        //
        adapter.arrayList = (ArrayList<BookPdf>) filterResults.values;
        adapter.notifyDataSetChanged();
    }
}
