package com.sanai.filters_v_01.users;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sanai.filters_v_01.R;
import com.sanai.filters_v_01.consts.AllConsts;
import com.sanai.filters_v_01.utils.AllNeedUtils;
import com.sanai.filters_v_01.utils.ApiKey;
import com.sanai.filters_v_01.utils.StringUTF8Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UseFilterInfo extends Activity{
    private static final String TAG = UseFilterInfo.class.getSimpleName();

    private EditText editAddr;
    private EditText editPhone;
    private EditText editDateBegin;
    private EditText editDateEnd;

    private ProgressDialog pDialog;

    private JSONObject jObj;

    private int live_time;
    private int live_vol ;

    private Integer id_use_filter ;

    int myDay, myMonth, myYear;
    static final int DATE_DIALOG_BEG = 0;
    static final int DATE_DIALOG_END = 1;

    private RequestQueue requestQueue ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.use_filter_layout);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        editAddr      = (EditText) findViewById(R.id.editAddr);
        editPhone     = (EditText) findViewById(R.id.editPhone);
        editDateBegin = (EditText) findViewById(R.id.editDateBegin);
        editDateEnd   = (EditText) findViewById(R.id.editDateEnd);

        Button btnSave = (Button) findViewById(R.id.btnUpdFilter);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getStringExtra("action").equals("new")) {
                    addNewUseFilter();
                }else {
                    updUseFilter();
                }

            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        final Calendar cal = Calendar.getInstance();
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myMonth = cal.get(Calendar.MONTH)+1;
        myYear = cal.get(Calendar.YEAR);

        editDateBegin.setText(AllNeedUtils.setStrDate(myDay, myMonth, myYear));

        editDateBegin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showDialog(DATE_DIALOG_BEG);
            }
        });

        editDateEnd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showDialog(DATE_DIALOG_END);
            }
        });

    }

    @Override
    protected Dialog onCreateDialog (int id){
        if (id == DATE_DIALOG_BEG)
            return new DatePickerDialog(this, dpickerListenerBeg, myYear, myMonth, myDay);
        if (id == DATE_DIALOG_END)
            return new DatePickerDialog(this, dpickerListenerEnd, myYear, myMonth, myDay);
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListenerBeg =
            new DatePickerDialog.OnDateSetListener(){
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
                    myYear  = year;
                    myMonth = monthOfYear;
                    myDay   = dayOfMonth;
                    editDateBegin.setText(AllNeedUtils.setStrDate(myDay, myMonth + 1, myYear));
                }
            };

    private DatePickerDialog.OnDateSetListener dpickerListenerEnd =
            new DatePickerDialog.OnDateSetListener(){
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
                    myYear = year;
                    myMonth = monthOfYear;
                    myDay = dayOfMonth;
                    editDateEnd.setText(AllNeedUtils.setStrDate(myDay, myMonth + 1, myYear));
                }
            };

    private void addNewUseFilter(){
        pDialog.setMessage("Сохранение данных ...");
        if (!pDialog.isShowing()) pDialog.show();

        StringUTF8Request strReq = new StringUTF8Request(Request.Method.POST, AllConsts.url_api + "/usefilters", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "Add Use Filters Response: " + response);

                try {
                    jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(AllConsts.TAG_ERR);
                    if (!error){
                        Toast.makeText(getApplicationContext(), jObj.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                        id_use_filter = jObj.getInt(AllConsts.TAG_ID_USE_FILTER);
                        getInfoOneModule();
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
                Log.e(TAG, "Add Use Filters Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                try {
                    String db = AllNeedUtils.formatDate(editDateBegin.getText().toString(), "dd-MM-yyyy", "yyyy-MM-dd" );
                    String de = editDateEnd.getText().toString();
                    if (de.equals("")) de = "";
                    else de = AllNeedUtils.formatDate(de, "dd-MM-yyyy", "yyyy-MM-dd");
                    params.put(AllConsts.TAG_DATEBEG, db );
                    params.put(AllConsts.TAG_DATEEND, de );

                } catch (ParseException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                String fid = getIntent().getStringExtra("filterId");
                params.put(AllConsts.TAG_ID_FILTER    , fid  );
                params.put(AllConsts.TAG_ADDR         , editAddr.getText().toString() );
                params.put(AllConsts.TAG_CONTACT_PHONE, editPhone.getText().toString());

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization" , ApiKey.getApiKey());
                return headers;
            }
        };

        requestQueue.add(strReq);
    }

    private void updUseFilter(){
        Toast.makeText(getApplicationContext(),"ТУТ БУДЕТ РЕДАКТИРОВАНИЕ ДАННЫХ О ФИЛЬТРЕ!!!!", Toast.LENGTH_SHORT).show();
    }

    private void addModuleForUseFilter(){
        StringUTF8Request strReq = new StringUTF8Request(Request.Method.POST, AllConsts.url_api + "/usemoduls", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "Add Module to Use Filter Response: " + response);

                try {
                    jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(AllConsts.TAG_ERR);
                    if (!error){
                        Toast.makeText(getApplicationContext(), jObj.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), UserInfoActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
                Log.e(TAG, "Add Module to Use Filter Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                try {
                    String db = AllNeedUtils.formatDate(editDateBegin.getText().toString(), "dd-MM-yyyy", "yyyy-MM-dd");
                    String de = AllNeedUtils.formatDate(AllNeedUtils.getDateEnd(editDateBegin.getText().toString(), live_time), "dd-MM-yyyy", "yyyy-MM-dd") ;
                    params.put(AllConsts.TAG_DATEBEG, db );
                    params.put(AllConsts.TAG_DATEEND, de );
                } catch (ParseException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                params.put(AllConsts.TAG_ID_MODULE    , getIntent().getStringExtra("moduleId") );
                params.put(AllConsts.TAG_ID_USE_FILTER, id_use_filter.toString() );

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization" , ApiKey.getApiKey());
                return headers;
            }
        };
        requestQueue.add(strReq);
    }

    public void getInfoOneModule(){
        StringUTF8Request strReq = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + "/moduls/one/" + getIntent().getStringExtra("moduleId"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "One Module Info Response: " + response);

                try {
                    jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(AllConsts.TAG_ERR);
                    if (!error){
                        live_time = jObj.getInt(AllConsts.TAG_LIVE_TIME);
                        live_vol  = jObj.getInt(AllConsts.TAG_LIVE_VOL );
                        addModuleForUseFilter();
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
                Log.e(TAG, "One Module Info Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization" , ApiKey.getApiKey());
                return headers;
            }
        };
        requestQueue.add(strReq) ;
    }

}
