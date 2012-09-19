package ukm.teou.async.server.select;

import java.nio.channels.SelectionKey;

public class KeyAttr {

	public static final int KEY_STATE_NONE = 0;
	public static final int KEY_STATE_READABLE = 0x01;
	public static final int KEY_STATE_WRITABLE = 0x02;

	private int state = 0;
	private String data;
	private CallBack callBack;
	private long connTime;

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
	
	public void setReadOnly(){
		addReadable(true);
		addWritable(false);
	}
	
	public void setWriteOnly(){
		addReadable(false);
		addWritable(true);
	}

	public void addWritable(boolean yes) {
		if (yes)
			state |= KEY_STATE_WRITABLE;
		else
			state &= ~KEY_STATE_WRITABLE;
	}

	public boolean isWritable() {
		return (state & KEY_STATE_WRITABLE) == KEY_STATE_WRITABLE;
	}

	public void addReadable(boolean yes) {
		if (yes)
			state |= KEY_STATE_READABLE;
		else
			state &= ~KEY_STATE_READABLE;
	}

	public boolean isReadable() {
		return (state & KEY_STATE_READABLE) == KEY_STATE_READABLE;
	}

	public void setState(int state) {
		switch (state) {
		case SelectionKey.OP_WRITE: {
			addWritable(true);
			return;
		}
		case SelectionKey.OP_READ: {
			addReadable(true);
			return;
		}
		default: {
			return;
		}
		}
	}

	@Override
	public String toString() {
		return new StringBuilder("KeyAttr").append(",state=").append(state)
				.append(",data=").append(data)
				.append(",callBack=").append(callBack)
				.toString();
	}

}
