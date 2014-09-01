package com.example.ninjarun;

import android.graphics.Rect;

public class Stage{
	final static int GAP_BETWEEN_WALL=250;
	static double speed;
	static double speedInc;
	
	Wall wall[];	
	boolean can_use;

	public Stage(){
		can_use=false;
		speed=3;
		speedInc=0.5;
		wall = new Wall[30];
		
		for(int i=0;i<wall.length;i++){
			wall[i]=new Wall();
		}	
	}
	
	int getBlockAttrCollisionWithCharacter(Rect obj){
		Rect rectFirst;
		Rect rectSecond;
		
		for(int i=0;i<wall.length;i++){
			if(wall[i].first.isBroken==false){
				rectFirst= new Rect(
						wall[i].first.x, 
						wall[i].y, 
						wall[i].first.x+wall[i].first.width, 
						wall[i].y+wall[i].first.height
						);
				
				if(rectFirst.intersect(obj)){
					return wall[i].first.attribute;
				}
			}
			
			if(wall[i].second.type!=0 && wall[i].second.isBroken==false){
				rectSecond= new Rect(
						wall[i].second.x, 
						wall[i].y, 
						wall[i].second.x+wall[i].second.width, 
						wall[i].y+wall[i].second.height
						);
				
				if(rectSecond.intersect(obj)){
					return wall[i].second.attribute;
				}
			}
		}

		return -1;
	}
	
	int getBlockAttrCollisionWithDagger(Rect obj){
		Rect rectFirst1, rectFirst2;
		Rect rectSecond1, rectSecond2;
		
		for(int i=0;i<wall.length;i++){
			if(wall[i].first.isBroken==false && wall[i].first.attribute==Block.ELECTRONIC){
				//전기벽 이미지의 왼쪽 전기생성 부분
				rectFirst1= new Rect(
						wall[i].first.x, 
						wall[i].y, 
						wall[i].first.x+wall[i].first.height, //height를 가로길이로 사용한것은 정사각형을 만들기 위함
						wall[i].y+wall[i].first.height
						);
				
				//전기벽 이미지의 오른쪽 전기생성 부분
				rectFirst2= new Rect(
						wall[i].first.x+wall[i].first.width-wall[i].first.height, 
						wall[i].y, 
						wall[i].first.x+wall[i].first.width, 
						wall[i].y+wall[i].first.height
						);
				
				if(rectFirst1.intersect(obj) ){
					wall[i].first.isBroken=true;	
					wall[i].first.startExplosion(rectFirst1.centerX(), rectFirst1.centerY());//해당 위치에서 폭발 시작
					return Block.ELECTRONIC;
				}
				else if(rectFirst2.intersect(obj)){
					wall[i].first.isBroken=true;	
					wall[i].first.startExplosion(rectFirst2.centerX(), rectFirst2.centerY());//해당 위치에서 폭발 시작
					return Block.ELECTRONIC;
				}
			}
			//가시벽에 부딪힐 경우
			else if(wall[i].first.isBroken==false && wall[i].first.attribute==Block.SPIKE){
				rectFirst1= new Rect(
						wall[i].first.x, 
						wall[i].y, 
						wall[i].first.x+wall[i].first.width, 
						wall[i].y+wall[i].first.height
						);
				
				if(rectFirst1.intersect(obj)){
					return Block.SPIKE;
				}
			}
			
			if(wall[i].second.type!=0){
				if(wall[i].second.isBroken==false && wall[i].second.attribute==Block.ELECTRONIC){
					//전기벽 이미지의 왼쪽 전기생성 부분
					rectSecond1= new Rect(
							wall[i].second.x, 
							wall[i].y, 
							wall[i].second.x+wall[i].second.height, 
							wall[i].y+wall[i].second.height
							);
					
					//전기벽 이미지의 오른쪽 전기생성 부분
					rectSecond2= new Rect(
							wall[i].second.x+wall[i].second.width-wall[i].second.height, 
							wall[i].y, 
							wall[i].second.x+wall[i].second.width, 
							wall[i].y+wall[i].second.height
							);
					
					if(rectSecond1.intersect(obj)){
						wall[i].second.isBroken=true;
						wall[i].second.startExplosion(rectSecond1.centerX(), rectSecond1.centerY());//해당 위치에서 폭발 시작
						return Block.ELECTRONIC;
					}
					else if(rectSecond2.intersect(obj)){
						wall[i].second.isBroken=true;
						wall[i].second.startExplosion(rectSecond2.centerX(), rectSecond2.centerY());//해당 위치에서 폭발 시작
						return Block.ELECTRONIC;
					}
				}
				else if(wall[i].second.isBroken==false && wall[i].second.attribute==Block.SPIKE){
					rectSecond1= new Rect(
							wall[i].second.x, 
							wall[i].y, 
							wall[i].second.x+wall[i].second.width, 
							wall[i].y+wall[i].second.height
							);
					
					if(rectSecond1.intersect(obj)){
						return Block.SPIKE;
					}
				}
			}
		}

		return -1;
	}
}

