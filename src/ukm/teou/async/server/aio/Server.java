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

public class Server {

	public final static String IP = "127.0.0.1";
	public final static int PORT = 23876;
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
		group = AsynchronousChannelGroup.withThreadPool(
				Executors.newFixedThreadPool(4));
		server = AsynchronousServerSocketChannel.open(group).bind(
		new InetSocketAddress(IP, PORT));
	}

	public void startWithCompletionHandler()  {
		
		System.out.println("Server listen on " + PORT);
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

	public static void main(String args[]){
		try {
			new Server().startWithCompletionHandler();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
