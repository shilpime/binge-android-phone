package com.tatasky.binge.data.networking;

public interface ApiCallback<T> {

    void onSuccessFullyCallback(T t);
    void onFailure();
}