class Wall{
	int y;
	Block first, second=null;
	static final int START_POINT=0;

	Wall(){
		this(0,0);
	}
	
	Wall(int block_type1, int block_pos){
		this(block_type1, 0, block_pos, 0);
	}
	
	Wall(int block_type1, int block_type2, int block_pos1, int block_pos2){
		first = new Block(block_type1);
		first.x = START_POINT+Block.STD_SIZE*block_pos1;
		
		second = new Block(block_type2);//두번째 블럭이 없는 벽이면 세컨드의 타입은 0이됨
		second.x = START_POINT+Block.STD_SIZE*block_pos2;
	}
}

class Block{
	static final int STD_SIZE=48;
	static final int SPIKE=0;
	static final int ELECTRONIC=1;
	static final int ELEC_ANI_INDEX_CHANGE_RATE=200;
	static final int EXPLO_ANI_INDEX_CHANGE_RATE=200;
	static final int EXPLO_SIZE=60;
	
	int type;
	int attribute;
	boolean isBroken;//고장 유무(캐릭터를 방해할 수 있나 없나)
	int x;//y는 레이어에 있음
	int width, height;
	
	int elecAniIndex;
	long lastElecAniIndexChangedTime;
	
	boolean startExplo;
	int exploAniX;
	int exploAniY;
	int explosionAniIndex;//폭발 애니메이션이미지 배열의 인덱스
	long lastExploAniIndexChangedTime;
	
	public Block(){}
	
	public Block(int type){
		isBroken=false;
		
		this.type = type;
		if(type==2 || type==4 || type==6){
			attribute=ELECTRONIC;
		}
		else if(type==3 || type==5 || type==7 || type==8){
			attribute=SPIKE;
		}
		else//타입이 0이면
			attribute=-1;
		
		width=type*STD_SIZE;
		height=STD_SIZE/2;
		
		elecAniIndex=0;
		lastElecAniIndexChangedTime=0;
		
		startExplo=false;
		exploAniX=0;
		exploAniY=0;
		explosionAniIndex=-1;
		lastExploAniIndexChangedTime=0;
	}
	
	void copy(Block b){
		this.type=b.type;
		this.x=b.x;
		this.attribute=b.attribute;
		this.isBroken=b.isBroken;
		this.height=b.height;
		this.width=b.width;
		
		this.elecAniIndex=b.elecAniIndex;
		this.lastElecAniIndexChangedTime=b.lastElecAniIndexChangedTime;
		
		this.startExplo=b.startExplo;
		this.exploAniX=b.exploAniX;
		this.exploAniY=b.exploAniY;
		this.explosionAniIndex=b.explosionAniIndex;
		this.lastExploAniIndexChangedTime=b.lastExploAniIndexChangedTime;
	}
	
	void setElectroicAnimation(){
		Long thisTime;
		
		if(isBroken==false){
			thisTime=System.currentTimeMillis();
			
			if(thisTime-lastElecAniIndexChangedTime>=ELEC_ANI_INDEX_CHANGE_RATE){
				elecAniIndex=(elecAniIndex+1)%5;//5는 이미지배열의 크기
				lastElecAniIndexChangedTime=thisTime;
			}
		}
	}
	
	void startExplosion(int x, int y){
		startExplo=true;
		exploAniX=x-EXPLO_SIZE/2;
		exploAniY=y-EXPLO_SIZE/2;
	}
	
	void setExplosionAnimation(){
		Long thisTime;
		
		if(startExplo){
			thisTime=System.currentTimeMillis();
			
			//프레임이 바뀔 시간이 되면
			if(thisTime-lastExploAniIndexChangedTime>=EXPLO_ANI_INDEX_CHANGE_RATE){
				if(explosionAniIndex<2){//2는 이미지배열의 마지막 인덱스
					explosionAniIndex++;
					lastExploAniIndexChangedTime=thisTime;
				}
				else if(explosionAniIndex==2){//폭발 끝
					startExplo=false;
				}
			}
		}
	}
}