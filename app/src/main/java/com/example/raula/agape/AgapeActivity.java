package com.example.raula.agape;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AgapeActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fabAlbum;

    CharSequence photoOptions[] = new CharSequence[] {"Take photo", "Choose from gallery",};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agape);

        auth = FirebaseHelper.getAuthenticator();
        fabAlbum = (FloatingActionButton) findViewById(R.id.fab_album);
        fabAlbum.setVisibility(View.GONE);
        fabAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                AlertDialog.Builder builder = new AlertDialog.Builder(AgapeActivity.this);
                builder.setTitle("Choose an action");
                builder.setItems(photoOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);//zero can be replaced with any action code
                        }else {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
                        }
                    }
                });
                builder.show();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int visibility;
                if (position == 2){
                    visibility = View.VISIBLE;
                } else{
                    visibility = View.GONE;
                }
                fabAlbum.setVisibility(visibility);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, AccountSettingsActivity.class));
                return true;
            case R.id.action_logout:
                auth.signOut();
                startActivity(new Intent(AgapeActivity.this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatFragment(), "Chat");
        adapter.addFragment(new TodoFragment(), "To-do list");
        adapter.addFragment(new AlbumFragment(), "Album");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void deleteTodo(View view) {
        String id = (String) view.getTag();

        FirebaseHelper.getTodoReference().child(id).removeValue();
    }

    protected void onActivityResult(int requestCode, int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == RESULT_OK){
            final EditText taskPhotoTitleText = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Upload photo")
                    .setMessage("Please set a title for this photo")
                    .setView(taskPhotoTitleText)
                    .setPositiveButton("Set title", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String title = String.valueOf(taskPhotoTitleText.getText());
                            String id = UUID.randomUUID().toString()+".jpg";
                            final PhotoLink photo = new PhotoLink(id, title);
                            StorageReference newRef = FirebaseHelper.getPhotoFolderRef().child(id);
                            UploadTask task = newRef.putFile(imageReturnedIntent.getData());
                            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    FirebaseHelper.getPhotoLinkReference().push().setValue(photo);
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();


        }
    }
}
