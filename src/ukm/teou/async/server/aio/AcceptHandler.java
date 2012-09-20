package ukm.teou.async.server.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Connection> {

	@Override
	public void completed(AsynchronousSocketChannel channel, Connection connection) {
		// accept next
		Server.server.accept(new Connection(), Server.ACCEPTOR);
		
		// handle this
//		System.out.println("do accept. ");
		Server.connections.put(channel, connection);
		connection.setChannel(channel);
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		connection.setBuffer(buffer);
		channel.read(buffer, connection, Server.READER);
	}
	
	@Override
	public void failed(Throwable exc, Connection attachment) {
		// TODO Auto-generated method stub
		exc.printStackTrace();
		if(attachment!=null){
			Server.closeConnection(attachment.getChannel());
		}
	}

}
