package com.example.antonsskafferiapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ShowFinishedOrders extends AppCompatActivity implements DocumentCallBack{

    private LinearLayout orderCardLayout;
    private ExtractOrder orderExtracter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_finished_orders);

        orderExtracter = new ExtractOrder();
        orderCardLayout = findViewById(R.id.finishedOrdersLayout);

        new DatabaseRequest(this).execute("http://10.0.2.2:8080/website/webresources/api.order1");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new DatabaseRequest(ShowFinishedOrders.this).execute("http://10.0.2.2:8080/website/webresources/api.order1");
            }
        }, 2000, 2000);
    }


    public void createCard(HashMap<String, OrderData> orders){
        Set<String> keys = orders.keySet();
        Iterator it = orders.entrySet().iterator();
        while(it.hasNext()){
            HashMap.Entry pair = (HashMap.Entry)it.next();
            KitchenOrderCard orderCard = new KitchenOrderCard(this, ((OrderData) pair.getValue()).getTableNumber(), (OrderData) pair.getValue(), "Har levererats", "0");
            orderCardLayout.setPadding(20,20,20,20);
            orderCardLayout.addView(orderCard);
        }
    }

    @Override
    public void callBackDocument(JSONArray jsonArr) {
        orderCardLayout.removeAllViews();
        createCard(orderExtracter.extractOrders(jsonArr, "2"));
    }

}
