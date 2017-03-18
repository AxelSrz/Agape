package com.example.raula.agape;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_details);

        String title = getIntent().getStringExtra("title");
        Bitmap bitmap = PhotoLink.loadedPhotos.get(getIntent().getStringExtra("image"));

        TextView titleTextView = (TextView) findViewById(R.id.image_detail_title);
        titleTextView.setText(title);

        ImageView imageView = (ImageView) findViewById(R.id.image_detail_image);
        imageView.setImageBitmap(bitmap);
    }
}
