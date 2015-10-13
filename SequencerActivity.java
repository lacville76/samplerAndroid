package com.ms.activities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import opensl_example.opensl_example;

import org.json.JSONException;

import com.ms.sequencer.*;
import com.ms.soundpack1.MySQLiteHelper;
import com.ms.soundpack1.OnSwipeTouchListener;
import com.ms.soundpack1.jsonWriter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
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
	jsonWriter writer;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;
	int priority = 1;
	int no_loop = 0;
	private int volume;

	float normal_playback_rate = 1f;
	private ImageButton startButton;
	private Button resetButton;
	private Button saveButton;
	private Button loadButton;
	private SeekArc seekArc;
	private Button clearButton;
	private TextView timerValue;
	private TextView padCounter;
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
	Boolean[][] toggled;
	int pad = 0;
	int update;
	int volume_modifier;
	RecordAudio recordTask;
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	boolean started = true;

	// ////////////////////////////////////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		writer = new jsonWriter();
		soundPool = new SoundPool(17, AudioManager.STREAM_MUSIC, 100);
		seekArc = (SeekArc) findViewById(R.id.seekArc);
		
		soundPoolMap = new HashMap<Integer, Integer>();
		for(int i=0;i<3;i++)
		{
		sequencer[i] = new Sequencer(this, TOTAL_SAMPLES, TOTAL_BEATS,this);
		}
		AudioManager audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);

		transformer = new RealDoubleFFT(blockSize);

		// ////////////////////////////////////////////
		imageView = (ImageView) this.findViewById(R.id.imageView1);
		toggled = new Boolean[17][TOTAL_BEATS];
		volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		frl = (FrameLayout) findViewById(R.id.ProgressFrame);
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

		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		startButton = (ImageButton) findViewById(R.id.startButton);
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
		recordTask = new RecordAudio();
		recordTask.execute();
		paths = new String[17];
		MySQLiteHelper db = new MySQLiteHelper(this);
		update = 0;

		try {
			initializePaths();
		} catch (Exception e) {
			db.createTable();
		}
		beatTime = (60 * 1000) / sequencer.getBpm();
		progressTime = TOTAL_BEATS * beatTime;

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
				sequencer.setVolume(volume_modifier);
			}
		});

		v.setOnTouchListener(new OnSwipeTouchListener(context) {

			@Override
			public void onSwipeRight() {

			}

			@Override
			public void onSwipeLeft() {
				Intent subActivity = new Intent(SequencerActivity.this,
						LibraryActivity.class);
				// The enter/exit animations for the two activities are
				// specified by xml resources

				Bundle translateBundle = ActivityOptions.makeCustomAnimation(
						SequencerActivity.this, R.anim.slide_in_left,
						R.anim.slide_out_left).toBundle();
				startActivity(subActivity, translateBundle);
				finish();
			}

		});

		OnClickListener onClickListener = new OnClickListener() {
			int index = 0;

			@Override
			public void onClick(View v) {

				switch (v.getId()) {
				case R.id.Button01:
					index = 1;
				//	play(paths[1], index);
					if (toggled[1][update] == false)
						sequencer.enableCell(1, update);
					else
						sequencer.disableCell(1, update);
					break;
				case R.id.Button02:
					index = 2;
				//	play(paths[2], index);
					if (toggled[2][update] == false)
						sequencer.enableCell(2, update);
					else
						sequencer.disableCell(2, update);
					break;
				case R.id.Button03:
					index = 3;
				//	play(paths[3], index);
					if (toggled[3][update] == false)
						sequencer.enableCell(3, update);
					else
						sequencer.disableCell(3, update);
					break;
				case R.id.Button04:
					index = 4;
				//	play(paths[4], index);
					if (toggled[4][update] == false)
						sequencer.enableCell(4, update);
					else
						sequencer.disableCell(4, update);
					break;
				case R.id.Button05:
					index = 5;
				//	play(paths[5], index);
					if (toggled[5][update] == false)
						sequencer.enableCell(5, update);
					else
						sequencer.disableCell(5, update);
					break;
				case R.id.Button06:
					index = 6;
				//	play(paths[6], index);
					if (toggled[6][update] == false)
						sequencer.enableCell(6, update);
					else
						sequencer.disableCell(6, update);
					break;
				case R.id.Button07:
					index = 7;
				//	play(paths[7], index);
					if (toggled[7][update] == false)
						sequencer.enableCell(7, update);
					else
						sequencer.disableCell(7, update);
					break;
				case R.id.Button08:
					index = 8;
				//	play(paths[8], index);
					if (toggled[8][update] == false)
						sequencer.enableCell(8, update);
					else
						sequencer.disableCell(8, update);
					break;
				case R.id.Button09:
					index = 9;
				//	play(paths[9], index);
					if (toggled[9][update] == false)
						sequencer.enableCell(9, update);
					else
						sequencer.disableCell(9, update);
					break;
				case R.id.Button10:
					index = 10;
				//	play(paths[10], index);
					if (toggled[10][update] == false)
						sequencer.enableCell(10, update);
					else
						sequencer.disableCell(10, update);
					break;
				case R.id.Button11:
					index = 11;
				//	play(paths[11], index);
					if (toggled[11][update] == false)
						sequencer.enableCell(11, update);
					else
						sequencer.disableCell(11, update);
					break;
				case R.id.Button12:
					index = 12;
				//	play(paths[12], index);
					if (toggled[12][update] == false)
						sequencer.enableCell(12, update);
					else
						sequencer.disableCell(12, update);
					break;
				case R.id.Button13:
					index = 13;
				//	play(paths[13], index);
					if (toggled[13][update] == false)
						sequencer.enableCell(13, update);
					else
						sequencer.disableCell(13, update);
					break;
				case R.id.Button14:
					index = 14;
				//	play(paths[14], index);
					if (toggled[14][update] == false)
						sequencer.enableCell(14, update);
					else
						sequencer.disableCell(14, update);
					break;
				case R.id.Button15:
					index = 15;
				//	play(paths[15], index);
					if (toggled[15][update] == false)
						sequencer.enableCell(15, update);
					else
						sequencer.disableCell(15, update);
					break;
				case R.id.Button16:
					index = 16;
					play(paths[16], index);
					if (toggled[16][update] == false)
						sequencer.enableCell(16, update);
					else
						sequencer.disableCell(16, update);
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
				sequencer.play();
				startTime = SystemClock.uptimeMillis();
				customHandler.postDelayed(updateTimerThread, 0);
				if (started == false) {
					started = true;
					recordTask.execute();
				}
			}

		});



	
		clearButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				for (int i = 1; i < 17; i++) {

					for (int y = 0; y < TOTAL_BEATS; y++) {
						toggled[i][y] = false;
						sequencer.disableCell(i, y);
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
				sequencer.stop();
				recordTask.cancel(true);
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
			String pressedButton = sequencer.getActuallyPlayedButton();
		if(pressedButton!=null){	int resourceId = getResources().getIdentifier(pressedButton, "id", getPackageName());
		bt = (Button)findViewById(resourceId);
			bt.setPressed(true);
		
			bt.setPressed(false);
		}
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

			updatedTime = timeSwapBuff + timeInMilliseconds;

			update = sequencer.getCount();

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
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}

	public void initializePaths() {
		MySQLiteHelper db = new MySQLiteHelper(this);
		for (int i = 0; i < 17; i++) {
			paths[i] = new String();

		}

		for (int i = 1; i < 17; i++) {
			paths[i] = db.getTrack(i).getSource();
			if (!paths[i].equals("null")) {
				soundPoolMap.put(i, soundPool.load(paths[i], 1));
				sequencer.setSample(i, paths[i]);
			}
			for (int y = 0; y < TOTAL_BEATS; y++) {
				toggled[i][y] = new Boolean(false);

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
		View promptView;
		if(v==findViewById(R.id.saveButton))
		 promptView = layoutInflater.inflate(R.layout.input_dialog, null);
		else
		 promptView = layoutInflater.inflate(R.layout.inputdialog2, null);
		
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
		// setup a dialog window
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String filename = new String();
						// get user input and set it to result
						if(v==findViewById(R.id.saveButton))
					{	filename = input.getText().toString();
						try {
							writer.writeJSON(sequencer.getBpm(), beatTime,
									TOTAL_BEATS,paths ,toggled, filename);
							// Toast.makeText(getApplicationContext(),
							// "zapis powiód³ siê w "+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
							// + " /filename.json", Toast.LENGTH_LONG).show();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// Toast.makeText(getApplicationContext(),
							// "zapis nie uda³ sie", Toast.LENGTH_LONG).show();

						}
					
					
					}
						else
						{
							SequencerActivity activity =new SequencerActivity();
							int seconds = Integer.valueOf(input1.getText().toString());
							int mseconds = Integer.valueOf(input2.getText().toString());
							progressTime = seconds*1000+mseconds*100;
							TOTAL_BEATS=progressTime/beatTime;
							progressTime=TOTAL_BEATS*beatTime;
							update = 0;
							progressBar1.setProgress(0);
							timeSwapBuff = 0;
							startTime = 0;
							updatedTime = 0;
							customHandler.removeCallbacks(updateTimerThread);
							timerValue.setText("00:00:00");
							update = 0;
							sequencer.stop();
							recordTask.cancel(true);
							sequencer = new Sequencer(context, TOTAL_SAMPLES, TOTAL_BEATS,activity);
							
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

}