package com.talnts.talnatsflickrapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.SearchParameters;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONException;

import java.io.IOException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements Observer<Object> {

    private static final String API_KEY = "f590f888312150a81d0e1b4c591f6163";
    public static final String API_SEC = "01fde9a7ccfefe56";

    ImageView imageView;
    ImageLoader imageLoader;
    ProgressBar progressBar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        imageView = (ImageView) view.findViewById(R.id.image_view);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        ((SearchView) view.findViewById(R.id.search_view)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                requestImage(query);
                hideKeyboard(getContext());
                progressBar.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return view;
    }

    void requestImage(final String query) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                Photo photo = null;
                try {
                    photo = fetchImage(query);
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                if (photo != null) {
                    subscriber.onNext(photo);
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(this);
    }


    void updateImageView(Object o) {
        Photo photo = (Photo) o;
        String url = ((Photo) o).getMediumUrl();
        if (photo != null) {
            imageLoader.displayImage(url, imageView);
        }
    }

    Photo fetchImage(final String query) throws JSONException, FlickrException, IOException {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setText(query);
        Flickr flickr = new Flickr(API_KEY, API_SEC);
        PhotoList photoList = null;
        photoList = flickr.getPhotosInterface().search(searchParameters, 1, 1);

        Photo photo = photoList.size() > 0 ? photoList.get(0) : null;
        return photo;
    }

    static public void hideKeyboard(Context aContext) {
        // Check if no view has focus:
        View view = ((Activity) aContext).getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) aContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
    }

    @Override
    public void onNext(Object o) {
        progressBar.setVisibility(View.GONE);
        updateImageView(o);
    }
}
