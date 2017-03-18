package com.example.raula.agape;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class AlbumFragment extends Fragment {
    private FirebaseListAdapter<PhotoLink> adapter;
    public GridView gridView;

    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        FloatingActionButton fab =
                (FloatingActionButton)getView().findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)getView().findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseHelper.getPhotoLinkReference()
                        .push()
                        .setValue(new MessageModel(input.getText().toString(),
                                UserModel.currentUser.name)
                        );
                // Clear the input
                input.setText("");
            }
        });
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        displayPhotos();
        gridView = (GridView) getView().findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PhotoLink item = (PhotoLink) parent.getItemAtPosition(position);
                //Create intent
                Intent intent = new Intent(AlbumFragment.this.getActivity(), PhotoDetailsActivity.class);
                intent.putExtra("title", item.photoTitle);
                intent.putExtra("image", item.photoLink);

                //Start details activity
                startActivity(intent);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                final PhotoLink item = (PhotoLink) parent.getItemAtPosition(position);
                AlertDialog dialog = new AlertDialog.Builder(AlbumFragment.this.getActivity())
                        .setTitle("Delete photo")
                        .setMessage("Are you sure you want to delete "+item.photoTitle+"?")
                        .setPositiveButton("Delete photo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseHelper.getPhotoLinkReference().child(view.getTag().toString())
                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseHelper.getPhotoFolderRef().child(item.photoLink).delete();
                                        PhotoLink.loadedPhotos.remove(item.photoLink);
                                    }
                                }
                                );
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                return true;
            }
        });
}

    private void displayPhotos() {

        GridView gridOfPhotos = (GridView) getView().findViewById(R.id.gridView);

        adapter = new FirebaseListAdapter<PhotoLink>(this.getActivity(), PhotoLink.class,
                R.layout.grid_item_layout, FirebaseHelper.getPhotoLinkReference()) {
            @Override
            protected void populateView(View v, final PhotoLink model, int position) {
                // Get references to the views of message.xml
                final ImageView imageView = (ImageView)v.findViewById(R.id.image_downloaded);
                final TextView imageTitle = (TextView)v.findViewById(R.id.image_title);

                imageView.setImageDrawable(null);
                imageTitle.setText(model.photoTitle);
                v.setTag(this.getRef(position).getKey());

                Glide.with(AlbumFragment.this)
                        .using(new FirebaseImageLoader())
                        .load(FirebaseHelper.getPhotoFolderRef().child(model.photoLink))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                int nh = (int) ( resource.getHeight() * (512.0 / resource.getWidth()) );
                                Bitmap scaled = Bitmap.createScaledBitmap(resource, 512, nh, true);

                                PhotoLink.loadedPhotos.put(model.photoLink, scaled);

                                imageView.setImageBitmap(scaled);
                            }
                        });

            }
        };

        gridOfPhotos.setAdapter(adapter);
    }
}
