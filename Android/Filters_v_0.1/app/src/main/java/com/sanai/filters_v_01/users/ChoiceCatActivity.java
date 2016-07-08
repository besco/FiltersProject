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

public class ChoiceCatActivity extends Activity {

    private static final String TAG = ChoiceCatActivity.class.getSimpleName();

    private ProgressDialog pDialog;

    private ExpandableListView ExpandList ;
    ArrayList<Map<String, String>> groupData;
    ArrayList<ArrayList<Map<String, String>>> childData;
    ArrayList<Map<String, String>> childDataItem;
    Map<String, String> m;

    private JSONArray firms = null;
    private JSONArray cats  = null;
    private JSONObject getFirms;
    private JSONObject getCats ;

    private TextView idGroup;
    private TextView titleGroup;
    private TextView idChild ;
    private TextView titleChild ;

    private RequestQueue requestQueue ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_cat_layout);

        requestQueue = Volley.newRequestQueue(this);

        ExpandList = (ExpandableListView) findViewById(R.id.choiseCat);

        ExpandList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                idGroup = (TextView) view.findViewById(R.id.groupId);
                titleGroup = (TextView) view.findViewById(R.id.groupTitle);

                idChild = (TextView) view.findViewById(R.id.childId);
                titleChild = (TextView) view.findViewById(R.id.childTitle);

//                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
//                    Toast.makeText(getApplicationContext(), "ГРУППА " + idGroup.getText().toString() + " " + titleGroup.getText().toString(), Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(getApplicationContext(), "ПОДГРУППА " + idChild.getText().toString() + " " + titleChild.getText().toString(), Toast.LENGTH_SHORT).show();

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    Intent about = new Intent(getApplicationContext(), AboutActivity.class);
                    about.putExtra("aboutId", idGroup.getText().toString());
                    about.putExtra("url", "/firms/");
                    startActivity(about);
                } else {
                    showAlertChoiseCat(view);
                }
                return true;
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getFirms();
    }

    private void UpdListFirms() {
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

    private void getFirms (){

        pDialog.setMessage("Получение данных о фирмах производителях ...");
        if (!pDialog.isShowing()) pDialog.show();

        StringUTF8Request strReqFirms = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + "/firms", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) pDialog.dismiss();

                Log.d(TAG, "Firms Response: " + response);

                try {
                    getFirms = new JSONObject(response);
                    boolean error = getFirms.getBoolean(AllConsts.TAG_ERR);
                    if (!error){

                        //Toast.makeText(getApplicationContext(), "Получил фирмы", Toast.LENGTH_SHORT).show();

                        firms = getFirms.getJSONArray(AllConsts.TAG_FIRMS);

                        groupData     = new ArrayList<>();
                        childData     = new ArrayList<>();

                        for (int i = 0; i < firms.length(); i++) {
                            JSONObject f = firms.getJSONObject(i);

                            String id_f    = f.getString(AllConsts.TAG_ID   );
                            String title_f = f.getString(AllConsts.TAG_TITLE);

                            m = new HashMap<>();
                            m.put(AllConsts.TAG_ID   , id_f   );
                            m.put(AllConsts.TAG_TITLE, title_f);
                            groupData.add(m);

                            getOneFirmCats(firms.getJSONObject(i).getString(AllConsts.TAG_ID));

                        }

                        UpdListFirms();

                    } else {
                        Toast.makeText(getApplicationContext(), getFirms.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
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

        requestQueue.add(strReqFirms);
    }

    private void getOneFirmCats (String id_firm){
        StringUTF8Request strReqCats = new StringUTF8Request(Request.Method.GET, AllConsts.url_api + "/firm_cats/" + id_firm, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Cats Response: " + response);
                try {
                    getCats = new JSONObject(response);
                    boolean error = getCats.getBoolean(AllConsts.TAG_ERR);
                    if (!error){
                        //Toast.makeText(getApplicationContext(), "Получил категории одной фирмы", Toast.LENGTH_LONG).show();
                        cats = getCats.getJSONArray(AllConsts.TAG_CATS);

                        childDataItem = new ArrayList<>();
                        for (int j = 0; j < cats.length(); j++){
                            JSONObject c = cats.getJSONObject(j);

                            String id_c    = c.getString(AllConsts.TAG_ID   );
                            String title_c = c.getString(AllConsts.TAG_TITLE);

                            m = new HashMap<>();
                            m.put(AllConsts.TAG_ID   , id_c   );
                            m.put(AllConsts.TAG_TITLE, title_c);
                            childDataItem.add(m);
                        }
                        childData.add(childDataItem);

                    } else {
                        Toast.makeText(getApplicationContext(), getCats.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Cats Error: " + error.getMessage());
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

    public void showAlertChoiseCat(View v){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.applyChoise);
        alertDialog.setMessage(R.string.applyChoiseCat);

        alertDialog.setPositiveButton(R.string.txtOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent catFilters = new Intent(getApplicationContext(), ChoiceCatFilterActivity.class);
                catFilters.putExtra("catId"   , idChild.getText().toString());
                catFilters.putExtra("catTitle", titleChild.getText().toString());
                startActivity(catFilters);
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
                about.putExtra("url", "/cats/");
                startActivity(about);
            }
        });
        alertDialog.show();
    }

}
