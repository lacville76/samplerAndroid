package com.ms.activities;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


import com.ms.utils.FileArrayAdapter;
import com.ms.utils.ItemList;

import android.os.Bundle;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.view.DragEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class TrackSelectActivity extends Activity {
	private File currentDir;
	String path;
	private FileArrayAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentDir = new File("/sdcard/");
		setContentView(R.layout.activity_track_select);
		fill(currentDir);
		Context context = TrackSelectActivity.this;
	}
	private void fill(File f) {
		final ListView listview = (ListView) findViewById(R.id.listView1);
		File[] dirs = f.listFiles();
		
		this.setTitle("Current Dir: " + f.getName());
		List<ItemList> dir = new ArrayList<ItemList>();
		List<ItemList> fls = new ArrayList<ItemList>();
		try {
			for (File ff : dirs) {
				Date lastModDate = new Date(ff.lastModified());
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if (ff.isDirectory()) {

					File[] fbuf = ff.listFiles();
					int buf = 0;
					if (fbuf != null) {
						buf = fbuf.length;
					} else
						buf = 0;
					String num_item = String.valueOf(buf);
					if (buf == 0)
						num_item = num_item + " item";
					else
						num_item = num_item + " items";

					// String formated = lastModDate.toString();
					dir.add(new ItemList(ff.getName(), num_item, date_modify,
							ff.getAbsolutePath(), "directory_icon"));
				} else {

					String something = ff.getName();

					String extension = something.substring(something
							.lastIndexOf(".") + 1);

					if (extension.equals("txt") || extension.equals("mp3")
							|| extension.equals("wav")) {
						fls.add(new ItemList(ff.getName(), ff.length()
								+ " Byte", date_modify, ff.getAbsolutePath(),
								"file_icon"));
					}
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!f.getName().equalsIgnoreCase("sdcard"))
			dir.add(0, new ItemList("..", "Parent Directory", "",
					f.getParent(), "directory_up"));
		adapter = new FileArrayAdapter(TrackSelectActivity.this,
				R.layout.editlist_layout, dir);
		listview.setAdapter(adapter);


		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {

				ItemList o = adapter.getItem(position);
				if (o.getImage().equalsIgnoreCase("directory_icon")
						|| o.getImage().equalsIgnoreCase("directory_up")) {
					currentDir = new File(o.getPath());
					fill(currentDir);
				} else {
					onFileClick(o);
				}
			}

		});
	}
	
	private void onFileClick(ItemList o) {
		 Toast.makeText(this, "Folder Clicked: "+ currentDir,
		 Toast.LENGTH_LONG).show();
		Intent intent = new Intent(TrackSelectActivity.this, EditActivity.class);
		intent.putExtra("GetPath", currentDir.toString());
		intent.putExtra("GetFileName", o.getName());
		
		path=currentDir.toString()+"/"+o.getName();
		intent.putExtra("GetFile", path);
		setResult(RESULT_OK, intent);
		TrackSelectActivity.this.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.track_select, menu);
		return true;
	}

}
