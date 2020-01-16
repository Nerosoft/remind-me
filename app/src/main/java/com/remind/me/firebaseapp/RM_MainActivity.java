package com.remind.me.firebaseapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

import static com.remind.me.firebaseapp.RM_Login.fireIDData;

public class RM_MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    RM_SharedPreference preference;
    RM_User UserLogin;
    //--------- search View -----------\\
    SearchView mSearchView;
    SearchHistoryTable mHistoryDatabase;
    SearchAdapter searchAdapter;
    List<SearchItem> suggestionsList = new ArrayList<>();
    WaveSwipeRefreshLayout mWaveSwipeRefreshLayout;
    ProgressBar progressBar;
    RM_Post rm_post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rm_main);
        preference = new RM_SharedPreference(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupSearchView();
        onActivityResult(toolbar);
        setUpRecyclerView();
        setupRefresh();

    }

    private void setupRefresh() {

        progressBar=(ProgressBar)findViewById(R.id.progressbar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.MULTIPLY);
        mWaveSwipeRefreshLayout = (WaveSwipeRefreshLayout) findViewById(R.id.main_swipe);
        mWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        mWaveSwipeRefreshLayout.setWaveColor(getResources().getColor(R.color.colorPrimaryDark));
        mWaveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpRecyclerView();
            }
        });
    }

    public void logOut() {
        preference.userLogOut();
        startActivity(new Intent(RM_MainActivity.this, RM_Login.class));
        finish();
    }


    void onActivityResult(Toolbar toolbar) {
        UserLogin = preference.getUserInfo();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        final TextView UserName = (TextView) headerView.findViewById(R.id.user_name);
        CircleImageView UserPhoto = (CircleImageView) headerView.findViewById(R.id.user_photo);

        UserName.setText(UserLogin.getName() + "@rmindme.com");
        RM_SharedPreference.showSavedImage(this, UserPhoto);

        rm_post = new RM_Post(this, new ArrayList<SubUser>());
        rm_post.setupRecyclerView((RecyclerView) findViewById(R.id.recyclerview));
        new MangeSubscribeUser(UserLogin.getName(), this);

    }



    void setUpRecyclerView() {

        MangeSubscribeUser.getSubscribeUser().setUpSubscribeUser(new OnSuccessListener<JSONArray>() {
            @Override
            public void onSuccess(final JSONArray jsonArraySubUser) {

                rm_post.subPosts.clear();
                for (int i = 0; i < jsonArraySubUser.length(); i++) {
                    try {

                        final String userName = jsonArraySubUser.getString(i);

                        DatabaseReference mDatabase = FirebaseDatabase.
                                getInstance("https://"+fireIDData+".firebaseio.com").getReference();
                        Query lastQuery = mDatabase.child("UserPost").child(userName).limitToLast(1);
                        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) { // لو المستخدم مكنش رفع اي بوسط
                                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                                    final String firstKey = (String) hashMap.keySet().toArray()[0];
                                    RM_SharedPreference.loadUserPhoto(userName, new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            CircleImageView drawable = new CircleImageView(getApplicationContext());
                                            drawable.setImageBitmap(BitmapFactory.
                                                    decodeByteArray(bytes, 0, bytes.length));

                                            rm_post.subPosts.add(new SubUser(userName, drawable.getDrawable(), dataSnapshot.child(firstKey)
                                                    .getValue(UserPost.class)));
                                            rm_post.notifyDataSetChanged();

                                            if( rm_post.subPosts.size()==jsonArraySubUser.length()
                                                    && mWaveSwipeRefreshLayout.isRefreshing())
                                                mWaveSwipeRefreshLayout.setRefreshing(false);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if( mWaveSwipeRefreshLayout.isRefreshing())
                mWaveSwipeRefreshLayout.setRefreshing(false);
            else
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Do you want to exit ?")
                    .setIcon(R.drawable.exit)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }
                    )
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rm_main_activity_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.user_log_out) {
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Do you really want to log out ?")
                    .setIcon(R.drawable.warning2)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                           logOut();
                           dialog.cancel();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();


        } else if (id == R.id.Menu_search)
            if (!(progressBar.getVisibility()==View.VISIBLE))
                 mSearchView.open(true, item);
            else Snackbar.make(rm_post.getRecyclerView(),
                    "Loading. Please wait...", Snackbar.LENGTH_LONG).show();

        else if(id == R.id.action_CH){
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Do you really want to delete the history ?")
                    .setIcon(R.drawable.warning2)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ClearHistory();
                            Toast.makeText(getApplicationContext(),
                                    "History Deleted Successfully", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile_setting) {
            Bundle bundle = new Bundle();
            bundle.putString("name", this.UserLogin.getName());
            bundle.putString("state", "me");
            bundle.putString("gender", this.UserLogin.getGender());
            bundle.putString("birthday", this.UserLogin.getBirthday());
            bundle.putString("number", this.UserLogin.getNumber());
            startActivity(new Intent(this, SettingProfile.class).putExtras(bundle));
        } else if (id == R.id.nav_favorite_post) {

            startActivity(new Intent(this, FavoriatPost.class)
                    .putExtra("name", UserLogin.getName()));

        } else if (id == R.id.nav_share_post) {
            startActivity(new Intent(this, SharePost.class)
                    .putExtra("name", UserLogin.getName()));
        }
//         else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    void setupSearchView() {
        mSearchView = (SearchView) findViewById(R.id.searchView);
        getUserFromFirebase();
        setSearchView();
        mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
        mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM);
        mSearchView.setTheme(SearchView.THEME_LIGHT, true);
        //  mSearchView.setTextInput("neroSarch");


    }

    protected void setSearchView() {
        mHistoryDatabase = new SearchHistoryTable(this);

        if (mSearchView != null) {
            mSearchView.setHint("Enter the name");
            mSearchView.setVoiceText("Speak Now");
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    for (SearchItem searchItem : suggestionsList)
                        if (searchItem.get_text().equals(query)) {
                            getData(query);
                            return true;
                        }
                    Toast.makeText(getApplicationContext(), "not found !!", Toast.LENGTH_SHORT).show();
                    //mSearchView.close(false);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override
                public void onOpen() {

                }

                @Override
                public void onClose() {
                    //.....
                }
            });
            mSearchView.setOnVoiceClickListener(new SearchView.OnVoiceClickListener() {
                @Override
                public void onVoiceClick() {

                }
            });

            if (mSearchView.getAdapter() == null) {

                searchAdapter = new SearchAdapter(this, suggestionsList);
                searchAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        TextView textView = view.findViewById(R.id.textView_item_text);
                        String query = textView.getText().toString();
                        getData(query);
                        // mSearchView.close(false);
                    }
                });
                mSearchView.setAdapter(searchAdapter);
            }

        }
    }

    protected void getData(String text) {
        mHistoryDatabase.addItem(new SearchItem(text));
        mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
        mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM);
        mSearchView.setTheme(SearchView.THEME_LIGHT, true);
        mSearchView.setTextInput(text);
        goSearch(text);

    }

    void goSearch(final String SearchItem) {
        progressBar.setVisibility(View.VISIBLE);
        RM_SharedPreference.loadUserPhoto(SearchItem, new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bundle bundle = new Bundle();
                bundle.putString("name", SearchItem);
                bundle.putString("state", "??");
                SettingProfile.bytes=bytes;
                //bundle.putByteArray("img",bytes);
                //utString("img", Base64.encodeToString(bytes, 0));
                startActivity(new Intent(getApplicationContext(), SettingProfile.class).putExtras(bundle));
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    void ClearHistory() {
        mHistoryDatabase.clearDatabase();
        searchAdapter.mResultList.clear(); //   مسح للهستورع
    }

    // for sound search
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchView.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && results.size() > 0) {
                String searchWrd = results.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    if (mSearchView != null) {
                        mSearchView.setQuery(searchWrd);
                        mSearchView.setTextInput(searchWrd);
                    }
                }
            }
        }

    }

    void getUserFromFirebase() {
        new Firebase("https://"+fireIDData+".firebaseio.com/user").
                addValueEventListener(new com.firebase.client.ValueEventListener() {
                    @Override
                    public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                        suggestionsList.clear();
                        for (com.firebase.client.DataSnapshot ds : dataSnapshot.getChildren())
                            suggestionsList.add(new SearchItem(R.drawable.boy, ds.getValue(RM_User.class).getName()));


                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }


}
