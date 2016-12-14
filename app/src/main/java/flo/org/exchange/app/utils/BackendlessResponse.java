package flo.org.exchange.app.utils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mayur on 14/12/16.
 */

public class BackendlessResponse {
    public int offset;
    public String nextPage;
    public int totalObjects;
    public ArrayList<JSONObject> data = new ArrayList<JSONObject>();
}
