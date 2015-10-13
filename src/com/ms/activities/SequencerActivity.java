package com.ms.activities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import com.ms.sequencer.*;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.os.Handler;

import android.os.SystemClock;

import android.text.Layout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ms.utils.JSONSave;
import com.ms.utils.SQLiteHelper;
import com.ms.utils.OnSwipeTouchListener;
import com.ms.visualizer.*;
import com.triggertrap.seekarc.SeekArc;
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener;

import android.widget.*;

public class SequencerActivity extends Activity {

	// ////////////////////////////////////////////

	public static int TOTAL_BEATS = 40;
	public static final int TOTAL_SAMPLES = 16;
	int frequency = 8000;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private RealDoubleFFT transformer;
	int blockSize = 256;
	Sequencer[] sequencer;
	JSONSave writer;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;
	int priority = 1;
	int no_loop = 0;
	private int volume;
	int Seq_index;
	float normal_playback_rate = 1f;
	private Button startButton;
	private Button resetButton;
	private Button saveButton;
	private Button loadButton;
	private SeekArc seekArc;
	private Button clearButton;
	private TextView timerValue;
	private TextView padCounter;
	private TextView txtChannels;
	private ProgressBar progressBar1;
	FrameLayout frl;
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
	Button playedButton;
	private long startTime = 0L;
	int progressTime = 0;
	private Handler customHandler = new Handler();
	private Handler customHandler2 = new Handler();
	String[] paths;
	int beatTime;
	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;
	Boolean[][][] toggled;
	int pad = 0;
	int update;
	int volume_modifier;
	RecordAudio recordTask;
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	boolean started = true;
	private ProgressDialog progressDialog;

