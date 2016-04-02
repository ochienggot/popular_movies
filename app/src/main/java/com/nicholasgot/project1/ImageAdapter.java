package com.nicholasgot.project1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by ngot on 02/04/2016.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> thumbIds;
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();

    public ImageAdapter(Context context, ArrayList<String> imageUri) {
        mContext = context;
        thumbIds = imageUri;
    }

    @Override
    public int getCount() {
        return thumbIds.size();
    }

    @Override
    public Object getItem(int position) {
        return thumbIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);

        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mContext).load(thumbIds.get(position)).into(imageView);
        return imageView;
    }
}
