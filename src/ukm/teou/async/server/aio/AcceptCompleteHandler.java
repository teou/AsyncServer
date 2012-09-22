package ukm.teou.async.server.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptCompleteHandler implements CompletionHandler<AsynchronousSocketChannel, Connection> {

	private static void closeConnection(AsynchronousSocketChannel channel) {
		try {
			System.err.println("close channel , conn_num="
					+ Server.connections.size());
			channel.close();
			Server.connections.remove(channel);
		} catch (IOException e) {
			System.err.println("!!! error closing channel " + channel);
			e.printStackTrace();
			Server.connections.remove(channel);
			try {
				channel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void completed(AsynchronousSocketChannel channel, Connection connection) {
		// accept next
		Server.server.accept(new Connection(), Server.ACCEPTOR);
		
		// handle this
//		System.out.println("do accept. ");
		if(connection==null) {
			closeConnection(channel);
			return;
		}
		Server.connections.put(channel, connection);
		connection.setChannel(channel);
		// init read and write buffers
		if(connection.getReadBuffer()!=null){
			connection.getReadBuffer().clear();
		}else{
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			buffer.clear();
			connection.setReadBuffer(buffer);
		}
		if(connection.getWriteBuffer()!=null){
			connection.getWriteBuffer().clear();
		}else{
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			buffer.clear();
			connection.setWriteBuffer(buffer);
		}
		connection.read();
	}
	
	@Override
	public void failed(Throwable exc, Connection attachment) {
		exc.printStackTrace();
		if(attachment!=null){
			attachment.close();
		}
	}

}
