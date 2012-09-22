package ukm.teou.async.server.aio;

import java.nio.channels.CompletionHandler;

public class WriteCompleteHandler implements CompletionHandler<Integer, Connection>  {

	@Override
	public void completed(Integer writeCount, Connection connection) {
		
		if(connection==null) return;
		
//		System.out.println("write["+result+"], "+connection.getReply());
		if(writeCount>0){
			Server.oprCount.incrementAndGet();
		}
		connection.read();

	}

	@Override
	public void failed(Throwable exc, Connection connection) {
		// TODO Auto-generated method stub
		exc.printStackTrace();
		if(connection!=null){
			connection.close();
		}
	}

}
