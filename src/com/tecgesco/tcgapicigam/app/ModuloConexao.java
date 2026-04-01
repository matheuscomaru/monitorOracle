package com.tecgesco.tcgapicigam.app;

import java.sql.Connection;
import java.sql.DriverManager;

import com.tecgesco.tcgapicigam.dao.ConfigDao;
import com.tecgesco.tcgapicigam.dao.ConfigDao.typeProp;

public class ModuloConexao {
	private static Connection conexaoOracle;

	public static synchronized Connection conectorOracle() {

		String driver = "oracle.jdbc.driver.OracleDriver";

		/*
		 * String url = "jdbc:oracle:thin:@192.168.56.210:1521:XE"; String user =
		 * "homologacao"; String password = "tecsis";
		 */

		try {
			if (conexaoOracle != null && !conexaoOracle.isClosed()) {
				return conexaoOracle;
			}

			String user = (String) ConfigDao.lerConfig("prop.oracle.user", typeProp.TEXT);

			String url = (String) ConfigDao.lerConfig("prop.oracle.url", typeProp.TEXT);

			String password = (String) ConfigDao.lerConfig("prop.oracle.password", typeProp.TEXT);

			Class.forName(driver);

			conexaoOracle = DriverManager.getConnection(url, user, password);
			return conexaoOracle;

		} catch (Exception e) {
			e.printStackTrace();
			conexaoOracle = null;
			return null;
		}

	}

}
