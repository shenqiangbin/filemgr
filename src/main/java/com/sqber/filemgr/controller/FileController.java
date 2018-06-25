package com.sqber.filemgr.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sqber.filemgr.model.FileModel;

/*
 * 
 * byte[] 和 String 的转换
 * fastJson 读取字符串配置 （获取 resource 下面的文件路径）
 *  
 * */
@Controller
public class FileController {

	@GetMapping("/file/{id}")
	public void index(HttpServletRequest request, @PathVariable String id, HttpServletResponse response) {

		try {

			FileModel fileModel = getFile(id);

			String filePath = fileModel.getFilePath();
			byte[] contentBytes = getFileByte(id, filePath);
			//response.setContentLengthLong(contentBytes.length);

			String fileType = fileModel.getFileExtension();
			String contentType = fileTypeToContentType(fileType);
			response.setContentType(contentType);

			OutputStream stream = response.getOutputStream();
			stream.write(contentBytes);
			stream.flush();

		} catch (FileNotFoundException ex) {
			showMsg(ex.getMessage(), response);
		} catch (Exception e) {
			e.printStackTrace();
			showMsg("出错了", response);
		}
	}

	private byte[] getFileByte(String id, String filePath) throws IOException {

		File file = new File(filePath);
		if (!file.exists())
			throw new FileNotFoundException("文件不存在:" + id);

		FileInputStream stream = new FileInputStream(file);
		int length = stream.available();
		byte[] data = new byte[length];
		stream.read(data);

		stream.close();

		return data;
	}

	private FileModel getFile(String id) {
		// search db
		String fileid = id;

		FileModel model = new FileModel();

		model.setId("1");
		
		model.setFilePath("d:/1.zip");
		model.setFileSize(134244);
		model.setName("screen");
		model.setFileExtension(".zip");

		return model;
	}

	private void showMsg(String msg, HttpServletResponse response) {

		try {

			byte[] contentBytes = msg.getBytes();
			OutputStream stream = response.getOutputStream();			
			response.setHeader("content-type", "text/html;charset=UTF-8");
			stream.write(contentBytes);
			stream.flush();
			
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String fileTypeToContentType(String fileType) throws IOException {

		String configFilePath =	this.getClass().getResource("/contenttype.json").getPath();
		File file = new File(configFilePath);
		if (!file.exists()) {
			String mString = "文件不存在:" + configFilePath;
			System.out.println(mString);
			throw new FileNotFoundException(mString);
		}			

		FileInputStream stream = new FileInputStream(file);
		byte[] content = new byte[stream.available()];
		stream.read(content);
		stream.close();

		String fileContent = new String(content);	
		
		JSONObject obj = JSON.parseObject(fileContent);		
		
		String key = fileType.toLowerCase();

		if (obj.containsKey(key))
			return obj.getString(key);

		return null;
	}
}
