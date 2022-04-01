package com.pointer.wave.easyship.interfaces;

import com.pointer.wave.easyship.core.CacheDao;

public interface OnProcessErrorListener {
    void onErrorRead(CacheDao.MSG msg);
}
