package charlyn23.c4q.nyc.projectx;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StatsFragment extends android.support.v4.app.Fragment {
    private EditText zipCode;
    private String userInput;
    private int lastChildFragmentShown = 0;
    private static Fragment[] fragments;
    static ViewPager innerViewPager;

    public void pageSelected() {
        if (lastChildFragmentShown == 0) {
            ((PieChartFragment) fragments[0]).animateChart();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stats_fragment, container, false);
        Button update = (Button) view.findViewById(R.id.update);
        zipCode = (EditText) view.findViewById(R.id.zipcode);

        //child fragments
        fragments= new Fragment[2];
        fragments[0] = new PieChartFragment();
        fragments[1] = new BarChartFragment();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInput = zipCode.getText().toString();
                if (userInput.length() == 5) {
                    ((PieChartFragment) fragments[0]).getCountShameTypes(userInput);
                    ((BarChartFragment) fragments[1]).getCountGroups(userInput);
                }
                else {
                    Toast.makeText(getActivity(), "Invalid zipCode", Toast.LENGTH_LONG).show();
                }
            }
        });

        innerViewPager = (ViewPager) view.findViewById(R.id.inner_pager);
        InnerAdapter innerAdapter = new InnerAdapter(getChildFragmentManager());
        innerViewPager.setAdapter(innerAdapter);
        innerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ((PieChartFragment) fragments[0]).animateChart();
                }
                lastChildFragmentShown = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return view;
    }

    //inner view pager adapter
   public static class InnerAdapter extends FragmentPagerAdapter {

        public InnerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return fragments[0];
                case 1:
                    return  fragments[1];
                default:
                    return null;
            }
        }
    }
}
