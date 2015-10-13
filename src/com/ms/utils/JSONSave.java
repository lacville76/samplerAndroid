package com.ms.utils;

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

public class JSONSave {
	
	String BPM;
	String beatTime;
	String totalBeats;
	int length;
	public void writeJSON(int BPM,int beatTime,int totalBeats,String[] paths, Boolean[][][] toggled,String filename) throws JSONException {
		JSONObject object = new JSONObject();
		  FileOutputStream fos;
		this.BPM= String.valueOf(BPM);
		this.beatTime= String.valueOf(beatTime);
		this.totalBeats= String.valueOf(totalBeats);
		length = totalBeats;
		JSONArray[][] listToggled =  createArray(toggled);
		JSONArray listTracks = new JSONArray();
		try {
			object.put("BPM", this.BPM);
			object.put("beatTime", this.beatTime);
			object.put("totalBeats", this.totalBeats);
		for	 (int x = 0; x < 3; x++)
		{ 
			for (int y = 1; y < 17; y++) 
			{
				object.put("matrix" + String.valueOf(x)+String.valueOf(y), listToggled[x][y]);
			}
		}
			for (int z = 1; z < 17; z++) 
			{
				if(paths[z]!="null")
				{
					listTracks =	 new JSONArray();
				listTracks.put(paths[z]);         
				object.put("paths", listTracks.get(z));
				}
				else object.put("paths", "null");
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
		
	

	public JSONArray[][] createArray(Boolean[][][] toggled) throws JSONException {
		
Boolean value;
		JSONArray[][] list1 = new JSONArray[4][17];
for(int x=0;x<3;x++){
		for (int i = 1; i < 17; i++) {
			list1[x][i]= new JSONArray();
			for (int y = 0; y < length; y++) 
			{
				
				value=toggled[x][i][y];
				list1[x][i].put(y, value);
			}

		}
}
		return list1;
	}
}