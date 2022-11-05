package model;

import java.time.OffsetDateTime;

public class Bind {
	
	private String[] bind;
	private OffsetDateTime time;
	private String messageID;
	
	public Bind(String[] bind, OffsetDateTime time, String id) {
		super();
		this.bind = bind;
		this.time = time;
		this.messageID = id;
	}

	public String[] getBind() {
		return bind;
	}

	public void setBind(String[] bind) {
		this.bind = bind;
	}

	public OffsetDateTime getTime() {
		return time;
	}

	public void setTime(OffsetDateTime time) {
		this.time = time;
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}
	
	

}
