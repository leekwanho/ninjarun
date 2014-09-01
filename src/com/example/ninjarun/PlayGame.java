package com.example.ninjarun;

import java.util.Random;
import android.app.Activity;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Vibrator;
import android.view.*;

public class PlayGame extends SurfaceView implements SurfaceHolder.Callback{
	final static int DELAY=1000/100;//100FPS
	final static int NO_STAGE_USING=-1;
	final static int GAME_READY=1;
	final static int GAME_START=2;
	final static int GAME_OVER=3;
	
	SurfaceHolder holder;//SurfaceView�� ����, Canvas�� ����
	Context context;
	SoundManager soundManager;

	int gameState;
	boolean isPause;
	Countdown countdown;

	Character ninja;
	long lastTouchTime;//���� Ŭ���� Ȯ���ϱ� ���� ����
	int touchStartX;//ĳ���� �̵��� ���� ����
	
	
	Stage[] stage;
	int now;//���� ����ϴ� ���������� �ε����� ���
	
	Score score;
	int passedWallIndex;//���� �� ����� ���� ���� �Ҵ��� ���� ����
	
	int backgroundY[];//����� ���ѽ�ũ���� ���� ����

	DrawThread drawThread;
	SetStageThread  setStageThread;
	
	public PlayGame(Context context) {
		super(context);
		holder=getHolder();
		holder.addCallback(this);//�ݹ��� �ڽ����� ����	
		this.context=context;	
		soundManager=new SoundManager(context);

		gameState=GAME_READY;
		isPause=false;
		countdown=new Countdown();
		 
		ninja=new Character();
		lastTouchTime=0;
		
		score= new Score();
		passedWallIndex=-1;//������ �� ����
		touchStartX=0;
		
		stage=new Stage[2];
		for(int i=0;i<stage.length;i++){
			stage[i]=new Stage();
		}
		
		now=NO_STAGE_USING;
		
		backgroundY=new int[2];
		for(int i=0; i<backgroundY.length; i++){
			backgroundY[i]=0;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		drawThread = new DrawThread(this.holder);
		setStageThread  = new SetStageThread ();
		
		setStageThread.start();
		drawThread.start();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {//�� ��?
		if(drawThread==null){
			drawThread.SizeChange(width,height);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		//for(;;){//���� ������ �� �ؾ��ϳ�?
		drawThread.exit=true;
		setStageThread.exit=true;
		
			try {
				drawThread.interrupt();
				setStageThread.interrupt();
			//	break;
			} catch (Exception e) {;}
		//}
			
			return;
			
			//���ῡ���� �� �߰��ؾ���
			//������ 1�� ������ ������ �����Ҷ� ������ �ȵ�
	}
	
	public void activityMove(){

		if(UserInfo.loginFlag){
			Intent intent= new Intent(context, GameOver.class);
			intent.putExtra("newScore", score.value);
			
			context.startActivity(intent);
			((Activity)context).finish();
		}
		else{
			Intent intent= new Intent(context, GameOver_Single.class);
			intent.putExtra("newScore", score.value);
			
			context.startActivity(intent);
			((Activity)context).finish();
		}
		
	}

	public boolean onTouchEvent(MotionEvent event){
		int touchEndX;//�巡���� ��ŭ�� ���� ������ ���� ����
		
		switch(event.getAction()){
		
		//���� Ŭ�� ó��
		case MotionEvent.ACTION_DOWN:
			if(ninja.status==Character.MOVING){		
				long thisTime=System.currentTimeMillis();
				touchStartX=(int)event.getX();//��ġ�� ���۵� x��ǥ�� ����
				
				//250ms�ȿ� �ٽ� Ŭ���� ���
				if(thisTime-lastTouchTime<250){
					for(int i=0; i<stage[now].wall.length;i++){
						
						//ĳ���Ϳ� ������ ���� ã��(�ʹ� �ָ� ������ �ȵ�)
						if(stage[now].wall[i].y<ninja.y && ninja.y < stage[now].wall[i].y+Stage.GAP_BETWEEN_WALL*2/3){
							
							//�տ� �������� ���� ������
							if((stage[now].wall[i].first.x <= ninja.x+Character.SIZE 
									&& ninja.x <= stage[now].wall[i].first.x+stage[now].wall[i].first.width)
								||(stage[now].wall[i].second.type!=0 
									&& stage[now].wall[i].second.x <= ninja.x+Character.SIZE 
									&& ninja.x <= stage[now].wall[i].second.x+stage[now].wall[i].second.width)){
								
								ninja.status=Character.JUMPING;
								ninja.landingY=stage[now].wall[i].y-Character.SIZE-10;		
								ninja.setJumpImgChangePoint();
								soundManager.play(soundManager.ninjaJump);
							}
							break;
						}
					}
				}
				
				lastTouchTime=thisTime;
			}
			break;
			
		//�巡�� ó��
		case MotionEvent.ACTION_MOVE:
			if(ninja.status==Character.MOVING){
				touchEndX=(int)event.getX();//�巡�� ���� x��ǥ�� ����
				ninja.x+=touchEndX-touchStartX;//������ŭ �̵�
				
				//ĳ���Ͱ� ȭ�� ������ ������ �ʰ�
				if(ninja.x<0){
					ninja.x=0;
				}
				else if(ninja.x+Character.SIZE>480){
					ninja.x=480-Character.SIZE;
				}
				
				touchStartX=touchEndX;//�巡�װ� ��� �� �� �����Ƿ�
			}		
			break;	
		}
		
		return true;
	}
	
	void updateGame(){//���� ���� �޼���
		int blockAttr;//�浹�޼��忡�� ��ȯ�Ǵ� ����� �Ӽ��� ���� 
		
		if(isPause==false){
		//���ӻ��¿����� �ٸ��� ����
			if(gameState==GAME_READY){
				countdown.setIndex();
				soundManager.play(soundManager.countdown);
				if(countdown.isEnd){
					gameState=GAME_START;
					soundManager.play(soundManager.start);
				}
			}
			else if(gameState==GAME_START){
				//���� ���� ���������� ������  �������� ��ü
				if(now!=NO_STAGE_USING && stage[now].wall[stage[now].wall.length-1].y>=810){	
					stage[now].can_use=false;
					now=NO_STAGE_USING;
				}
				
				//���� ����� �������� ����, �������� ���� �ӵ� ����
				if(now==NO_STAGE_USING){
					for(int i=0;i<stage.length;i++){
						if(stage[i].can_use==true){
							now=i;
							Stage.speed+=Stage.speedInc;
							passedWallIndex=-1;//������ ���� ����
							break;
						}
					}
				}
				
				if(now!=NO_STAGE_USING){
					//�浹 �˻� �� ������ �������� ����
					blockAttr=stage[now].getBlockAttrCollisionWithCharacter(
							new Rect(ninja.x+Character.SIZE/2-Character.SIZE_FOR_CHECK_COLLISION/2, 
									ninja.y, 
									ninja.x+Character.SIZE/2+Character.SIZE_FOR_CHECK_COLLISION/2, 
									ninja.y+Character.SIZE_FOR_CHECK_COLLISION));
					
					if(blockAttr==Block.SPIKE){
						if(ninja.status==Character.MOVING){
							ninja.status=Character.DIE_BY_SPIKE;
							soundManager.play(soundManager.ninjaDieBySpike);
							((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);//0.5�� ����
							gameState=GAME_OVER;
						}
					}
					else if(blockAttr==Block.ELECTRONIC){
						ninja.status=Character.DIE_BY_ELECTRONIC;
						soundManager.play(soundManager.ninjaDieByElec);
						((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
						gameState=GAME_OVER;

					}
					
					//�̵� �� ���� �ð����� ǥâ �߻�
					if(ninja.status==Character.MOVING){
						Long thisTime=System.currentTimeMillis();
						if(thisTime-ninja.lastFireTime>=ninja.fireRate){
							ninja.fireDagger();
							ninja.lastFireTime=thisTime;
							soundManager.play(soundManager.daggerThrow);
						}
					}
					
					//ǥâ ����, �⵹ Ȯ��
					for(int i=0; i<ninja.dagger.length; i++){
						if(ninja.dagger[i].nowUsing){
							ninja.dagger[i].going();
							
							blockAttr=stage[now].getBlockAttrCollisionWithDagger(
									new Rect(ninja.dagger[i].x, 
													ninja.dagger[i].y,
													ninja.dagger[i].x+Dagger.SIZE, 
													ninja.dagger[i].y+Dagger.SIZE));
							
							//� ��ϰ� �浹���� ��
							if(blockAttr!=-1){
								if(blockAttr==Block.ELECTRONIC){
									score.value+=30;//���⺮ ���� 30��
									soundManager.play(soundManager.explosion);
								}
								else if(blockAttr==Block.SPIKE){
									soundManager.play(soundManager.daggerStuck);
								}
								ninja.dagger[i].nowUsing=false;
							}
						}
					}
					
					//���� ������ ��
					if(ninja.status==Character.JUMPING){
						//�������� �����ϸ�
						if(ninja.landingY>=ninja.y){
							ninja.status=Character.MOVING;
							score.value+=30;//���� ���� 30��
						}
						else{
							//�������� jumpImgChangePoint�� �Ʒ��� ���� ĳ���Ϳ� ��������� ��
							ninja.landingY+=Stage.speed;
							for(int i=0;i<ninja.jumpImgChangePoint.length;i++){
								ninja.jumpImgChangePoint[i]+=Stage.speed;
							}
						}
					}
					
					//ĳ���Ͱ� �� �� ���� ���� ���� ������ ����ϸ�
					for(int i=0;i<stage[now].wall.length;i++){
						//ĳ���Ͱ� ������ �� ��
						if(stage[now].wall[i].y< ninja.y+Character.SIZE 
						   && ninja.y+Character.SIZE<stage[now].wall[i].y+stage[now].wall[i].first.height){ 
							//wall[i]�� ����� 2�� �ְ�
							if(stage[now].wall[i].second.type!=0){
								//�� ����� ��� �μ����� �ʾҰ�
								if(stage[now].wall[i].first.isBroken==false && stage[now].wall[i].second.isBroken==false){
									//���� ƴ�� ũ�Ⱑ Block.STD_SIZE
									if(stage[now].wall[i].second.x-(stage[now].wall[i].first.x+stage[now].wall[i].first.width)==Block.STD_SIZE){
										//ĳ���Ͱ� �� ���̷� �������ٸ�
										if(stage[now].wall[i].first.x+stage[now].wall[i].first.width< ninja.x && ninja.x<stage[now].wall[i].second.x){
											//���� ���� ó���� ���� �ƴϸ�
											if(i!=passedWallIndex){
												passedWallIndex=i;
												score.value+=70;
												break;
											}
											//break;
										}
									}
								}
							}
						}
					}
					
					//ĳ���� �ִϸ��̼� ����
					switch(ninja.status){
					case Character.MOVING:
						ninja.setMoveAnimation();
						break;
						
					case Character.JUMPING:
						ninja.setJumpAnimation();
						break;
					}
					
					//ǥâ �ִϸ��̼� ����
					for(int i=0; i<ninja.dagger.length; i++){
						if(ninja.dagger[i].nowUsing){
							ninja.dagger[i].setSpinAnimation();
						}
					}
					
					//���⺮ �ִϸ��̼� ����
					if(stage[now].can_use){
						for(int j=0;j<stage[now].wall.length;j++){
							if(stage[now].wall[j].first.attribute==Block.ELECTRONIC && stage[now].wall[j].first.isBroken==false){
								stage[now].wall[j].first.setElectroicAnimation();
							}
							
							//�����尡 �����ϸ�
							if(stage[now].wall[j].second.type!=0){
								if(stage[now].wall[j].second.attribute==Block.ELECTRONIC && stage[now].wall[j].second.isBroken==false){
									stage[now].wall[j].second.setElectroicAnimation();
								}
							}
						}
					}
					
					//��� ���� �ִϸ��̼� ����
					if(stage[now].can_use){
						for(int j=0;j<stage[now].wall.length;j++){
							if(stage[now].wall[j].first.startExplo){
								stage[now].wall[j].first.setExplosionAnimation();
								stage[now].wall[j].first.exploAniY+=Stage.speed;
							}
							
							//�����尡 �����ϸ�
							if(stage[now].wall[j].second.type!=0){
								if(stage[now].wall[j].second.startExplo){
									stage[now].wall[j].second.setExplosionAnimation();
									stage[now].wall[j].second.exploAniY+=Stage.speed;
								}
							}
						}			
					}
		
					//���ӿ�ҵ��� �Ʒ��� ���� ĳ���Ͱ� ������ ���� ���� ȿ���� ��
					for(int i=0;i<stage[now].wall.length;i++){
						stage[now].wall[i].y+=Stage.speed;
					}
					
					//��� �̵�
					backgroundY[0]=(backgroundY[0]+(int)Stage.speed)%1600;
					backgroundY[1]=backgroundY[0]-1600;
					
					//�ڸ��� ����
					score.setCipher();
				}
			}
			
			if(gameState==GAME_OVER){
				surfaceDestroyed(holder);
				activityMove();
			}
		}
	}
	
	//�̳� Ŭ����
	class DrawThread extends Thread{
		final static int BASIC_WIDTH=480;
		final static int BASIC_HEIGHT=800;
		
		SurfaceHolder t_holder;
		
		boolean exit;
		int width, height;
		int realWidth;
		int realHeight;
		float scale;
		
		//���� �̹���(�迭�� ��� �ִϸ��̼��� ����)
		Bitmap backgroundImg;
		Bitmap blockImg[][];
		Bitmap explosionImg[];
		Bitmap characterImg[];
		Bitmap jumpImg[];
		Bitmap ghostImg;
		Bitmap daggerImg[];
		Bitmap numberImg[];
		Bitmap countdownImg[];
		Bitmap gameOverImg;
		
		DrawThread(SurfaceHolder holder){
			t_holder=holder;
			exit=false;
			
			Resources res=getResources();
			
			realWidth=res.getDisplayMetrics().widthPixels;//���� �ȼ� ���� ������
			realHeight=res.getDisplayMetrics().heightPixels;
			scale=res.getDisplayMetrics().density;
			
			//���� �̹����� �ҷ���
			BitmapDrawable bd= (BitmapDrawable)res.getDrawable(R.drawable.background);
			backgroundImg=bd.getBitmap();
			backgroundImg=Bitmap.createScaledBitmap(backgroundImg, getWidth(), 1600, false);
			
			
			blockImg=new Bitmap[7][];//7= ��� Ÿ���� ��
			blockImg[0]=new Bitmap[5];//���⺮, 5= ���⺮ �ִϸ��̼� �̹����� ��
			blockImg[1]=new Bitmap[1];//���ú��̶� �ִϸ��̼��� ���� (�̹��� �ϳ�)
			blockImg[2]=new Bitmap[5];
			blockImg[3]=new Bitmap[1];
			blockImg[4]=new Bitmap[5];
			blockImg[5]=new Bitmap[1];
			blockImg[6]=new Bitmap[1];
			
			//���ú�
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall3);
			blockImg[1][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall5);
			blockImg[3][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall7);
			blockImg[5][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall8);
			blockImg[6][0]=bd.getBitmap();
			
			//���⺮
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall2_1);
			blockImg[0][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall2_2);
			blockImg[0][1]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall2_3);
			blockImg[0][2]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall2_4);
			blockImg[0][3]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall2_5);
			blockImg[0][4]=bd.getBitmap();
			
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall4_1);
			blockImg[2][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall4_2);
			blockImg[2][1]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall4_3);
			blockImg[2][2]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall4_4);
			blockImg[2][3]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall4_5);
			blockImg[2][4]=bd.getBitmap();
			
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall6_1);
			blockImg[4][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall6_2);
			blockImg[4][1]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall6_3);
			blockImg[4][2]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall6_4);
			blockImg[4][3]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall6_5);
			blockImg[4][4]=bd.getBitmap();
			
			explosionImg=new Bitmap[3];
			bd= (BitmapDrawable)res.getDrawable(R.drawable.explosion1);
			explosionImg[0]=bd.getBitmap();
			explosionImg[0]=Bitmap.createScaledBitmap(explosionImg[0], Block.EXPLO_SIZE, Block.EXPLO_SIZE, false);
			bd= (BitmapDrawable)res.getDrawable(R.drawable.explosion2);
			explosionImg[1]=bd.getBitmap();
			explosionImg[1]=Bitmap.createScaledBitmap(explosionImg[1], Block.EXPLO_SIZE, Block.EXPLO_SIZE, false);
			bd= (BitmapDrawable)res.getDrawable(R.drawable.explosion3);
			explosionImg[2]=bd.getBitmap();
			explosionImg[2]=Bitmap.createScaledBitmap(explosionImg[2], Block.EXPLO_SIZE, Block.EXPLO_SIZE, false);
			
			characterImg=new Bitmap[8];
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_move1);
			characterImg[0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_move2);
			characterImg[1]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_move3);
			characterImg[2]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_move4);
			characterImg[3]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_move5);
			characterImg[4]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_move6);
			characterImg[5]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_move7);
			characterImg[6]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_move8);
			characterImg[7]=bd.getBitmap();
			
			jumpImg=new Bitmap[3];
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_jump1);
			jumpImg[0]=Bitmap.createScaledBitmap(bd.getBitmap(), Character.SIZE+4, Character.SIZE+4, false);
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_jump2);
			jumpImg[1]=Bitmap.createScaledBitmap(bd.getBitmap(), Character.SIZE+6, Character.SIZE+6, false);
			jumpImg[2]=Bitmap.createScaledBitmap(bd.getBitmap(), Character.SIZE+8, Character.SIZE+8, false);
			
			bd= (BitmapDrawable)res.getDrawable(R.drawable.ninja_die);
			ghostImg=bd.getBitmap();
			
			daggerImg=new Bitmap[3];
			bd= (BitmapDrawable)res.getDrawable(R.drawable.dagger1);
			daggerImg[0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.dagger2);
			daggerImg[1]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.dagger3);
			daggerImg[2]=bd.getBitmap();
			
			numberImg=new Bitmap[10];
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_zero);
			numberImg[0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_one);
			numberImg[1]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_two);
			numberImg[2]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_three);
			numberImg[3]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_four);
			numberImg[4]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_five);
			numberImg[5]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_six);
			numberImg[6]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_seven);
			numberImg[7]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_eight);
			numberImg[8]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_nine);
			numberImg[9]=bd.getBitmap();
			
			countdownImg=new Bitmap[3];
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_three_big);
			countdownImg[0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_two_big);
			countdownImg[1]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.number_one_big);
			countdownImg[2]=bd.getBitmap();
			/*
			countdownImg[0]=Bitmap.createScaledBitmap(numberImg[3], Countdown.SIZE, Countdown.SIZE, false);
			countdownImg[1]=Bitmap.createScaledBitmap(numberImg[2], Countdown.SIZE, Countdown.SIZE, false);
			countdownImg[2]=Bitmap.createScaledBitmap(numberImg[1], Countdown.SIZE, Countdown.SIZE, false);
			*/
			bd= (BitmapDrawable)res.getDrawable(R.drawable.gameover);
			gameOverImg=Bitmap.createScaledBitmap(bd.getBitmap(), 350, 63, false);
		}
		
		public void SizeChange(int width, int height){
			this.width=width;
			this.height=height;
		}
		
		int convertX(float x){//������ float double int �� �ϳ���
			return (int)(x*realWidth/BASIC_WIDTH);
			//return (int)(x*scale);
		}
		
		int convertY(float y){
			return (int)(y*realWidth/BASIC_WIDTH);
			//return (int)(y*scale);
		}
		
		public void run(){
			Canvas canvas;
			Paint pnt=new Paint();
			
			pnt.setAntiAlias(true);//��Ƽ�˸��ƽ�: ���� ���κ��� �ε巴��
			
			while(exit==false){
				updateGame();
				
				synchronized(t_holder){
					canvas=t_holder.lockCanvas();//�̰� ���� �ϴ°���
					
					if(canvas==null) 
						break;
					
					//���
					for(int i=0; i<backgroundY.length; i++){
						canvas.drawBitmap(backgroundImg, 0, backgroundY[i], pnt);//���
					}
					
					//��������, ��� ����
					if(now!=NO_STAGE_USING && stage[now].can_use==true){
						for(int j=0;j<stage[now].wall.length;j++){
							if(-30<stage[now].wall[j].y && stage[now].wall[j].y<BASIC_HEIGHT){//ȭ�鿡 ���� -30~BASIC_HEIGHT���̿� ��ġ�� ���� ��ο�
								if(stage[now].wall[j].first.isBroken==false){
									
									//Ÿ�Կ� �°� �׷������
									switch(stage[now].wall[j].first.attribute){
									case Block.SPIKE:
										canvas.drawBitmap(blockImg[stage[now].wall[j].first.type-2][0],
																		convertX(stage[now].wall[j].first.x), 
																		convertY(stage[now].wall[j].y), 
																		pnt);
										break;
										
									case Block.ELECTRONIC:
										canvas.drawBitmap(blockImg[stage[now].wall[j].first.type-2][stage[now].wall[j].first.elecAniIndex],
																		convertX(stage[now].wall[j].first.x), 
																		convertY(stage[now].wall[j].y), 
																		pnt);
										break;
									}
								}
								 
								if(stage[now].wall[j].first.startExplo){
									canvas.drawBitmap(explosionImg[stage[now].wall[j].first.explosionAniIndex],
																	convertX(stage[now].wall[j].first.exploAniX), 
																	convertY(stage[now].wall[j].first.exploAniY),
																	pnt);
								}
									
								//�����尡 �����ϸ�
								if(stage[now].wall[j].second.type!=0){
									if(stage[now].wall[j].second.isBroken==false){
										switch(stage[now].wall[j].second.attribute){
										case Block.SPIKE:
											canvas.drawBitmap(blockImg[stage[now].wall[j].second.type-2][0],
																			convertX(stage[now].wall[j].second.x), 
																			convertY(stage[now].wall[j].y), 
																			pnt);
											break;
											
										case Block.ELECTRONIC:
											canvas.drawBitmap(blockImg[stage[now].wall[j].second.type-2][stage[now].wall[j].second.elecAniIndex],
																			convertX(stage[now].wall[j].second.x), 
																			convertY(stage[now].wall[j].y), 
																			pnt);
											break;
										}
									}
									
									if(stage[now].wall[j].second.startExplo){
										canvas.drawBitmap(explosionImg[stage[now].wall[j].second.explosionAniIndex],
																		convertX(stage[now].wall[j].second.exploAniX), 
																		convertY(stage[now].wall[j].second.exploAniY),
																		pnt);
									}
								}
							}
						}			
					}
					
					//ǥâ
					for(int i=0; i<ninja.dagger.length; i++){
						if(ninja.dagger[i].nowUsing){
							canvas.drawBitmap(daggerImg[ninja.dagger[i].spinAniIndex], 
															convertX(ninja.dagger[i].x), 
															convertY(ninja.dagger[i].y), 
															pnt);
						}
					}
					
					//ĳ����
					switch(ninja.status){
					case Character.MOVING:
						canvas.drawBitmap(characterImg[ninja.moveAniIndex], convertX(ninja.x), convertY(ninja.y), pnt);
						break;
					case Character.JUMPING:
						canvas.drawBitmap(jumpImg[ninja.jumpAniIndex], convertX(ninja.x), convertY(ninja.y), pnt);
						break;
					case Character.DIE_BY_SPIKE:
					case Character.DIE_BY_ELECTRONIC:
						canvas.drawBitmap(ghostImg, convertX(ninja.x), convertY(ninja.y), pnt);
						break;
					}
					
					
					//����
					for(int i=0; i< score.cipher.length; i++){
						//0�� �ƴ� �κк��� ���
						//�ƹ��͵� ��� �ȵǸ� �����ϹǷ� ������ 0�� score.cipher[score.cipher.length-1].n�� �׻� ���
						if(score.cipher[i].n==0 && i!= score.cipher.length-1){
							continue;
						}
						else{
							for(int j=i;j<score.cipher.length; j++){
								canvas.drawBitmap(numberImg[score.cipher[j].n],
																convertX(score.cipher[j].x),
																convertY(score.cipher[j].y),
																pnt);
							}
							break;
						}
					}
					
					//ī��Ʈ �ٿ�
					if(gameState==GAME_READY){
						canvas.drawBitmap(countdownImg[countdown.index],
														convertX(countdown.x),
														convertY(countdown.y),
														pnt);
					}
					
					//���� ����
					if(gameState==GAME_OVER){
						canvas.drawBitmap(gameOverImg, 
														convertX(BASIC_WIDTH/2-350/2), 
														convertY(BASIC_HEIGHT/2-63/2), 
														pnt);
					}
					
					t_holder.unlockCanvasAndPost(canvas);
				}
				
				try{
					Thread.sleep(DELAY);
				}catch(Exception e){;}
			}
		}
	}
	
	//�̳� Ŭ����
	class SetStageThread extends Thread{
		boolean exit;
		Wall original_wall[];
		int height;
		
		public SetStageThread(){
			exit=false;
			original_wall=new Wall[105];
			
			int b_t1=2, b_p1=0;//block_type, block_pos
			int b_t2=2, b_p2=0;
			int rb_t2, rb_p2;//b_t2, b_p2�� �� ����
			
			int[][] n ={
					{35, 50, 60, 66, 69, 69},
					{70, 80, 86, 89, 89},
					{90, 96, 99, 99},
					{100, 103, 103},
					{104, 104}				
			};
			
			int i, j, k;
			
			//���� �ϳ��� �� �ʱ�ȭ
			for(i=0;i<35;i++){
				original_wall[i] = new Wall(b_t1, b_p1++);
				if(b_p1==11-b_t1){
					b_t1++;
					b_p1=0;
					
				}
			}
			/*
			 |22*******|  
			 |*22******|  
			 |**22*****|  
			 |***22****|  
			 |****22***|  
			 |*****22**|  
			 |******22*| 
			 |*******22|  
			 |333******|  
			 |*333*****|  �̷� ������ �ʱ�ȭ ��
			 */
			
			//���� ���� �� �ʱ�ȭ	
			for(i=0;i<n.length;i++){
				b_t1=i+2;
				b_p1=0;
				rb_t2=2;
				rb_p2=i+3;
		
				for(j=0;j<n[i].length-1;j++){
					b_t2=rb_t2;
					b_p1++;
					b_p2=++rb_p2;
					
					for(k=n[i][j];k<=n[i][j+1];k++){ 
						original_wall[k] = new Wall(b_t1, b_t2, b_p1, b_p2++);
						if(b_p2==11-b_t2){
							b_t2++;
							b_p2=rb_p2;
						}
					}
					/*
					|22*22****|   2, 1, 2, 4
					|22**22***|   2, 1, 2, 5
					|22***22**|   2, 1, 2, 6
					|22****22*|   2, 1, 2, 7
					|22*****22|   2, 1, 2, 8
					|22*333***|   2, 1, 3, 4
					|22**333**|   2, 1, 3, 5
					|22***333*|   2, 1, 3, 6
					|22****333|   2, 1, 3, 7
					|22*4444**|   2, 1, 4, 4
					|22**4444*|   2, 1, 4, 5
					|22***4444|   2, 1, 4, 6
					|22*55555*|   2, 1, 5, 4
					|22**55555|   2, 1, 5, 5
					|22*666666|   2, 1, 6, 4
					*/
				}
				/*
				|22*22****|  2, 1, 2, 4
				...
				|*22*22***|  2, 2, 2, 5
				...
				|**22*22**|  2, 3, 2, 6
				...
				*/
			}
			/*
			|22*22****|  2, 1, 2, 4
			...
			|333*22***|  3, 1, 2, 5
			...
			|4444*22**|  4, 1, 2, 6
			...
			*/
		}
		
		public void run(){
			Random rand= new Random();
			int rd;
			int i, j;
			
			//���� ������� �ʴ� ���������� �ʱ�ȭ
			while(exit!=true){
				for(i=0;i<stage.length;i++){
					if(stage[i].can_use==false){
						for(j=0;j<stage[i].wall.length;j++){
							rd=rand.nextInt(original_wall.length);
							
							//ī��			
							stage[i].wall[j].first.copy(original_wall[rd].first);
							stage[i].wall[j].second.copy(original_wall[rd].second);
						}
						
						//�� ���� ���� �ʱ�ȭ
						stage[i].wall[0].y=0;//ȭ�� ��
						for(j=1;j<stage[i].wall.length;j++){
							stage[i].wall[j].y=stage[i].wall[j-1].y-Stage.GAP_BETWEEN_WALL;
						}
						
						stage[i].can_use=true;
					}
				}
				
				try{
					Thread.sleep(DELAY);
				}catch(Exception e){;}
			}
		}
	}
}