package com.sqber.filemgr.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sqber.filemgr.controller.response.CheckChunkResponse;
import com.sqber.filemgr.controller.response.MergeChunksResponse;
import com.sqber.filemgr.controller.response.UploadChunkResponse;

@Controller
public class WebUploaderController {

	/*
	 * https://blog.csdn.net/chenXiaoYu_csdn/article/details/70847203
	 * https://blog.csdn.net/qq_34698126/article/details/54429721
	 * http://blog.sqber.com/articles/big-file-upload.html
	 */
	@ResponseBody
	@PostMapping("webuploader/uploadchunk")
	public Object uploadChunk(HttpServletRequest request) {

		String chunkStr = "";
		String chunksStr = "";
		String fileGuid = "";
		FileItem fileItem = null;
		InputStream inputStream = null;
		String extName = "";
		
		DiskFileItemFactory factory = new DiskFileItemFactory(); 
		ServletFileUpload fileUpload = new ServletFileUpload(factory);
		fileUpload.setHeaderEncoding("utf-8");

		try {

			List<FileItem> items = fileUpload.parseRequest((RequestContext) request);
			for (FileItem item : items) {
				if (item.isFormField()) {
					String fieldName = item.getFieldName();
					if (fieldName.equals("guid"))
						fileGuid = item.getString("utf-8");
					if (fieldName.equals("chunk"))
						chunkStr = item.getString("utf-8");
					if (fieldName.equals("chunks"))
						chunksStr = item.getString("utf-8");
				} else {
					fileItem = item;
					inputStream = item.getInputStream();
					String fileName = item.getName();
					fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
					extName = fileName.substring(fileName.lastIndexOf("."));
				}
			}

			String rootPath = this.getClass().getResource("/").toString();
			String folder = rootPath + "/upload/" + fileGuid + "/";

			if (!new File(folder).exists())
				new File(folder).mkdirs();

			// 如果进行了分片
			if (!StringUtils.isEmpty(chunkStr)) {
				// 当前分片在上传分片中的顺序
				int chunk = Integer.parseInt(chunkStr);
				// 总分片数
				int chunks = Integer.parseInt(chunksStr);

				File chunkFile = new File(folder + "/" + chunkStr);

				/*
				 * 将Request中的文件流(inputStream)读取到byte[]数组中，再将其写入文件
				 */
				int length = inputStream.available();
				byte[] content = new byte[length];
				inputStream.read(content);

				FileOutputStream stream = new FileOutputStream(chunkFile);
				stream.write(content);

				stream.close();
				inputStream.close();

				return new UploadChunkResponse(true, false, extName);

			}

			else { // 如果没有分片

				String fileName = UUID.randomUUID().toString() + extName;
				File file = new File(folder + "/" + fileName);
				fileItem.write(file);

				return new UploadChunkResponse(false, false, extName);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new UploadChunkResponse(true, true, "");
		}
	}

	//合并分块
	@ResponseBody
	@PostMapping("webuploader/mergechunks")
	public Object mergeChunks(HttpServletRequest request) {

		String fileGuid = "";
		String extName = "";

		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload fileUpload = new ServletFileUpload(factory);
		fileUpload.setHeaderEncoding("utf-8");

		try {

			List<FileItem> items = fileUpload.parseRequest((RequestContext) request);
			for (FileItem item : items) {
				if (item.isFormField()) {
					String fieldName = item.getFieldName();
					if (fieldName.equals("guid"))
						fileGuid = item.getString("utf-8");
					if (fieldName.equals("fileName")) {
						String fileName = item.getString("utf-8");
						extName = fileName.substring(fileName.lastIndexOf("."));
					}
				} else {
					// fileItem = item;
					// inputStream = item.getInputStream();
					// String fileName = item.getName();
					// fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
					// extName = fileName.substring(fileName.lastIndexOf("."));
				}
			}

			String rootPath = this.getClass().getResource("/").toString();
			String folder = rootPath + "/upload/" + fileGuid + "/";

			File dir = new File(folder);
			if (dir.exists()) {
				File[] files = dir.listFiles(new FileFilter() {
					// 排除目录只要文件
					@Override
					public boolean accept(File pathname) {

						if (pathname.isDirectory()) {
							return false;
						}
						return true;
					}
				});

				// 转成集合，便于排序 //排一下序，保证从0-N Write
				List<File> fileList = new ArrayList<File>(Arrays.asList(files));
				Collections.sort(fileList, new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
							return -1;
						}
						return 1;
					}
				});

				String fileName = UUID.randomUUID().toString() + extName;
				File file = new File(folder + "/" + fileName);
				FileOutputStream stream = new FileOutputStream(file);

				for (File item : fileList) {

					FileInputStream input = new FileInputStream(item);
					byte[] content = new byte[input.available()];
					input.read(content);
					input.close();
					item.delete();

					stream.write(content);
				}

				stream.close();
				dir.delete();

				return new MergeChunksResponse(1, "/uploads/" + fileName);
			} else {
				return new MergeChunksResponse(1, "");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new MergeChunksResponse(0, "");
		}

	}

	// 检查当前分块是否上传成功
	@ResponseBody
	@RequestMapping("webuploader/checkchunk")
	public Object checkChunk(HttpServletRequest request) {
		
		String fileMd5 = request.getParameter("fileMd5");
		String chunk = request.getParameter("chunk");
		String chunkSize = request.getParameter("chunkSize");
		
		String rootPath = this.getClass().getResource("/").toString();
		String folder = rootPath + "/upload/" + fileMd5 + "/";
		String path = folder + chunk;
		File file = new File(path);

		// 检查文件是否存在，且大小是否一致
		if (file.exists() && file.length() == Integer.parseInt(chunkSize))
			return new CheckChunkResponse(1);
		else
			return new CheckChunkResponse(0);

	}
}
