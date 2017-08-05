package org.polaric.cluttr.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.polaric.cluttr.R;
import org.polaric.cluttr.editor.CropImageView;
import org.polaric.colorful.ColorfulActivity;

import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditActivity extends ColorfulActivity {

    public static final String IMAGE_URI_KEY="IMAGE_URI";
    private String imageUri;

    @BindView(R.id.cropImageView) CropImageView cropImageView;
    @BindView(R.id.edit_toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.edit);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        toolbar.inflateMenu(R.menu.image_edit_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.rotate_left: {
                        cropImageView.rotateImage(-90);
                        break;
                    }case R.id.rotate_right: {
                        cropImageView.rotateImage(90);
                        break;
                    }case R.id.edit_save: {
                        saveBitmap();
                        Toast.makeText(EditActivity.this, R.string.saved, Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    }
                }
                return false;
            }
        });

        imageUri = getIntent().getExtras().getString(IMAGE_URI_KEY);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri, options);

        cropImageView.setImageBitmap(bitmap);
    }

    private void saveBitmap() {
        Bitmap bmp = cropImageView.getCroppedImage();
        String filename=imageUri.substring(0, imageUri.lastIndexOf("."));
        filename+="_edit.png";
        System.out.println(imageUri);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
