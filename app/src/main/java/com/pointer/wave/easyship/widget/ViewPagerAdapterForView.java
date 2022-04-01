package com.pointer.wave.easyship.widget;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class ViewPagerAdapterForView extends PagerAdapter {

    private final List<View> mListView;

    public ViewPagerAdapterForView(List<View> list)
    {
        // TODO Auto-generated method stub
        this.mListView = list;
    }

    @Override
    /**这个方法，是从ViewGroup中移出当前View**/
    public void destroyItem(View container, int position, Object object)
    {
        // TODO Auto-generated method stub
        ((ViewGroup) container ).removeView(mListView.get(position));
    }

    @Override
    public void finishUpdate(View view)
    {
        // TODO Auto-generated method stub

    }

    @Override
    /**这个方法，是获取当前窗体界面数**/
    public int getCount()
    {
        // TODO Auto-generated method stub
        return mListView.size();
    }

    @Override
    /**这个方法，return一个对象，这个对象表明了PagerAdapter适配器选择哪个对象*放在当前的ViewPager中**/
    public Object instantiateItem(View container, int position)
    {
        // TODO Auto-generated method stub
        ((ViewGroup) container).addView(mListView.get(position), 0);
        return mListView.get(position);
    }

    @Override
    /**这个方法，在帮助文档中原文是could be implemented as return view == object,*也就是用于判断是否由对象生成界面**/
    public boolean isViewFromObject(View view, Object object)
    {
        // TODO Auto-generated method stub
        return view == (object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Parcelable saveState()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void startUpdate(View v)
    {
        // TODO Auto-generated method stub

    }

}