
package com.ms.sequencer;



import com.ms.activities.R;
import com.ms.activities.SequencerActivity;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.widget.Button;


 
public class Sequencer {
    // attributes
    private int rows; // no. of samples

    private int beats; // no. of time divisions

    private int[] samples; // array of samples

    private int bpm;
    
    private int count;
    
    private SoundPool sound;

    private Context context;

    private Runnable playback;

    private boolean playing = false;

    private float volume = 1.0f;

    private Matrix matrix;

    private String actuallyPlayedButton;
   
    Button PressesedButton;

	private static Activity act;
  
    public Sequencer(Context ctx) {
        this(ctx, 3, 4, act);
    }

   
    public Sequencer(Context ctx, int nsamples, int nbeats, Activity SeqActivity) {
        context = ctx;
        rows = nsamples+1;
        beats = nbeats;
        bpm = 340;
        samples = new int[nsamples+1];
        sound = new SoundPool(nsamples+1, AudioManager.STREAM_MUSIC, 0);
        matrix = new Matrix(ctx, rows, beats);
        actuallyPlayedButton="";
        this.act=SeqActivity;
    }

  
    public void setSample(int id, int sampleSrc) {
        samples[id] = sound.load(context, sampleSrc, 1);
    }

    public void setSample(int id, String path) {
        samples[id] = sound.load(path, 1);
    }

    
    public void enableCell(int sampleId, int beatId) {
        matrix.setCellValue(sampleId, beatId, 1);
    }


    public void disableCell(int sampleId, int beatId) {
        matrix.setCellValue(sampleId, beatId, 0);
    }

 

  

    public int getBpm() {
        return bpm;
    }
    public String getActuallyPlayedButton() {
        if(!actuallyPlayedButton.isEmpty())
    	return actuallyPlayedButton;
        else
        	return null;
    }
    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public void addColumns(int ncol) {
        this.beats = this.beats + ncol;
    }

    public void deleteColumns(int ncol) {
        this.beats = this.beats - ncol;
    }
    
    /**
     * Start the playback. This function goes through an infinite loop (until it
     * is stopped using the stop() method). The matrix of samples and beats is
     * divided by the number of beats. On each iteration, a BPM callback is sent
     * back to the objects that were registered on the OnBPMListener().
     */
    public void play() {
        // play sound periodically
        playback = new Runnable() {
            int count = 0;
         
            public void run() {
      synchronized(this){
                while (playing) {
                	
                    long millis = System.currentTimeMillis();
                    for (int i = 1; i < rows; i++) {
                       
                        if (matrix.getCellValue(i, count) != 0)
                        { sound.play(samples[i], volume, volume, 1, 0, 1);
                       actuallyPlayedButton=setButtonString(i);
               
                        }
                    }

                    count = (count + 1) % beats;
                    setCount(count);
                    long next = (60 * 1000) / bpm;
                   
                    try {
                        Thread.sleep(next - (System.currentTimeMillis() - millis));
                        
                    } catch (InterruptedException e) {
                       
                        e.printStackTrace();
                    }
                }
                }
            }
        };

        playing = true;
        Thread thandler = new Thread(playback);
        thandler.start();
    }

    /**
     * Stop the playback.
     */
    public void stop() {
        playing = false;
    }

     public int getCount()
     {
		return count;
     }
    	 
     public void setCount(int nr)
     {
    	 this.count=nr;
     }
     
    public String setButtonString(int i)
    {
    	String idButton = String.valueOf(i);
    	int length= idButton.length();
    	if(length==1) idButton="0"+idButton;
    	idButton="Button"+idButton;
    	
    	return idButton;
    }
    /**
     * Toggle the reproduction
     */
    public void toggle() {
        if (playing) {
            stop();
        } else {
            play();
        }
        
    }
    
    public void setVolume(float mVolume)
    {
    	this.volume=mVolume;
    	
    }
    

  
}
