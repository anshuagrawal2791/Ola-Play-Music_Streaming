package com.ola.hackerearth.ola;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by anshu on 30/10/15.
 */
public class VolleySingleton {
    private static VolleySingleton inst = null;
    private RequestQueue mRequestQueue;
    private ImageLoader mimageloader;
    public Context context;
    private VolleySingleton(){

        mRequestQueue = Volley.newRequestQueue(MyApp.getAppContext());
        mimageloader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private LruCache<String,Bitmap> cache = new LruCache<>((int) Runtime.getRuntime().maxMemory()/1024/8);
            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url,bitmap);

            }
        });

    }

    public static VolleySingleton getinstance(){
        if(inst == null)
        {
            inst = new VolleySingleton();
        }
        return inst;
    }

    public RequestQueue getrequestqueue()
    {
        return mRequestQueue;
    }
    public ImageLoader getimageloader()
    {
        return mimageloader;
    }
}
