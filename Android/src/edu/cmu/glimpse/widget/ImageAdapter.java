package edu.cmu.glimpse.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

@SuppressWarnings("deprecation")
public class ImageAdapter extends BaseAdapter {
    private final List<Bitmap> mBitmapList;
    private final Context mContext;

    private static int WIDTH = 120;
    private static int HEIGHT = 160;

    public ImageAdapter(Context context) {
        this(context, new ArrayList<Bitmap>());
    }

    public ImageAdapter(Context context, List<Bitmap> bitmapList) {
        mContext = context;
        mBitmapList = bitmapList;
    }

    public int getCount() {
        return mBitmapList.size();
    }

    public Object getItem(int position) {
        return mBitmapList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imgView = new ImageView(mContext);

        imgView.setImageBitmap(mBitmapList.get(position));

        imgView.setLayoutParams(new Gallery.LayoutParams(WIDTH, HEIGHT));
        imgView.setScaleType(ImageView.ScaleType.FIT_XY);

        return imgView;
    }

    public void addImage(Bitmap bitmap) {
        mBitmapList.add(bitmap);
        notifyDataSetChanged();
    }

}
