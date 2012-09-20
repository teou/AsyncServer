package ukm.teou.async.server.select;

import java.nio.channels.SelectionKey;

public class KeyAttr {

	public static final int KEY_STATE_NONE = 0;
	public static final int KEY_STATE_READABLE = SelectionKey.OP_READ;
	public static final int KEY_STATE_WRITABLE = SelectionKey.OP_WRITE;

	private int state = 0;
	private String data;
	private CallBack callBack;
	private long connTime;
	private long lastActive;

	public int getState(){return state;}
	
	public long getLastActive() {
		return lastActive;
	}

	public void setLastActive(long lastActive) {
		this.lastActive = lastActive;
	}

	public long getConnTime() {
		return connTime;
	}

	public void setConnTime(long connTime) {
		this.connTime = connTime;
	}

	public CallBack getCallBack() {
		return callBack;
	}

	public void setCallBack(CallBack callBack) {
		this.callBack = callBack;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setStateNone() {
		state = KEY_STATE_NONE;
	}

	public boolean isStateNone() {
		return state == KEY_STATE_NONE;
	}
	
	public boolean isWritable() {
		return (state & KEY_STATE_WRITABLE) == KEY_STATE_WRITABLE;
	}

	public boolean isReadable() {
		return (state & KEY_STATE_READABLE) == KEY_STATE_READABLE;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return new StringBuilder("KeyAttr").append(",state=").append(state)
				.append(",data=").append(data)
				.append(",callBack=").append(callBack)
				.toString();
	}

}
