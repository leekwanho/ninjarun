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
		
		UserInfo.id=null;//ó�� ���ӽ� ���̵� �ʱ�ȭ
		
		view = (RelativeLayout) findViewById(R.id.startup);//����ȭ��. �ִϸ��̼������� �α����������� �̵�
	    startAni = AnimationUtils.loadAnimation(this, R.anim.start_anim);
    	view.startAnimation(startAni);

	    startAni.setAnimationListener(this);
		
		
	}
	

	@Override
	public void onAnimationEnd(Animation animation) {
    	startActivity(new Intent(this,Login.class));//���������� ������ �α����������� �̵�
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
