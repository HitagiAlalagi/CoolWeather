package com.coolweather.android;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;//
    private NavigationView navigationView;//
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<String> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_news);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationView = (NavigationView)findViewById(R.id.nav_design);
        View v = navigationView.getHeaderView(0);
        CircleImageView circleImageView =(CircleImageView) v.findViewById(R.id.icon_image);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        list = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        list.add("头条");
        list.add("社会");
        list.add("国内");
        list.add("国际");
        list.add("娱乐");
        list.add("体育");
        list.add("军事");
        list.add("科技");
        list.add("财经");
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return list.get(position);
            }

            @Override
            public int getCount() {
                return list.size();
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                NewsFragment newsFragment = (NewsFragment) super.instantiateItem(container, position);
                return newsFragment;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return FragmentStatePagerAdapter.POSITION_NONE;
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                NewsFragment newsFragment = new NewsFragment();
                //判断所选的标题，进行传值显示
                Bundle bundle = new Bundle();
                if (list.get(position).equals("头条")) {
                    bundle.putString("name", "top");
                } else if (list.get(position).equals("社会")) {
                    bundle.putString("name", "shehui");
                } else if (list.get(position).equals("国内")) {
                    bundle.putString("name", "guonei");
                } else if (list.get(position).equals("国际")) {
                    bundle.putString("name", "guoji");
                } else if (list.get(position).equals("娱乐")) {
                    bundle.putString("name", "yule");
                } else if (list.get(position).equals("体育")) {
                    bundle.putString("name", "tiyu");
                } else if (list.get(position).equals("军事")) {
                    bundle.putString("name", "junshi");
                } else if (list.get(position).equals("科技")) {
                    bundle.putString("name", "keji");
                } else if (list.get(position).equals("财经")) {
                    bundle.putString("name", "caijing");
                } else if (list.get(position).equals("时尚")) {
                    bundle.putString("name", "shishang");
                }
                newsFragment.setArguments(bundle);
                return newsFragment;
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
