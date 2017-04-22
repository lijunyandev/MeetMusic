package com.lijunyan.blackmusic.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lijunyan.blackmusic.fragment.PlayBarFragment;
import com.lijunyan.blackmusic.receiver.PlayerManagerReceiver;

//此线程只是用于循环发送广播，通知更改歌曲播放进度。
public class UpdateUIThread extends Thread {

	private static final String TAG = UpdateUIThread.class.getName();
	private int threadNumber;
	private Context context;
	private PlayerManagerReceiver playerManagerReceiver;
	private int duration;
	private int curPosition;
	
	public UpdateUIThread(PlayerManagerReceiver playerManagerReceiver, Context context, int threadNumber) {
		Log.i(TAG, "UpdateUIThread: " );
		this.playerManagerReceiver = playerManagerReceiver;
		this.context = context;
		this.threadNumber = threadNumber;
	}

	@Override
	public void run() {
		try {
			while (playerManagerReceiver.getThreadNumber() == this.threadNumber) {
				if (playerManagerReceiver.status == Constant.STATUS_STOP) {
					Log.e(TAG, "run: Constant.STATUS_STOP");
					break;
				}
				if (playerManagerReceiver.status == Constant.STATUS_PLAY ||
						playerManagerReceiver.status == Constant.STATUS_PAUSE) {
					if (!playerManagerReceiver.getMediaPlayer().isPlaying()) {
						Log.i(TAG, "run: getMediaPlayer().isPlaying() = " + playerManagerReceiver.getMediaPlayer().isPlaying());
						break;
					}
					duration = playerManagerReceiver.getMediaPlayer().getDuration();
					curPosition = playerManagerReceiver.getMediaPlayer().getCurrentPosition();
//				Log.d(TAG, "duration = "+duration);
//				Log.d(TAG, "current = "+curPosition);
					Intent intent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);
					intent.putExtra(Constant.STATUS, Constant.STATUS_RUN);
//				intent.putExtra("status2", playerManagerReceiver.status);
					intent.putExtra(Constant.KEY_DURATION, duration);
					intent.putExtra(Constant.KEY_CURRENT, curPosition);
					context.sendBroadcast(intent);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
}

