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
	//判断Service是否被销毁
	private boolean mIsServiceDestroy = false;
	private String[] mDefinedMessage = new String[]{
			"你好呀，Hello!",
			"请问你叫啥呀",
			"今天北京天气不错啊",
			"你知道吗？我可是可以和很多人一起聊天的呀"
	};
	
	private class TcpService implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ServerSocket serverSocket = null;
			try{
				//约定的端口
				serverSocket = new ServerSocket(8688);
			}catch (IOException e) {
				// TODO: handle exception
				System.err.println("establish tcp server failed,port:8688");
				e.printStackTrace();
				return;
			}
			
			while (!mIsServiceDestroy) {
				try {
					//阻塞等待客户端的连接
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
		//客户端输入的流
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//从服务端输出到客户端的流
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true) ;
		//一进聊天首先输出文字
		out.println("欢迎来到聊天室");
		while(!mIsServiceDestroy){
			//读输入流
			String str = in.readLine();
			System.out.println("msg from client:"+str);
			if(str == null){
				break;
			}
			
			int i = new Random().nextInt(mDefinedMessage.length);
			String msg = mDefinedMessage[i];
			System.out.println("Send:"+msg);
			//将信息写入输出流
			out.println(msg);

		}
		System.out.println("client quit");
		out.close();
		in.close(); 
		socket.close();
	}
	
	@Override
	public void onCreate(){
		//Service还是在主线程中工作，因此网络连接这种耗时工作将要在子线程中完成
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
