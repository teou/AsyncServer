package ukm.teou.async.client.aio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import ukm.teou.async.server.aio.Server;

public class Client {

	public final static String IP = Server.IP;
	public final static int PORT = Server.PORT;
	
	public static void main(String... args) throws Exception {
		AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
		client.connect(new InetSocketAddress(IP, PORT));
		client.write(ByteBuffer.wrap("test".getBytes())).get();
	}

}