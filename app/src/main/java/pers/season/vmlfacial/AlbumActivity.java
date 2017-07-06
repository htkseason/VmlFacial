package pers.season.vmlfacial;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Iris on 20/05/2017.
 */

public class AlbumActivity extends Activity {

    private ImageButton btn_back;
    private List<Bitmap> photos = new ArrayList<Bitmap>();
    private List<Bitmap> photos_cut = new ArrayList<Bitmap>();
    private List<String> fileConns = new ArrayList<String>();
    private PhotosAdapter pa;
    private GridView gv;
    private int wid_pic;

    private ImageView show_photo;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    private AlertDialog al2 = null;
    private AlertDialog.Builder bd2 = null;

    public AlbumActivity() {

        System.out.println("--------------------------Album Activity Created----------------------");
    }

    @Override
    protected void onStart() {
        super.onStart();
        photos.clear();
        photos_cut.clear();
        fileConns.clear();


        if (WorldVar.userName != null) {
            File cloudPicFolder = new File(VfUtils.PicDataPath + "/" + WorldVar.userName + "/");
            if (cloudPicFolder.exists()) {
                File[] cloudPicFiles = cloudPicFolder.listFiles();
                for (int i = cloudPicFiles.length - 1; i >= 0; i--)
                    if (cloudPicFiles[i].isFile()) {
                        try {
                            setPhotos(BitmapFactory.decodeFile(cloudPicFiles[i].toString()));
                            fileConns.add(cloudPicFiles[i].toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }
        }

        File nativePicFolder = new File(VfUtils.PicDataPath);
        if (nativePicFolder.exists()) {
            File[] nativePicFiles = nativePicFolder.listFiles();
            for (int i = nativePicFiles.length - 1; i >= 0; i--) {
                if (nativePicFiles[i].isFile()) {
                    try {
                        setPhotos(BitmapFactory.decodeFile(nativePicFiles[i].toString()));
                        fileConns.add(nativePicFiles[i].toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_album);
        //根据屏幕尺寸设置ImageView宽度
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        float scale = metric.density;
        wid_pic = metric.widthPixels; //px
        int width = (int) (wid_pic / scale + 0.5f); //dp
        wid_pic = (int) ((width - 8) / 4 * scale + 0.5f);
        //设置返回按钮
        btn_back = (ImageButton) findViewById(R.id.back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                Intent it = new Intent();
                it.setClass(AlbumActivity.this, MainActivity.class);
                startActivity(it);
            }
        });

        gv = (GridView) findViewById(R.id.grid_photos);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                showPhoto(position);
                //Toast.makeText(AlbumActivity.this, "pic" + position, Toast.LENGTH_SHORT).show();
            }
        });

        gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                deletePhoto(position);
                return true;
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        pa = new PhotosAdapter(this, photos_cut, fileConns);
        pa.setWidth(wid_pic);
        gv.setAdapter(pa);
    }

    //添加照片
    public void setPhotos(Bitmap photo) {
        photos.add(photo);
        int pw = photo.getWidth();
        int ph = photo.getHeight();
        if (ph >= pw) {
            ph = ph * wid_pic / pw;
            pw = wid_pic;
            photo = Bitmap.createScaledBitmap(photo, pw, ph, true);
            photo = Bitmap.createBitmap(photo, 0, (ph - pw) / 2, pw, pw);
        } else {
            pw = pw * wid_pic / ph;
            ph = wid_pic;
            photo = Bitmap.createScaledBitmap(photo, pw, ph, true);
            photo = Bitmap.createBitmap(photo, (pw - ph) / 2, 0, ph, ph);
        }
        photos_cut.add(photo);
    }

    private void showPhoto(int position) {

        //初始化Builder
        builder = new AlertDialog.Builder(AlbumActivity.this);
        show_photo = new ImageView(this);
        //show_photo.setLayoutParams(new GridView.LayoutParams((int) (width * 0.72), (int) (width * 1.28)));
        show_photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //show_photo.setPadding(0, 0, 0, 0);//设置间距
        show_photo.setImageBitmap(photos.get(position));
        builder.setView(show_photo);
        builder.setCancelable(true);
        alert = builder.create();

        show_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        alert.show();
    }

    private void deletePhoto(final int position) {

        bd2 = new AlertDialog.Builder(AlbumActivity.this);
        bd2.setMessage("Do you want to delete this photo?");
        bd2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        bd2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("Delete " + fileConns.get(position));
                new File(fileConns.get(position)).delete();
                fileConns.remove(position);
                photos.remove(position);
                photos_cut.remove(position);
                gv.setAdapter(pa);
            }
        });

        al2 = bd2.create();
        al2.show();

    }
}
