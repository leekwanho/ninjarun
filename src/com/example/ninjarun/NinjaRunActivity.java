package com.example.ninjarun;

import android.os.Bundle;
import android.view.KeyEvent;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class NinjaRunActivity extends Activity {
	PlayGame playGame;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		playGame= new PlayGame(this);//실질적인 게임진행을 담당
		setContentView(playGame);
	}
	
	protected void onPause() {
		super.onPause();
	}
	
	protected void onRestart() {
		super.onRestart();
	}
	
	protected void onResume() {
		super.onResume();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
        super.onKeyDown(keyCode, event);
        switch (keyCode){

        //게임 중 종료 처리
        case KeyEvent.KEYCODE_BACK:
        	playGame.isPause=true;//게임 진행 일시 정지
        	AlertDialog.Builder builder = new AlertDialog.Builder(NinjaRunActivity.this);
			builder.setMessage("종료할까요?")
				.setCancelable(false)
				.setPositiveButton("예", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
							NinjaRunActivity.this.finish();
						}
					})
					.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							playGame.isPause=false;//게임 진행 일시 정지 해제
							dialog.cancel();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
			break;
        default:
        	break;

        }
        return true;
    }
}
