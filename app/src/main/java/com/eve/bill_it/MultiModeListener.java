package com.eve.bill_it;

public interface MultiModeListener {

    void onSetMultiMode(boolean b);
    void onAddItem(int pos,String key);
    void onRemoveItem(int pos,String key);
}
