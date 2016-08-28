package com.example.ipcforsocket.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TPCService extends Service{
	//�ж�Service�Ƿ�����
	private boolean mIsServiceDestroy = false;
	private String[] mDefinedMessage = new String[]{
			"���ѽ��Hello!",
			"�������ɶѽ",
			"���챱����������",
			"��֪�����ҿ��ǿ��Ժͺܶ���һ�������ѽ"
	};
	
	private class TcpService implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ServerSocket serverSocket = null;
			try{
				//Լ���Ķ˿�
				serverSocket = new ServerSocket(8688);
			}catch (IOException e) {
				// TODO: handle exception
				System.err.println("establish tcp server failed,port:8688");
				e.printStackTrace();
				return;
			}
			
			while (!mIsServiceDestroy) {
				try {
					//�����ȴ��ͻ��˵�����
					final Socket client = serverSocket.accept();
					System.out.print("accept");
					new Thread(){
						@Override
						public void run(){
							try {
								responseClient(client);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}.start();
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private void responseClient(Socket socket)throws IOException{
		//�ͻ����������
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//�ӷ����������ͻ��˵���
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true) ;
		//һ�����������������
		out.println("��ӭ����������");
		while(!mIsServiceDestroy){
			//��������
			String str = in.readLine();
			System.out.println("msg from client:"+str);
			if(str == null){
				break;
			}
			
			int i = new Random().nextInt(mDefinedMessage.length);
			String msg = mDefinedMessage[i];
			System.out.println("Send:"+msg);
			//����Ϣд�������
			out.println(msg);

		}
		System.out.println("client quit");
		out.close();
		in.close(); 
		socket.close();
	}
	
	@Override
	public void onCreate(){
		//Service���������߳��й�������������������ֺ�ʱ������Ҫ�����߳������
		new Thread(new TcpService()).start();
		super.onCreate();
	}
	
	@Override
	public void onDestroy(){
		mIsServiceDestroy = true;
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
