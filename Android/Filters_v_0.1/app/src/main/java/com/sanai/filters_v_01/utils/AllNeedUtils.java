package com.sanai.filters_v_01.utils;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sanai.filters_v_01.R;
import com.sanai.filters_v_01.consts.AllConsts;
import com.sanai.filters_v_01.users.AddUseModuleActivity;
import com.sanai.filters_v_01.users.UserInfoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AllNeedUtils {

    public static void showPopupMenuForGroup(View v, final Context mContext, final String id, final String idFilter) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_for_groups);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu1:
                                //Toast.makeText(mContext, "Group One|" + id + "|" + idFilter, Toast.LENGTH_SHORT).show();
                                Intent addNew = new Intent(mContext, AddUseModuleActivity.class);
                                addNew.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                addNew.putExtra(AllConsts.TAG_ID_USE_FILTER, id);
                                addNew.putExtra(AllConsts.TAG_ID_FILTER    , idFilter);
                                mContext.startActivity(addNew);
                                return true;
                            case R.id.menu2:
                                Toast.makeText(mContext, "Group Dva|" + id + "|" + idFilter, Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.menu3:
                                Toast.makeText(mContext, "Group Tri|" + id + "|" + idFilter, Toast.LENGTH_SHORT).show();
                                delNeedUseFilter(id, mContext);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
        popupMenu.show();
    }

    public static void showPopupMenuForChild(View v, final Context mContext, final String id) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_for_child);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu1:
                        Toast.makeText(mContext, "1 " + id, Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu2:
                        Toast.makeText(mContext, "2 " + id, Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu3:
                        Toast.makeText(mContext, "3 " + id, Toast.LENGTH_SHORT).show();
                        delNeedUseModule(id, mContext);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    public static void delNeedUseModule(final String id_need_del_module, final Context mContext){
        StringUTF8Request strReq = new StringUTF8Request(Request.Method.DELETE, AllConsts.url_api + "/usemoduls/" + id_need_del_module, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);

                    if (!jObj.getBoolean(AllConsts.TAG_ERR)) {
                        Intent i = new Intent(mContext, UserInfoActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);
                    }

                    Toast.makeText(mContext, jObj.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                }catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext,error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(AllConsts.TAG_ID_MODULE, id_need_del_module );
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization" , ApiKey.getApiKey());
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(strReq);
    }

    public static void delNeedUseFilter(final String id_need_del_filter, final Context mContext){
        StringUTF8Request strReq = new StringUTF8Request(Request.Method.DELETE, AllConsts.url_api + "/usefilters/" + id_need_del_filter, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);

                    if (!jObj.getBoolean(AllConsts.TAG_ERR)) {
                        Intent i = new Intent(mContext, UserInfoActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);
                    }

                    Toast.makeText(mContext, jObj.getString(AllConsts.TAG_MESS), Toast.LENGTH_LONG).show();
                }catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext,error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(AllConsts.TAG_ID_FILTER, id_need_del_filter );
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization" , ApiKey.getApiKey());
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(strReq);
    }


    public static String formatDate (String date, String initDateFormat, String endDateFormat) throws ParseException {

        Date initDate = new SimpleDateFormat(initDateFormat).parse(date);
        SimpleDateFormat formatter = new SimpleDateFormat(endDateFormat);
        String parsedDate = formatter.format(initDate);

        return parsedDate;
    }

    public static String getDateEnd (String date, int vol) {
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(date.substring(6, 10)), Integer.parseInt(date.substring(3, 5)) + vol, Integer.parseInt(date.substring(0, 2)));
        return setStrDate(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)) ;
    }

    public static String setStrDate(int d, int m, int y){
        if (d < 10)
            if (m < 10)
                return  "0" + d + "-" + "0" + m + "-" + y ;
            else return  "0" + d + "-" + m + "-" + y ;
        else if (m < 10)
            return  d + "-" + "0" + m + "-" + y ;
        else return  d + "-" + m + "-" + y ;
    }
}
