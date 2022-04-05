package com.pointer.wave.easyship.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

abstract public class BaseFragment extends Fragment {

    public BaseFragment(){
        super();
    }

    public BaseFragment(@LayoutRes int layoutId){
        super(layoutId);
    }

    private boolean firstResumed = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater,container,savedInstanceState);
        if(rootView == null){
            rootView = onCreateViewByReturn(inflater, container);
        }
        return rootView;
    }

    protected View onCreateViewByReturn(@NonNull LayoutInflater inflater, @Nullable ViewGroup container){
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        firstResumed = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!firstResumed){
            firstResumed = true;
            loadSingleData();
        }
        loadData();
    }

    protected void initView(View root){}

    protected void loadSingleData(){}

    protected void loadData(){}
}


