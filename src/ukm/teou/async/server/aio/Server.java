package ukm.teou.async.server.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/*
 *  long conn socket need a heart beat
 */
public class Server {

	public static volatile int WORK_NUM = 4;
	public volatile static String IP = "10.3.144.136"; //10.3.144.136
	public volatile static int PORT = 23876;
	public static final Charset CHAR_SET =  Charset.forName("UTF8");
//	public static final CharsetDecoder DECODER  =  CHAR_SET.newDecoder();
	
	public static AsynchronousServerSocketChannel server;
	public static final AcceptCompleteHandler ACCEPTOR = new AcceptCompleteHandler();
	public static final ReadCompleteHandler READER = new ReadCompleteHandler();
	public static final WriteCompleteHandler WRITER = new WriteCompleteHandler();
	private AsynchronousChannelGroup group;
	public static final ConcurrentHashMap<AsynchronousSocketChannel, Connection> connections = 
			new ConcurrentHashMap<AsynchronousSocketChannel, Connection>();
	public static final AtomicInteger fetchCount = new AtomicInteger(0);
	public static final AtomicInteger oprCount = new AtomicInteger(0);

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
						+", used_mem="+usedMem+", fetch_count="+fetchCount.getAndSet(0)
						+", opr_count="+oprCount.getAndSet(0));
			} catch (InterruptedException e) {
				e.printStackTrace();
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
