package com.sanai.filters_v_01.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

public class StringUTF8Request extends StringRequest {

    public StringUTF8Request(int method, String url, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed= new String(response.data);
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}