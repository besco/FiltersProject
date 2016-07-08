package com.sanai.filters_v_01.users;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sanai.filters_v_01.R;
import com.sanai.filters_v_01.consts.AllConsts;
import com.sanai.filters_v_01.utils.StringUTF8Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegActivity extends Activity {

    private static final String TAG = RegActivity.class.getSimpleName();

    private EditText newName   ;
    private EditText newEmail  ;
    private EditText newPass   ;
    private EditText newPhone  ;

    private ProgressDialog pDialog;

    private JSONObject jObj;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_layout);

        Button btnNewUser   = (Button) findViewById(R.id.btnNewUser);

        newName  = (EditText) findViewById(R.id.newName) ;
        newEmail = (EditText) findViewById(R.id.newEmail);
        newPass  = (EditText) findViewById(R.id.newPass) ;
        newPhone = (EditText) findViewById(R.id.newPhone);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btnNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewUser(newName.getText().toString(),
                           newEmail.getText().toString(),
                           newPass.getText().toString(),
                           newPhone.getText().toString());
            }
        });
    }

    private void addNewUser(final String name, final String email, final String pass,final String phone) {
        pDialog.setMessage("Сохранение данных ...");
        if (!pDialog.isShowing()) pDialog.show();

        StringUTF8Request strReq = new StringUTF8Request(Request.Method.POST, AllConsts.url_api + "/register", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "Register Response: " + response);

                try {
                    jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(AllConsts.TAG_ERR);
                    if (!error){
                        Toast.makeText(getApplicationContext(), jObj.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), jObj.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(AllConsts.TAG_NAME , name );
                params.put(AllConsts.TAG_PHONE, phone);
                params.put(AllConsts.TAG_EMAIL, email);
                params.put(AllConsts.TAG_PASS , pass );
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strReq);
    }


}