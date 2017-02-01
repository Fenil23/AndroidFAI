package com.example.mind.androidfai;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

public class RestraurantDetails extends AppCompatActivity {

    String id;
    TextView title, address;
    SimpleDraweeView restro_banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restraurant_details);

        title = (TextView) findViewById(R.id.tv_restro_title);
        address = (TextView) findViewById(R.id.tv_restro_address);
        restro_banner = (SimpleDraweeView) findViewById(R.id.sdv_store_banner);
        getIntentData();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        if (intent != null){
            id = intent.getExtras().getString("id");
            title.setText(intent.getExtras().getString("name"));
            address.setText(intent.getExtras().getString("address"));
            restro_banner.setImageURI(intent.getExtras().getString("photo"));
        }
    }
}
