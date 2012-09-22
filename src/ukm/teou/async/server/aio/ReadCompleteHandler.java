package ukm.teou.async.server.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ReadCompleteHandler implements CompletionHandler<Integer, Connection> {

	private static final String HELLO = "hello";
	private static final String WORLD = "world";
	private static final String ERROR_RET = "error command";
	
	private String handle(String rcvd){
		return HELLO.equals(rcvd)?WORLD:ERROR_RET;
	}
	
	@Override
	public void completed(Integer readCount, Connection connection) {

		if(connection==null) return;
		if(connection.getReadBuffer()==null){
			connection.setReadBuffer(ByteBuffer.allocate(1024));
			return;
		}
		
		// handle
		Server.fetchCount.incrementAndGet();
		String rcvd = null;
		if(readCount>0){
			ByteBuffer readBuffer = connection.getReadBuffer();
			readBuffer.flip();
			rcvd = new String(readBuffer.array(), Server.CHAR_SET);
			
			// do write
			String reply = handle(rcvd);
			boolean needWrite = reply==null?false:true;
			if(needWrite){
				connection.write(reply);
			}else{
				// read next
				connection.read();
			}
		}else if(readCount==0){
			connection.close();
		}else{
			connection.close();
		}
		
//		System.out.println("read["+readCount+"], "+ rcvd);
		
	}

	@Override
	public void failed(Throwable exc, Connection connection) {
		exc.printStackTrace();
		if(connection!=null){
			connection.close();
		}
	}

}
