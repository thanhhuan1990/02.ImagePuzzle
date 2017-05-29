package com.hcm.imagepuzzlemaker.db;

public class Result {

	private int id;
	private int type;
	private int time;

	public Result(int id, int type, int time) {
		super();
		this.id = id;
		this.type = type;
		this.time = time;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "Result [id=" + id + ", type=" + type + ", time=" + time + "]";
	}

}
