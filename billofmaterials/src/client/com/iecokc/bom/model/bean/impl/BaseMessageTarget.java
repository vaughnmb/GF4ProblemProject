package com.iecokc.bom.model.bean.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.iecokc.bom.model.bean.Message;
import com.iecokc.bom.model.bean.MessageTarget;
import com.iecokc.bom.model.bean.Message.Severity;

public class BaseMessageTarget implements MessageTarget {
	private static final long serialVersionUID = 1L;
	private List<Message> messages = new LinkedList<Message>();

	public void addMessage(Message msg) {
		this.messages.add(msg);
	}

	public SortedSet<Severity> getMessageSeverities() {
		SortedSet<Severity> ss = new TreeSet<Severity>();
		for (Message m : this.messages) {
			ss.add(m.getSeverity());
		}
		return ss;
	}

	public List<Message> getMessages() {
		List<Message> sl = new ArrayList<Message>(this.messages);
		Collections.sort(sl);
		return sl;
	}

}
