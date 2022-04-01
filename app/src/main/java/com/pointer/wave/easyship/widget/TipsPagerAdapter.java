package com.pointer.wave.easyship.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.pojo.TipsBen;

import java.util.List;

public class TipsPagerAdapter extends PagerAdapter{
    private final Context context;
    private final List<TipsBen> mListView;
    public TipsPagerAdapter(List<TipsBen> list, Context context) {
        this.context = context;
        this.mListView = list;
    }

    @Override
    public int getCount() {
        return mListView != null ? mListView.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup view, int position, @NonNull Object object) {
        view.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {
        View a = View.inflate(context, R.layout.item_tips_viewpager, null);
        a.<TextView>findViewById(R.id.tips_title).setText(mListView.get(position).getContent());
        view.addView(a);
        return a;
    }
}