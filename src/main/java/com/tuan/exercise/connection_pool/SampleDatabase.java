package com.tuan.exercise.connection_pool;

import java.util.Hashtable;

public class SampleDatabase {

	Hashtable<String, String> db = new Hashtable<String, String>();

	public SampleDatabase() {
		db.put("zyot", "12345");
		db.put("zuratama", "1111");
		db.put("test", "123");
		db.put("eie", "1234");
	}

	private static class User {
		private String name;
		private String password;

		public User(String name, String password) {
			this.name = name;
			this.password = password;
		}
	}

	public boolean checkAuthentication(String name, String password) {
		User user = new User(name, password);
		if (this.db.contains(user))
			return true;
		return false;
	}
}
