package com.ms.soundpack1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

public class jsonWriter {
	
	String BPM;
	String beatTime;
	String totalBeats;
	public void writeJSON(int BPM,int beatTime,int totalBeats,Boolean[][] toggle,String filename) throws JSONException {
		JSONObject object = new JSONObject();
		  FileOutputStream fos;
		this.BPM= String.valueOf(BPM);
		this.beatTime= String.valueOf(beatTime);
		this.totalBeats= String.valueOf(totalBeats);
		JSONArray[] list1 =  createArray(toggle);
		try {
			object.put("BPM", this.BPM);
			object.put("beatTime", this.beatTime);
			object.put("totalBeats", this.totalBeats);
			for (int y = 0; y < 8; y++) 
			{
				object.put("matrix", list1[y]);
			}
			Log.d("informacja", "Wysz³o");
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("informacja", "nie wysz³o");
			
		}
		Log.d("deleteTrack", object.toString());
		FileWriter fWriter;
		
	
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath() + "/SavedTracks");
	//	Log.d("TAG", sdCardFile.getPath()); //<-- check the log to make sure the path is correct.
		dir.mkdirs();
		File file = new File(dir, filename+".json");
	   
	        try {
	            fos = new FileOutputStream(file, true);

	          

	            try {
	                fWriter = new FileWriter(fos.getFD());
	                fWriter.write(object.toString());
	                fWriter.close();
	            } catch (Exception e) {
	                e.printStackTrace();
	            } finally {
	                fos.getFD().sync();
	                fos.close();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	     
	    }
		
	

	public JSONArray[] createArray(Boolean[][] toggle) throws JSONException {
		int length = 0;
		length = 8;
Boolean value;
		JSONArray[] list1 = new JSONArray[16];

		for (int i = 0; i < 16; i++) {
			list1[i]= new JSONArray();
			for (int y = 0; y < length; y++) 
			{
				
				value=toggle[i][y];
				list1[i].put(y, value);
			}

		}
		return list1;
	}
}