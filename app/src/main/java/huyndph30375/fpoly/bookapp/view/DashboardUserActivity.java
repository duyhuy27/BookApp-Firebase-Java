package huyndph30375.fpoly.bookapp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import huyndph30375.fpoly.bookapp.databinding.ActivityDashboardUserBinding;
import huyndph30375.fpoly.bookapp.models.Category;
import huyndph30375.fpoly.bookapp.view.fragment.UserBookPdfFragment;

public class DashboardUserActivity extends AppCompatActivity {

    private ActivityDashboardUserBinding binding;
    private FirebaseAuth mAuth;

    //
    public ArrayList<Category> categoryArrayList;
    //
    public ViewPagerAdapter viewPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        checkRoleUser();

        configViewPagerAdapter(binding.viewpager2);
        binding.tabLayout.setupWithViewPager(binding.viewpager2);

        listener();

    }

    private void configViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), DashboardUserActivity.this, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        categoryArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                Category objectAll = new Category("01", "All", "",1);
                Category objectMostView = new Category("02", "Most View", "",1);
                Category objectMostFab = new Category("03", "Most Favorite", "",1);

                categoryArrayList.add(objectAll);
                categoryArrayList.add(objectMostView);
                categoryArrayList.add(objectMostFab);

                //add du lieu vao viewpager
                viewPagerAdapter.addFragment(UserBookPdfFragment.newInstance(
                        "" + objectAll.getId(),
                        "" + objectAll.getCategory(),
                        "" + objectAll.getUid()), objectAll.getCategory());

                viewPagerAdapter.addFragment(UserBookPdfFragment.newInstance(
                        "" + objectMostView.getId(),
                        "" + objectMostView.getCategory(),
                        "" + objectMostView.getUid()), objectMostView.getCategory());

                viewPagerAdapter.addFragment(UserBookPdfFragment.newInstance(
                        "" + objectMostFab.getId(),
                        "" + objectMostFab.getCategory(),
                        "" + objectMostFab.getUid()), objectMostFab.getCategory());

                viewPagerAdapter.notifyDataSetChanged();

                // load data from firebase
                for (DataSnapshot ds : snapshot.getChildren()){
                    Category object = ds.getValue(Category.class);
                    categoryArrayList.add(object);
                    // add data to viewpager
                    viewPagerAdapter.addFragment(UserBookPdfFragment.newInstance(
                            "" + object.getId(),
                            "" + object.getCategory(),
                            "" + object.getUid()), object.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{

        public ArrayList<UserBookPdfFragment> fragmentArrayList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, Context context, int behavior) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentArrayList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentArrayList.size();
        }

        private void addFragment(UserBookPdfFragment fragment, String title){
            fragmentArrayList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void listener() {
        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
                finish();
            }
        });

        binding.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardUserActivity.this, ProfileActivity.class));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void checkRoleUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            //check role cua nguoi dung, neu khon phai la admin thi se bi da ra ngoai
           binding.tvEmailUser.setText("Enjoy without account. Create An Account To Explore More");
        } else {
            String emailUser = firebaseUser.getEmail();
            binding.tvEmailUser.setText(emailUser);
        }
    }
}