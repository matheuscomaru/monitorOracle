package com.tecgesco.tcgapicigam.app;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class Tools {

	Connection conexao = null;
	PreparedStatement pst = null;
	ResultSet rs = null;

	public String zeroEsquerda(String DADO, int QTDEZEROS) {

		int number = Integer.parseInt(DADO);
		String formatted = String.format("%0" + QTDEZEROS + "d", number);
		return formatted;

	}

	public int deletaRegistro(int CHAVE, String TABELA) {

		String sql = "UPDATE " + TABELA + " SET ATIVO=2 WHERE CHAVE=?";
		System.out.println(sql);

		try {
			pst = conexao.prepareStatement(sql);
			pst.setInt(1, CHAVE);
			if (pst.executeUpdate() > 0) {
				JOptionPane.showMessageDialog(null, "Procedimento executado com sucesso.");
				return 1;
			}

		} catch (Exception e) {

			System.out.println(e);
			JOptionPane.showMessageDialog(null, e);
			return 0;

		}
		return 0;
	}

	public static String encodeBase64(String imagePath) {
		String base64Image = "";
		File file = new File(imagePath);
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			// Reading a Image file from file system
			byte imageData[] = new byte[(int) file.length()];
			imageInFile.read(imageData);
			base64Image = Base64.getEncoder().encodeToString(imageData);
		} catch (FileNotFoundException e) {
			System.out.println("Image not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading the Image " + ioe);
		}
		return base64Image;
	}

	public static void decodeBase64(String base64Image, String pathFile) {
		try (FileOutputStream imageOutFile = new FileOutputStream(pathFile)) {
			// Converting a Base64 String into Image byte array
			byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
			imageOutFile.write(imageByteArray);

			String temp = System.getProperty("java.io.tmpdir");
			OutputStream out = new FileOutputStream(new File(temp + pathFile));
			out.write(imageByteArray);
			out.close();
			Desktop desktop = Desktop.getDesktop();
			try {

				desktop.open(new File(temp + pathFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			System.out.println("Image not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading the Image " + ioe);
		}
	}

	public Image getImageFromClipboard() throws Exception {
		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
		} else {
			return null;
		}
	}

	public void download(String address, String localFileName, String host, int porta) {

		// leitor do arquivo a ser baixado
		InputStream in = null;
		// conex�o com a internete
		URLConnection conn = null;
		// escritor do arquivo que ser� baixado
		OutputStream out = null;

		System.out.println("Update.download() BAIXANDO " + address);

		try {
			URL url = new URL(address);
			out = new BufferedOutputStream(new FileOutputStream(localFileName));
			// int tamanho = (int) url.getContent();

			// verifica se existe proxy
			if (host != "" && host != null) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, porta));
				conn = url.openConnection(proxy);
			} else {
				conn = url.openConnection();
			}

			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
				System.out.println("numWritten:" + numWritten);
			}
			System.out.println(localFileName + "\t" + numWritten);

		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
			}
		}
	}

	public String convertDate(String inputDate) {

		// inputDate = "Thu Jul 06 00:00:00 AMT 2023";

		SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy", Locale.ENGLISH);
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

		try {
			Date date = inputFormat.parse(inputDate);
			String outputDate = outputFormat.format(date);
			return outputDate;
		} catch (ParseException e2) {
			e2.printStackTrace();
		}

		return null;
	}

	public String convertDateAmdToDmy(String data) {
		// "20230515";
		DateFormat formatoEntrada = new SimpleDateFormat("yyyyMMdd");
		DateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy");

		try {
			Date date = formatoEntrada.parse(data);
			String dataFormatada = formatoSaida.format(date);
			return dataFormatada;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return "";
	}

	public String convertDateAmdPadraoAmericano(String data) {

		DateFormat formatoEntrada = new SimpleDateFormat("yyyyMMdd");
		DateFormat formatoSaida = new SimpleDateFormat("yyyy-MM-dd");

		try {
			Date date = formatoEntrada.parse(data);
			String dataFormatada = formatoSaida.format(date);
			return dataFormatada;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return "";
	}

	public String convertDateUtil(String data, String frmtEntrada, String frmtSaida) {

		DateFormat formatoEntrada = new SimpleDateFormat(frmtEntrada);
		DateFormat formatoSaida = new SimpleDateFormat(frmtSaida);

		try {
			Date date = formatoEntrada.parse(data);
			String dataFormatada = formatoSaida.format(date);
			return dataFormatada;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return "";
	}

	public NumberFormat moneyFormat = NumberFormat.getInstance();

	public NumberFormat getMoneyFormat() {
		moneyFormat.setMinimumFractionDigits(2);
		return moneyFormat;
	}

	public double stringToDouble(String numero) {
		String aux = numero.replace(".", "").replace(",", ".");
		return Double.parseDouble(aux);
	}

	public String formartMoney(double numero) {
		return moneyFormat.format(numero);
	}

	public String decimalFormat(double valor) {
		return new DecimalFormat("#,##0.00").format(valor);
	}

	public String formatarCampoTxt(String texto) {
		// usado pra formatar valor digitado em JTextField
		String textoOriginal = texto.trim();

		if (!textoOriginal.isEmpty()) {
			try {

				textoOriginal = textoOriginal.replaceAll("\\.", "");
				DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
				Number numero = decimalFormat.parse(textoOriginal);
				String textoFormatado = decimalFormat.format(numero);
				return textoFormatado;

			} catch (ParseException e) {

				JOptionPane.showMessageDialog(null, e);
				e.printStackTrace();
			}
		}

		return "0";
	}



	public static void setaLarguraAutomaticaJtable(JTable table) {

		for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
			TableColumn column = table.getColumnModel().getColumn(columnIndex);
			TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
			Component headerComponent = headerRenderer.getTableCellRendererComponent(table, column.getHeaderValue(),
					false, false, 0, columnIndex);
			int headerWidth = headerComponent.getPreferredSize().width;
			int maxWidth = headerWidth;

			for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
				TableCellRenderer cellRenderer = table.getCellRenderer(rowIndex, columnIndex);
				Object value = table.getValueAt(rowIndex, columnIndex);
				Component cellComponent = cellRenderer.getTableCellRendererComponent(table, value, false, false,
						rowIndex, columnIndex);
				maxWidth = Math.max(maxWidth, cellComponent.getPreferredSize().width);
			}

			column.setPreferredWidth(maxWidth + 5);
			column.setMinWidth(headerWidth + 5);
		}
	}

	

	public String escolherDiretorio() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedDirectory = fileChooser.getSelectedFile();
			return selectedDirectory.getAbsolutePath();
		}

		return null;
	}

	public String escolherArquivo(String... extensoes) {
		JFileChooser fileChooser = new JFileChooser();

		// Cria um filtro com as extensões permitidas
		if (extensoes != null && extensoes.length > 0) {
			String descricao = "Arquivos (" + String.join(", ", extensoes) + ")";
			FileNameExtensionFilter filtro = new FileNameExtensionFilter(descricao, extensoes);
			fileChooser.setFileFilter(filtro);
		}

		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int resultado = fileChooser.showOpenDialog(null);

		if (resultado == JFileChooser.APPROVE_OPTION) {
			File arquivoSelecionado = fileChooser.getSelectedFile();
			return arquivoSelecionado.getAbsolutePath();
		}

		return null;
	}

	public String lerConteudoArquivo(String caminhoArquivo) {
		try {
			return Files.readString(Paths.get(caminhoArquivo));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void salvarCsv(ArrayList<String> linhas) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Escolha onde salvar o arquivo CSV");

		// Sugestão de nome padrão
		fileChooser.setSelectedFile(new File("dados.csv"));

		int resultado = fileChooser.showSaveDialog(null);

		if (resultado == JFileChooser.APPROVE_OPTION) {
			File arquivoSelecionado = fileChooser.getSelectedFile();

			// Garante extensão .csv
			if (!arquivoSelecionado.getName().toLowerCase().endsWith(".csv")) {
				arquivoSelecionado = new File(arquivoSelecionado.getAbsolutePath() + ".csv");
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSelecionado))) {
				for (String linha : linhas) {
					writer.write(linha);
					writer.newLine();
				}
				JOptionPane.showMessageDialog(null, "Arquivo CSV salvo com sucesso!");
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Erro ao salvar o arquivo: " + e.getMessage(), "Erro",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			System.out.println("Operação cancelada pelo usuário.");
		}
	}
	
	public boolean verificarOuCriarDiretorio(String caminhoDiretorio) {
	    File dir = new File(caminhoDiretorio);

	    if (dir.exists()) {
	        return true; 
	    } else {
	        return dir.mkdirs(); 
	    }
	}
	
	public static String generateInsert(String tabela, Map<String, Object> valores) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(tabela).append(" (");

		for (String coluna : valores.keySet()) {
			sql.append(coluna).append(", ");
		}

		sql.delete(sql.length() - 2, sql.length());
		sql.append(") VALUES (");

		for (Object valor : valores.values()) {
			if (valor instanceof String) {
				String strVal = (String) valor;
				if (strVal.startsWith("@RAW:")) {
					sql.append(strVal.substring(5)).append(", ");
				} else {
					sql.append("'").append(strVal).append("', ");
				}
			} else {
				sql.append(valor).append(", ");
			}
		}

		sql.delete(sql.length() - 2, sql.length());
		sql.append(");");

		return sql.toString();
	}

}
