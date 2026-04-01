package com.tecgesco.tcgapicigam.app;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.tecgesco.tcgapicigam.dao.ConfigDao;
import com.tecgesco.tcgapicigam.dao.ConfigDao.typeProp;

public class FrmEditor extends JFrame {

	private JPanel contentPane;
	private JTextField txt_oraurl;
	private JTextField txt_orauser;
	private JTextField txt_orasenha;

	public FrmEditor() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(FrmEditor.class.getResource("/com/tecgesco/tcgapicigam/img/config30x30.png")));
		setBackground(SystemColor.menu);

		setTitle("Editor de dados de conex\u00E3o");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 238);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.menu);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setLocationRelativeTo(null);

		txt_oraurl = new JTextField();
		txt_oraurl.setBounds(146, 42, 278, 25);
		contentPane.add(txt_oraurl);
		txt_oraurl.setColumns(10);

		txt_orauser = new JTextField();
		txt_orauser.setBounds(146, 78, 278, 25);
		contentPane.add(txt_orauser);
		txt_orauser.setColumns(10);

		txt_orasenha = new JTextField();
		txt_orasenha.setBounds(146, 114, 278, 25);
		contentPane.add(txt_orasenha);
		txt_orasenha.setColumns(10);

		JButton btnSalvar = new JButton("Salvar");
		btnSalvar.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnSalvar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				salvar();

			}
		});
		btnSalvar.setBounds(335, 153, 89, 35);
		contentPane.add(btnSalvar);

		JLabel lblNewLabel_3_1 = new JLabel("ORA - SENHA");
		lblNewLabel_3_1.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblNewLabel_3_1.setBounds(20, 119, 116, 14);
		contentPane.add(lblNewLabel_3_1);

		JLabel lblNewLabel_3_2 = new JLabel("ORA - USU\u00C1RIO");
		lblNewLabel_3_2.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblNewLabel_3_2.setBounds(20, 83, 116, 14);
		contentPane.add(lblNewLabel_3_2);

		JLabel lblNewLabel_3_3 = new JLabel("ORA URL");
		lblNewLabel_3_3.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblNewLabel_3_3.setBounds(20, 47, 116, 14);
		contentPane.add(lblNewLabel_3_3);
		
		JLabel lblNewLabel = new JLabel("jdbc:oracle:thin:@//{ip}:{porta}/{name service}");
		lblNewLabel.setBounds(146, 17, 278, 14);
		contentPane.add(lblNewLabel);

		ler();
	}

	public void salvar() {

		String oraurl = txt_oraurl.getText();
		String orauser = txt_orauser.getText();
		String orasenha = txt_orasenha.getText();

		ConfigDao.salvarConfig("prop.oracle.user", orauser);
		ConfigDao.salvarConfig("prop.oracle.ip", oraurl);
		ConfigDao.salvarConfig("prop.oracle.url", "jdbc:oracle:thin:@" + oraurl);
		ConfigDao.salvarConfig("prop.oracle.password", orasenha);

		JOptionPane.showMessageDialog(null, "Dados de conexão salvos com sucesso.");

	}

	public void ler() {

		txt_oraurl.setText((String) ConfigDao.lerConfig("prop.oracle.ip", typeProp.TEXT));
		txt_orauser.setText((String) ConfigDao.lerConfig("prop.oracle.user", typeProp.TEXT));
		txt_orasenha.setText((String) ConfigDao.lerConfig("prop.oracle.password", typeProp.TEXT));

	}
}
