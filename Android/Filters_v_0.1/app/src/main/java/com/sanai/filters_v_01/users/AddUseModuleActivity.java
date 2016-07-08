package com.sanai.filters_v_01.users;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
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
import com.sanai.filters_v_01.utils.AllNeedUtils;
import com.sanai.filters_v_01.utils.ApiKey;
import com.sanai.filters_v_01.utils.StringUTF8Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddUseModuleActivity extends Activity{
    private static final String TAG = ChoiceCatFilterActivity.class.getSimpleName();

    private ProgressDialog pDialog;

    private ExpandableListView ExpandList ;
    ArrayList<Map<String, String>> groupData;
    ArrayList<ArrayList<Map<String, String>>> childData;
    ArrayList<Map<String, String>> childDataItem;
    Map<String, String> m;

    private JSONArray  filterModuls = null;
    private JSONObject getModuls;

    private TextView idGroup;
    private TextView titleGroup;
    private TextView idChild ;
    private TextView titleChild ;

    int myDay, myMonth, myYear;
    static final int DATE_DIALOG = 0;

    private JSONObject jObj;

    private String id_module ;
    private Map<String, Integer> arrLiveTime ;

    private RequestQueue requestQueue ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_cat_layout);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        ExpandList = (ExpandableListView) findViewById(R.id.choiseCat);

        ExpandList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                idGroup = (TextView) view.findViewById(R.id.groupId);
                titleGroup = (TextView) view.findViewById(R.id.groupTitle);
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    id_module = idGroup.getText().toString();
                    showAlertChoiseModule(view);
                } else {
                }
                return true;
            }

        });

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        getFilterModuls() ;
    }

    public void showAlertChoiseModule(View v){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.applyChoise);
        alertDialog.setMessage(R.string.applyChoiseMod);

        alertDialog.setPositiveButton(R.string.txtOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Calendar cal = Calendar.getInstance();
                myDay = cal.get(Calendar.DAY_OF_MONTH);
                myMonth = cal.get(Calendar.MONTH) + 1;
                myYear = cal.get(Calendar.YEAR);

                showDialog(DATE_DIALOG);
            }
        });
        alertDialog.setNegativeButton(R.string.txtNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setNeutralButton(R.string.txtInfo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent about = new Intent(getApplicationContext(), AboutActivity.class);
                about.putExtra("aboutId", idGroup.getText().toString());
                about.putExtra("url", "/moduls/one/");
                startActivity(about);
            }
        });
        alertDialog.show();
    }

    @Override
    protected Dialog onCreateDialog (int id){
        if (id == DATE_DIALOG)
            return new DatePickerDialog(this, dpickerListener, myYear, myMonth, myDay);
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListener =
            new DatePickerDialog.OnDateSetListener(){
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
                    myYear  = year;
                    myMonth = monthOfYear;
                    myDay   = dayOfMonth;
                    if (view.isShown()) addModuleForUseFilter();
                }
            };

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
                        //Toast.makeText(getApplicationContext(), jObj.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
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

                int live_time = 0;

                for (Map.Entry entry : arrLiveTime.entrySet()) {
                    if (entry.getKey().equals(id_module)) {
                        live_time = (Integer) entry.getValue();
                        break;
                    }
                }

                try {
                    String db = AllNeedUtils.formatDate(AllNeedUtils.setStrDate(myDay, myMonth, myYear), "dd-MM-yyyy", "yyyy-MM-dd");
                    String de = AllNeedUtils.formatDate(AllNeedUtils.getDateEnd(AllNeedUtils.setStrDate(myDay, myMonth, myYear), live_time), "dd-MM-yyyy", "yyyy-MM-dd") ;
                    params.put(AllConsts.TAG_DATEBEG, db );
                    params.put(AllConsts.TAG_DATEEND, de );
                } catch (ParseException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                params.put(AllConsts.TAG_ID_MODULE    , id_module );
                params.put(AllConsts.TAG_ID_USE_FILTER, getIntent().getStringExtra(AllConsts.TAG_ID_USE_FILTER ));

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

    private void getFilterModuls () {
        pDialog.setMessage("Получение данных о подходящих картриджах...");
        if (!pDialog.isShowing()) pDialog.show();

        StringUTF8Request strReq = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + "/filter_moduls/" + getIntent().getStringExtra(AllConsts.TAG_ID_FILTER), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "Filter Moduls Response: " + response);

                try {
                    getModuls = new JSONObject(response);
                    boolean error = getModuls.getBoolean(AllConsts.TAG_ERR);

                    if (!error){
                        filterModuls = getModuls.getJSONArray(AllConsts.TAG_FILTER_MODULS);

                        groupData     = new ArrayList<>();
                        childData     = new ArrayList<>();

                        arrLiveTime = new HashMap<>();

                        for (int i = 0; i < filterModuls.length(); i++) {

                            JSONObject c = filterModuls.getJSONObject(i);

                            String id = c.getString(AllConsts.TAG_ID) ;
                            String title = c.getString(AllConsts.TAG_TITLE);
                            int live_time = c.getInt(AllConsts.TAG_LIVE_TIME);
                            String about = c.getString(AllConsts.TAG_ABOUT) ;

                            if (about.equals("null")) about = "Информация о фильтре отсутствует\n" ;
                            else about += "\n" ;
                            about += "Срок службы: " + live_time + " мес.\n" ;
                            about += "Цена: " + c.getDouble(AllConsts.TAG_PRICE) + " р.";

                            m = new HashMap<>();
                            m.put(AllConsts.TAG_ID,id);
                            m.put(AllConsts.TAG_TITLE,title);
                            groupData.add(m);

                            childDataItem = new ArrayList<>();

                            m = new HashMap<>();
                            m.put(AllConsts.TAG_ABOUT, about);
                            childDataItem.add(m);

                            childData.add(childDataItem);

                            arrLiveTime.put(id,live_time);
                        }

                        String groupFrom[] = new String[]{AllConsts.TAG_ID,AllConsts.TAG_TITLE};
                        int groupTo[] = new int[]{R.id.groupId,R.id.groupTitle};
                        String childFrom[] = new String[]{AllConsts.TAG_ABOUT};
                        int childTo[] = new int[]{R.id.childTitle};
                        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                                getApplicationContext(),
                                groupData, R.layout.group_fields,
                                groupFrom, groupTo,
                                childData, R.layout.child_fields_without_menu,
                                childFrom, childTo);
                        ExpandList.setAdapter(adapter);

                    } else {
                        Toast.makeText(getApplicationContext(), getModuls.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Category Filters Error: " + error.getMessage());
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
