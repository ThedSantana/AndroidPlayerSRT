
package com.zzl.playersrt;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.VideoView;
import com.zzl.playersrt.R;



/**
 * @author zzl
 * 
 * 字幕的例子
 * 先解析 后TextView显示
 * 支持本地字幕和网络字幕
 *
 */
public class VideoActivity extends Activity {
	private static final String TAG = "VideoActivity";
	private VideoView videoView;
	private ArrayList<SRT> srts = null;
	private TextView srt1View;
	
	private boolean isPlaying = true;//

	private String filePath = "http://localhost/test.mp4";
	Timer	startShowSRTTimer ;


	
	//private String filePath = "/mnt/usb/sda1/1.mp4";
	
	private Handler handlerShowSrt = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
				Bundle bundle = msg.getData();
				String srt1 = bundle.getString("srt1", "");
				String srt2 = bundle.getString("srt2", "");

				SRT mSTR = (SRT)msg.obj;
				
				srt1View.setText(Html.fromHtml(srt1)+"\n"+Html.fromHtml(srt2));
				
				long  delayTime = mSTR.getEndTime() - mSTR.getBeginTime()+500;
				
				handlerShowSrt.sendEmptyMessageDelayed(101, delayTime);//字幕消失
				
				break;
				
			case 101:
				
				
				srt1View.setText("");
			
				break;
				
			
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);

		srt1View = (TextView) findViewById(R.id.srt_1_text);
		

		videoView = (VideoView) findViewById(R.id.videoview);
		videoView.setVideoURI(Uri.parse(filePath));
		videoView.requestFocus();
		videoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				
				
			}
		});

		videoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				isPlaying = false;
			}
		});
		
		
	
		new Thread(new Runnable() {

			@Override
			public void run() {
				//网络字幕
				srts = SubtitleTool.parseSrtFromNetwork(filePath.substring(0, filePath.lastIndexOf(".")) + ".srt");
				//本地字幕
				//srts = SubtitleTool.parseSrt(filePath.substring(0, filePath.lastIndexOf(".")) + ".srt");
			}
		}).start();
		
		
		startShowSRT();
	}


	 
	void startShowSRT() {

		if (startShowSRTTimer == null) {
			startShowSRTTimer = new Timer();

		}

		TimerTask mTimerTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if (isPlaying == true) {
						showSRT();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};

		startShowSRTTimer.schedule(mTimerTask, 2000, 1000); // 

	}
	
	
	private void showSRT() {
		if (srts == null) {
			return;
		}
		
		SRT srtbean;
		for (int i = 0; i < srts.size(); i++) {
			srtbean = srts.get(i);
		
			int currentPosition = videoView.getCurrentPosition();
			
			//Log.d("","currentPosition=="+currentPosition);
			
			
			if (currentPosition> srtbean.getBeginTime() && currentPosition< srtbean.getEndTime()) {
				
				Log.d("","currentPosition=="+videoView.getCurrentPosition()+"开始时间=="+ srtbean.getBeginTime());
				
				
				Message msg = handlerShowSrt.obtainMessage(100);
				Bundle bundle = new Bundle();
				bundle.putString("srt1", srtbean.getSrt1());
				bundle.putString("srt2", srtbean.getSrt2());
				msg.setData(bundle);
				msg.obj= srtbean;
				msg.sendToTarget();
				srts.remove(i);//将符合条件的字幕移走
				break;
			} 
		}
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		if (startShowSRTTimer == null) {
			startShowSRTTimer.cancel();

		}
		
		isPlaying = false;
	}
	
	

}
