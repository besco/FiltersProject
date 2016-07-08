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
import com.sanai.filters_v_01.utils.ApiKey;
import com.sanai.filters_v_01.utils.StringUTF8Request;
import com.sanai.filters_v_01.utils.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText editEmail ;
    private EditText editPass  ;
    private ProgressDialog pDialog;

    private JSONObject jObj;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnReg   = (Button) findViewById(R.id.btnReg);

        editEmail = (EditText) findViewById(R.id.editEmail);
        editPass  = (EditText) findViewById(R.id.editPass) ;

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin(editEmail.getText().toString(), editPass.getText().toString());
            }
        });


        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegActivity.class);
                startActivity(i);
            }
        });
    }

    private void checkLogin(final String email, final String password) {
        pDialog.setMessage("Проверка данных ...");
        showDialog();

        StringUTF8Request strReq = new StringUTF8Request(Request.Method.POST, AllConsts.url_api + "/login", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();

                Log.d(TAG, "Login Response: " + response);

                try {
                    jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(AllConsts.TAG_ERR);
                    if (!error){

                        new ApiKey(jObj.getString(AllConsts.TAG_APIKEY));
                        new UserInfo(response);

                        Intent i = new Intent(getApplicationContext(), UserInfoActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(),jObj.getString(AllConsts.TAG_MESS), Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(AllConsts.TAG_EMAIL, email   );
                params.put(AllConsts.TAG_PASS , password);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strReq);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}