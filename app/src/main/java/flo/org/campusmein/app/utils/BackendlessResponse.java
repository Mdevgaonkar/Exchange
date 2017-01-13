package flo.org.campusmein.app.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mayur on 14/12/16.
 */

public class BackendlessResponse {
    public int offset;
    public String nextPage;
    public int totalObjects;
    public JSONArray data;
}
