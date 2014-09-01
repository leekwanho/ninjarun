package com.example.ninjarun;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
	SoundPool soundPool;
	AudioManager audioManager;
	
	int ninjaJump;
	int ninjaDieByElec;
	int ninjaDieBySpike;
	int daggerThrow;
	int daggerStuck;
	int explosion;
	int countdown;
	int start;

	SoundManager(Context context){
		soundPool= new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		audioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		ninjaJump=soundPool.load(context, R.raw.ninjajump, 1);
		ninjaDieByElec=soundPool.load(context, R.raw.ninjadie2, 0);
		ninjaDieBySpike=soundPool.load(context, R.raw.ninjadie1, 0);
		daggerThrow=soundPool.load(context, R.raw.daggerthrow, 4);
		daggerStuck=soundPool.load(context, R.raw.daggerstuck, 3);
		explosion=soundPool.load(context, R.raw.explosion, 2);
		countdown=soundPool.load(context, R.raw.lonote, 0);
		start=soundPool.load(context, R.raw.hinote, 0);
	}
	
	
	void play(int index){
		//º¼·ý Á¶Àý
		float streamVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume=streamVolume/audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		soundPool.play(index, streamVolume, streamVolume, 1, 0, 1f);
	}
	
	void playLooped(int index){
		//º¼·ý Á¶Àý
		float streamVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume=streamVolume/audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		soundPool.play(index, streamVolume, streamVolume, 1, -1, 1f);
	}
}