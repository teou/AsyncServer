package ukm.teou.async.server.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import ukm.teou.async.client.select.Client;

public class Server {

	public static volatile int WORK_NUM = 4;
	public volatile static String IP = "127.0.0.1";
	public volatile static int PORT = 23876;
	public static final Charset CHAR_SET =  Charset.forName("UTF8");
//	public static final CharsetDecoder DECODER  =  CHAR_SET.newDecoder();
	
	public static AsynchronousServerSocketChannel server;
	public static final AcceptHandler ACCEPTOR = new AcceptHandler();
	public static final ReadHandler READER = new ReadHandler();
	public static final WriteHandler WRITER = new WriteHandler();
	private AsynchronousChannelGroup group;
	public static final ConcurrentHashMap<AsynchronousSocketChannel, Connection> connections = 
			new ConcurrentHashMap<AsynchronousSocketChannel, Connection>();
	public static final AtomicInteger reqCount = new AtomicInteger(0);

	public Server() throws IOException {
//		AsynchronousChannelGroup.withFixedThreadPool(nThreads, threadFactory)
		group = AsynchronousChannelGroup.withThreadPool(
				Executors.newFixedThreadPool(WORK_NUM));
		server = AsynchronousServerSocketChannel.open(group).bind(
		new InetSocketAddress(IP, PORT));
	}

	public void start()  {
		
		System.out.println("Server listen on " + IP+", "+PORT);
		// for future connections
		server.accept(new Connection(), ACCEPTOR);

		// do reading
//		try {
//			group.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		while(true){
			try {
				Runtime r = Runtime.getRuntime();
				long m = 1024*1024, f = r.freeMemory(), t = r.totalMemory();
				long usedMem = (t-f)/m;
				Thread.sleep(1000);
				System.out.println("monitor, connum="+connections.size()
						+", used_mem="+usedMem+", req_count="+reqCount.getAndSet(0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void closeConnection(AsynchronousSocketChannel channel){
		try {
			System.err.println("close channel , conn_num="+connections.size());
			channel.close();
			connections.remove(channel);
		} catch (IOException e) {
			System.err.println("!!! error closing channel "+channel);
			e.printStackTrace();
			connections.remove(channel);
			try {
				channel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void main(String args[]){
		try {
			if(args!=null && args.length>=1){
				try{
					WORK_NUM = Integer.valueOf(args[1]);
				}catch(Exception e){}
				String ip = "127.0.0.1";
				try{
					ip = args[2];
				}catch(Exception e){}
				int port = 23876;
				try{
					port = Integer.valueOf(args[3]);
				}catch(Exception e){}
				IP = ip;
				PORT = port;
			}
			new Server().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
