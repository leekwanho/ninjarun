package com.example.ninjarun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Gaip extends Activity {

	private Socket clientSocket;
	static String flag="A";//회원가입이 됬는지 안됬는지 서버로부터 전송받아 저장할 임시 변수
	static final int PORT=4494;

	PrintWriter out;
	BufferedReader in;
	
	Thread gaip;
	Boolean threadOnOff=true;
	Boolean serverOnOff=true;
	
	boolean btnClick=false;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gaip);
	}
	
	@SuppressLint("NewApi")
	public void mOnClick(View v){
		
		Intent loginActivity=new Intent(this,Login.class);
		
		switch(v.getId()){
		case R.id.gaip_gaip: //가입버튼클릭 가입 메소드 호출

			EditText idText=(EditText)findViewById(R.id.gaip_id);//아이디와 비밀번호를 받아옴
			String id=idText.getText().toString();
			EditText passwdText=(EditText)findViewById(R.id.gaip_password);
			String passwd=passwdText.getText().toString();
			
			if(id.equals("")||passwd.equals("")){
				Toast.makeText(this, "아이디 및 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
				break;
				//아이디나 비밀번호 미입력시 실행하지 않음
			}
			
			send();
			btnClick=true;
			
			while(serverOnOff){//응답을 받을때까지 계속 반복.
				if(flag.equals("Y")){
					Toast.makeText(this, "가입성공", Toast.LENGTH_SHORT).show();
					startActivity(loginActivity);//성공
					finish();
					break;
				}
				else if(flag.equals("N")){
					Toast.makeText(this, "가입실패 다른아이디 입력하세요", Toast.LENGTH_SHORT).show();
					recreate();//실패
					break;
				}
			}
			
			if(serverOnOff.equals(false)){
				Toast.makeText(this, "서버가 다운되있습니다", Toast.LENGTH_SHORT).show();
			}
			
			flag="A";
			break;
			
		case R.id.gaip_cancle: //취소버튼클릭 전페이지로 돌아감
			startActivity(loginActivity);
			finish();
			break;
		}	
	}
	
	public void send(){//회원가입 요청을 보냄
		
		EditText idText=(EditText)findViewById(R.id.gaip_id);//아이디와 비밀번호를 받아옴
		String id=idText.getText().toString();
		EditText passwdText=(EditText)findViewById(R.id.gaip_password);
		String passwd=passwdText.getText().toString();
		
		String insert="INSERT INTO userinfo(id,passwd) VALUES ('" + id + "','" + passwd + "');";
		//DB명은 유동적
		
		try{
			out=new PrintWriter(clientSocket.getOutputStream(),true);
			out.println(insert);			
		}catch(Exception e){
	    	e.printStackTrace();
		}
	}
	
	@Override
	 protected void onResume() {
		super.onResume();

		threadOnOff=true;//스레드 무한반복
		
		gaip = new Thread() {

			   public void run() { //스레드 실행구문
				   try {
					   SocketAddress socketAddress=new InetSocketAddress(GameInfo.ipAddress,PORT);
					   clientSocket=new Socket();//소켓생성
					   clientSocket.connect(socketAddress, 3000);//서버연결은 3초동안

					   while(threadOnOff){//무한반복
						   if(btnClick){
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

		 gaip.start();

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
		startActivity(new Intent(this,Login.class));
		finish();
	}
}