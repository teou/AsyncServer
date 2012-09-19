package ukm.teou.async.server.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class Connection {
	
	private AsynchronousSocketChannel channel;
	private ByteBuffer buffer;
	private String reply;
	
	public String getReply() {
		return reply;
	}
	public void setReply(String reply) {
		this.reply = reply;
	}
	public AsynchronousSocketChannel getChannel() {
		return channel;
	}
	public void setChannel(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}
	public ByteBuffer getBuffer() {
		return buffer;
	}
	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

}
