package com.sqber.filemgr.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.qos.logback.core.pattern.Converter;

@Controller
public class WebUploaderController {

	/*
	 * https://blog.csdn.net/chenXiaoYu_csdn/article/details/70847203
	 * https://blog.csdn.net/qq_34698126/article/details/54429721
	 * http://blog.sqber.com/articles/big-file-upload.html
	 * */
	@ResponseBody
	@RequestMapping("WebUploader/uploadChunk")
	public Object uploadChunk(HttpServletRequest request) {
		String chunkStr = request.getParameter("chunk");
		String chunksStr = request.getParameter("chunks");

		if (!StringUtils.isEmpty(chunkStr)) {
			// 当前分片在上传分片中的顺序
			int chunk = Integer.parseInt(chunkStr);
			// 总分片数
			int chunks = Integer.parseInt(chunksStr);

			String fileGuid = request.getParameter("guid");			
			String rootPath = this.getClass().getResource("/").toString();
			String folder = rootPath + "/upload/" + fileGuid + "/";
			
			if(!new File(folder).exists())
				new File(folder).mkdirs();
			
			
		}
	}
}
