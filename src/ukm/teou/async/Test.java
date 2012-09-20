package ukm.teou.async;

import java.io.IOException;

public class Test {

	public static void main(String args[]){
		
		if(args==null || args.length<1){
			System.err.println("usage : test server ip port clientthreadnum");
			return;
		}
		if("SelectServer".equals(args[0])){
			try {
				ukm.teou.async.server.select.Server.main(args);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("fatal exit");
			}
		}
		else if("AioServer".equals(args[0])){
			ukm.teou.async.server.aio.Server.main(args);
		}
		else if("SelectClient".equals(args[0])){
			ukm.teou.async.client.select.Client.main(args);
		}
		
	}
	
}
