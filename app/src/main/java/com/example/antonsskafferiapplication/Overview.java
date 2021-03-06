package com.example.antonsskafferiapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


public class Overview extends AppCompatActivity {

    private String tableId;
    private LinearLayout layoutFoodName;
    private LinearLayout layoutFoodQuantity;
    //private ArrayList<Pair<String, String>> orderDetails = new ArrayList<>();
    private TextView tableNumberView;
    private Button goBackButton;
    private Button sendPost;
    ArrayList<OrderObj> orderObjectArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        goBackButton = findViewById(R.id.goBack);
        tableNumberView = findViewById(R.id.tableNumberOverview);
        layoutFoodName = findViewById(R.id.foodListContainer);
        layoutFoodQuantity = findViewById(R.id.numberOverviewLayout);

        Intent in = getIntent();
        orderObjectArray = in.getParcelableArrayListExtra("orderObjectArray");
        for(int i=0; i<orderObjectArray.size(); i++){
            TextView foodName = new TextView(this);
            foodName.setText(orderObjectArray.get(i).getFoodName());
            layoutFoodName.addView(foodName);

            TextView foodQuantity = new TextView(this);
            foodQuantity.setText(Integer.toString(orderObjectArray.get(i).getQuantity()));
            layoutFoodQuantity.addView(foodQuantity);

            //orderDetails.add(new Pair<String, String>(orderObjectArray.get(i).getFoodName(), Integer.toString(orderObjectArray.get(i).getQuantity())));
        }

        tableId = in.getStringExtra("tableNumber");
        tableNumberView.setText(tableId);

        sendPost = findViewById(R.id.sendOrder);
        sendPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getNoteForOrder();
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Overview.this.finish();
            }
        });

    }


    public void createMessageOrderPlaced(String title, String message){
        AlertDialog alert = new AlertDialog.Builder(Overview.this).create();
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                        Overview.this.finish();
                        Intent i = new Intent(Overview.this, ChooseTable.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                });
        alert.show();
    }

    public void getNoteForOrder(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Extra anteckningar");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Lägg till", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                sendPost.setVisibility(View.INVISIBLE);
                goBackButton.setVisibility(View.INVISIBLE);
                findViewById(R.id.progressBarOverview).setVisibility(View.VISIBLE);
                String encodedText = Uri.encode(input.getText().toString());
                new PostData().execute(extractQueryString(orderObjectArray, encodedText));
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public String getDateTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z[UTC]'",
                Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


    public ArrayList<String> extractQueryString(ArrayList<OrderObj> orders, String note){
        /*Example string
        *
        * {"active":1,"amount":5,"dateTime":"2019-02-26T10:28:50Z[UTC]","item":"Drink_2","note":"-kaloua2","prepared":1,"tableNumber":5}
        *
        * */

        String currentTimeString = getDateTime();

        ArrayList<String> queries = new ArrayList<>();

        for(OrderObj p : orders){
            String activeStatus = "1";
            if(p.getIsLunch()){
                activeStatus = "2";//If it's a lunch then the dish is already prepared
            }
            String JSONObject = "{\"active\":"+activeStatus+",\"dateTime\":\""+currentTimeString+"\",\"prepared\":1";
            JSONObject += ",\"tableNumber\":"+tableId;
            JSONObject += ",\"item\":\""+p.getFoodName()+"\"";
            JSONObject += ",\"amount\":"+p.getQuantity();
            JSONObject += ",\"note\":\""+note+"\"";
            JSONObject += "}";

            queries.add(JSONObject);
        }



        return queries;
    }


    class PostData extends AsyncTask<ArrayList<String>, Void, Void> {

        private int sendJSON(String JSONData){
            try {
                URL url = new URL("http://10.0.2.2:8080/website/webresources/api.order1");
                HttpURLConnection client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");

                client.setRequestProperty("Content-Type", "application/json");
                client.setDoInput(true);
                client.setDoOutput(true);

                OutputStream os = client.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new BufferedWriter(new OutputStreamWriter(os, "UTF-8")));
                writer.write(JSONData);

                System.out.println("JSONData: "+JSONData);

                writer.flush();
                writer.close();
                os.close();

                System.out.println("Response code: "+Integer.toString(client.getResponseCode()));
                return client.getResponseCode();



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        protected Void doInBackground(ArrayList<String>... listOfJSON) {



            int completed = 0;
            for(ArrayList<String> JSONList : listOfJSON){
                for(String json : JSONList){
                    if(sendJSON(json) == 200){
                        completed++;
                    }
                }
            }

            runOnUiThread(new Runnable(){
                public void run(){
                    createMessageOrderPlaced("Status", "Beställningen har skickats!");
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void v){

        }
    }
}
