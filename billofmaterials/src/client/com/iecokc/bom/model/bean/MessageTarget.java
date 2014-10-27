package com.iecokc.bom.model.bean;

import java.io.Serializable;
import java.util.List;
import java.util.SortedSet;

public interface MessageTarget extends Serializable {

	public void addMessage(Message msg);
	public List<Message> getMessages();
	public SortedSet<Message.Severity> getMessageSeverities();
}
