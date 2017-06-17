package main.ui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import main.model.enums.EAzioniGiocatore;
import main.model.enums.EColoriPedine;
import main.model.enums.ECostiCarte;
import main.model.enums.EEffettiPermanenti;
import main.model.enums.ESceltePrivilegioDelConsiglio;
import main.ui.gui.components.ButtonLIM;
import main.ui.gui.components.PanelImmagine;
import main.util.Costants;
import res.images.Resources;

public class SceltaSupportoChiesa extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8525471854298151358L;
	private JPanel contentPane;
	private EAzioniGiocatore azione;
	private boolean supporto;

	private Frame framePrincipale;
	private JRadioButton[] radioButtons;
	private ButtonLIM btnOK = new ButtonLIM("OK");
	private JLabel lblComunicazione;

	private ArrayList<String> scelte;
	private int numeroScelte;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SceltaSupportoChiesa frame = new SceltaSupportoChiesa(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SceltaSupportoChiesa(Frame framePrincipale) {
		setIconImage(new ImageIcon(Resources.class.getResource(Costants.FOLDER_BASE + "/giglio.png")).getImage());
		setTitle("         lorenzo il magnifico");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		// "./src/cornice3.png"
		// contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		setExtendedState(MAXIMIZED_BOTH);
		this.framePrincipale = framePrincipale;
	}

	public void mostraFinestra(EAzioniGiocatore azione) {
		framePrincipale.setVisible(false);
		setVisible(true);
		getContentPane().setLayout(null);
		contentPane.setBounds(0, 0, 1362, 694);
		contentPane.setLayout(null);
		this.azione = azione;

		this.scelte = new ArrayList<String>();
		scelte.add(0, "YES, I support the Church!");
		scelte.add(1, "NO, I don't support the Church!");

		this.numeroScelte = 1;

		aggiungiBtnOK();
		aggiungiLblComunicazione();
		rimuoviRadioButton();
		aggiungiRadioButton(1, scelte);
	}

	public void rimuoviRadioButton() {
		if (radioButtons != null) {
			for (int i = 0; i < radioButtons.length; i++) {
				if (radioButtons[i] != null)
					remove(radioButtons[i]);
			}
		}

	}

	public void aggiungiRadioButton(int numeroScelte, ArrayList<String> scelte) {
		if (radioButtons == null)
			rimuoviRadioButton();
		radioButtons = new JRadioButton[scelte.size()];
		for (int i = 0; i < scelte.size(); i++) {
			radioButtons[i] = new JRadioButton(scelte.get(i));
			radioButtons[i].setBounds(600, 200 + 30 * i, 500, 25);
			radioButtons[i].setVisible(true);
			radioButtons[i].setOpaque(false);
			radioButtons[i].setFont(new Font("ALGERIAN", 20, 20));
			radioButtons[i].setForeground(Color.WHITE);
			radioButtons[i].addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent arg0) {
					JRadioButton temp = (JRadioButton) (arg0.getSource());
					if (!temp.isSelected())
						temp.setForeground(Color.YELLOW);

				}

				@Override
				public void mouseEntered(MouseEvent arg0) {
					JRadioButton temp = (JRadioButton) (arg0.getSource());
					temp.setForeground(Color.RED);
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
					JRadioButton temp = (JRadioButton) (arg0.getSource());
					if (!temp.isSelected())
						temp.setForeground(Color.WHITE);
					else
						temp.setForeground(Color.YELLOW);

				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}

			});
			contentPane.add(radioButtons[i]);
		}
	}

	public void aggiungiLblComunicazione() {
		lblComunicazione = new JLabel("Support the Church? ");
		lblComunicazione.setBounds(550, 100, 719, 35);
		lblComunicazione.setFont(new Font("ALGERIAN", 50, 20));
		lblComunicazione.setForeground(Color.WHITE);
		getContentPane().add(lblComunicazione);
	}

	public void aggiungiBtnOK() {
		btnOK.setBounds(575, 450, 185, 30);
		btnOK.setVisible(true);
		contentPane.setLayout(null);
		contentPane.add(btnOK);
		btnOK.addActionListener(new Conferma(this));
	}

	private class Conferma implements ActionListener {

		public SceltaSupportoChiesa frameSceltaCosti;

		public Conferma(SceltaSupportoChiesa frameSceltaCosti) {
			this.frameSceltaCosti = frameSceltaCosti;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int contaValoriSelezionati = 0;
			ArrayList<String> decisioni = new ArrayList<String>();
			decisioni.clear();
			for (int i = 0; i < radioButtons.length; i++) {
				if (radioButtons[i].isSelected()) {
					decisioni.add(scelte.get(i));
					contaValoriSelezionati++;
				}
			}
			if (contaValoriSelezionati != numeroScelte) {
				lblComunicazione.setForeground(Color.RED);
				return;
			}

			/*
			 * CHIAMATA A SERVER PER COMUNICAZIONE DECISIONI ( all'interno di
			 * ArrayList<String> decisioni AGGIORNAMENTO (utilizzare
			 * framePrincipale)
			 */

			// VEDI: scelte.add(0, "YES, I support the Church!");
			if (decisioni.get(0).equals(scelte.get(0))) {
				supporto = true;
			}
			// VEDI: scelte.add(1, "NO, I don't support the Church!");
			else if (decisioni.get(0).equals(scelte.get(1))) {
				supporto = false;
			}

			framePrincipale.supportChurch(supporto);
		}

	}

}
