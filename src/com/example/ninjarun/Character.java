package com.example.ninjarun;

public class Character {
	final static int SIZE=45;
	final static int SIZE_FOR_CHECK_COLLISION=32;
	final static int MOVING=1;
	final static int JUMPING=2;
	final static int DIE_BY_SPIKE=3;
	final static int DIE_BY_ELECTRONIC=4;
	
	int x, y;
	
	int moveAniIndex;//무브애니메이션이미지 배열의 인덱스
	long lastMoveAniIndexChangedTime;
	int moveAniIndexChangeRate;
	
	int landingY;//점프시 착지할 y좌표 
	int jumpImgChangePoint[];//점프시 애니메이션 전환을 위한 y좌표
	int jumpAniIndex;//점프애니메이션이미지 배열의 인덱스
	int setJumpAniFlag;//setJumpAnimation 메소드에서 활용
	
	int status;
	
	Dagger dagger[];
	long lastFireTime;
	int fireRate;
	
	Character(){
		status=MOVING;
		
		x=240-SIZE/2;//화면 중앙
		y=650;//화면 아래쪽
		
		moveAniIndex=0;
		lastMoveAniIndexChangedTime=0;
		moveAniIndexChangeRate=100;
		
		landingY=0;
		jumpImgChangePoint= new int[]{0, 0, 0, 0, 0, 0};
		setJumpAniFlag=-1;
		
		dagger=new Dagger[30];
		for(int i=0; i<dagger.length; i++){
			dagger[i]=new Dagger();
		}
		lastFireTime=0;
		fireRate=750;
		
	}
	
	void setMoveAnimation(){
		Long thisTime=System.currentTimeMillis();
		
		if(thisTime-lastMoveAniIndexChangedTime>=moveAniIndexChangeRate){
			moveAniIndex=(moveAniIndex+1)%8;//8은 이미지배열의 크기
			lastMoveAniIndexChangedTime=thisTime;
		}
	}
	
	void setJumpImgChangePoint(){
		for(int i=0;i<jumpImgChangePoint.length;i++){
			jumpImgChangePoint[i]=y-i*(y-landingY)/5;
		}
	}
	
	void setJumpAnimation(){
		if(jumpImgChangePoint[0]>=y && y>jumpImgChangePoint[1]){ 
			
			//아래의 실행문이 1번만 실행되도록 함(같은 이미지 인덱스에 x, y좌표를 계속 바꾸면 안되기때문)
			if(setJumpAniFlag!=0){
				setJumpAniFlag=0;
				
				jumpAniIndex=0;
				//인덱스가 0인 이미지의 크기가 49*49이므로
				//이전 캐릭터가 점프(확대)한 느낌을 주려면 아래와 같이 좌표를 수정해야함
				x-=2;
				y-=2;
			}
		}
		else if(jumpImgChangePoint[1]>=y && y>jumpImgChangePoint[2]){
			if(setJumpAniFlag!=1){
				setJumpAniFlag=1;
				
				jumpAniIndex=1;
				//인덱스가 1인 이미지의 크기가 51*51이므로
				x-=1;
				y-=1;
			}
		}
		else if(jumpImgChangePoint[2]>=y && y>jumpImgChangePoint[3]){
			if(setJumpAniFlag!=2){
				setJumpAniFlag=2;
			
				jumpAniIndex=2;
				//인덱스가 2인 이미지의 크기가 53*53이므로
				x-=1;
				y-=1;
			}
		}
		else if(jumpImgChangePoint[3]>=y && y>jumpImgChangePoint[4]){
			if(setJumpAniFlag!=3){
				setJumpAniFlag=3;
			
				jumpAniIndex=1;
				//인덱스가 1인 이미지의 크기가 51*51이므로
				//이전 캐릭터가 하강(축소)한 느낌을 주려면 아래와 같이 좌표를 수정해야함
				x+=1;
				y+=1;
			}
		}
		else if(jumpImgChangePoint[4]>=y && y>jumpImgChangePoint[5]){
			if(setJumpAniFlag!=4){
				setJumpAniFlag=4;
			
				jumpAniIndex=0;
				//인덱스가 0인 이미지의 크기가 49*49이므로
				x+=1;
				y+=1;
			}
		}
		else if(jumpImgChangePoint[5]>=y){//애니메이션 종료
			if(setJumpAniFlag!=5){
				setJumpAniFlag=5;
			
				//캐릭터 이미지의 크기가 45*45이므로
				x+=2;
				y+=2;
			}
		}
	}
	
	void fireDagger(){
		for(int i=0; i<dagger.length; i++){
			if(!dagger[i].nowUsing){
				dagger[i].x=x+SIZE/2-Dagger.SIZE/2;//캐릭터 중앙에 배치
				dagger[i].y=y;
				dagger[i].nowUsing=true;
				dagger[i].spinAniIndex=0;
				dagger[i].lastSpinAniIndexChangedTime=0;
				break;
			}
		}
	}

}

class Dagger{
	final static int SPEED=7;
	final static int SIZE=20;
	final static int SPIN_ANI_INDEX_CHANGE_RATE=200;
	int x, y;
	boolean nowUsing;
	
	int spinAniIndex;//스핀 애니메이션이미지 배열의 인덱스
	long lastSpinAniIndexChangedTime;
	
	Dagger(){
		nowUsing=false;
	}
	
	Dagger(int x, int y){
		this.x=x;
		this.y=y;
		nowUsing=false;
		
		spinAniIndex=0;
		lastSpinAniIndexChangedTime=0;
	}	
	
	void going(){
		if(y<=0-SIZE){
			nowUsing=false;
		}
		else{
			y-=SPEED;
		}
	}
	
	void setSpinAnimation(){
		Long thisTime;
		
		if(nowUsing){
			thisTime=System.currentTimeMillis();
			
			if(thisTime-lastSpinAniIndexChangedTime>=SPIN_ANI_INDEX_CHANGE_RATE){
				spinAniIndex=(spinAniIndex+1)%3;//3은 이미지배열의 크기
				lastSpinAniIndexChangedTime=thisTime;
			}
		}
	}
}