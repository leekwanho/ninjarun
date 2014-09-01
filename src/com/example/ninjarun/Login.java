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
	static String flag=" ";//�α����� ����� �ȉ���� �����κ��� ���۹޾� ������ �ӽ� ����.

	PrintWriter out;
	BufferedReader in;
		
	static final int PORT=4911;
	Thread login;
	Boolean threadOnOff=true;
	Boolean serverOnOff=true;
	
	CheckBox idSave;//���̵����� üũ������ ����
	SharedPreferences setting;
	EditText idText;
	
	boolean btnClick=false;

	AlertDialog mPopupDlg;//���̾�α� �����
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		UserInfo.loginFlag=false;//��α������� �ʱ�ȭ
		
		setting=getSharedPreferences("MyPreference",MODE_PRIVATE);

		String s=receiveText("id");//id�� �޾ƿ�
		idText=(EditText)findViewById(R.id.id);
		idText.setText(s);
		
		idSave=(CheckBox)findViewById(R.id.idSave);
		if(!s.equals("")){//������ ������ ���̵� �ִٸ� üũ�ڽ� üũ���ְ�
			idSave.setChecked(true);
		}
		else{//���ٸ� üũ�ڽ� ����
			idSave.setChecked(false);
		}
	}
	
	@SuppressLint("NewApi")
	public void mOnClick(View v){
	
		switch(v.getId()){
		case R.id.login: //�α��� ��û
			send();
			btnClick=true;//��û������ �ޱ� ����

			EditText idText=(EditText)findViewById(R.id.id);//���̵� �޾ƿ�
			String id=idText.getText().toString();
			
			while(serverOnOff){//������ ���������� ��� �ݺ�.
				if(!flag.equals(" ")){//���޹��� ���� ������ ����
					if(flag.equals("Y")){//��ġ
						Toast.makeText(this, "�α��μ���!", Toast.LENGTH_SHORT).show();
						UserInfo.id=id;//id����. ���̵�� �������� �������� ���.
						
						/** ���̵����� üũ�� ���̵� ���� */
						idSave=(CheckBox)findViewById(R.id.idSave);
						
						Editor editor=setting.edit();
						if(idSave.isChecked()){//üũ�����ְ� �α��μ����� ���̵� ����
							editor.putString("id", id);
							editor.commit();
							
						}
						else{//üũ �ȵ������� �ʱ�ȭ
							editor.putString("id", "");
							editor.commit();
						}
						/** ---------------- */
						
						UserInfo.loginFlag=true;//�α����Ѱ����� ����
						
						startActivity(new Intent(this,GameMenu.class));
						finish();
						break;
					}
					else{//��й�ȣ ���� ��� �ٸ���
						Toast.makeText(this, "���̵�or��й�ȣ Ʋ��  ", Toast.LENGTH_SHORT).show();
						recreate();//�α��� ���н�
						break;
					}
				}
			}
			
			if(serverOnOff.equals(false)){
				Toast.makeText(this, "������ �ٿ���ֽ��ϴ�", Toast.LENGTH_SHORT).show();
			}
			
			flag=" ";
			
			break;
			
		case R.id.gaip: //ȸ���������� �̵�
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
	
	public void send(){//ȸ������ ��û�� ����
		EditText idText=(EditText)findViewById(R.id.id);//���̵�� ��й�ȣ�� �޾ƿ�
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
			
			   public void run() { //������ ���౸��

					try{
						
						SocketAddress socketAddress=new InetSocketAddress(GameInfo.ipAddress,PORT);
						clientSocket=new Socket();//���ϻ���
						clientSocket.connect(socketAddress, 3000);//3�ʵ��� ������ �ȵǸ� �����ٿ����� ����
						
						threadOnOff=true;//������ ���ѹݺ�
						
			    		while(threadOnOff){
							if(btnClick){//��ư�� Ŭ���Ǽ� �α��ο�û ������ �۵�
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

		login.start();
		
	 }
	

	@Override
	 protected void onPause() {  //�� �����
		super.onPause();
		try {
			threadOnOff=false;//����������. ���ϸ�� ����
			
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
	
	//���۽� ���̵� ����Ȱ� ������ �ҷ���
	private String receiveText(String input){
		String initText="";
		if(setting.contains(input)){
			initText=setting.getString(input, "");
		}
		
		return initText;
	}
	

	@Override
	public void onBackPressed(){
		
		/** ���̾�α� ���� */
		View view = this.getLayoutInflater().inflate(R.layout.dialog_login, null);//�並 ������
		
		mPopupDlg=new AlertDialog.Builder(this).create();//������
		mPopupDlg.setView(view,0,0,0,0);//���� ������ �ʰ��ϱ�����
		mPopupDlg.show();//���̾�α� ����
		
		Button exitBtn=(Button) view.findViewById(R.id.exit);//���̾�α׿��� �����ư
		exitBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//���̾�α׾��ְ�
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