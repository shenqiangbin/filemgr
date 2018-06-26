package com.sqber.filemgr.controller.response;

public class CheckChunkResponse {

	private int ifExist;

	public CheckChunkResponse() {

	}

	public CheckChunkResponse(int ifExist) {
		this.ifExist = ifExist;
	}

	public int getIfExist() {
		return this.ifExist;
	}

	public void setIfExist(int ifExist) {
		this.ifExist = ifExist;
	}
}
