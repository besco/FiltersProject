package com.sanai.filters_v_01.users;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sanai.filters_v_01.R;
import com.sanai.filters_v_01.consts.AllConsts;
import com.sanai.filters_v_01.utils.ApiKey;
import com.sanai.filters_v_01.utils.StringUTF8Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AboutActivity extends Activity {

    private static final String TAG = AboutActivity.class.getSimpleName();

    private TextView aboutTitle;
    private TextView aboutAbout;
    private ImageView aboutLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        aboutTitle = (TextView) findViewById(R.id.aboutTitle);
        aboutAbout = (TextView) findViewById(R.id.aboutAbout);
        aboutLogo  = (ImageView)findViewById(R.id.aboutLogo) ;

        getAboutInfo();
    }

    private void getAboutInfo(){

        StringUTF8Request strReqFirms = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + getIntent().getStringExtra("url") + getIntent().getStringExtra("aboutId"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "About Response: " + response);

                try {
                    JSONObject aboutJSON = new JSONObject(response);
                    boolean error = aboutJSON.getBoolean(AllConsts.TAG_ERR);
                    if (!error){
                        Toast.makeText(getApplicationContext(), "Получил информацию с " + getIntent().getStringExtra("url") + getIntent().getStringExtra("aboutId"), Toast.LENGTH_SHORT).show();

                        aboutTitle.setText(aboutJSON.getString("title"));
                        aboutAbout.setText(aboutJSON.getString("about"));
                        //aboutLogo.setImageURI(aboutJSON.getString("logo"));

                    } else {
                        Toast.makeText(getApplicationContext(), aboutJSON.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Info Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization" , ApiKey.getApiKey() );
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strReqFirms);
    }
}
