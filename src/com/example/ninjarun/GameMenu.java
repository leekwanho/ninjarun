package com.example.ninjarun;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import org.json.*;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.view.*;
import android.widget.*;

public class GameMenu extends Activity {
	static final int PORT=4121;
	
	private Socket clientSocket;
	PrintWriter out;
	BufferedReader in;
	Thread scoreThread;//���ھ� �ޱ����� ������
	String receive=" ";//���ھ� ������ ������ ��Ʈ�� ����
	
	int myRank;//�ڽ��� ������� ���庯��
	
	Boolean threadOnOff=true;
	Boolean serverOnOff=true;
	
	AlertDialog mPopupDlg;//���̾�α� �����
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamemenu);

		UserInfo.topRank.clear();//�ʱ�ȭ �����ָ� ������ �۵��Ǵµ��� ��� �����̵�
		
		//���̵� ������ �׿����� ���ھ �ޱ����� ������
		scoreThread = new Thread() {
			   public void run() {

					try{
						SocketAddress socketAddress=new InetSocketAddress(GameInfo.ipAddress,PORT);
						clientSocket=new Socket();//���ϻ���
						clientSocket.connect(socketAddress, 3000);//���������� 3�ʵ���
						
						threadOnOff=true;//������ ��ŷ ���������� ���ѹݺ�
						
						send();//���� ����
						
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

		scoreThread.start();//������ ����
		
		while(serverOnOff){//������ ���������� ��� �ݺ�. �޾Ҵٸ� break�� ��������
			if(!receive.equals(" ")){//���޹��� ���� ������ ����
								
				try {
					JSONObject jb=new JSONObject(receive);//JSON����
					
					UserInfo.myScore=jb.getInt("myscore");//�������̾ƿ�
					
					JSONArray ja=new JSONArray(jb.getString("topscore"));
					//topscore�� �迭�����̹Ƿ� JSON�迭 ����					
					
					for(int i=0;i<10;i++){//ž��ũ���� ����
						UserInfo.topRank.add(ja.getString(i));//ž���ھ ����
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		if(serverOnOff.equals(false)){
			Toast.makeText(this, "��ũ �ҷ����⿡ �����߽��ϴ�", Toast.LENGTH_SHORT).show();
		}
		
		myRank=0;//��ŷ ����Ʈ�� 0
		
		for(int i=0;i<UserInfo.topRank.size();i++){
			if(UserInfo.myScore==Integer.parseInt(UserInfo.topRank.get(i))){//�ڽ��� ���ھ ž10�� ������
				myRank=i+1;//myRank�� ����
				break;
			}
		}
		
		//����Ʈ�� ����� ����
		ListView menuList = (ListView) findViewById(R.id.topRank);
		menuList.setAdapter(new CustomAdapter(this,R.layout.gamerankitem,UserInfo.topRank));
		TextView textView=(TextView)findViewById(R.id.myRank);
		
		//�ڽ��� ��ŷ �� ���ھ� ���
		if(myRank==0){//��ŷ ��ȭ���°� ž10�� ���°�
			textView.setText("�������� : " + UserInfo.myScore + "�� \n��ŷ10����");
		}
		else{//�׿ܿ� ž10�� ����
			textView.setText("�������� : " + UserInfo.myScore + "��\n��ŷ" + myRank + "��");//�ڽ��� ��ŷ �� ���ھ� ���
			menuList.setSelection(myRank-1);//��Ŀ�� �̵�
		}
	}
	
	public void mOnClick(View v){
		
		switch(v.getId()){//Ŭ���� �̵�
		case R.id.recreate_btn:
			recreate();
			break;
			
		case R.id.makerinfo_btn:
			startActivity(new Intent(this,MakerInfo.class));
			break;
			
		case R.id.game_btn:
			// Activity ����
			startActivity(new Intent(this,NinjaRunActivity.class));
			finish();
			break;
		}
	}
		
	public void send(){//���ھ �ޱ����� ���̵� ����
		try{
			out=new PrintWriter(clientSocket.getOutputStream(),true);
			out.println(UserInfo.id);
		}catch(Exception e){
	    	Toast.makeText(this, "send ����", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	 protected void onPause() {
		super.onPause();
		try {
			threadOnOff=false;
			
			if(clientSocket!=null){
				clientSocket.close(); //������ �ݴ´�.
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

//����Ʈ�� �����
class CustomAdapter extends ArrayAdapter<String>{
	
	private LayoutInflater inflater=null;
	private ArrayList<String> info=new ArrayList<String>();
	
	//������ �⺻����
	public CustomAdapter(Context c,int textViewResourceId,ArrayList<String> arrays){
		super(c,textViewResourceId,arrays);
		this.info=arrays;
		this.inflater=LayoutInflater.from(c);

	}
	
	@Override
	public int getCount() {
		return super.getCount();
	}

	@Override
	public String getItem(int position) {
		return super.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v=convertView;
		v=inflater.inflate(R.layout.gamerankitem,null);
		
		String s=info.get(position);
		//�迭�� �ִ°� s�� ����
		((TextView)v.findViewById(R.id.rank_item_menu)).setText(position+1 + "�� ���� : " + s);
		//����Ʈ���� �ؽ�Ʈ�信 �ؽ�Ʈ ����

		switch(position){//1~10�� 1~3���� ��������
		case 0:
			v.setBackgroundResource(R.drawable.gold);
			break;
		case 1:
			v.setBackgroundResource(R.drawable.silver);
			break;
		case 2:
			v.setBackgroundResource(R.drawable.bronze);
			break;		
		}
		
		return v;
	}
	
}