package com.tecgesco.tcgapicigam.app;

import java.util.ArrayList;
import java.util.List;

public class App {

	private static final boolean debug = true;
	private static final String APPNOME = "TCGMonitorOracle";
	private static final String VERSAO = "1.0.0";
	public static List<String> errors = new ArrayList<>();
	public static List<String> msgs = new ArrayList<>();

	public static final String getVersao() {
		return VERSAO;
	}

	public static final String getNome() {
		return APPNOME;
	}

	public static final boolean isDebug() {
		return debug;
	}

	public static void main(String[] args) {
		System.out.println("Iniciando a aplicação!");
		System.out.println("Versão: " + getVersao());

	
		FrmHome frmHome = new FrmHome();
		frmHome.setVisible(true);
	}
}
