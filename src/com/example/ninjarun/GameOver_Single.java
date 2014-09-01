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
	AlertDialog mPopupDlg;//다이얼로그 종료용
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gameover_single);
		
		Intent intent = this.getIntent();   // 값을 받기 위한 Intent 생성
		newScore = intent.getIntExtra("newScore", 0);//게임후 스코어를 받아옴
		
		TextView textView=(TextView)findViewById(R.id.score);
		textView.setText("최종점수 \n" + newScore);
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
		
		/** 다이얼로그 셋팅 */
		View view = this.getLayoutInflater().inflate(R.layout.dialog_gamemenu, null);//뷰를 가져옴
		mPopupDlg=new AlertDialog.Builder(this).create();//생성자
		mPopupDlg.setView(view,0,0,0,0);//여백 생기지 않게하기위함
		mPopupDlg.show();//다이얼로그 실행
		
		Button exitBtn=(Button) view.findViewById(R.id.exit);//다이얼로그에서 종료버튼
		exitBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//다이얼로그없애고
				startActivity(intent);//로그인 액티비티로 나간후
				finish();//액티비티종료
			}			
		});
		
		Button backBtn=(Button) view.findViewById(R.id.back);
		backBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//다이얼로그만 없앰
			}			
		});
	}
}