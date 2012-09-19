package ukm.teou.async.server.aio;

import java.nio.channels.CompletionHandler;

public class WriteHandler implements CompletionHandler<Integer, Connection>  {

	@Override
	public void completed(Integer writeCount, Connection connection) {
		
//		System.out.println("write["+result+"], "+connection.getReply());

		if(writeCount>0){
			Server.reqCount.incrementAndGet();
		}
		connection.getBuffer().clear();
		connection.getChannel().read(connection.getBuffer(), connection, Server.READER);
	}

	@Override
	public void failed(Throwable exc, Connection attachment) {
		// TODO Auto-generated method stub
		exc.printStackTrace();
	}

}
