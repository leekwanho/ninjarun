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
	
	SurfaceHolder holder;//SurfaceView를 관리, Canvas에 접근
	Context context;
	SoundManager soundManager;

	int gameState;
	boolean isPause;
	Countdown countdown;

	Character ninja;
	long lastTouchTime;//더블 클릭을 확인하기 위한 변수
	int touchStartX;//캐릭터 이동을 위한 변수
	
	
	Stage[] stage;
	int now;//현재 사용하는 스테이지의 인덱스를 기억
	
	Score score;
	int passedWallIndex;//고난도 벽 통과에 대한 점수 할당을 위한 변수
	
	int backgroundY[];//배경의 무한스크롤을 위한 변수

	DrawThread drawThread;
	SetStageThread  setStageThread;
	
	public PlayGame(Context context) {
		super(context);
		holder=getHolder();
		holder.addCallback(this);//콜백을 자신으로 지정	
		this.context=context;	
		soundManager=new SoundManager(context);

		gameState=GAME_READY;
		isPause=false;
		countdown=new Countdown();
		 
		ninja=new Character();
		lastTouchTime=0;
		
		score= new Score();
		passedWallIndex=-1;//지나온 벽 없음
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
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {//왜 써?
		if(drawThread==null){
			drawThread.SizeChange(width,height);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		//for(;;){//무한 포문을 꼭 해야하나?
		drawThread.exit=true;
		setStageThread.exit=true;
		
			try {
				drawThread.interrupt();
				setStageThread.interrupt();
			//	break;
			} catch (Exception e) {;}
		//}
			
			return;
			
			//종료에대해 더 추가해야함
			//게임이 1번 끝나고 다음에 실행할때 실행이 안됨
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
		int touchEndX;//드래그한 만큼의 변위 생성을 위한 변수
		
		switch(event.getAction()){
		
		//더블 클릭 처리
		case MotionEvent.ACTION_DOWN:
			if(ninja.status==Character.MOVING){		
				long thisTime=System.currentTimeMillis();
				touchStartX=(int)event.getX();//터치가 시작된 x좌표를 저장
				
				//250ms안에 다시 클릭한 경우
				if(thisTime-lastTouchTime<250){
					for(int i=0; i<stage[now].wall.length;i++){
						
						//캐릭터와 마주한 벽을 찾음(너무 멀리 있으면 안됨)
						if(stage[now].wall[i].y<ninja.y && ninja.y < stage[now].wall[i].y+Stage.GAP_BETWEEN_WALL*2/3){
							
							//앞에 넘을만한 것이 있으면
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
			
		//드래그 처리
		case MotionEvent.ACTION_MOVE:
			if(ninja.status==Character.MOVING){
				touchEndX=(int)event.getX();//드래그 중인 x좌표를 저장
				ninja.x+=touchEndX-touchStartX;//변위만큼 이동
				
				//캐릭터가 화면 밖으로 나가지 않게
				if(ninja.x<0){
					ninja.x=0;
				}
				else if(ninja.x+Character.SIZE>480){
					ninja.x=480-Character.SIZE;
				}
				
				touchStartX=touchEndX;//드래그가 계속 될 수 있으므로
			}		
			break;	
		}
		
		return true;
	}
	
	void updateGame(){//게임 진행 메서드
		int blockAttr;//충돌메서드에서 반환되는 블록의 속성을 저장 
		
		if(isPause==false){
		//게임상태에따라 다르게 진행
			if(gameState==GAME_READY){
				countdown.setIndex();
				soundManager.play(soundManager.countdown);
				if(countdown.isEnd){
					gameState=GAME_START;
					soundManager.play(soundManager.start);
				}
			}
			else if(gameState==GAME_START){
				//진행 중인 스테이지가 끝나면  스테이지 교체
				if(now!=NO_STAGE_USING && stage[now].wall[stage[now].wall.length-1].y>=810){	
					stage[now].can_use=false;
					now=NO_STAGE_USING;
				}
				
				//현재 사용할 스테이지 선택, 스테이지 진행 속도 증가
				if(now==NO_STAGE_USING){
					for(int i=0;i<stage.length;i++){
						if(stage[i].can_use==true){
							now=i;
							Stage.speed+=Stage.speedInc;
							passedWallIndex=-1;//지나온 벽이 없음
							break;
						}
					}
				}
				
				if(now!=NO_STAGE_USING){
					//충돌 검사 후 게임을 종료할지 결정
					blockAttr=stage[now].getBlockAttrCollisionWithCharacter(
							new Rect(ninja.x+Character.SIZE/2-Character.SIZE_FOR_CHECK_COLLISION/2, 
									ninja.y, 
									ninja.x+Character.SIZE/2+Character.SIZE_FOR_CHECK_COLLISION/2, 
									ninja.y+Character.SIZE_FOR_CHECK_COLLISION));
					
					if(blockAttr==Block.SPIKE){
						if(ninja.status==Character.MOVING){
							ninja.status=Character.DIE_BY_SPIKE;
							soundManager.play(soundManager.ninjaDieBySpike);
							((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);//0.5초 진동
							gameState=GAME_OVER;
						}
					}
					else if(blockAttr==Block.ELECTRONIC){
						ninja.status=Character.DIE_BY_ELECTRONIC;
						soundManager.play(soundManager.ninjaDieByElec);
						((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
						gameState=GAME_OVER;

					}
					
					//이동 중 일정 시간마다 표창 발사
					if(ninja.status==Character.MOVING){
						Long thisTime=System.currentTimeMillis();
						if(thisTime-ninja.lastFireTime>=ninja.fireRate){
							ninja.fireDagger();
							ninja.lastFireTime=thisTime;
							soundManager.play(soundManager.daggerThrow);
						}
					}
					
					//표창 진행, 출돌 확인
					for(int i=0; i<ninja.dagger.length; i++){
						if(ninja.dagger[i].nowUsing){
							ninja.dagger[i].going();
							
							blockAttr=stage[now].getBlockAttrCollisionWithDagger(
									new Rect(ninja.dagger[i].x, 
													ninja.dagger[i].y,
													ninja.dagger[i].x+Dagger.SIZE, 
													ninja.dagger[i].y+Dagger.SIZE));
							
							//어떤 블록과 충돌했을 때
							if(blockAttr!=-1){
								if(blockAttr==Block.ELECTRONIC){
									score.value+=30;//전기벽 폭파 30점
									soundManager.play(soundManager.explosion);
								}
								else if(blockAttr==Block.SPIKE){
									soundManager.play(soundManager.daggerStuck);
								}
								ninja.dagger[i].nowUsing=false;
							}
						}
					}
					
					//점프 상태일 때
					if(ninja.status==Character.JUMPING){
						//착지점에 도달하면
						if(ninja.landingY>=ninja.y){
							ninja.status=Character.MOVING;
							score.value+=30;//점프 성공 30점
						}
						else{
							//착지점과 jumpImgChangePoint를 아래로 내려 캐릭터와 가까워지게 함
							ninja.landingY+=Stage.speed;
							for(int i=0;i<ninja.jumpImgChangePoint.length;i++){
								ninja.jumpImgChangePoint[i]+=Stage.speed;
							}
						}
					}
					
					//캐릭터가 두 블럭 사이 가장 좁은 공간을 통과하면
					for(int i=0;i<stage[now].wall.length;i++){
						//캐릭터가 지나간 벽 중
						if(stage[now].wall[i].y< ninja.y+Character.SIZE 
						   && ninja.y+Character.SIZE<stage[now].wall[i].y+stage[now].wall[i].first.height){ 
							//wall[i]에 블록이 2개 있고
							if(stage[now].wall[i].second.type!=0){
								//두 블록이 모두 부서지지 않았고
								if(stage[now].wall[i].first.isBroken==false && stage[now].wall[i].second.isBroken==false){
									//좁은 틈의 크기가 Block.STD_SIZE
									if(stage[now].wall[i].second.x-(stage[now].wall[i].first.x+stage[now].wall[i].first.width)==Block.STD_SIZE){
										//캐릭터가 그 사이로 지나갔다면
										if(stage[now].wall[i].first.x+stage[now].wall[i].first.width< ninja.x && ninja.x<stage[now].wall[i].second.x){
											//점수 증가 처리된 벽이 아니면
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
					
					//캐릭터 애니메이션 설정
					switch(ninja.status){
					case Character.MOVING:
						ninja.setMoveAnimation();
						break;
						
					case Character.JUMPING:
						ninja.setJumpAnimation();
						break;
					}
					
					//표창 애니메이션 설정
					for(int i=0; i<ninja.dagger.length; i++){
						if(ninja.dagger[i].nowUsing){
							ninja.dagger[i].setSpinAnimation();
						}
					}
					
					//전기벽 애니메이션 설정
					if(stage[now].can_use){
						for(int j=0;j<stage[now].wall.length;j++){
							if(stage[now].wall[j].first.attribute==Block.ELECTRONIC && stage[now].wall[j].first.isBroken==false){
								stage[now].wall[j].first.setElectroicAnimation();
							}
							
							//세컨드가 존재하면
							if(stage[now].wall[j].second.type!=0){
								if(stage[now].wall[j].second.attribute==Block.ELECTRONIC && stage[now].wall[j].second.isBroken==false){
									stage[now].wall[j].second.setElectroicAnimation();
								}
							}
						}
					}
					
					//블록 폭발 애니메이션 설정
					if(stage[now].can_use){
						for(int j=0;j<stage[now].wall.length;j++){
							if(stage[now].wall[j].first.startExplo){
								stage[now].wall[j].first.setExplosionAnimation();
								stage[now].wall[j].first.exploAniY+=Stage.speed;
							}
							
							//세컨드가 존재하면
							if(stage[now].wall[j].second.type!=0){
								if(stage[now].wall[j].second.startExplo){
									stage[now].wall[j].second.setExplosionAnimation();
									stage[now].wall[j].second.exploAniY+=Stage.speed;
								}
							}
						}			
					}
		
					//게임요소들을 아래로 내려 캐릭터가 앞으로 가는 듯한 효과를 줌
					for(int i=0;i<stage[now].wall.length;i++){
						stage[now].wall[i].y+=Stage.speed;
					}
					
					//배경 이동
					backgroundY[0]=(backgroundY[0]+(int)Stage.speed)%1600;
					backgroundY[1]=backgroundY[0]-1600;
					
					//자릿수 설정
					score.setCipher();
				}
			}
			
			if(gameState==GAME_OVER){
				surfaceDestroyed(holder);
				activityMove();
			}
		}
	}
	
	//이너 클래스
	class DrawThread extends Thread{
		final static int BASIC_WIDTH=480;
		final static int BASIC_HEIGHT=800;
		
		SurfaceHolder t_holder;
		
		boolean exit;
		int width, height;
		int realWidth;
		int realHeight;
		float scale;
		
		//여러 이미지(배열일 경우 애니메이션을 위함)
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
			
			realWidth=res.getDisplayMetrics().widthPixels;//뷰의 픽셀 수를 가져옴
			realHeight=res.getDisplayMetrics().heightPixels;
			scale=res.getDisplayMetrics().density;
			
			//각종 이미지를 불러옴
			BitmapDrawable bd= (BitmapDrawable)res.getDrawable(R.drawable.background);
			backgroundImg=bd.getBitmap();
			backgroundImg=Bitmap.createScaledBitmap(backgroundImg, getWidth(), 1600, false);
			
			
			blockImg=new Bitmap[7][];//7= 블록 타입의 수
			blockImg[0]=new Bitmap[5];//전기벽, 5= 전기벽 애니메이션 이미지의 수
			blockImg[1]=new Bitmap[1];//가시벽이라 애니메이션이 없음 (이미지 하나)
			blockImg[2]=new Bitmap[5];
			blockImg[3]=new Bitmap[1];
			blockImg[4]=new Bitmap[5];
			blockImg[5]=new Bitmap[1];
			blockImg[6]=new Bitmap[1];
			
			//가시벽
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall3);
			blockImg[1][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall5);
			blockImg[3][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall7);
			blockImg[5][0]=bd.getBitmap();
			bd= (BitmapDrawable)res.getDrawable(R.drawable.wall8);
			blockImg[6][0]=bd.getBitmap();
			
			//전기벽
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
		
		int convertX(float x){//리턴을 float double int 중 하나로
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
			
			pnt.setAntiAlias(true);//안티알리아스: 색의 경계부분을 부드럽게
			
			while(exit==false){
				updateGame();
				
				synchronized(t_holder){
					canvas=t_holder.lockCanvas();//이게 뭔데 하는건지
					
					if(canvas==null) 
						break;
					
					//배경
					for(int i=0; i<backgroundY.length; i++){
						canvas.drawBitmap(backgroundImg, 0, backgroundY[i], pnt);//배경
					}
					
					//스테이지, 블록 폭발
					if(now!=NO_STAGE_USING && stage[now].can_use==true){
						for(int j=0;j<stage[now].wall.length;j++){
							if(-30<stage[now].wall[j].y && stage[now].wall[j].y<BASIC_HEIGHT){//화면에 대해 -30~BASIC_HEIGHT사이에 위치한 벽만 드로우
								if(stage[now].wall[j].first.isBroken==false){
									
									//타입에 맞게 그려줘야함
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
									
								//세컨드가 존재하면
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
					
					//표창
					for(int i=0; i<ninja.dagger.length; i++){
						if(ninja.dagger[i].nowUsing){
							canvas.drawBitmap(daggerImg[ninja.dagger[i].spinAniIndex], 
															convertX(ninja.dagger[i].x), 
															convertY(ninja.dagger[i].y), 
															pnt);
						}
					}
					
					//캐릭터
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
					
					
					//점수
					for(int i=0; i< score.cipher.length; i++){
						//0이 아닌 부분부터 출력
						//아무것도 출력 안되면 허전하므로 언제나 0인 score.cipher[score.cipher.length-1].n은 항상 출력
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
					
					//카운트 다운
					if(gameState==GAME_READY){
						canvas.drawBitmap(countdownImg[countdown.index],
														convertX(countdown.x),
														convertY(countdown.y),
														pnt);
					}
					
					//게임 오버
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
	
	//이너 클래스
	class SetStageThread extends Thread{
		boolean exit;
		Wall original_wall[];
		int height;
		
		public SetStageThread(){
			exit=false;
			original_wall=new Wall[105];
			
			int b_t1=2, b_p1=0;//block_type, block_pos
			int b_t2=2, b_p2=0;
			int rb_t2, rb_p2;//b_t2, b_p2의 값 기억용
			
			int[][] n ={
					{35, 50, 60, 66, 69, 69},
					{70, 80, 86, 89, 89},
					{90, 96, 99, 99},
					{100, 103, 103},
					{104, 104}				
			};
			
			int i, j, k;
			
			//블럭이 하나인 벽 초기화
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
			 |*333*****|  이런 식으로 초기화 됨
			 */
			
			//블럭이 둘인 벽 초기화	
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
			
			//현재 사용하지 않는 스테이지를 초기화
			while(exit!=true){
				for(i=0;i<stage.length;i++){
					if(stage[i].can_use==false){
						for(j=0;j<stage[i].wall.length;j++){
							rd=rand.nextInt(original_wall.length);
							
							//카피			
							stage[i].wall[j].first.copy(original_wall[rd].first);
							stage[i].wall[j].second.copy(original_wall[rd].second);
						}
						
						//각 벽의 높이 초기화
						stage[i].wall[0].y=0;//화면 끝
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