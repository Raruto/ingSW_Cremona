package main.game;

import java.util.*;

import main.game.board.Tabellone;
import main.game.board.tower.AreaTorre;
import main.game.board.vatican.Vaticano;
import main.game.cards.Mazzo;
import main.game.dices.Dado;
import main.game.handlers.GestorePartita;
import main.game.players.Giocatore;

/**
 * 
 */
public class Partita {

	/**
	 * Default constructor
	 */
	public Partita() {
	}

	private ArrayList<Giocatore> giocatori;
	private Tabellone tabellone;
	private Mazzo mazzo;
	private Dado dado;
	private GestorePartita gestorePartita;
	private Vaticano vaticano;
	private int periodo;
	private ArrayList<Giocatore> ordineTurno;
	private int turno;

	/**
	 * @return
	 */
	public void run() {
		// TODO implement here
		return;
	}

	/**
	 * @return
	 */
	public void preparaPartita() {

		// ipotizzo che siano gia passati tutti i dati per la realizzazione
		// della lista di giocatori
		this.periodo = 1;
		this.turno = 1;
		this.tabellone = new Tabellone();
		this.mazzo = new Mazzo();
		this.dado = new Dado();
		this.giocatori = new ArrayList<Giocatore>();
		this.ordineTurno = new ArrayList<Giocatore>();
		return;
	}

	public void Periodo() {
		// si devono svolgere tutte le azioni svolte all'interno di un periodo:
		// mescolo il mazzo secondo le carte che mi servono nel periodo
		// corrispondente
		int contatore;
		
		this.mazzo.mescolaMazzo();
		for(contatore=0;contatore<8;contatore++){
			if(contatore==0|contatore==4)//al primo e al quinto giro di turni bisogna riorganizzare il tabellone
				inizializzaGiroDiTurni();
			giroDiTurni();
			if(contatore==3)
				calcoloOrdineGiocatori();
			if(contatore==7&&!(this.periodo==3))
				calcoloOrdineGiocatori();
		}
		rapportoVaticano();
		this.periodo++;
		this.mazzo.incrementaPeriodo();
	}

	private void rapportoVaticano() {
		// TODO Auto-generated method stub

	}

	/*
	 * All'inizio di ogni giro di turni si devono togliere tutti i familiari dal tabellone,
	 *  mettere delle nuove carte nelle torri e lanciare i dadi
	 * */
	public void inizializzaGiroDiTurni(){
		int contatore;
		AreaTorre torri = this.tabellone.getTorri();

		// posiziono le carte sul tabellone
		for (contatore = 0; contatore < 5; contatore++) {
			torri.getTorreImpresa().setCartaPiano(this.mazzo.getCartaImpresa(), contatore);
			torri.getTorreTerritorio().setCartaPiano(this.mazzo.getCartaTerritorio(), contatore);
			torri.getTorrePersonaggio().setCartaPiano(this.mazzo.getCartaPersonaggio(), contatore);
			torri.getTorreEdificio().setCartaPiano(this.mazzo.getCartaEdificio(), contatore);
		}
		// faccio lanciare i dadi
		this.dado.lancia();
		// ad ogni giocatore metto i valori del dado alle corrispondenti pedine
		for (contatore = 0; contatore <= this.giocatori.size(); contatore++) {
			this.ordineTurno.get(contatore).setValoriPedine(this.dado.getDadoBianco(), this.dado.getDadoArancione(),
					this.dado.getDadoNero(),0);

		}
	}
	
	/*
	 * Calcolo dell'ordine dei giocatori per il turno successivo
	 * */
	public void calcoloOrdineGiocatori(){
		ArrayList<Giocatore> ordineTurniTemporaneo = new ArrayList<Giocatore>();
		// calcolo l'ordine dei turni per il giro successivo
				ordineTurniTemporaneo = this.tabellone.getPalazzoDelConsiglio().getOrdineTurnoSuccessivo();

				// TODO: finire di implementare
				// for(contatore=0;contatore<=this.tabellone.get(contatore))
				// }
	}
	public void giroDiTurni() {
		/*
		 * 
		 * */
		int contatore;

		
		// faccio fare i turni ai giocatori
		for (contatore = 0; contatore <= this.giocatori.size(); contatore++) {
			turnoGiocatore(this.ordineTurno.get(contatore));

		}

		
	}

	private void turnoGiocatore(Giocatore giocatore) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return
	 */
	public void finePartita() {
		// TODO implement here
		return;
	}

}