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
	String receive=" ";//�ű�� �޼���  ������ ����ϰ� �������� �ޱ����� ����
	Boolean threadOnOff=true;
	Boolean serverOnOff=true;
	Boolean updateBoolean=true;

	AlertDialog mPopupDlg;//���̾�α� �����
	
	int newScore;//�ű���̸� �� ����� ������ ����
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_over);
		
		TextView textView=(TextView)findViewById(R.id.game_over_text);
		LinearLayout linearLayout=(LinearLayout)findViewById(R.id.game_over_back);

		Intent intent = this.getIntent();   // ���� �ޱ� ���� Intent ����
		newScore = intent.getIntExtra("newScore", 0);//������ ���ھ �޾ƿ�
		
		if(UserInfo.myScore>=newScore){//�ְ����� ���Ž���
			textView.setText("�ְ��������� ���׿�̤� \n�ְ����� : "+UserInfo.myScore + "\n������ : " + newScore);
			linearLayout.setBackgroundResource(R.drawable.gameover_fail);
		}
		
		else{//�ְ��� ���Ž� ��������
			
			//���ο� ���ھ� ���� ������. ����ϷḦ ���������� ���ѹݺ�
			gameOverThread = new Thread() {
				   public void run() {

						try{
							SocketAddress socketAddress=new InetSocketAddress(GameInfo.ipAddress,PORT);
							clientSocket=new Socket();//���ϻ���
							clientSocket.connect(socketAddress, 3000);//���������� 3�ʵ���

							send();
							threadOnOff=true;//������ ��ŷ ���������� ���ѹݺ�
							
				    		while(threadOnOff){

				    			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				    			
				    			if(!in.equals(null)){
					    			receive=in.readLine();//�޴°�
					    			break;
				    			}//���� �޾����� ����
				    					    							    
				    		}

						}catch(Exception e){
							e.printStackTrace();
							serverOnOff=false;//���� �׾��ִٰ� �˸�
						}
				   }
			};

			gameOverThread.start();//������ ����
		
			while(serverOnOff){//������ ���������� ��� �ݺ�. �޾Ҵٸ� break�� ��������
				if(receive.equals("Y")){//���� ���޹޾����� ��������
					updateBoolean=true;//���ż���
					break;
				}
				else if(receive.equals("N")){//���������� ���� ����
					Toast.makeText(this, "���� �������ſ� �����߽��ϴ�.", Toast.LENGTH_SHORT).show();
					updateBoolean=false;	//���Ž���				
					break;
					//���� ���⼭ �ٽ� �������� �ҽ� send();�� gameOverThread�� �����/���� ���ָ��.
					//����� ���н� ���и޼����� ���� �������� ��������
					//���� ���нÿ� ���� �������� ȭ���� �ٸ�
				}
			}
			//�̰Ծ��ٸ� �������� ó������ ������ ���������� ������ω�ٰ� ���ü��� ����
			
			if(serverOnOff.equals(false)){//���� �ٿ�� �޴��� ���ư�
				Toast.makeText(this, "������ �ٿ���ֽ��ϴ�. �޴��� ���ư��ϴ�.", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this,GameMenu.class));//�α� ��Ƽ��Ƽ���ƴ� ���� ��Ƽ��Ƽ���̵�
				finish();
			}
			
			if(updateBoolean){//���� ���ż����� ��������ȭ��
				textView.setText("�ְ��������!\n�������� : " + UserInfo.myScore + "\n���ο����� : " + newScore);
				linearLayout.setBackgroundResource(R.drawable.gameover_success);
				UserInfo.myScore=newScore;//������ ������ ����
			}
			else{//���н�
				textView.setText("�������ſ� �����߽��ϴ�. �̷������� ��� ��Ÿ���� �������ּ���");
				linearLayout.setBackgroundResource(R.drawable.ic_launcher);
			}
		}	
	}
	
	public void send(){//�ְ����Ͻ� DB����
		
		String update="UPDATE userinfo SET score=" + newScore + " WHERE id='" + UserInfo.id + "'";
		//DB���� ������
		
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
			finish();//�޴��� �̵�
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
		
		final Intent intent=new Intent(this,Login.class);
		
		/** ���̾�α� ���� */
		View view = this.getLayoutInflater().inflate(R.layout.dialog_gamemenu, null);//�並 ������

		mPopupDlg=new AlertDialog.Builder(this).create();//������
		mPopupDlg.setView(view,0,0,0,0);//���� ������ �ʰ��ϱ�����
		mPopupDlg.show();//���̾�α� ����
		
		Button exitBtn=(Button) view.findViewById(R.id.exit);//���̾�α׿��� �����ư
		exitBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//���̾�α׾��ְ�
				startActivity(intent);//�α��� ��Ƽ��Ƽ�� ������
				finish();//��Ƽ��Ƽ����
			}			
		});
		
		Button backBtn=(Button) view.findViewById(R.id.back);
		backBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//���̾�α׸� ����
			}			
		});	
	}
	
}