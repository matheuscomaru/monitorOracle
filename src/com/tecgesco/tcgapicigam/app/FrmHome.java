package com.tecgesco.tcgapicigam.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JToolBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Dimension;

import com.tecgesco.tcgapicigam.dao.ConfigDao;
import com.tecgesco.tcgapicigam.dao.ConfigDao.typeProp;

public class FrmHome extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<String> model = new DefaultListModel<String>();
	private JButton btnMonitor;
	private JList<String> list;
	private JTextField txtPesquisa;
	private JButton btnPesquisa;
	private volatile boolean monitorando = false;
	private Thread monitorThread;
	private Timestamp ultimaConsulta;
	private final Set<String> eventosRecentes = new LinkedHashSet<String>();
	private final List<String> linhasMonitor = new ArrayList<String>();
	private JButton btnNewButton_1;
	private JButton btnCopiarLog;
	private JTextField txtDesconsiderar;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JSeparator separator;
	private JSeparator separator_1;
	private static final String PROP_DESCONSIDERAR = "prop.monitor.desconsiderar";

	public FrmHome() {

		setRootPaneCheckingEnabled(false);

		setBackground(Color.WHITE);
		setTitle("Monitor Oracle");
		setBounds(100, 100, 775, 528);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(SystemColor.menu);
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		list = new JList<String>(model);
		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				java.awt.Component component = super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				String texto = value == null ? "" : value.toString();

				if (!isSelected) {
					component.setForeground(definirCorOperacao(texto));
					component.setBackground(Color.WHITE);
				}

				return component;
			}
		});
		scrollPane.setViewportView(list);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String linhaSelecionada = list.getSelectedValue();
					if (linhaSelecionada != null && !linhaSelecionada.trim().isEmpty()) {
						abrirEditorTexto(linhaSelecionada);
					}
				}
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		panel_1.add(toolBar, BorderLayout.NORTH);

		btnMonitor = new JButton("Iniciar / Parar Monitor");
		toolBar.add(btnMonitor);
		btnMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (monitorando) {
					pararMonitoramento();
				} else {
					iniciarMonitoramento();
				}
			}
		});

		JButton btnNewButton = new JButton("Conex\u00E3o");
		toolBar.add(btnNewButton);
		
		btnNewButton_1 = new JButton("Limpar");
		toolBar.add(btnNewButton_1);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				limparMonitor();
			}
		});

		btnCopiarLog = new JButton("Copiar Log");
		toolBar.add(btnCopiarLog);
		btnCopiarLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copiarLogFiltrado();
			}
		});
		
		separator_1 = new JSeparator();
		separator_1.setPreferredSize(new Dimension(5, 2));
		separator_1.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_1);
		
		lblNewLabel = new JLabel("Desconsiderar");
		toolBar.add(lblNewLabel);
		
		txtDesconsiderar = new JTextField();
		toolBar.add(txtDesconsiderar);
		txtDesconsiderar.setColumns(10);
		
		separator = new JSeparator();
		separator.setPreferredSize(new Dimension(5, 2));
		separator.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator);
		
		lblNewLabel_1 = new JLabel("Pesquisar");
		toolBar.add(lblNewLabel_1);

		txtPesquisa = new JTextField();
		txtPesquisa.setColumns(22);
		toolBar.add(txtPesquisa);

		btnPesquisa = new JButton("Pesquisar");
		toolBar.add(btnPesquisa);
		btnPesquisa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				salvarFiltros();
				aplicarFiltro();
			}
		});
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrmEditor frm = new FrmEditor();
				frm.setVisible(true);
			}
		});

		carregarFiltrosSalvos();

	}

	private void iniciarMonitoramento() {
		Connection conexao = ModuloConexao.conectorOracle();
		if (conexao == null) {
			monitorAdd("Falha ao conectar no Oracle. Verifique os dados em Conexão.");
			return;
		}

		monitorando = true;
		ultimaConsulta = new Timestamp(System.currentTimeMillis() - 30000L);
		btnMonitor.setText("Parar Monitor");
		monitorAdd("Monitor iniciado. Conexão Oracle estabelecida.");

		monitorThread = new Thread(() -> loopMonitorOracle(), "monitor-oracle-thread");
		monitorThread.setDaemon(true);
		monitorThread.start();
	}

	private void pararMonitoramento() {
		monitorando = false;
		if (monitorThread != null) {
			monitorThread.interrupt();
		}
		btnMonitor.setText("Iniciar Monitor");
		monitorAdd("Monitor parado.");
	}

	private void loopMonitorOracle() {
		while (monitorando) {
			capturarEventosBanco();
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	private void capturarEventosBanco() {
		String sql = "SELECT s.last_active_time, s.parsing_schema_name, s.sql_id, s.child_number, s.sql_text, "
				+ "c.command_name FROM v$sql s " + "LEFT JOIN v$sqlcommand c ON s.command_type = c.command_type "
				+ "WHERE s.last_active_time IS NOT NULL " + "AND s.command_type IN (2, 3, 6, 7) "
				+ "AND s.last_active_time > ? " + "ORDER BY s.last_active_time ASC";

		Connection conexao = ModuloConexao.conectorOracle();
		if (conexao == null) {
			monitorAdd("Conexão com Oracle indisponível.");
			pararMonitoramento();
			return;
		}

		Timestamp maiorData = ultimaConsulta;

		try (PreparedStatement pst = conexao.prepareStatement(sql)) {
			pst.setTimestamp(1, ultimaConsulta);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					Timestamp horario = rs.getTimestamp("last_active_time");
					String schema = rs.getString("parsing_schema_name");
					String sqlId = rs.getString("sql_id");
					int childNumber = rs.getInt("child_number");
					String operacao = rs.getString("command_name");
					String textoSql = rs.getString("sql_text");
					String binds = buscarParametros(conexao, sqlId, childNumber);

					String chave = sqlId + "|" + (horario != null ? horario.getTime() : 0L) + "|" + operacao;
					if (registrarEvento(chave)) {
						StringBuilder linha = new StringBuilder();
						linha.append("[").append(operacao).append("] schema=").append(schema).append(" sql_id=")
								.append(sqlId).append(" | ").append(resumoSql(textoSql));
						if (!binds.isEmpty()) {
							linha.append(" | params: ").append(binds);
						}
						monitorAdd(linha.toString());
					}

					if (horario != null && (maiorData == null || horario.after(maiorData))) {
						maiorData = horario;
					}
				}
			}

			if (maiorData != null) {
				ultimaConsulta = maiorData;
			}
		} catch (Exception e) {
			monitorAdd("Erro no monitoramento Oracle: " + e.getMessage());
		}
	}

	private String buscarParametros(Connection conexao, String sqlId, int childNumber) {
		String sql = "SELECT name, position, value_string FROM v$sql_bind_capture "
				+ "WHERE sql_id = ? AND child_number = ? ORDER BY position";

		List<String> parametros = new ArrayList<String>();
		try (PreparedStatement pst = conexao.prepareStatement(sql)) {
			pst.setString(1, sqlId);
			pst.setInt(2, childNumber);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					String nome = rs.getString("name");
					int posicao = rs.getInt("position");
					String valor = rs.getString("value_string");

					if (nome == null || nome.trim().isEmpty()) {
						nome = ":" + posicao;
					}
					if (valor == null || valor.trim().isEmpty()) {
						valor = "<nao capturado>";
					}

					parametros.add(nome + "=" + valor);
				}
			}
		} catch (Exception e) {
			return "";
		}

		return String.join(", ", parametros);
	}

	private synchronized boolean registrarEvento(String chave) {
		boolean novo = eventosRecentes.add(chave);
		if (eventosRecentes.size() > 500) {
			Iterator<String> it = eventosRecentes.iterator();
			if (it.hasNext()) {
				it.next();
				it.remove();
			}
		}
		return novo;
	}

	private String resumoSql(String sql) {
		if (sql == null) {
			return "SQL sem texto disponível";
		}

		return sql.replaceAll("\\s+", " ").trim();
	}

	public void monitorAdd(String texto) {
		SwingUtilities.invokeLater(() -> {
			linhasMonitor.add(" " + dataHoraAtual() + " - " + texto);
			aplicarFiltro();
			scrollToBottom();
		});

	}

	private void aplicarFiltro() {
		List<String> termosPesquisa = extrairTermos(txtPesquisa.getText());
		List<String> termosDesconsiderar = extrairTermos(txtDesconsiderar.getText());

		model.clear();
		for (String linha : linhasMonitor) {
			String linhaNormalizada = linha.toLowerCase(Locale.ROOT);
			boolean atendePesquisa = termosPesquisa.isEmpty() || contemAlgumTermo(linhaNormalizada, termosPesquisa);
			boolean atendeDesconsiderar = termosDesconsiderar.isEmpty()
					|| !contemAlgumTermo(linhaNormalizada, termosDesconsiderar);
			if (atendePesquisa && atendeDesconsiderar) {
				model.addElement(linha);
			}
		}
	}

	private List<String> extrairTermos(String texto) {
		List<String> termos = new ArrayList<String>();
		if (texto == null || texto.trim().isEmpty()) {
			return termos;
		}

		String[] partes = texto.toLowerCase(Locale.ROOT).split("/");
		for (String parte : partes) {
			String termo = parte.trim();
			if (!termo.isEmpty()) {
				termos.add(termo);
			}
		}

		return termos;
	}

	private boolean contemAlgumTermo(String linha, List<String> termos) {
		for (String termo : termos) {
			if (linha.contains(termo)) {
				return true;
			}
		}
		return false;
	}

	private void limparMonitor() {
		linhasMonitor.clear();
		eventosRecentes.clear();
		model.clear();
	}

	private void copiarLogFiltrado() {
		if (model.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Nao ha linhas visiveis para copiar.");
			return;
		}

		StringBuilder conteudo = new StringBuilder();
		for (int i = 0; i < model.size(); i++) {
			if (i > 0) {
				conteudo.append(System.lineSeparator());
			}
			conteudo.append(model.getElementAt(i));
		}

		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(conteudo.toString()), null);
	}

	private void carregarFiltrosSalvos() {
		txtDesconsiderar.setText((String) ConfigDao.lerConfig(PROP_DESCONSIDERAR, typeProp.TEXT));
	}

	private void salvarFiltros() {
		ConfigDao.salvarConfig(PROP_DESCONSIDERAR, txtDesconsiderar.getText().trim());
	}

	private void scrollToBottom() {
		int lastIndex = list.getModel().getSize() - 1;
		if (lastIndex >= 0) {
			list.ensureIndexIsVisible(lastIndex);
		}
	}

	private void abrirEditorTexto(String conteudo) {
		JFrame editorFrame = new JFrame("Visualizar Linha do Monitor");
		editorFrame.setSize(900, 450);
		editorFrame.setLocationRelativeTo(this);

		JTextArea editorTexto = new JTextArea();
		editorTexto.setEditable(true);
		editorTexto.setLineWrap(false);
		editorTexto.setWrapStyleWord(false);
		editorTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
		editorTexto.setText(conteudo);
		editorTexto.setCaretPosition(0);

		JScrollPane scrollEditor = new JScrollPane(editorTexto);
		editorFrame.getContentPane().add(scrollEditor, BorderLayout.CENTER);
		editorFrame.setVisible(true);
	}

	private Color definirCorOperacao(String texto) {
		if (texto.contains("[SELECT]")) {
			return new Color(0, 100, 0);
		}
		if (texto.contains("[INSERT]")) {
			return new Color(0, 70, 180);
		}
		if (texto.contains("[UPDATE]")) {
			return new Color(128, 0, 128);
		}
		if (texto.contains("[DELETE]")) {
			return new Color(200, 0, 0);
		}
		return Color.BLACK;
	}

	public String dataHoraAtual() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		return dtf.format(LocalDateTime.now());

	}
}
