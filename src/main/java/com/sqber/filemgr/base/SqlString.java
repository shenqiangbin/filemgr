package com.sqber.filemgr.base;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class SqlString {

	public static <T> String toInsertSql(Class<T> type) {
		String classname = type.getSimpleName();

		List<Field> fields = filterFields(type);
		String[] values = new String[fields.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = "?";
		}
		String valStr = String.join(",", values);

		String[] cols = new String[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			cols[i] = fields.get(i).getName();
		}
		String colStr = String.join(",", cols);

		return MessageFormat.format("insert {0}({1}) values({2})", classname, colStr, valStr);
	}

	public static <T> List<Object> toInsertParams(T instance) throws IllegalArgumentException, IllegalAccessException {

		ArrayList<Object> params = new ArrayList<Object>();

		Class<?> type = instance.getClass();
		List<Field> fields = filterFields(type);
		for (Field field : fields) {
			field.setAccessible(true);
			Object val = field.get(instance);

			params.add(val);
		}

		return params;
	}

	private static <T> List<Field> filterFields(Class<T> type) {
		ArrayList<Field> list = new ArrayList<Field>();

		String className = type.getSimpleName();

		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().equalsIgnoreCase("id") || field.getName().equalsIgnoreCase(className + "id")) {
				continue;
			}
			list.add(field);
		}

		return list;
	}

	private static <T> Field primaryField(Class<T> type) throws Exception {

		String className = type.getSimpleName();

		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().equalsIgnoreCase("id") || field.getName().equalsIgnoreCase(className + "id")) {
				return field;
			}
		}
		
		throw new Exception("没有主键( [id] 或 [表名id] )");
	}

	public static <T> String toUpdateSql(Class<T> type) throws Exception {

		/* update user set username = ? where userid = ? */

		String classname = type.getSimpleName();

		List<Field> fields = filterFields(type);
		String[] cols = new String[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			cols[i] = MessageFormat.format("{0} = ?", fields.get(i).getName());
		}
		String colStr = String.join(",", cols);

		Field primaryField = primaryField(type);				
		String whereStr = MessageFormat.format("{0} = ?", primaryField.getName());

		return MessageFormat.format("update {0} set {1} where {2}", classname, colStr, whereStr);
	}
	
	public static <T> List<Object> toUpdateParams(T instance) throws Exception {

		ArrayList<Object> params = new ArrayList<Object>();

		Class<?> type = instance.getClass();
		List<Field> fields = filterFields(type);
		for (Field field : fields) {
			field.setAccessible(true);
			Object val = field.get(instance);

			params.add(val);
		}
		
		Field primaryField = primaryField(type);
		primaryField.setAccessible(true);
		params.add(primaryField.get(instance));

		return params;
	}
}
