package com.pointer.wave.easyship.interfaces;

import com.pointer.wave.easyship.core.CacheDao;

public interface OnRunTimeListener {
    void onRead(final CacheDao.MSG read);
}
