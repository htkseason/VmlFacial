package pers.season.vmlfacial;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

/**
 * Created by Iris on 21/05/2017.
 */

public class PhotosAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> photos;
    private List<String> fileConns;
    private int width=75;

    PhotosAdapter(Context context, List<Bitmap> photos, List<String> fileConns){
        super();
        this.context = context;
        this.photos = photos;
        this.fileConns = fileConns;
    }

    public void setPhotos(List<Bitmap> photos) {
        this.photos = photos;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getCount() {
        if(photos==null) {
            return 0;
        }
        return photos.size();

    }

    @Override
    public Bitmap getItem(int position) {
        return this.photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView photo_view;
        if (convertView == null) {
            photo_view = new ImageView(context);
            photo_view.setLayoutParams(new GridView.LayoutParams(width, width));//设置ImageView对象布局
            photo_view.setAdjustViewBounds(false);//设置边界对齐
            photo_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photo_view.setPadding(0, 0, 0, 0);//设置间距
        }
        else {
            photo_view = (ImageView) convertView;
        }
        /*Bitmap bm = photos.get(position);
        if(bm.getWidth()<=width){
            photo_view.setImageBitmap(bm);
        }else{
            Bitmap bmp=Bitmap.createScaledBitmap(bm, width, bm.getHeight()*width/bm.getWidth(), true);
            photo_view.setImageBitmap(bmp);
        }*/
        photo_view.setImageBitmap(photos.get(position));
        return photo_view;
    }

    public void deletePhoto(int position) {
        photos.remove(position);

    }
}
