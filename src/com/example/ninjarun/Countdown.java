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
		
		//프레임이 바뀔 시간이 되면
		if(thisTime-lastIndexChangedTime>=INDEX_CHANGE_RATE){
			if(index<2){//2는 이미지배열의 마지막 인덱스
				index++;
				lastIndexChangedTime=thisTime;
			}
			else if(index==2){//카운트 다운 끝
				isEnd=true;
			}
		}
	}
}