	// ////////////////////////////////////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		writer = new JSONSave();
		soundPool = new SoundPool(17, AudioManager.STREAM_MUSIC, 100);
		seekArc = (SeekArc) findViewById(R.id.seekArc);
		Seq_index = 0;
		// soundPoolMap = new HashMap<Integer, Integer>();
		sequencer = new Sequencer[4];
		for (int i = 0; i < 3; i++) {
			sequencer[i] = new Sequencer(this, TOTAL_SAMPLES, TOTAL_BEATS, this);
		}
		AudioManager audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);

		transformer = new RealDoubleFFT(blockSize);

		// ////////////////////////////////////////////
		imageView = (ImageView) this.findViewById(R.id.imageView1);
		toggled = new Boolean[4][17][TOTAL_BEATS];
		volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

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
		timerValue = (TextView) findViewById(R.id.timerValue);
		padCounter = (TextView) findViewById(R.id.padstxt);
		txtChannels = (TextView) findViewById(R.id.txtChannels);

		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		startButton = (Button) findViewById(R.id.startButton);
		saveButton = (Button) findViewById(R.id.saveButton);
		clearButton = (Button) findViewById(R.id.clearButton);
		loadButton = (Button) findViewById(R.id.loadButton);
		final Context context = SequencerActivity.this;
		View v = (View) findViewById(R.id.myView);

		// ////////////////////////////////////////////
		seekArc.setProgress(100);
		bitmap = Bitmap.createBitmap((int) 300, (int) 100,
				Bitmap.Config.ARGB_8888);

		canvas = new Canvas(bitmap);

		paint = new Paint();
		paint.setColor(Color.BLACK);
		imageView.setImageBitmap(bitmap);

		paths = new String[17];
		SQLiteHelper db = new SQLiteHelper(this);
		update = 0;
		String DB_PATH;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			DB_PATH = context.getFilesDir().getAbsolutePath()
					.replace("files", "databases")
					+ File.separator;
		} else {
			DB_PATH = context.getFilesDir().getPath()
					+ context.getPackageName() + "/databases/";
		}
		try {
			db.writeToSD(DB_PATH);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			LoadViewTask task = new LoadViewTask();
			task.execute();

		} catch (Exception e) {
			db.createTable();
		}
		for (int i = 0; i < 3; i++) {
			beatTime = (60 * 1000) / sequencer[i].getBpm();
		}

		progressTime = TOTAL_BEATS * beatTime;
		recordTask = new RecordAudio();
		recordTask.execute();
		seekArc.setOnSeekArcChangeListener(new OnSeekArcChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekArc seekArc) {
			}

			@Override
			public void onStartTrackingTouch(SeekArc seekArc) {
			}

			@Override
			public void onProgressChanged(SeekArc seekArc, int progress,
					boolean fromUser) {
				// SeekArcProgress.setText(String.valueOf(progress));
				volume_modifier = progress / 100;
				sequencer[Seq_index].setVolume(volume_modifier);
			}
		});

		v.setOnTouchListener(new OnSwipeTouchListener(context) {

			@Override
			public void onSwipeRight() {

			}

			@Override
			public void onSwipeLeft() {
				started=false;
				Intent subActivity = new Intent(SequencerActivity.this.getApplicationContext(),
						LibraryActivity.class);
				// The enter/exit animations for the two activities are
				// specified by xml resources

				Bundle translateBundle = ActivityOptions.makeCustomAnimation(
						SequencerActivity.this.getApplicationContext(), R.anim.slide_in_left,
						R.anim.slide_out_left).toBundle();
		
				startActivity(subActivity, translateBundle);
				
			}

		});

		OnClickListener onClickListener = new OnClickListener() {
			int index = 0;

			@Override
			public void onClick(View v) {

				switch (v.getId()) {
				case R.id.Button01:
					index = 1;
					// play(paths[1], index);
					if (toggled[Seq_index][1][update] == false)
						sequencer[Seq_index].enableCell(1, update);
					else
						sequencer[Seq_index].disableCell(1, update);
					break;
				case R.id.Button02:
					index = 2;
					// play(paths[2], index);
					if (toggled[Seq_index][2][update] == false)
						sequencer[Seq_index].enableCell(2, update);
					else
						sequencer[Seq_index].disableCell(2, update);
					break;
				case R.id.Button03:
					index = 3;
					// play(paths[3], index);
					if (toggled[Seq_index][3][update] == false)
						sequencer[Seq_index].enableCell(3, update);
					else
						sequencer[Seq_index].disableCell(3, update);
					break;
				case R.id.Button04:
					index = 4;
					// play(paths[4], index);
					if (toggled[Seq_index][4][update] == false)
						sequencer[Seq_index].enableCell(4, update);
					else
						sequencer[Seq_index].disableCell(4, update);
					break;
				case R.id.Button05:
					index = 5;
					// play(paths[5], index);
					if (toggled[Seq_index][5][update] == false)
						sequencer[Seq_index].enableCell(5, update);
					else
						sequencer[Seq_index].disableCell(5, update);
					break;
				case R.id.Button06:
					index = 6;
					// play(paths[6], index);
					if (toggled[Seq_index][6][update] == false)
						sequencer[Seq_index].enableCell(6, update);
					else
						sequencer[Seq_index].disableCell(6, update);
					break;
				case R.id.Button07:
					index = 7;
					// play(paths[7], index);
					if (toggled[Seq_index][7][update] == false)
						sequencer[Seq_index].enableCell(7, update);
					else
						sequencer[Seq_index].disableCell(7, update);
					break;
				case R.id.Button08:
					index = 8;
					// play(paths[8], index);
					if (toggled[Seq_index][8][update] == false)
						sequencer[Seq_index].enableCell(8, update);
					else
						sequencer[Seq_index].disableCell(8, update);
					break;
				case R.id.Button09:
					index = 9;
					// play(paths[9], index);
					if (toggled[Seq_index][9][update] == false)
						sequencer[Seq_index].enableCell(9, update);
					else
						sequencer[Seq_index].disableCell(9, update);
					break;
				case R.id.Button10:
					index = 10;
					// play(paths[10], index);
					if (toggled[Seq_index][10][update] == false)
						sequencer[Seq_index].enableCell(10, update);
					else
						sequencer[Seq_index].disableCell(10, update);
					break;
				case R.id.Button11:
					index = 11;
					// play(paths[11], index);
					if (toggled[Seq_index][11][update] == false)
						sequencer[Seq_index].enableCell(11, update);
					else
						sequencer[Seq_index].disableCell(11, update);
					break;
				case R.id.Button12:
					index = 12;
					// play(paths[12], index);
					if (toggled[Seq_index][12][update] == false)
						sequencer[Seq_index].enableCell(12, update);
					else
						sequencer[Seq_index].disableCell(12, update);
					break;
				case R.id.Button13:
					index = 13;
					// play(paths[13], index);
					if (toggled[Seq_index][13][update] == false)
						sequencer[Seq_index].enableCell(13, update);
					else
						sequencer[Seq_index].disableCell(13, update);
					break;
				case R.id.Button14:
					index = 14;
					// play(paths[14], index);
					if (toggled[Seq_index][14][update] == false)
						sequencer[Seq_index].enableCell(14, update);
					else
						sequencer[Seq_index].disableCell(14, update);
					break;
				case R.id.Button15:
					index = 15;
					// play(paths[15], index);
					if (toggled[Seq_index][15][update] == false)
						sequencer[Seq_index].enableCell(15, update);
					else
						sequencer[Seq_index].disableCell(15, update);
					break;
				case R.id.Button16:
					index = 16;
					play(paths[16], index);
					if (toggled[Seq_index][16][update] == false)
						sequencer[Seq_index].enableCell(16, update);
					else
						sequencer[Seq_index].disableCell(16, update);
					break;
				}

			}
		};

		bt1.setOnClickListener(onClickListener);
		bt2.setOnClickListener(onClickListener);
		bt3.setOnClickListener(onClickListener);
		bt4.setOnClickListener(onClickListener);
		bt5.setOnClickListener(onClickListener);
		bt6.setOnClickListener(onClickListener);
		bt7.setOnClickListener(onClickListener);
		bt8.setOnClickListener(onClickListener);
		bt9.setOnClickListener(onClickListener);
		bt10.setOnClickListener(onClickListener);
		bt11.setOnClickListener(onClickListener);
		bt12.setOnClickListener(onClickListener);
		bt13.setOnClickListener(onClickListener);
		bt14.setOnClickListener(onClickListener);
		bt15.setOnClickListener(onClickListener);
		bt16.setOnClickListener(onClickListener);

		loadButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				registerForContextMenu(loadButton);
				openContextMenu(loadButton);

			}
		});

		startButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				for (int i = 0; i < 3; i++) {
					sequencer[i].play();
				}
				startTime = SystemClock.uptimeMillis();
				customHandler.postDelayed(updateTimerThread, 0);
				if (started == false) {
					started = true;

				}
			}

		});

		clearButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				for (int i = 1; i < 17; i++) {

					for (int y = 0; y < TOTAL_BEATS; y++) {
						toggled[Seq_index][i][y] = false;
						sequencer[Seq_index].disableCell(i, y);
					}
				}
			}
		});

		saveButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				showInputDialog(view);

			}
		});

		resetButton = (Button) findViewById(R.id.resetButton);
		resetButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				progressBar1.setProgress(0);
				timeSwapBuff = 0;
				startTime = 0;
				updatedTime = 0;
				customHandler.removeCallbacks(updateTimerThread);
				timerValue.setText("00:00:00");
				update = 0;

				for (int i = 0; i < 3; i++) {
					sequencer[i].stop();
					;
				}
				// recordTask.cancel(true);
			}
		});

	}

	public void onClick(View v) {
		showInputDialog(v);
	}

	private Runnable updateTimerThread = new Runnable() {
		int progress;
		Button bt;

		public void run() {
			
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

			updatedTime = timeSwapBuff + timeInMilliseconds;

			update = sequencer[Seq_index].getCount();

			padCounter.setText(String.valueOf(update));

			int secs = (int) (updatedTime / 1000);
			progress = (int) ((updatedTime * 100) / progressTime);
			int mins = secs / 60;
			secs = secs % 60;

			int milliseconds = (int) (updatedTime % 1000);
			timerValue.setText("" + mins + ":" + String.format("%02d", secs)
					+ ":" + String.format("%03d", milliseconds));

			progressBar1.setProgress(progress);
			if (progress >= 100) {

				progressBar1.setProgress(0);
				timeSwapBuff = 0;
				timeInMilliseconds = 0;
				startTime = SystemClock.uptimeMillis();

				updatedTime = 0;
				customHandler.removeCallbacks(updateTimerThread);
				timerValue.setText("00:00:00");
			}
			customHandler.postDelayed(this, 0);

		}

	};

	@Override
	public void finish() {
		super.finish();
		recordTask.cancel(true);
		
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}

	public void initializePaths() {
		SQLiteHelper db = new SQLiteHelper(this);
		for (int i = 0; i < 17; i++) {
			paths[i] = new String();

		}
		for (int z = 0; z < 3; z++) {
			for (int i = 1; i < 17; i++) {
				paths[i] = db.getTrack(i).getSource();
				if (!paths[i].equals("null")) {
					// soundPoolMap.put(i, soundPool.load(paths[i], 1));
					if (z == 0)
						sequencer[0].setSample(i, paths[i]);

				}
				for (int y = 0; y < TOTAL_BEATS; y++) {
					toggled[z][i][y] = new Boolean(false);

				}
				if (z == 1)
					sequencer[1] = sequencer[0];
				if (z == 2)
					sequencer[2] = sequencer[1];
			}

		}

	}

	public void play(String path, int index) {
		if (!path.equals("null")) {
			soundPool.play(index, AudioManager.STREAM_MUSIC,
					AudioManager.STREAM_MUSIC, priority, no_loop,
					normal_playback_rate);

		}

	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		File sdCard = Environment.getExternalStorageDirectory();
		if (v.getId() == R.id.loadButton) {
			try {
				menu.setHeaderTitle("Wybierz track do wczytania");
				File dir = new File(sdCard.getAbsolutePath() + "/SavedTracks");

				File[] listOfFiles = dir.listFiles();

				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						menu.add(0, v.getId(), 0,
								("File " + listOfFiles[i].getName()));
					} else if (listOfFiles[i].isDirectory()) {
						System.out.println("Directory "
								+ listOfFiles[i].getName());
					}
				}

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

	}

	protected void showInputDialog(final View v) {
		final Context context = this;
		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View promptView = null;
		if (v == findViewById(R.id.saveButton))
			promptView = layoutInflater.inflate(R.layout.input_dialog1, null);
		else if (v == findViewById(R.id.timerValue))
			promptView = layoutInflater.inflate(R.layout.input_dialog2, null);
		else if (v == findViewById(R.id.txtChannels))
			promptView = layoutInflater.inflate(R.layout.input_dialog3, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		// set prompts.xml to be the layout file of the alertdialog builder
		alertDialogBuilder.setView(promptView);

		final EditText input = (EditText) promptView
				.findViewById(R.id.userInput);
		final EditText input1 = (EditText) promptView
				.findViewById(R.id.secondsTxt);
		final EditText input2 = (EditText) promptView
				.findViewById(R.id.msecondsTxt);
		final NumberPicker nrPicker = (NumberPicker) promptView
				.findViewById(R.id.numberPicker1);
		if (v == findViewById(R.id.txtChannels))

		{
			nrPicker.setMaxValue(3);
			nrPicker.setMinValue(1);
		}
		// setup a dialog window
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String filename = new String();
						// get user input and set it to result
						if (v == findViewById(R.id.saveButton)) {
							filename = input.getText().toString();
							try {
								writer.writeJSON(sequencer[Seq_index].getBpm(),
										beatTime, TOTAL_BEATS, paths, toggled,
										filename);
								// Toast.makeText(getApplicationContext(),
								// "zapis powiód³ siê w "+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
								// + " /filename.json",
								// Toast.LENGTH_LONG).show();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								// Toast.makeText(getApplicationContext(),
								// "zapis nie uda³ sie",
								// Toast.LENGTH_LONG).show();

							}

						} else if (v == findViewById(R.id.timerValue)) {
							SequencerActivity activity = new SequencerActivity();
							int seconds = Integer.valueOf(input1.getText()
									.toString());
							int mseconds = Integer.valueOf(input2.getText()
									.toString());
							progressTime = seconds * 1000 + mseconds * 100;
							TOTAL_BEATS = progressTime / beatTime;
							progressTime = TOTAL_BEATS * beatTime;
							update = 0;
							progressBar1.setProgress(0);
							timeSwapBuff = 0;
							startTime = 0;
							updatedTime = 0;
							customHandler.removeCallbacks(updateTimerThread);
							timerValue.setText("00:00:00");
							update = 0;
							toggled = null;
							toggled = new Boolean[4][17][TOTAL_BEATS];
							for (int z = 0; z < 3; z++) {
								sequencer[z].stop();

								sequencer[z] = new Sequencer(context,
										TOTAL_SAMPLES, TOTAL_BEATS, activity);
								for (int i = 1; i < 17; i++) {
									for (int y = 0; y < TOTAL_BEATS; y++) {

										toggled[z][i][y] = new Boolean(false);

									}
								}
							}
							initializePaths();

						} else if (v == findViewById(R.id.txtChannels)) {
							Seq_index = nrPicker.getValue() - 1;
							txtChannels.setText("Channel:"
									+ String.valueOf(nrPicker.getValue()));
						}

					}

				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create an alert dialog
		AlertDialog alertD = alertDialogBuilder.create();

		alertD.show();

	}

	public class RecordAudio extends AsyncTask<Void, double[], Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			try {

				int bufferSize = AudioRecord.getMinBufferSize(frequency,
						channelConfiguration, audioEncoding);

				AudioRecord audioRecord = new AudioRecord(
						MediaRecorder.AudioSource.MIC, frequency,
						channelConfiguration, audioEncoding, bufferSize);

				short[] buffer = new short[blockSize];
				double[] toTransform = new double[blockSize];

				audioRecord.startRecording();

				while (started) {
					int bufferReadResult = audioRecord.read(buffer, 0,
							blockSize);

					for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
						toTransform[i] = (double) buffer[i] / 32768.0; // signed
																		// 16
					} // bit
					transformer.ft(toTransform);
					publishProgress(toTransform);

				}

				audioRecord.stop();

			} catch (Throwable t) {
				t.printStackTrace();
				Log.e("AudioRecord", "Recording Failed");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(double[]... toTransform) {

			canvas.drawColor(Color.WHITE);

			for (int i = 0; i < toTransform[0].length; i++) {
				int x = i;
				int downy = (int) (100 - (toTransform[0][i] * 10));
				int upy = 100;

				canvas.drawLine(x, downy, x, upy, paint);
			}

			imageView.invalidate();

		}

	}

	private class LoadViewTask extends AsyncTask<Void, Void, Integer> {
		// Before running code in the separate thread
		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(SequencerActivity.this,
					"Loading...", "Loading application View, please wait...",
					false, false);
			// Create a new progress dialog
			// progressDialog =
			// ProgressDialog.show(SequencerActivity.this,"Loading...",
			// "Loading application View, please wait...", false, false);
		}

		// The code to be executed in a background thread.
		@Override
		protected Integer doInBackground(Void... params) {

			SQLiteHelper db = new SQLiteHelper(SequencerActivity.this);

			for (int i = 0; i < 17; i++) {
				paths[i] = new String();

			}

			for (int z = 0; z < 3; z++) {
				for (int i = 1; i < 17; i++) {
					paths[i] = db.getTrack(i).getSource();
					if (!paths[i].equals("null")) {
						// soundPoolMap.put(i, soundPool.load(paths[i], 1));
						sequencer[z].setSample(i, paths[i]);
					}
					for (int y = 0; y < TOTAL_BEATS; y++) {
						toggled[z][i][y] = new Boolean(false);

					}
				}

			}

			return 1;
		}

		// after executing the code in the thread
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			// close the progress dialog
			progressDialog.dismiss();
			// initialize the View

		}
	}

}