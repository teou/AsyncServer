package ukm.teou.async.server.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.TimeUnit;

public class Connection {
	
	private AsynchronousSocketChannel channel;
	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;
	private long timeout;
	
	public Connection(){
		timeout = 0;
	}
	
	public Connection(long timeout){
		this.timeout = timeout;
	}
	
	public ByteBuffer getWriteBuffer() {
		return writeBuffer;
	}
	public void setWriteBuffer(ByteBuffer writeBuffer) {
		this.writeBuffer = writeBuffer;
	}
	public AsynchronousSocketChannel getChannel() {
		return channel;
	}
	public void setChannel(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}
	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}
	public void setReadBuffer(ByteBuffer buffer) {
		this.readBuffer = buffer;
	}

	public void close(){
		if(channel!=null){
			try {
				System.err.println("close channel , conn_num="+Server.connections.size());
				channel.close();
				Server.connections.remove(channel);
			} catch (IOException e) {
				System.err.println("!!! error closing channel "+channel);
				e.printStackTrace();
				Server.connections.remove(channel);
				try {
					channel.close();
				} catch (IOException e1) {
					System.err.println("!!! close again! error again !closing channel "+channel);
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void read(){
		if(readBuffer==null){
			readBuffer = ByteBuffer.allocate(1024);
		}
		
		readBuffer.clear();
		channel.read(readBuffer, timeout, 
				TimeUnit.MILLISECONDS, this, Server.READER);
	}
	
	public void write(String reply){
		if(reply==null) return;
		if(writeBuffer==null){
			writeBuffer = ByteBuffer.allocate(1024);
		}
		
		writeBuffer.clear();
		writeBuffer.put(reply.getBytes(Server.CHAR_SET));
		writeBuffer.flip();
		channel.write(writeBuffer, timeout, TimeUnit.MILLISECONDS, 
				this, Server.WRITER);
	}
	
}
