package com.example.ninjarun;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class StartPage extends Activity implements Animation.AnimationListener {

	RelativeLayout view;
    Animation startAni;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startpage);
		
		UserInfo.id=null;//처음 접속시 아이디 초기화
		
		view = (RelativeLayout) findViewById(R.id.startup);//시작화면. 애니메이션적용후 로그인페이지로 이동
	    startAni = AnimationUtils.loadAnimation(this, R.anim.start_anim);
    	view.startAnimation(startAni);

	    startAni.setAnimationListener(this);
		
		
	}
	

	@Override
	public void onAnimationEnd(Animation animation) {
    	startActivity(new Intent(this,Login.class));//시작페이지 끝나면 로그인페이지로 이동
    	finish();		
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}

}
