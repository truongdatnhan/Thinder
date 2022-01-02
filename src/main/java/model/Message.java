package model;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
    private String data;
    private Status status;

    public Message() {
    	
    }

    public Message(String name, String data, Status status) {
        this.name = name;
        this.data = data;
        this.status = status;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Message [name=" + name + ", data=" + data + ", status=" + status + "]";
	}
	
}
