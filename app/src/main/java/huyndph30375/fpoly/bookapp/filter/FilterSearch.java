package huyndph30375.fpoly.bookapp.filter;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.Locale;

import huyndph30375.fpoly.bookapp.adapter.CategoryAdapter;
import huyndph30375.fpoly.bookapp.models.Category;

public class FilterSearch extends Filter {
    ArrayList<Category> list;

    CategoryAdapter adapter;

    //add contructor

    public FilterSearch(ArrayList<Category> list, CategoryAdapter adapter) {
        this.list = list;
        this.adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults = new FilterResults();
        //check gia tri co the null hoac empty
        if (charSequence != null && charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase(Locale.ROOT);
            ArrayList<Category> filterObject = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getCategory().toUpperCase().contains(charSequence)) {
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
        adapter.categories = (ArrayList<Category>) filterResults.values;
        adapter.notifyDataSetChanged();
    }
}
