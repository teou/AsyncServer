package ukm.teou.async.server.select;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class Server {
	
	public volatile static int BIND_PORT = 23876;
	public volatile  static String BIND_IP = "127.0.0.1";//10.3.144.136
	public static final int SELECT_TIMEOUT = 2000;
	public static final int MAX_CONN_NUM = 102400;
	
	private final String bindIp;
	private final int bindPort;
	private final int selectTimeout;
	private final int maxConnNum;
	private volatile boolean stop;
	private Selector selector;
	private SelectionKey serverKey;
	private Set<SelectionKey> selectionKeys;
	private ByteBuffer rcvBuffer;
	private ByteBuffer writeBuffer;
	private int reqnum;
	private int eventCount;
	
	public Server(){
		stop = false;
		bindIp = BIND_IP;
		bindPort = BIND_PORT;
		selectTimeout = SELECT_TIMEOUT;
		maxConnNum = MAX_CONN_NUM;
		selectionKeys = new HashSet<SelectionKey>(maxConnNum);
		selector = null;
		serverKey = null;
		rcvBuffer = ByteBuffer.allocate(1024);
		writeBuffer = ByteBuffer.allocate(1024);
		reqnum = 0;
		eventCount = 0;
	}
	
	public void stop(){
		stop = true;
	}
		
	private void beforeStop(){
		serverKey.cancel();
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void registOp(SelectionKey key, int op){
		if(key.attachment()!=null){
			KeyAttr attr = (KeyAttr)key.attachment();
			attr.setState(attr.getState() | op);
		}
		key.interestOps(key.interestOps() | op);
	}
	
	private static void unRegistOp(SelectionKey key, int op){
		if(key.attachment()!=null){
			KeyAttr attr = (KeyAttr)key.attachment();
			attr.setState(attr.getState() & ~op);
		}
		key.interestOps(key.interestOps() & ~op);
	}
	
	private void start(){	
		// create the listening
		try{
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			ServerSocket serverSocket = serverChannel.socket();
			System.out.println("select Server listen on " + bindIp+", "+bindPort);
			serverSocket.bind(new InetSocketAddress(bindIp, bindPort));
			serverChannel.configureBlocking(false);
			// register the selector
			selector = Selector.open();
			serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT, null);
		}catch(IOException e){
			selector = null;
			serverKey = null;
			e.printStackTrace();
			return;
		}
		
		// the server event loop
		System.out.println("server start");
		while(!stop){
			eventCount = 0;
			try {
				eventCount = selector.select();
				if (eventCount <= 0) {
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}			

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				try {
					SelectionKey key = (SelectionKey) it.next();
					it.remove();
					if(key==null) continue;
//					System.out.println("handle "+n+", ready="+key.readyOps()+", inter="+key.interestOps()+", usedMem="+usedMem+", connSize="+selectionKeys.size());
					
					if (key.isAcceptable()) {
						if(selectionKeys.size()>=maxConnNum){
							System.err.println(String.format("reach max conn num : ", maxConnNum));
							closeConnection(key, key.channel());
							continue;
						}
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel channel = server.accept();
						channel.configureBlocking(false);
						KeyAttr attr = new KeyAttr();
						attr.setState(attr.getState() | SelectionKey.OP_READ);
						attr.setConnTime(System.currentTimeMillis());
						SelectionKey registed = channel.register(selector, SelectionKey.OP_READ, attr);
						selectionKeys.add(registed);
//						System.out.println("connect, connNum:"+selectionKeys.size()+", usedMem="+usedMem);
					}
					if (key.isWritable()) {
						KeyAttr keyAttr = (KeyAttr)key.attachment();
						keyAttr.setLastActive(System.currentTimeMillis());
						if(keyAttr==null || keyAttr.isStateNone()) closeConnection(key, key.channel());
						if(keyAttr.isWritable()){
							SocketChannel channel = (SocketChannel) key.channel();
							int writelen = writeToSocket(channel, keyAttr);
							if(writelen>=0){
								unRegistOp(key, SelectionKey.OP_WRITE);
							}else{
								closeConnection(key, channel);
							}
						}
					}
					if (key.isReadable()) {
						KeyAttr keyAttr = (KeyAttr)key.attachment();
						keyAttr.setLastActive(System.currentTimeMillis());
						if(keyAttr==null || keyAttr.isStateNone()) closeConnection(key, key.channel());
						if(keyAttr.isReadable()){
							SocketChannel channel = (SocketChannel) key.channel();
							reqnum++;
							int readlen = readFromSocket(channel, keyAttr);
							if(readlen>0 && keyAttr.getData()!=null){
								registOp(key, SelectionKey.OP_WRITE);
							}else if(readlen==0){
								// close ? or try again?
								closeConnection(key, channel);
							}else{
								closeConnection(key, channel);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				
			}
		}
		
		beforeStop();
		
	}
	
	private static final String HELLO = "hello";
	private static final String WORLD = "world";
	private static final String ERROR_RET = "error command";
	
	private void closeConnection(SelectionKey key, SelectableChannel channel) throws IOException{
		System.err.println("close conn, connnum="+selectionKeys.size());
		selectionKeys.remove(key);
		channel.close();
		key.cancel();
	}
	
	private int writeToSocket(SocketChannel channel, KeyAttr attr) throws IOException{
		if(channel==null) return -1;
		if(attr.getData()!=null){
			String content = HELLO.equals(attr.getData())?
					WORLD:ERROR_RET;
			writeBuffer.clear();
			try {
//				System.out.println("send:"+content+", connNum:"+selectionKeys.size()+", usedMem="+usedMem);
				writeBuffer.put(content.getBytes("UTF8"));
				writeBuffer.flip();
				int written = 0;
				// 当数据非常大的时候一次不一定能写完，可以选择把当前
				// 写到的位置放回KeyAttr,在下次写事件触发的时候写
				while(writeBuffer.hasRemaining()){
					written+=channel.write(writeBuffer);
				}
				return written;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return 0;
			}
		}

		return 0;
	}
	
	private int readFromSocket(SocketChannel channel, KeyAttr attr){
		if(channel==null) return -1;
		int readcount = 0;
		rcvBuffer.clear();
		// 当数据非常大的时候一次不一定能读完，可以选择把当前
		// 读到的位置放回KeyAttr,在下次写事件触发的时候继续读
		try {
			readcount = channel.read(rcvBuffer);
		} catch (IOException e1) {
			e1.printStackTrace();
			return -1;
		}
		rcvBuffer.flip();	
		
		Charset  charset  =  Charset.forName( "UTF8" );
		CharsetDecoder decoder  =  charset.newDecoder();
		CharBuffer charBuffer = null;
		try {
			charBuffer = decoder.decode(rcvBuffer);
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}
		String rcvd = charBuffer.toString();
//		System.out.println("rcv:"+rcvd+", connNum:"+selectionKeys.size()+", usedMem="+usedMem);
		attr.setData(rcvd);
		return readcount;
	}
	
	public int getConnNum(){return selectionKeys.size();}
	public int getRequestCount(){return reqnum;}
	public void clearRequestCount(){reqnum=0;}
	
	public static void main(String args[]) throws IOException {
		
		if(args!=null && args.length>=1){
			String ip = "127.0.0.1";
			try{
				ip = args[1];
			}catch(Exception e){}
			int port = 23876;
			try{
				port = Integer.valueOf(args[2]);
			}catch(Exception e){}
			BIND_IP = ip;
			BIND_PORT = port;
		}
		
		final Server s = new Server();
		Thread watcher = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!s.stop){
					Runtime r = Runtime.getRuntime();
					long m = 1024*1024, f = r.freeMemory(), t = r.totalMemory();
					long usedMem = (t-f)/m;
					System.out.println("watcher, used_mem="+usedMem
							+",con_num="+s.getConnNum()
							+",req_count="+s.getRequestCount()
							+",eventCount="+s.eventCount);
					s.clearRequestCount();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		watcher.start();
		s.start();
	}
	
}
