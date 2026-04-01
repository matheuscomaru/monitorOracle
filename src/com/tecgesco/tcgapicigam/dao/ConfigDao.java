package com.tecgesco.tcgapicigam.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigDao {

	public enum typeProp {
		TEXT, INT, DECIMAL, BOOLEAN
	}

	public static void salvarConfig(String param, String value) {

		try {

			File configFile = new File("config.properties");

			if (!configFile.exists()) {
				configFile.createNewFile();
			}

			FileInputStream in = new FileInputStream(configFile);
			Properties props = new Properties();
			props.load(in);
			in.close();

			FileOutputStream out = new FileOutputStream("config.properties");
			props.setProperty(param, value);
			props.store(out, null);
			out.close();

		} catch (IOException io) {
			io.printStackTrace();
		}

	}

	public static Object lerConfig(String param, typeProp type) {

		File configFile = new File("config.properties");

		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try (InputStream input = new FileInputStream(configFile)) {

			Properties prop = new Properties();
			prop.load(input);

			if (prop.containsKey(param)) {
				String value = prop.getProperty(param);

				switch (type) {
				case INT:
					return Integer.parseInt(value);
				case DECIMAL:
					return Double.parseDouble(value);
				case BOOLEAN:
					return Boolean.parseBoolean(value);
				case TEXT:
				default:
					return value;
				}
			} else {
				System.out.println("A propriedade " + param + " não foi encontrada.");
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			System.out.println("Formato inválido para a propriedade " + param + " no arquivo de configuração.");
		}

		// Retorno padrão baseado no tipo
		switch (type) {
		case INT:
			return 0;
		case DECIMAL:
			return 0.0;
		case BOOLEAN:
			return false;
		case TEXT:
		default:
			return "";
		}
	}

}