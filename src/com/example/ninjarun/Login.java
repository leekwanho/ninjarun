package com.example.ninjarun;

import java.io.*;
import java.net.*;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.*;

public class Login extends Activity {

	private Socket clientSocket;
	static String flag=" ";//로그인이 됬는지 안됬는지 서버로부터 전송받아 저장할 임시 변수.

	PrintWriter out;
	BufferedReader in;
		
	static final int PORT=4911;
	Thread login;
	Boolean threadOnOff=true;
	Boolean serverOnOff=true;
	
	CheckBox idSave;//아이디저장 체크에대한 변수
	SharedPreferences setting;
	EditText idText;
	
	boolean btnClick=false;

	AlertDialog mPopupDlg;//다이얼로그 종료용
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		UserInfo.loginFlag=false;//비로그인으로 초기화
		
		setting=getSharedPreferences("MyPreference",MODE_PRIVATE);

		String s=receiveText("id");//id를 받아옴
		idText=(EditText)findViewById(R.id.id);
		idText.setText(s);
		
		idSave=(CheckBox)findViewById(R.id.idSave);
		if(!s.equals("")){//이전에 저장한 아이디 있다면 체크박스 체크되있게
			idSave.setChecked(true);
		}
		else{//없다면 체크박스 해제
			idSave.setChecked(false);
		}
	}
	
	@SuppressLint("NewApi")
	public void mOnClick(View v){
	
		switch(v.getId()){
		case R.id.login: //로그인 요청
			send();
			btnClick=true;//요청보내고 받기 시작

			EditText idText=(EditText)findViewById(R.id.id);//아이디를 받아옴
			String id=idText.getText().toString();
			
			while(serverOnOff){//응답을 받을때까지 계속 반복.
				if(!flag.equals(" ")){//전달받은 값이 있을때 실행
					if(flag.equals("Y")){//일치
						Toast.makeText(this, "로그인성공!", Toast.LENGTH_SHORT).show();
						UserInfo.id=id;//id전달. 아이디는 모든곳에서 공동으로 사용.
						
						/** 아이디저장 체크시 아이디 저장 */
						idSave=(CheckBox)findViewById(R.id.idSave);
						
						Editor editor=setting.edit();
						if(idSave.isChecked()){//체크가되있고 로그인성공시 아이디 저장
							editor.putString("id", id);
							editor.commit();
							
						}
						else{//체크 안되있으면 초기화
							editor.putString("id", "");
							editor.commit();
						}
						/** ---------------- */
						
						UserInfo.loginFlag=true;//로그인한것으로 설정
						
						startActivity(new Intent(this,GameMenu.class));
						finish();
						break;
					}
					else{//비밀번호 외의 모든 다른값
						Toast.makeText(this, "아이디or비밀번호 틀림  ", Toast.LENGTH_SHORT).show();
						recreate();//로그인 실패시
						break;
					}
				}
			}
			
			if(serverOnOff.equals(false)){
				Toast.makeText(this, "서버가 다운되있습니다", Toast.LENGTH_SHORT).show();
			}
			
			flag=" ";
			
			break;
			
		case R.id.gaip: //회원가입으로 이동
			startActivity(new Intent(this,Gaip.class));
			finish();
			break;
			
		case R.id.alone:
			UserInfo.loginFlag=false;
			startActivity(new Intent(this,NinjaRunActivity.class));
			finish();
			break;
		}
	}
	
	public void send(){//회원가입 요청을 보냄
		EditText idText=(EditText)findViewById(R.id.id);//아이디와 비밀번호를 받아옴
		String id=idText.getText().toString();
		
		EditText passwdText=(EditText)findViewById(R.id.password);
		String passwd=passwdText.getText().toString();
		
		String select="SELECT * FROM userinfo WHERE id='" + id + "' AND passwd='" + passwd + "'";

		try{
			out=new PrintWriter(clientSocket.getOutputStream(),true);
			out.println(select);
		}catch(Exception e){
	    	e.printStackTrace();
		}
	}

	@Override
	 protected void onResume() {  
		super.onResume();

		login = new Thread() {
			
			   public void run() { //스레드 실행구문

					try{
						
						SocketAddress socketAddress=new InetSocketAddress(GameInfo.ipAddress,PORT);
						clientSocket=new Socket();//소켓생성
						clientSocket.connect(socketAddress, 3000);//3초동안 연결이 안되면 서버다운으로 간주
						
						threadOnOff=true;//스레드 무한반복
						
			    		while(threadOnOff){
							if(btnClick){//버튼이 클릭되서 로그인요청 보낼시 작동
				    			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				    			flag=in.readLine();	 //받는값
							}
						}
						
					}catch(Exception e){
						e.printStackTrace();
						serverOnOff=false;
					}
			   }
		};

		login.start();
		
	 }
	

	@Override
	 protected void onPause() {  //앱 종료시
		super.onPause();
		try {
			threadOnOff=false;//스레드종료. 이하모두 같음
			
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
		catch (Exception e){}
	 }
	
	//시작시 아이디 저장된게 있으면 불러옴
	private String receiveText(String input){
		String initText="";
		if(setting.contains(input)){
			initText=setting.getString(input, "");
		}
		
		return initText;
	}
	

	@Override
	public void onBackPressed(){
		
		/** 다이얼로그 셋팅 */
		View view = this.getLayoutInflater().inflate(R.layout.dialog_login, null);//뷰를 가져옴
		
		mPopupDlg=new AlertDialog.Builder(this).create();//생성자
		mPopupDlg.setView(view,0,0,0,0);//여백 생기지 않게하기위함
		mPopupDlg.show();//다이얼로그 실행
		
		Button exitBtn=(Button) view.findViewById(R.id.exit);//다이얼로그에서 종료버튼
		exitBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//다이얼로그없애고
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