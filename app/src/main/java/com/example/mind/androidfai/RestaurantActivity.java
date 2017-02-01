package com.example.mind.androidfai;

import android.content.Intent;

import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.appindexing.AndroidAppUri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Actions;

import java.util.ArrayList;
import java.util.List;


public class RestaurantActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private List<Restaurant> restaurantList = new ArrayList<>();
    RestaurantAdapter restaurantAdapter;

    private static final String TAG = RestaurantActivity.class.getName();
    private static final String TITLE = "Andro Restro";
    public String restaurantId="";
    private Restaurant restaurant;

    @Nullable
    @Override
    public Uri getReferrer() {

        // There is a built in function available from SDK>=22
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return super.getReferrer();
        }

        Intent intent = this.getIntent();
        Uri referrer = (Uri) intent.getExtras().get("android.intent.extra.REFERRER");
        if (referrer != null) {
            return referrer;
        }

        String referrer_name = intent.getStringExtra("android.intent.extra.REFERRER_NAME");

        if (referrer_name != null) {
            try {
                return Uri.parse(referrer_name);
            } catch (ParseException e) {
            }
        }

        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        Uri referrer = getReferrer();
        onNewIntent(getIntent());
        recyclerView = (RecyclerView) findViewById(R.id.rv_restaurant_list);

        restaurantAdapter = new RestaurantAdapter(restaurantList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(restaurantAdapter);

        prepareMovieData();

        if (referrer != null) {
            if (referrer.getScheme().equals("http") || referrer.getScheme().equals("https")) {
                // App was opened from a browser
                // host will contain the host path (e.g. www.google.com)
                String host = referrer.getHost();

            } else if (referrer.getScheme().equals("android-app")) {
                // App was opened from another app
                AndroidAppUri appUri = AndroidAppUri.newAndroidAppUri(referrer);
                String referrerPackage = appUri.getPackageName();
                if ("com.google.android.googlequicksearchbox".equals(referrerPackage)) {
                    // App was opened from the Google app
                    // host will contain the host path (e.g. www.google.com)
                    String host = appUri.getDeepLinkUri().getHost();

                } else if ("com.google.appcrawler".equals(referrerPackage)) {
                    // Make sure this is not being counted as part of app usage
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (restaurantId != null) {

            final Uri BASE_URL = Uri.parse("https://www.example.com/restaurant/");
            final String APP_URI = BASE_URL.buildUpon().appendPath(restaurantId).build().toString();

            Indexable articleToIndex = new Indexable.Builder()
                    .setName(TITLE)
                    .setUrl(APP_URI)
                    .build();

            Task<Void> task = FirebaseAppIndex.getInstance().update(articleToIndex);

            // If the Task is already complete, a call to the listener will be immediately
            // scheduled
            task.addOnSuccessListener(RestaurantActivity.this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "App Indexing API: Successfully added " + TITLE + " to index");
                }
            });

            task.addOnFailureListener(RestaurantActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "App Indexing API: Failed to add " + TITLE + " to index. " + exception.getMessage());
                }
            });

            // log the view action
            Task<Void> actionTask = FirebaseUserActions.getInstance().start(Actions.newView(TITLE, APP_URI));

            actionTask.addOnSuccessListener(RestaurantActivity.this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "App Indexing API: Successfully started view action on " + TITLE);
                }
            });

            actionTask.addOnFailureListener(RestaurantActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "App Indexing API: Failed to start view action on " + TITLE + ". "
                            + exception.getMessage());
                }
            });
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (restaurantId != null) {
            final Uri BASE_URL = Uri.parse("https://www.example.com/restaurant/");
            final String APP_URI = BASE_URL.buildUpon().appendPath(restaurantId).build().toString();

            Task<Void> actionTask = FirebaseUserActions.getInstance().end(Actions.newView(TITLE,
                    APP_URI));

            actionTask.addOnSuccessListener(RestaurantActivity.this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "App Indexing API: Successfully ended view action on " + TITLE);
                }
            });

            actionTask.addOnFailureListener(RestaurantActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "App Indexing API: Failed to end view action on " + TITLE + ". " + exception.getMessage());
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

      /*  String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String recipeId = data.substring(data.lastIndexOf("/") + 1);
            Uri contentUri = RecipeContentProvider.CONTENT_URI.buildUpon()
                    .appendPath(recipeId).build();
            showRecipe(contentUri);
        }*/

        String action = intent.getAction();
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            restaurantId = data.getLastPathSegment();
            System.out.println("data.getLastPathSegment() = " + data.getLastPathSegment());
        }
    }

    public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.MyViewHolder> {

        private List<Restaurant> restaurantList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, address;
            public SimpleDraweeView sdv_banner;
            LinearLayout ll_header;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.tv_title);
                address = (TextView) view.findViewById(R.id.tv_address);
                sdv_banner = (SimpleDraweeView) view.findViewById(R.id.sdv_banner);
                ll_header = (LinearLayout) view.findViewById(R.id.ll_header);
            }
        }

        public RestaurantAdapter(List<Restaurant> restaurantList) {
            this.restaurantList = restaurantList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_restaurant, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final Restaurant restaurant = restaurantList.get(position);
            holder.title.setText(restaurant.getName());
            holder.address.setText("@"+restaurant.getAddress());
            holder.sdv_banner.setImageURI(restaurant.getPhoto());

            if(restaurantId.equalsIgnoreCase(restaurant.getName())){
                Intent intent = new Intent(RestaurantActivity.this, RestraurantDetails.class);
                intent.putExtra("id", restaurant.getId());
                intent.putExtra("name", restaurantId);
                intent.putExtra("address", restaurant.getAddress());
                intent.putExtra("photo", restaurant.getPhoto());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            holder.ll_header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(RestaurantActivity.this, RestraurantDetails.class);
                    intent.putExtra("id", restaurant.getId());
                    intent.putExtra("name", restaurant.getName());
                    intent.putExtra("address", restaurant.getAddress());
                    intent.putExtra("photo", restaurant.getPhoto());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return restaurantList.size();
        }
    }

    private void prepareMovieData() {
        Restaurant restaurant = new Restaurant("0","Real Peprika", "Ahemedanad", "http://www.hdwallpaperspop.com/wp-content/uploads/2016/03/superb-seaside-restaurant-full-hd-wallpaper.jpeg");
        restaurantList.add(restaurant);

        Restaurant restaurant2 = new Restaurant("1","One Ten", "Ahemedanad", "http://www.hdwallpaperspop.com/wp-content/uploads/2016/03/restaurant-widescreen-wallpaper.jpg");
        restaurantList.add(restaurant2);

        Restaurant restaurant3 = new Restaurant("2","Dinnerbell", "Surat", "http://hq-wall.net/i/med_thumb/14/65/f0fc14d0c5882275796e0b7f7d78cf32.jpg");
        restaurantList.add(restaurant3);

        Restaurant restaurant4 = new Restaurant("3","Foodies Stop", "Vadodara", "http://www.dhresource.com/0x0s/f2-albu-g4-M01-2D-83-rBVaEVd8ocyAYLGCABK4xiliPtE459.jpg/modern-nature-peacock-mural-suitable-for.jpg");
        restaurantList.add(restaurant4);

        Restaurant restaurant5 = new Restaurant("4","Pizza point", "Anand", "http://cdn.paper4pc.com/images/restaurant-wallpaper-10.jpg");
        restaurantList.add(restaurant5);

        Restaurant restaurant6 = new Restaurant("5","U.S. Pizza", "Bhuj", "https://3.bp.blogspot.com/-kBl6MzsFfJY/UkbSneHXiYI/AAAAAAAAYoQ/_P3DbE_SpXA/s1600/Interior+Decorative+Design+Forest+Restaurant+HD+wallpaper.jpg");
        restaurantList.add(restaurant6);

        restaurantAdapter.notifyDataSetChanged();
    }
}
