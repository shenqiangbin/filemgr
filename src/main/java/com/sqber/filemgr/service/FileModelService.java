package com.sqber.filemgr.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sqber.filemgr.base.SQLHelper;
import com.sqber.filemgr.model.FileModel;

@Service
public class FileModelService {

	@Autowired
	private SQLHelper sqlHelper;

	public FileModel getById(String id) {

		String sql = "select * from file where fileid = ? and status = 1";
		List<Object> params = new ArrayList<Object>();
		params.add(id);
		List<FileModel> files = sqlHelper.query(sql, params, FileModel.class);
		if (files != null && files.size() > 0)
			return files.get(0);

		return null;
	}
}
