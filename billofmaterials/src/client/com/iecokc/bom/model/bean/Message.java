package com.iecokc.bom.model.bean;

import java.io.Serializable;

public class Message implements Comparable<Message>, Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum  Severity { NONE, INFO, WARN, ERROR, SEVERE }
	private Severity severity;
	private String type;
	private String message;
	
	public Message(Severity severity, String type, String msg) {
		this.severity = severity;
		this.type = (type != null) ? type : "";
		this.message = (msg != null) ? msg : "";
	}

	public String getMessage() {
		return message;
	}
	
	public String getType() {
		return type;
	}

	public Severity getSeverity() {
		return severity;
	}
	
	public int compareTo(Message m) {
		if (m == null) {
			return 1;
		}
		int severityCompare = this.severity.compareTo(m.getSeverity());
		if (severityCompare != 0) {
			return severityCompare;
		}
		int typeCompare = this.type.compareTo(m.getType());
		if (typeCompare != 0) {
			return typeCompare;
		}
		return this.message.compareTo(m.getMessage());
	}
		
}
