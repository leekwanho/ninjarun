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
		playGame= new PlayGame(this);//�������� ���������� ���
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

        //���� �� ���� ó��
        case KeyEvent.KEYCODE_BACK:
        	playGame.isPause=true;//���� ���� �Ͻ� ����
        	AlertDialog.Builder builder = new AlertDialog.Builder(NinjaRunActivity.this);
			builder.setMessage("�����ұ��?")
				.setCancelable(false)
				.setPositiveButton("��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
							NinjaRunActivity.this.finish();
						}
					})
					.setNegativeButton("�ƴϿ�", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							playGame.isPause=false;//���� ���� �Ͻ� ���� ����
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
