package com.sqber.filemgr.controller.response;

public class MergeChunksResponse {
	
	private int r;
	private String path;
	
	public MergeChunksResponse() {
		
	}
	
	public MergeChunksResponse(int r,String path) {
		this.r = r;
		this.path = path;
	}
	
	public int getR() {
		return r;
	}
	public void setR(int r) {
		this.r = r;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
