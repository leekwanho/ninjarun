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
	static String flag="A";//ȸ�������� ����� �ȉ���� �����κ��� ���۹޾� ������ �ӽ� ����
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
		case R.id.gaip_gaip: //���Թ�ưŬ�� ���� �޼ҵ� ȣ��

			EditText idText=(EditText)findViewById(R.id.gaip_id);//���̵�� ��й�ȣ�� �޾ƿ�
			String id=idText.getText().toString();
			EditText passwdText=(EditText)findViewById(R.id.gaip_password);
			String passwd=passwdText.getText().toString();
			
			if(id.equals("")||passwd.equals("")){
				Toast.makeText(this, "���̵� �� ��й�ȣ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
				break;
				//���̵� ��й�ȣ ���Է½� �������� ����
			}
			
			send();
			btnClick=true;
			
			while(serverOnOff){//������ ���������� ��� �ݺ�.
				if(flag.equals("Y")){
					Toast.makeText(this, "���Լ���", Toast.LENGTH_SHORT).show();
					startActivity(loginActivity);//����
					finish();
					break;
				}
				else if(flag.equals("N")){
					Toast.makeText(this, "���Խ��� �ٸ����̵� �Է��ϼ���", Toast.LENGTH_SHORT).show();
					recreate();//����
					break;
				}
			}
			
			if(serverOnOff.equals(false)){
				Toast.makeText(this, "������ �ٿ���ֽ��ϴ�", Toast.LENGTH_SHORT).show();
			}
			
			flag="A";
			break;
			
		case R.id.gaip_cancle: //��ҹ�ưŬ�� ���������� ���ư�
			startActivity(loginActivity);
			finish();
			break;
		}	
	}
	
	public void send(){//ȸ������ ��û�� ����
		
		EditText idText=(EditText)findViewById(R.id.gaip_id);//���̵�� ��й�ȣ�� �޾ƿ�
		String id=idText.getText().toString();
		EditText passwdText=(EditText)findViewById(R.id.gaip_password);
		String passwd=passwdText.getText().toString();
		
		String insert="INSERT INTO userinfo(id,passwd) VALUES ('" + id + "','" + passwd + "');";
		//DB���� ������
		
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

		threadOnOff=true;//������ ���ѹݺ�
		
		gaip = new Thread() {

			   public void run() { //������ ���౸��
				   try {
					   SocketAddress socketAddress=new InetSocketAddress(GameInfo.ipAddress,PORT);
					   clientSocket=new Socket();//���ϻ���
					   clientSocket.connect(socketAddress, 3000);//���������� 3�ʵ���

					   while(threadOnOff){//���ѹݺ�
						   if(btnClick){
							   in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						   	   flag=in.readLine();	 //�޴°�
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
			threadOnOff=false;//������ ����. ���� ��� �Ȱ���
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
	public void onBackPressed(){//�ڷΰ���� �α�����������
		startActivity(new Intent(this,Login.class));
		finish();
	}
}