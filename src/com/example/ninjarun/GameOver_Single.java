package com.example.ninjarun;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GameOver_Single extends Activity {

	int newScore;
	AlertDialog mPopupDlg;//���̾�α� �����
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gameover_single);
		
		Intent intent = this.getIntent();   // ���� �ޱ� ���� Intent ����
		newScore = intent.getIntExtra("newScore", 0);//������ ���ھ �޾ƿ�
		
		TextView textView=(TextView)findViewById(R.id.score);
		textView.setText("�������� \n" + newScore);
	}

	public void mOnClick(View v){
		
		switch(v.getId()){
		case R.id.go_gaip:
			startActivity(new Intent(this,Gaip.class));
			finish();
			break;
			
		case R.id.go_game:
			startActivity(new Intent(this,NinjaRunActivity.class));
			finish();
			break;
		}
	}
	
	@Override
	public void onBackPressed(){

		final Intent intent=new Intent(this,Login.class);
		
		/** ���̾�α� ���� */
		View view = this.getLayoutInflater().inflate(R.layout.dialog_gamemenu, null);//�並 ������
		mPopupDlg=new AlertDialog.Builder(this).create();//������
		mPopupDlg.setView(view,0,0,0,0);//���� ������ �ʰ��ϱ�����
		mPopupDlg.show();//���̾�α� ����
		
		Button exitBtn=(Button) view.findViewById(R.id.exit);//���̾�α׿��� �����ư
		exitBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//���̾�α׾��ְ�
				startActivity(intent);//�α��� ��Ƽ��Ƽ�� ������
				finish();//��Ƽ��Ƽ����
			}			
		});
		
		Button backBtn=(Button) view.findViewById(R.id.back);
		backBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//���̾�α׸� ����
			}			
		});
	}
}