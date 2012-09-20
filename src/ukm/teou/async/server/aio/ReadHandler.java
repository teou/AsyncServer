package ukm.teou.async.server.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

public class ReadHandler implements CompletionHandler<Integer, Connection> {

	private static final String HELLO = "hello";
	private static final String WORLD = "world";
	private static final String ERROR_RET = "error command";
	
	@Override
	public void completed(Integer readCount, Connection connection) {
	
		String rcvd = null;
		if(readCount>0){
			AsynchronousSocketChannel channel = connection.getChannel();
			ByteBuffer buffer = connection.getBuffer();
			buffer.flip();
			CharBuffer charBuffer = null;
			try {
				CharsetDecoder decoder = Server.CHAR_SET.newDecoder();
				charBuffer = decoder.decode(buffer);
			} catch (CharacterCodingException e) {
				e.printStackTrace();
			}
			rcvd = charBuffer.toString();
			
			String reply = HELLO.equals(rcvd)?WORLD:ERROR_RET;
			connection.setReply(reply);
			buffer.clear();
			buffer.put(reply.getBytes(Server.CHAR_SET));
			buffer.flip();
			channel.write(buffer, connection, Server.WRITER);
		}else{
			Server.closeConnection(connection.getChannel());
		}
		
//		System.out.println("read["+readCount+"], "+ rcvd);
		
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
