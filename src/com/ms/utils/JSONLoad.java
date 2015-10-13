package com.ms.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
public class JSONLoad {
  static InputStream is = null;
  static JSONObject jObj = null;
  static String json = "";
  String BPM;
	String beatTime;
	String totalBeats;
	String[] paths; 
	 boolean[][][] toggled ;
  // constructor
  public JSONLoad() {
  }
  public JSONObject getJSONFromUrl(String url) {
    // Making HTTP request
    try {
      // defaultHttpClient
      DefaultHttpClient httpClient = new DefaultHttpClient();
      HttpPost httpPost = new HttpPost(url);
      HttpResponse httpResponse = httpClient.execute(httpPost);
      HttpEntity httpEntity = httpResponse.getEntity();
      is = httpEntity.getContent();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          is, "iso-8859-1"), 8);
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line + "n");
      }
      is.close();
      json = sb.toString();
    } catch (Exception e) {
      Log.e("Buffer Error", "Error converting result " + e.toString());
    }
    // try parse the string to a JSON object
    try {
      jObj = new JSONObject(json);
      JSONArray JSONpaths;
      BPM = jObj.getString("BPM");
      beatTime = jObj.getString("beatTime");
      totalBeats = jObj.getString("totalBeats");
      int length = Integer.valueOf(totalBeats);
      JSONObject json_data;
    toggled = new boolean[3][16][length];
      for(int i=0; i<3; i++){
    	 
    	  for(int z=1; i<16; i++){
    		  JSONpaths =jObj.getJSONArray("matrix"+String.valueOf(i)+String.valueOf(z));
    		    for(int y=0; y<length; y++){
    		    	json_data = JSONpaths.getJSONObject(i);
    		    	toggled[i][z][y]=Boolean.getBoolean(json_data.toString());
    		    }
      	    
      	} 
    	    
    	}
      
      
    } catch (JSONException e) {
      Log.e("JSON Parser", "Error parsing data " + e.toString());
    }
    // return JSON String
    return jObj;
  }
  int getBeatTime()
  {
	  return Integer.valueOf(beatTime);
  }
  int getTotalBeats()
  {
	  return Integer.valueOf(totalBeats);
  }
   boolean[][][] getToggled()
  {
	  return toggled;
  }
  int getBPM()
  {
	  return Integer.valueOf(BPM);
  }
  
  
  
}