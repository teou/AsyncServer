/**  
 * Client.java  
 * ukm.teou.async  
 *  
 * Function：   
 *  
 *   ver     date           author  
 * ──────────────────────────────────  
 *           2012-9-18        teouli  
 *  
 * Copyright (c) 2012, TNT All Rights Reserved.  
 */

package ukm.teou.async.server.select;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName:Client Function: Reason:
 * 
 * @author teouli
 * @version
 * @since Ver 1.1
 * @Date 2012-9-18 下午4:42:45
 * 
 * @see
 */
public class Client {

	public static final long SLEEP_BEFORE_WRITE = 5;
	//10.3.144.136
	private static InetSocketAddress ip = new InetSocketAddress(Server.BIND_IP, Server.BIND_PORT);
	
	private static AtomicInteger writeCount = new AtomicInteger(0);

	static class Message implements Runnable {
		protected String name;
		String msg = "";

		public Message(String index) {
			this.name = index;
		}

		public void run() {
			Selector selector = null;
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			SocketChannel client = null;
			try {
				// 打开Socket通道
				client = SocketChannel.open();
				// 设置为非阻塞模式
				client.configureBlocking(false);
				// 打开选择器
				selector = Selector.open();
				// 注册连接服务端socket动作
				client.register(selector, SelectionKey.OP_CONNECT);
				// 连接
				boolean connected = client.connect(ip);
//				System.out.println("connected="+connected);
				// 分配内存
//				buffer = ByteBuffer.allocate(100);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("conn failed!");
				return;
			}
			

				long start = System.currentTimeMillis();
				_FOR: for (;;) {
					int n = 0;
					try{
						n = selector.select(2000);
					}catch(IOException e){
						e.printStackTrace();
						System.out.println("select error!");
						continue;
					}
					if(n<=0){
//						System.out.println("empty....");
						continue;
					}
					Iterator iter = selector.selectedKeys().iterator();
					while (iter.hasNext()) {
						SelectionKey key = (SelectionKey) iter.next();
//						System.out.println("eventsize="+n+",key="+key.readyOps());
						iter.remove();
						if (key.isConnectable()) {
								SocketChannel channel = (SocketChannel) key.channel();
								if (channel.isConnectionPending()){
									boolean connected = false;
									try {
										connected = channel.finishConnect();
									} catch (IOException e1) {
										e1.printStackTrace();
										System.err.println("con key11 error! "+name);
										connected = false;
									}
									try {
										if(connected){
											byte[] aaa = name.getBytes("UTF8");
//											System.out.println("conn write:"+name);
											channel.write(buffer.wrap(aaa));
											// 写完可以监听读，server的响应就可以在下个select()时段检测到了
											channel.register(selector, SelectionKey.OP_READ);
										}else{
											continue;
										}
									}catch(IOException e){
										e.printStackTrace();
										System.err.println("con key22 error! "+name);
										return;
									}
								}
						} 
						else if (key.isReadable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							buffer.clear();
							int count;
							try {
								count = channel.read(buffer);
								if (count > 0) {
									buffer.flip();
									while (buffer.remaining() > 0) {
										byte b = buffer.get();
										msg += (char) b;
									}
									long last = System.currentTimeMillis() - start;
//									System.out.println("read "+msg + ", used time :" + last + "ms");
									msg = "";
									key.interestOps(SelectionKey.OP_WRITE);
								} else if(count<0) {
									client.close();
									System.err.println("client closed");
									break _FOR;
								}
							} catch (IOException e) {
								e.printStackTrace();  
								try {
									client.close();
									System.err.println("client closed");
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								break _FOR;
							}
						}
						else if (key.isWritable()) {
							start = System.currentTimeMillis();
							SocketChannel channel = (SocketChannel) key.channel();
							if(SLEEP_BEFORE_WRITE>0){
								try {
									Thread.sleep(SLEEP_BEFORE_WRITE);
								} catch (InterruptedException e2) {
									e2.printStackTrace();  
								}
							}
							byte[] aaa = null;
							try {
								aaa = name.getBytes("UTF8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();  
							}
							try {
								channel.write(ByteBuffer.wrap(aaa));
								// 写完可以监听读，server的响应就可以在下个select()时段检测到了
								channel.register(selector, SelectionKey.OP_READ);
								writeCount.incrementAndGet();
//								System.out.println("write:"+name);
							} catch (IOException e) {
								e.printStackTrace();  
								try {
									channel.close();
									System.err.println("client closed");
								} catch (IOException e1) { 
									e1.printStackTrace(); 
								}
								break _FOR;
							}
						} 
					}
				}

		}
	}

	public static final int MAX_CONN = 2000;
	
	public static void main(String[] args){
		String names[] = new String[MAX_CONN];
		for (int index = 0; index < MAX_CONN; index++) {
			if(index%2!=0){
				names[index] = "hello";
			}else{
				names[index] = "teou[" + index + "]";
			}
//			new Message(names[index]).run();
			try{
				new Thread(new Message(names[index])).start();
			}catch(java.lang.OutOfMemoryError e){
				e.printStackTrace();
				System.err.println("cannot create new thread:"+names[index]+", index="+index);
				return;
			}
		}
		
		while(true){
			try {
				Runtime r = Runtime.getRuntime();
				long m = 1024*1024, f = r.freeMemory(), t = r.totalMemory();
				long usedMem = (t-f)/m;
				System.out.println("used_mem="+usedMem+", write_count="+writeCount.getAndSet(0));
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();  
			}
		}
		
	}
	


}
