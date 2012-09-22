package ukm.teou.async.client.aio;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import ukm.teou.async.server.aio.Connection;
import ukm.teou.async.server.aio.Server;

public class Client {

	public volatile static String IP = Server.IP;
	public volatile static int PORT = Server.PORT;
	public static final int WORK_NUM = 5;
	private static volatile InetSocketAddress remote;
	private static final AtomicInteger writeCount = new AtomicInteger(0);
	
	public static void connectBy(final String word) {
		
		SocketChannel s = null;
		try{
			s = SocketChannel.open(remote);
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			byte[] bytes = word.getBytes("UTF8");
//			byte[] bytes = word.getBytes("UTF8");
//			OutputStream os = s.getOutputStream();
			while(true){
				buffer.clear();
				buffer.put(bytes);
				buffer.flip();
				int written = s.write(buffer);
//				os.write(bytes);
//				os.flush();
//				s.
				if(written>0)
					writeCount.incrementAndGet();
			}
		}catch(IOException e){
			e.printStackTrace();
			System.err.println("Thread "+word+" error, break!");
		}finally{
			if(s!=null){
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println(word+", close socket error!");
				}
			}
		}
		
	}
	
	public static void main(String... args) {
		
		//args 
		System.out.println("start aio client");
		if(args!=null && args.length>=1){
			String ip = "127.0.0.1";
			try{
				ip = args[1];
			}catch(Exception e){}
			int port = 23876;
			try{
				port = Integer.valueOf(args[2]);
			}catch(Exception e){}
			IP = ip;
			PORT = port;
		}
		
		remote = new InetSocketAddress(IP, PORT);
		System.out.println("write to "+remote);
		String[] workers = new String[WORK_NUM];
		for(int i=0;i<workers.length;++i){
			final String name = i%2==0?"hello":"teou-"+i;
			workers[i] = name;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					connectBy(name);
				}
			}, name);
			t.start();
		}
		
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			System.out.println("simple client, write count="+writeCount.getAndSet(0));
		}

	}

}