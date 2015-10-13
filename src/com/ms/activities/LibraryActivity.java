package com.ms.activities;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.ms.sequencer.Sequencer;
import com.ms.utils.FileArrayAdapter;
import com.ms.utils.ItemList;
import com.ms.utils.MyDragEventListener;
import com.ms.utils.SQLiteHelper;
import com.ms.utils.OnSwipeTouchListener;

public class LibraryActivity extends Activity {
	private File currentDir;
	TextView comments;
	Button bt1;
	Button bt2;
	Button bt3;
	Button bt4;
	Button bt5;
	Button bt6;
	Button bt7;
	Button bt8;
	Button bt9;
	Button bt10;
	Button bt11;
	Button bt12;
	Button bt13;
	Button bt14;
	Button bt15;
	Button bt16;
	TextView comments1;
	String path;
	private FileArrayAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentDir = new File("/sdcard/");
		setContentView(R.layout.activity_library);
		bt1 = (Button) findViewById(R.id.Button01);
		bt2 = (Button) findViewById(R.id.Button02);
		bt3 = (Button) findViewById(R.id.Button03);
		bt4 = (Button) findViewById(R.id.Button04);
		bt5 = (Button) findViewById(R.id.Button05);
		bt6 = (Button) findViewById(R.id.Button06);
		bt7 = (Button) findViewById(R.id.Button07);
		bt8 = (Button) findViewById(R.id.Button08);
		bt9 = (Button) findViewById(R.id.Button09);
		bt10 = (Button) findViewById(R.id.Button10);
		bt11 = (Button) findViewById(R.id.Button11);
		bt12 = (Button) findViewById(R.id.Button12);
		bt13 = (Button) findViewById(R.id.Button13);
		bt14 = (Button) findViewById(R.id.Button14);
		bt15 = (Button) findViewById(R.id.Button15);
		bt16 = (Button) findViewById(R.id.Button16);
		SQLiteHelper db = new SQLiteHelper(this);

