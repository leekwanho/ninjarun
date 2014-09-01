package com.example.ninjarun;

public class Character {
	final static int SIZE=45;
	final static int SIZE_FOR_CHECK_COLLISION=32;
	final static int MOVING=1;
	final static int JUMPING=2;
	final static int DIE_BY_SPIKE=3;
	final static int DIE_BY_ELECTRONIC=4;
	
	int x, y;
	
	int moveAniIndex;//����ִϸ��̼��̹��� �迭�� �ε���
	long lastMoveAniIndexChangedTime;
	int moveAniIndexChangeRate;
	
	int landingY;//������ ������ y��ǥ 
	int jumpImgChangePoint[];//������ �ִϸ��̼� ��ȯ�� ���� y��ǥ
	int jumpAniIndex;//�����ִϸ��̼��̹��� �迭�� �ε���
	int setJumpAniFlag;//setJumpAnimation �޼ҵ忡�� Ȱ��
	
	int status;
	
	Dagger dagger[];
	long lastFireTime;
	int fireRate;
	
	Character(){
		status=MOVING;
		
		x=240-SIZE/2;//ȭ�� �߾�
		y=650;//ȭ�� �Ʒ���
		
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
			moveAniIndex=(moveAniIndex+1)%8;//8�� �̹����迭�� ũ��
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
			
			//�Ʒ��� ���๮�� 1���� ����ǵ��� ��(���� �̹��� �ε����� x, y��ǥ�� ��� �ٲٸ� �ȵǱ⶧��)
			if(setJumpAniFlag!=0){
				setJumpAniFlag=0;
				
				jumpAniIndex=0;
				//�ε����� 0�� �̹����� ũ�Ⱑ 49*49�̹Ƿ�
				//���� ĳ���Ͱ� ����(Ȯ��)�� ������ �ַ��� �Ʒ��� ���� ��ǥ�� �����ؾ���
				x-=2;
				y-=2;
			}
		}
		else if(jumpImgChangePoint[1]>=y && y>jumpImgChangePoint[2]){
			if(setJumpAniFlag!=1){
				setJumpAniFlag=1;
				
				jumpAniIndex=1;
				//�ε����� 1�� �̹����� ũ�Ⱑ 51*51�̹Ƿ�
				x-=1;
				y-=1;
			}
		}
		else if(jumpImgChangePoint[2]>=y && y>jumpImgChangePoint[3]){
			if(setJumpAniFlag!=2){
				setJumpAniFlag=2;
			
				jumpAniIndex=2;
				//�ε����� 2�� �̹����� ũ�Ⱑ 53*53�̹Ƿ�
				x-=1;
				y-=1;
			}
		}
		else if(jumpImgChangePoint[3]>=y && y>jumpImgChangePoint[4]){
			if(setJumpAniFlag!=3){
				setJumpAniFlag=3;
			
				jumpAniIndex=1;
				//�ε����� 1�� �̹����� ũ�Ⱑ 51*51�̹Ƿ�
				//���� ĳ���Ͱ� �ϰ�(���)�� ������ �ַ��� �Ʒ��� ���� ��ǥ�� �����ؾ���
				x+=1;
				y+=1;
			}
		}
		else if(jumpImgChangePoint[4]>=y && y>jumpImgChangePoint[5]){
			if(setJumpAniFlag!=4){
				setJumpAniFlag=4;
			
				jumpAniIndex=0;
				//�ε����� 0�� �̹����� ũ�Ⱑ 49*49�̹Ƿ�
				x+=1;
				y+=1;
			}
		}
		else if(jumpImgChangePoint[5]>=y){//�ִϸ��̼� ����
			if(setJumpAniFlag!=5){
				setJumpAniFlag=5;
			
				//ĳ���� �̹����� ũ�Ⱑ 45*45�̹Ƿ�
				x+=2;
				y+=2;
			}
		}
	}
	
	void fireDagger(){
		for(int i=0; i<dagger.length; i++){
			if(!dagger[i].nowUsing){
				dagger[i].x=x+SIZE/2-Dagger.SIZE/2;//ĳ���� �߾ӿ� ��ġ
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
	
	int spinAniIndex;//���� �ִϸ��̼��̹��� �迭�� �ε���
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
				spinAniIndex=(spinAniIndex+1)%3;//3�� �̹����迭�� ũ��
				lastSpinAniIndexChangedTime=thisTime;
			}
		}
	}
}