package com.sanai.filters_v_01.users;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
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
import com.sanai.filters_v_01.utils.SimpleExpandableListAdapterCustom;
import com.sanai.filters_v_01.utils.StringUTF8Request;
import com.sanai.filters_v_01.utils.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends Activity {

    private static final String TAG = UserInfoActivity.class.getSimpleName();

    private ProgressDialog pDialog;

    private ExpandableListView ExpandList ;
    ArrayList<Map<String, String>> groupData;
    ArrayList<ArrayList<Map<String, String>>> childData;
    ArrayList<Map<String, String>> childDataItem;
    Map<String, String> m;

    private JSONArray useFilters = null;
    private JSONObject getUseFilters;

    private JSONArray useFilterModuls = null;
    private JSONObject getUseFilterModuls;

    private DateFormat inputFormat  = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

    private  RequestQueue requestQueue ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_layout);

        TextView userName  = (TextView) findViewById(R.id.userName);
        TextView userEmail = (TextView) findViewById(R.id.userEmail);
        TextView userPhone = (TextView) findViewById(R.id.userPhone);
        TextView userCrDt  = (TextView) findViewById(R.id.userDate);
        ExpandList = (ExpandableListView) findViewById(R.id.useFilters);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        requestQueue = Volley.newRequestQueue(this);

        userName.setText(String.format("Имя: %s", UserInfo.getUName()));
        userEmail.setText(String.format("E-Mail: %s", UserInfo.getEmail()));
        if (UserInfo.getPhone().equals("null")){
            userPhone.setText("При регистрации Вы не указали номер телефона");
        }else userPhone.setText(String.format("Контактный телефон: %s", UserInfo.getPhone()));
        try {
            userCrDt.setText(String.format("Дата регистрации: %s", outputFormat.format(inputFormat.parse(UserInfo.getDate()))));
        } catch (ParseException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        getUseFilters();

        Button btnAddUseFilter = (Button) findViewById(R.id.btnAddUseFilter);

        btnAddUseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ChoiceCatActivity.class);
                startActivity(i);
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        getUseFilters();
//    }

    private void updListFilters() {
        String groupFrom[] = new String[] {AllConsts.TAG_ID, AllConsts.TAG_ID_FILTER, AllConsts.TAG_TITLE};
        int groupTo[]      = new int[]    {R.id.groupId    , R.id.groupIdFilter     , R.id.groupTitle};

        String childFrom[] = new String[] {AllConsts.TAG_ID, AllConsts.TAG_ABOUT};
        int childTo[]      = new int[]    {R.id.childId    , R.id.childTitle};

        SimpleExpandableListAdapterCustom adapter = new SimpleExpandableListAdapterCustom(
                getApplicationContext(),
                groupData, R.layout.group_ex_fields,
                groupFrom, groupTo,
                childData, R.layout.child_ex_fields,
                childFrom, childTo );
        ExpandList.setAdapter(adapter);
    } //КОРЯВО СОБИРАЕТСЯ ЛИСТ ПОСЛЕ ПОВТОРНОГО ОБНОВЛЕНИЯ ЗАПИСЕ

    private void getUseFilters (){
        pDialog.setMessage("Получение данных об установленных фильтрах ...");
        if (!pDialog.isShowing()) pDialog.show();

        StringUTF8Request strReq = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + "/usefilters", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "Use Filters Response: " + response);

                try {
                    getUseFilters = new JSONObject(response);
                    boolean error = getUseFilters.getBoolean(AllConsts.TAG_ERR);

                    if (!error){
                        Toast.makeText(getApplicationContext(), "Получил фильтры пользователя", Toast.LENGTH_SHORT).show();

                        useFilters = getUseFilters.getJSONArray(AllConsts.TAG_USE_FILTERS);

                        groupData     = new ArrayList<>();
                        childData     = new ArrayList<>();

                        for (int i = 0; i < useFilters.length(); i++) {
                            JSONObject c = useFilters.getJSONObject(i);

                            String id        = c.getString(AllConsts.TAG_ID);
                            String id_filter = c.getString(AllConsts.TAG_ID_FILTER);
                            String title     = c.getString(AllConsts.TAG_TITLE) ;
                            String about = "";

                            m = new HashMap<>();
                            m.put(AllConsts.TAG_ID       , id);
                            m.put(AllConsts.TAG_ID_FILTER, id_filter);
                            m.put(AllConsts.TAG_TITLE    , title);
                            groupData.add(m);

                            try{
                                about += "Установлен: " + outputFormat.format(inputFormat.parse(c.getString(AllConsts.TAG_DATEBEG))) + "\n" ;
                            } catch (ParseException e){
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            about += "По адресу: " + c.getString(AllConsts.TAG_ADDR)  ;

                            getUseFilterModulsInfo(id, about);
                        }

                        updListFilters();


                    } else {
                        Toast.makeText(getApplicationContext(), getUseFilters.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
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
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization" , ApiKey.getApiKey() );
                return headers;
            }
        };

        requestQueue.add(strReq);

    }

    private void getUseFilterModulsInfo(final String id_use_filter, final String aboutFilter){
        StringUTF8Request strReq = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + "/usemoduls/" + id_use_filter, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "Use Moduls Response: " + response);

                try {
                    getUseFilterModuls = new JSONObject(response);
                    boolean error = getUseFilterModuls.getBoolean(AllConsts.TAG_ERR);

                    if (!error){
                        useFilterModuls = getUseFilterModuls.getJSONArray(AllConsts.TAG_USE_MODULS);

                        childDataItem = new ArrayList<>();

                        for (int i = 0; i < useFilterModuls.length(); i++) {
                            JSONObject c = useFilterModuls.getJSONObject(i);

                            String id    = c.getString(AllConsts.TAG_ID);
                            String about = aboutFilter + "\n" ;
                            about += "Картиридж: " + c.getString(AllConsts.TAG_TITLE) + "\n" ;
                            try{

                                about += "Установлен: " + outputFormat.format(inputFormat.parse(c.getString(AllConsts.TAG_DATEBEG))) + "\n" ;
                                about += "Годен до: "   + outputFormat.format(inputFormat.parse(c.getString(AllConsts.TAG_DATEEND))) ;
                            } catch (ParseException e){
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            m = new HashMap<>();
                            m.put(AllConsts.TAG_ID, id);
                            m.put(AllConsts.TAG_ABOUT, about);
                            childDataItem.add(m);
                        }
                        childData.add(childDataItem);

                    } else {
                        Toast.makeText(getApplicationContext(), getUseFilterModuls.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
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
        requestQueue.add(strReq);
    }
}