		fill(currentDir);
		Context context = LibraryActivity.this;
		View v = (View) findViewById(R.id.myView);
		comments = (TextView) findViewById(R.id.comments);
		comments1 = (TextView) findViewById(R.id.textView1);
		Toast.makeText(LibraryActivity.this, context.toString(),
				Toast.LENGTH_LONG).show();
		v.setOnTouchListener(new OnSwipeTouchListener(context) {

			@Override
			public void onSwipeRight() {
				Intent subActivity = new Intent(LibraryActivity.this,
						SequencerActivity.class);
				// The enter/exit animations for the two activities are
				// specified by xml resources
				Bundle translateBundle = ActivityOptions.makeCustomAnimation(
						LibraryActivity.this, R.anim.slide_in_right,
						R.anim.slide_out_right).toBundle();
				startActivity(subActivity, translateBundle);
				finish();
			}

			@Override
			public void onSwipeLeft() {
				Intent subActivity = new Intent(LibraryActivity.this.getApplicationContext(),
						SelectActivity.class);
				// The enter/exit animations for the two activities are
				// specified by xml resources
				Bundle translateBundle = ActivityOptions.makeCustomAnimation(
						LibraryActivity.this.getApplicationContext(), R.anim.slide_in_left,
						R.anim.slide_out_left).toBundle();
				
				startActivity(subActivity, translateBundle);
				
			}
		});
	}

	OnItemLongClickListener listSourceItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> l, View v, int position,
				long id) {
			String commentMsg = null;

			ItemList o = adapter.getItem(position);
			onFileClick(o);
			// Selected item is passed as item in dragData
			ClipData.Item item = new ClipData.Item(o.getName());

			String[] clipDescription = { ClipDescription.MIMETYPE_TEXT_PLAIN };
			ClipData dragData = new ClipData((CharSequence) v.getTag(),
					clipDescription, item);
			DragShadowBuilder myShadow = new MyDragShadowBuilder(v);

			v.startDrag(dragData, // ClipData
					myShadow, // View.DragShadowBuilder
					o.getName(), // Object myLocalState
					0); // flags

			commentMsg = v.getTag() + " : onLongClick.\n";
			comments.setText(commentMsg);

			return true;
		}
	};

	private static class MyDragShadowBuilder extends View.DragShadowBuilder {
		private static Drawable shadow;

		public MyDragShadowBuilder(View v) {
			super(v);
			Resources res = v.getResources();
			Drawable image = res.getDrawable(R.drawable.file_icon);
			shadow = image;
		}

		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			int width = getView().getResources()
					.getDrawable(R.drawable.file_icon).getIntrinsicWidth() * 2;
			int height = getView().getResources()
					.getDrawable(R.drawable.file_icon).getIntrinsicHeight() * 2;

			shadow.setBounds(0, 0, width, height);
			size.set(width, height);
			touch.set(width, height);
		}

		@Override
		public void onDrawShadow(Canvas canvas) {
			shadow.draw(canvas);
		}

	}

	private void fill(File f) {
		final ListView listview = (ListView) findViewById(R.id.listView1);
		File[] dirs = f.listFiles();
		listview.setOnItemLongClickListener(listSourceItemLongClickListener);
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
		adapter = new FileArrayAdapter(LibraryActivity.this,
				R.layout.editlist_layout, dir);
		listview.setAdapter(adapter);

		MyDragEventListener Drag = new MyDragEventListener() {
			public boolean onDrag(View v, DragEvent event) {
				final int action = event.getAction();

				String commentMsg = null;
				switch (action) {
				case DragEvent.ACTION_DRAG_STARTED:
					
					if (event.getClipDescription().hasMimeType(
							ClipDescription.MIMETYPE_TEXT_PLAIN)) {
						commentMsg += v.getTag()
								+ " : ACTION_DRAG_STARTED accepted.\n";
						comments.setText(commentMsg);
						return true; // Accept
					} else {
						commentMsg += v.getTag()
								+ " : ACTION_DRAG_STARTED rejected.\n";
						comments.setText(commentMsg);
						return false; // reject
					}
				case DragEvent.ACTION_DRAG_ENTERED:
					commentMsg += v.getTag() + " : ACTION_DRAG_ENTERED.\n";
					comments.setText(commentMsg);

					return true;
				case DragEvent.ACTION_DRAG_LOCATION:
					commentMsg += v.getTag() + " : ACTION_DRAG_LOCATION - "
							+ event.getX() + " : " + event.getY() + "\n";
					comments.setText(commentMsg);
					checkButtons(v);
					return true;
				case DragEvent.ACTION_DRAG_EXITED:
					commentMsg += v.getTag() + " : ACTION_DRAG_EXITED.\n";
					comments.setText(commentMsg);
					unCheckButtons(v);
					return true;
				case DragEvent.ACTION_DROP:
				
					ClipData.Item item = event.getClipData().getItemAt(0);

					commentMsg += v.getTag() + " : ACTION_DROP" + "\n";
					comments.setText(commentMsg);
					dropOnButtons(v, path);
				
					if (v.equals(bt1)) {
						String droppedItem = item.getText().toString();

						commentMsg += "Dropped item - " + droppedItem + "\n";
						comments.setText(commentMsg);

						return true;
					} else {

						return false;
					}

				case DragEvent.ACTION_DRAG_ENDED:
					if (event.getResult()) {
						commentMsg += v.getTag()
								+ " : ACTION_DRAG_ENDED - success.\n";
						comments.setText(commentMsg);
					} else {
						commentMsg += v.getTag()
								+ " : ACTION_DRAG_ENDED - fail.\n";
						comments.setText(commentMsg);
					}
					;

					return true;
				default: // unknown case
					commentMsg += v.getTag() + " : UNKNOWN !!!\n";
					comments.setText(commentMsg);
					return false;

				}
			}

		};
		listview.setOnDragListener(Drag);
		bt1.setOnDragListener(Drag);
		bt2.setOnDragListener(Drag);
		bt3.setOnDragListener(Drag);
		bt4.setOnDragListener(Drag);
		bt5.setOnDragListener(Drag);
		bt6.setOnDragListener(Drag);
		bt7.setOnDragListener(Drag);
		bt8.setOnDragListener(Drag);
		bt9.setOnDragListener(Drag);
		bt10.setOnDragListener(Drag);
		bt11.setOnDragListener(Drag);
		bt12.setOnDragListener(Drag);
		bt13.setOnDragListener(Drag);
		bt14.setOnDragListener(Drag);
		bt15.setOnDragListener(Drag);
		bt16.setOnDragListener(Drag);

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
		Toast.makeText(this, "Folder Clicked: " + currentDir, Toast.LENGTH_LONG)
				.show();
		
		float normal_playback_rate = 1f;
		int priority = 1;
		int no_loop = 0;
		Intent intent = new Intent();
		intent.putExtra("GetPath", currentDir.toString());
		intent.putExtra("GetFileName", o.getName());
		setResult(RESULT_OK, intent);
		path = currentDir.toString() + "/" + o.getName();
		SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
		HashMap<Integer, Integer> soundPoolMap = new HashMap<Integer, Integer>();
		
		AudioManager audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		soundPoolMap.put(1, soundPool.load(path, 1));
		soundPool.play(1, volume, volume, priority, no_loop, normal_playback_rate);

	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}

	public void checkButtons(View v) {
		if (v.equals(bt1)) {
			bt1.setPressed(true);

		}
		if (v.equals(bt2)) {
			bt2.setPressed(true);

		}
		if (v.equals(bt3)) {
			bt3.setPressed(true);

		}
		if (v.equals(bt4)) {
			bt4.setPressed(true);

		}
		if (v.equals(bt5)) {
			bt5.setPressed(true);

		}
		if (v.equals(bt6)) {
			bt6.setPressed(true);

		}
		if (v.equals(bt7)) {
			bt7.setPressed(true);

		}
		if (v.equals(bt8)) {
			bt8.setPressed(true);

		}
		if (v.equals(bt9)) {
			bt9.setPressed(true);

		}
		if (v.equals(bt10)) {
			bt10.setPressed(true);

		}
		if (v.equals(bt11)) {
			bt11.setPressed(true);

		}
		if (v.equals(bt12)) {
			bt12.setPressed(true);

		}
		if (v.equals(bt13)) {
			bt13.setPressed(true);

		}
		if (v.equals(bt14)) {
			bt14.setPressed(true);

		}
		if (v.equals(bt15)) {
			bt15.setPressed(true);

		}
		if (v.equals(bt16)) {
			bt16.setPressed(true);

		}

	}

	public void unCheckButtons(View v) {

		bt1.setPressed(false);
		bt2.setPressed(false);
		bt3.setPressed(false);
		bt4.setPressed(false);
		bt5.setPressed(false);
		bt6.setPressed(false);
		bt7.setPressed(false);
		bt8.setPressed(false);
		bt9.setPressed(false);
		bt10.setPressed(false);
		bt11.setPressed(false);
		bt12.setPressed(false);
		bt13.setPressed(false);
		bt14.setPressed(false);
		bt15.setPressed(false);
		bt16.setPressed(false);

	}

	public void dropOnButtons(View v, final String path2) {
		final SQLiteHelper db = new SQLiteHelper(this);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				LibraryActivity.this);
		alertDialogBuilder.setTitle("Wiadomoœæ");
		alertDialogBuilder
				.setMessage("Czy chcesz przypisaæ dŸwiêk do tego pada?" + path2);

		if (v.equals(bt1)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(1), path2);
						}
					});

		}
		if (v.equals(bt2)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(2), path2);
						}
					});

		}
		if (v.equals(bt3)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(3), path2);
						}
					});

		}
		if (v.equals(bt4)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(4), path2);
						}
					});

		}
		if (v.equals(bt5)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(5), path2);
						}
					});

		}
		if (v.equals(bt6)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(6), path2);
						}
					});

		}
		if (v.equals(bt7)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(7), path2);
						}
					});

		}
		if (v.equals(bt8)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(8), path2);
						}
					});

		}
		if (v.equals(bt9)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(9), path2);
						}
					});
		}
		if (v.equals(bt10)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(10), path2);
						}
					});

		}
		if (v.equals(bt11)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(11), path2);
						}
					});
		}
		if (v.equals(bt12)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(12), path2);
						}
					});
			;

		}
		if (v.equals(bt13)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(13), path2);
						}
					});
			;

		}
		if (v.equals(bt14)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(14), path2);
						}
					});
		}
		if (v.equals(bt15)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(15), path2);
						}
					});

		}
		if (v.equals(bt16)) {
			alertDialogBuilder.setPositiveButton("Tak",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							db.updateTrack(db.getTrack(16), path2);
						}
					});

		}
		alertDialogBuilder.setNegativeButton("Nie",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						return;
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
	}

}