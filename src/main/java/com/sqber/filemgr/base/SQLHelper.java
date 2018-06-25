package com.sqber.filemgr.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sqber.filemgr.config.MySQLDataSourceConfig;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class SQLHelper {

	public int add(String sql, List<Object> params) {

		int result = -1;

		try {
			HikariDataSource dataSource = getDataSource();
			Connection connection = dataSource.getConnection();

			PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					statement.setObject(i + 1, params.get(i));
				}
			}
			statement.execute();

			ResultSet resultSet = statement.getGeneratedKeys();
			if (resultSet != null) {
				if (resultSet.next())
					result = resultSet.getInt(1);
			}

			if (connection != null && !connection.isClosed())
				connection.close();
			if (dataSource != null && !dataSource.isClosed())
				dataSource.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}	

	public int update(String sql, List<Object> params) {
		int result = -1;
		try {

			HikariDataSource dataSource = getDataSource();
			Connection connection = dataSource.getConnection();

			PreparedStatement statement = connection.prepareStatement(sql);

			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					statement.setObject(i + 1, params.get(i));
				}
			}

			result = statement.executeUpdate();
			
			if (connection != null && !connection.isClosed())
				connection.close();
			if (dataSource != null && !dataSource.isClosed())
				dataSource.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public <T> List<T> query(String sql, List<Object> params, Class<T> type) {
		try {
			/* HikariDataSource 是需要关闭的 */
			HikariDataSource dataSource = getDataSource();
			Connection connection = dataSource.getConnection();

			PreparedStatement statement = connection.prepareStatement(sql);

			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					statement.setObject(i + 1, params.get(i));
				}
			}

			ResultSet resultSet = statement.executeQuery();
			List<T> list = ResultSetHelper.toList(resultSet, type);

			if (connection != null && !connection.isClosed())
				connection.close();
			if (dataSource != null && !dataSource.isClosed())
				dataSource.close();

			return list;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * 将上面的查询语句 和 参数 抽取出来
	 */
	public String executeScalar(String sql, List<Object> params) {
		String result = null;
		try {
			/* HikariDataSource 是需要关闭的 */
			HikariDataSource dataSource = getDataSource();
			Connection connection = dataSource.getConnection();

			PreparedStatement statement = connection.prepareStatement(sql);

			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					statement.setObject(i + 1, params.get(i));
				}
			}

			ResultSet resultSet = statement.executeQuery();
			if (resultSet != null && resultSet.next())
				result = resultSet.getString(1);

			if (connection != null && !connection.isClosed())
				connection.close();
			if (dataSource != null && !dataSource.isClosed())
				dataSource.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public <T> PageResult<T> queryPage(String sql, List<Object> params, int currentPage, int pageSize,
			Class<T> type) {

		PageResult<T> result = new PageResult<T>();

		int startIndex = (currentPage - 1) * pageSize;

		String querySql = MessageFormat.format("{0} limit {1},{2}", sql, startIndex, pageSize);
		List<T> models = query(querySql, params, type);
		result.setData(models);

		// 正则，将select 和 from 中间的字符串替换成 count(0);
		String countSql = MessageFormat.format("select count(0) from ({0})t ", sql);
		String countStr = executeScalar(countSql, params);
		int countVal = 0;
		if (!isBlank(countStr))
			countVal = Integer.parseInt(countStr);

		int totalPage = (int) ((countVal + pageSize - 1) / pageSize);

		result.setTotalCount(countVal);
		result.setTotalPage(totalPage);

		return result;
	}

	private boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}
	
	@Autowired
	private MySQLDataSourceConfig config;
	
	private HikariDataSource getDataSource() throws SQLException {

//		HikariConfig config = new HikariConfig();
//
//		config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/javablog?useUnicode=true&characterEncoding=utf8&useSSL=false");
//		config.setUsername("root");
//		config.setPassword("123456");
//		config.addDataSourceProperty("cachePrepStmts", "true");
//		config.addDataSourceProperty("prepStmtCacheSize", "250");
//		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

		return new HikariDataSource(config);
	}
}
