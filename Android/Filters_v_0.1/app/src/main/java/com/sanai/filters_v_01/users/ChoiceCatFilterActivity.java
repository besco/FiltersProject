package com.sanai.filters_v_01.users;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.sanai.filters_v_01.utils.ApiKey;
import com.sanai.filters_v_01.utils.StringUTF8Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChoiceCatFilterActivity extends Activity {

    private static final String TAG = ChoiceCatFilterActivity.class.getSimpleName();

    private ProgressDialog pDialog;

    private ExpandableListView ExpandList ;
    ArrayList<Map<String, String>> groupData;
    ArrayList<ArrayList<Map<String, String>>> childData;
    ArrayList<Map<String, String>> childDataItem;
    Map<String, String> m;

    private JSONArray  catFilters   = null;
    private JSONArray  filterModuls = null;
    private JSONObject getFilters;
    private JSONObject getModuls;

    private TextView idGroup;
    private TextView idChild ;

    private String tempId;

    private RequestQueue requestQueue ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_cat_layout);

        requestQueue = Volley.newRequestQueue(this);

        ExpandList = (ExpandableListView) findViewById(R.id.choiseCat);

        ExpandList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    idGroup = (TextView) view.findViewById(R.id.groupId);
                    Intent about = new Intent(getApplicationContext(), AboutActivity.class);
                    about.putExtra("aboutId", idGroup.getText().toString());
                    about.putExtra("url", "/filters/one/");
                    startActivity(about);
                } else {
                    idChild = (TextView) view.findViewById(R.id.childId);
                    showAlertChoiseFilter(view);
                }
                return true;
            }
        });

        ExpandList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                idGroup = (TextView) v.findViewById(R.id.groupId);
                return false;
            }
        });


        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    public void showAlertChoiseFilter(View v){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.applyChoise);
        alertDialog.setMessage(R.string.applyChoiseFilter);

        alertDialog.setPositiveButton(R.string.txtOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent addFilter = new Intent(getApplicationContext(), UseFilterInfo.class);
                addFilter.putExtra("filterId", idGroup.getText().toString());
                addFilter.putExtra("moduleId", idChild.getText().toString());
                addFilter.putExtra("action"  , "new");
                startActivity(addFilter);
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
                about.putExtra("aboutId", idChild.getText().toString());
                about.putExtra("url", "/moduls/one/");
                startActivity(about);
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCatFilters();
    }

    private void updListFilters() {
        String groupFrom[] = new String[]{AllConsts.TAG_ID,AllConsts.TAG_TITLE};
        int groupTo[] = new int[]{R.id.groupId,R.id.groupTitle};
        String childFrom[] = new String[]{AllConsts.TAG_ID,AllConsts.TAG_TITLE};
        int childTo[] = new int[]{R.id.childId,R.id.childTitle};
        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                getApplicationContext(),
                groupData, R.layout.group_fields,
                groupFrom, groupTo,
                childData, R.layout.child_fields,
                childFrom, childTo);
        ExpandList.setAdapter(adapter);
    }

    private void getCatFilters (){
        pDialog.setMessage("Получение данных о фильтрах данной категории...");
        if (!pDialog.isShowing()) pDialog.show();

        StringUTF8Request strReq = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + "/cat_filters/" + getIntent().getStringExtra("catId"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "Category Filters Response: " + response);

                try {
                    getFilters = new JSONObject(response);
                    boolean error = getFilters.getBoolean(AllConsts.TAG_ERR);

                    if (!error){
                        //Toast.makeText(getApplicationContext(), "Получил фильтры категории", Toast.LENGTH_SHORT).show();
                        catFilters = getFilters.getJSONArray(AllConsts.TAG_FILTERS);

                        groupData     = new ArrayList<>();
                        childData     = new ArrayList<>();

                        for (int i = 0; i < catFilters.length(); i++) {

                            JSONObject f = catFilters.getJSONObject(i);

                            String id_f    = f.getString(AllConsts.TAG_ID   );
                            String title_f = f.getString(AllConsts.TAG_TITLE);

                            m = new HashMap<>();
                            m.put(AllConsts.TAG_ID   , id_f   );
                            m.put(AllConsts.TAG_TITLE, title_f);
                            groupData.add(m);

                            getOneFilterModuls(id_f);
                        }

                        updListFilters();

                    } else {
                        Toast.makeText(getApplicationContext(), getFilters.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
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

    private void getOneFilterModuls (String id_filter){
        StringUTF8Request strReqCats = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + "/filter_moduls/" + id_filter, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "One Filter Moduls Response: " + response);
                try {
                    getModuls = new JSONObject(response);
                    boolean error = getModuls.getBoolean(AllConsts.TAG_ERR);
                    if (!error){
                        //Toast.makeText(getApplicationContext(), "Получил все картриджы, подходящие данному фильтру", Toast.LENGTH_LONG).show();
                        filterModuls = getModuls.getJSONArray(AllConsts.TAG_FILTER_MODULS);
                        childDataItem = new ArrayList<>();
                        for (int j = 0; j < filterModuls.length(); j++){
                            JSONObject c = filterModuls.getJSONObject(j);

                            String id_c    = c.getString(AllConsts.TAG_ID   );
                            String title_c = c.getString(AllConsts.TAG_TITLE);

                            m = new HashMap<>();
                            m.put(AllConsts.TAG_ID   , id_c   );
                            m.put(AllConsts.TAG_TITLE, title_c);
                            childDataItem.add(m);
                        }
                        childData.add(childDataItem);

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
                Log.e(TAG, "One Filter Moduls Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization" , ApiKey.getApiKey());
                return headers;
            }
        };
        requestQueue.add(strReqCats);
    }
}
