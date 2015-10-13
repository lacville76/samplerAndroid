package com.ms.soundpack1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import com.ms.soundfile.CheapSoundFile;
import com.ms.soundfile.WaveHeader;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.provider.MediaStore.Audio;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AndroidAudioInputStream;

/**
 * <p>
 * Writes a WAV-file to disk. It stores the bytes to a raw file and when the
 * processingFinished method is called it prepends the raw file with a header to
 * make it a legal WAV-file.
 * </p>
 * 
 * <p>
 * Writing a RAW file first and then a header is needed because the header
 * contains fields for the size of the file, which is unknown beforehand. See
 * Subchunk2Size and ChunkSize on this <a
 * href="https://ccrma.stanford.edu/courses/422/projects/WaveFormat/">wav file
 * reference</a>.
 * </p>
 * 
 * @author Joren Six
 */
public  class WaveFormWriter implements AudioProcessor {
	private final TarsosDSPAudioFormat format;
	private final File rawOutputFile;
	private final String fileName;
	private FileOutputStream rawOutputStream;
	private CheapSoundFile msCheapSoundFile;
	/**
	 * Log messages.
	 */
	private static final Logger LOG = Logger.getLogger(WaveFormWriter.class.getName());
	
	/**
	 * The overlap and step size defined not in samples but in bytes. So it
	 * depends on the bit depth. Since the integer data type is used only
	 * 8,16,24,... bits or 1,2,3,... bytes are supported.
	 */
	private int byteOverlap, byteStepSize;
	
	/**
	 * Initialize the writer.
	 * @param format The format of the received bytes.
	 * @param fileName The name of the wav file to store.
	 */
	public WaveFormWriter(final TarsosDSPAudioFormat format,final String fileName,CheapSoundFile file){
		this.format=format;
		this.msCheapSoundFile=file;
		this.fileName = fileName;
		//a temporary raw file with a random prefix
		 File sdCard = Environment.getExternalStorageDirectory();
	  		File dir = new File (sdCard.getAbsolutePath() + "/SavedTracks");
		this.rawOutputFile = new File(dir, new Random().nextInt() + "out.raw");
		try {
			this.rawOutputStream = new FileOutputStream(rawOutputFile);
		} catch (FileNotFoundException e) {
			//It should always be possible to write to a temporary file.
			String message;
			message = String.format("Could not write to the temporary RAW file %1s: %2s", rawOutputFile.getAbsolutePath(), e.getMessage());
			LOG.severe(message);
		}	
	}
	

	@Override
	public boolean process(AudioEvent audioEvent) {
		this.byteOverlap = audioEvent.getOverlap() * format.getFrameSize();
		this.byteStepSize = audioEvent.getBufferSize() * format.getFrameSize() - byteOverlap;
		try {
			rawOutputStream.write(audioEvent.getByteBuffer(), byteOverlap, byteStepSize);
		} catch (IOException e) {
			LOG.severe(String.format("Failure while writing temporary file: %1s: %2s", rawOutputFile.getAbsolutePath(), e.getMessage()));
		}
		return true;
	}

	@Override
	public void processingFinished() {
		File out = new File(fileName);
		try {
			
			final FileInputStream inputStream = new FileInputStream(rawOutputFile);
			msCheapSoundFile.ReadFile(rawOutputFile);
			msCheapSoundFile.WriteFile(out, 0, msCheapSoundFile.getNumFrames());
			rawOutputStream.close();
		//	rawOutputFile.delete();			
		} catch (IOException e) {
			String message;
			message = String.format("Error writing the WAV file %1s: %2s", out.getAbsolutePath(), e.getMessage());
			LOG.severe(message);
		}
	}
	
}