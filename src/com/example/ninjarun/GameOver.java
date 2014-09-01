package com.example.ninjarun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameOver extends Activity {
	static final int PORT=3327;
	private Socket clientSocket;

	PrintWriter out;
	BufferedReader in;
	
	Thread gameOverThread;
	String receive=" ";//신기록 달성시  서버에 기록하고 성공여부 받기위한 변수
	Boolean threadOnOff=true;
	Boolean serverOnOff=true;
	Boolean updateBoolean=true;

	AlertDialog mPopupDlg;//다이얼로그 종료용
	
	int newScore;//신기록이면 그 기록을 저장할 변수
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_over);
		
		TextView textView=(TextView)findViewById(R.id.game_over_text);
		LinearLayout linearLayout=(LinearLayout)findViewById(R.id.game_over_back);

		Intent intent = this.getIntent();   // 값을 받기 위한 Intent 생성
		newScore = intent.getIntExtra("newScore", 0);//게임후 스코어를 받아옴
		
		if(UserInfo.myScore>=newScore){//최고점수 갱신실패
			textView.setText("최고점수보다 낮네요ㅜㅜ \n최고점수 : "+UserInfo.myScore + "\n내점수 : " + newScore);
			linearLayout.setBackgroundResource(R.drawable.gameover_fail);
		}
		
		else{//최고기록 갱신시 서버전달
			
			//새로운 스코어 갱신 스레드. 수행완료를 받을때까지 무한반복
			gameOverThread = new Thread() {
				   public void run() {

						try{
							SocketAddress socketAddress=new InetSocketAddress(GameInfo.ipAddress,PORT);
							clientSocket=new Socket();//소켓생성
							clientSocket.connect(socketAddress, 3000);//서버연결은 3초동안

							send();
							threadOnOff=true;//스레드 랭킹 받을때까지 무한반복
							
				    		while(threadOnOff){

				    			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				    			
				    			if(!in.equals(null)){
					    			receive=in.readLine();//받는값
					    			break;
				    			}//값을 받았으면 종료
				    					    							    
				    		}

						}catch(Exception e){
							e.printStackTrace();
							serverOnOff=false;//서버 죽어있다고 알림
						}
				   }
			};

			gameOverThread.start();//스레드 실행
		
			while(serverOnOff){//응답을 받을때까지 계속 반복. 받았다면 break로 빠져나옴
				if(receive.equals("Y")){//값을 전달받았을때 빠져나감
					updateBoolean=true;//갱신성공
					break;
				}
				else if(receive.equals("N")){//서버오류로 인해 실패
					Toast.makeText(this, "서버 점수갱신에 실패했습니다.", Toast.LENGTH_SHORT).show();
					updateBoolean=false;	//갱신실패				
					break;
					//만약 여기서 다시 재전송을 할시 send();와 gameOverThread를 재생성/실행 해주면됨.
					//현재는 실패시 실패메세지만 띄우고 재전송은 하지않음
					//갱신 실패시에 따라 보여지는 화면은 다름
				}
			}
			//이게없다면 서버에서 처리된후 응답을 보내기전에 점수기록됬다고 나올수도 있음
			
			if(serverOnOff.equals(false)){//서버 다운시 메뉴로 돌아감
				Toast.makeText(this, "서버가 다운되있습니다. 메뉴로 돌아갑니다.", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this,GameMenu.class));//로긴 엑티비티가아닌 게임 엑티비티로이동
				finish();
			}
			
			if(updateBoolean){//서버 갱신성공시 보여지는화면
				textView.setText("최고점수기록!\n이전점수 : " + UserInfo.myScore + "\n새로운점수 : " + newScore);
				linearLayout.setBackgroundResource(R.drawable.gameover_success);
				UserInfo.myScore=newScore;//정보에 새점수 갱신
			}
			else{//실패시
				textView.setText("서버갱신에 실패했습니다. 이런현상이 계속 나타날시 문의해주세요");
				linearLayout.setBackgroundResource(R.drawable.ic_launcher);
			}
		}	
	}
	
	public void send(){//최고기록일시 DB수정
		
		String update="UPDATE userinfo SET score=" + newScore + " WHERE id='" + UserInfo.id + "'";
		//DB명은 유동적
		
		try{
			out=new PrintWriter(clientSocket.getOutputStream(),true);
			out.println(update);
		}catch(Exception e){
	    	e.printStackTrace();
		}
	}
	
	public void mOnClick(View v){
		
		switch(v.getId()){
		case R.id.menubtn:
			startActivity(new Intent(this,GameMenu.class));
			finish();//메뉴로 이동
			break;
			
		case R.id.gamebtn:
			startActivity(new Intent(this,NinjaRunActivity.class));
			finish();
			break;
		}		
	}
	
	@Override
	 protected void onPause() {
		super.onPause();
		
		try {
			threadOnOff=false;//스레드 종료. 이하 모두 똑같음
			if(clientSocket!=null){
				clientSocket.close();
			}
			if(in!=null){
				in.close();
			}
			if(out!=null){
				out.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	 }
	
	
	@Override
	public void onBackPressed(){//뒤로가기시 로그인페이지로
		
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