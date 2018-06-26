package com.sqber.filemgr.controller.response;

public class UploadChunkResponse {
	private boolean chunked;
	private boolean hasError;
	private String f_ext;
	
	public UploadChunkResponse() {
		
	}
	
	public UploadChunkResponse(boolean chunked,boolean hasError,String fileExtension) {
		this.chunked = chunked;
		this.hasError = hasError;
		this.f_ext = fileExtension;
	}
	
	public boolean isChunked() {
		return chunked;
	}
	public void setChunked(boolean chuncked) {
		this.chunked = chuncked;
	}
	public boolean isHasError() {
		return hasError;
	}
	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}
	public String getF_ext() {
		return f_ext;
	}
	public void setF_ext(String f_ext) {
		this.f_ext = f_ext;
	}
	
	
}
