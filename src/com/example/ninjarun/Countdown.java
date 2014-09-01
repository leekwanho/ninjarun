package com.example.ninjarun;

public class Countdown {
	static final int SIZE=100;
	static final int INDEX_CHANGE_RATE=1000;
	
	int x, y;
	int index;
	long lastIndexChangedTime;
	boolean isEnd;
	
	Countdown(){
		x=480/2-SIZE/2;
		y=800/2-SIZE/2;
		index=-1;
		lastIndexChangedTime=0;
		isEnd=false;
	}
	
	void setIndex(){
		Long thisTime=System.currentTimeMillis();
		
		//�������� �ٲ� �ð��� �Ǹ�
		if(thisTime-lastIndexChangedTime>=INDEX_CHANGE_RATE){
			if(index<2){//2�� �̹����迭�� ������ �ε���
				index++;
				lastIndexChangedTime=thisTime;
			}
			else if(index==2){//ī��Ʈ �ٿ� ��
				isEnd=true;
			}
		}
	}
}