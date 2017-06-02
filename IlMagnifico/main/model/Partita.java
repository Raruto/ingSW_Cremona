package main.model;

import java.util.*;

import main.network.server.game.RemotePlayer;
import main.util.errors.Errors;
import main.util.errors.GameError;
import main.util.game.PlayerColors;

/**
 * 
 */
public class Partita {

	/**
	 * 
	 */
	protected ArrayList<Carta> mazzo;

	/**
	 * Array giocatori (usato per determinare l'ordine attuale del turno di
	 * gioco).
	 */
	protected ArrayList<Giocatore> giocatori;

	/**
	 * Puntatore al giocatore attualmente di turno
	 */
	protected Giocatore giocatoreDiTurno;

	/**
	 * 
	 */
	protected SpazioAzione spazioAzione;

	/**
	 * 
	 */
	protected Scomunica[] scomuniche;

	/**
	 * TOT. 2 turni per periodo
	 */
	protected int turno;

	/**
	 * TOT. 2 periodi per partita
	 */
	protected int periodo;

	/**
	 * Flag usato per determinare se la partita � terminata.
	 */
	protected boolean partitaTerminata;

	/**
	 * Costruttore.
	 */
	public Partita() {
		giocatori = new ArrayList<Giocatore>();
		this.mazzo = new ArrayList<Carta>();
		this.giocatoreDiTurno = null;
		this.spazioAzione = new SpazioAzione();
		this.scomuniche = new Scomunica[3];
		for (int i = 0; i < 3; i++) {
			this.scomuniche[i] = new Scomunica();
		}
		this.turno = 0;
		this.periodo = 0;
		this.partitaTerminata = false;
	}

	/**
	 * Metodo usato per inizializzare la partita.
	 */
	protected void inizializzaPartita() {
		// TOT. 2 periodi per partita
		this.periodo = 1;

		// TOT. 2 Turni per periodo
		this.turno = 1;

		// TODO: come inizializzate il mazzo?
		inizializzaMazzo();
		mescolaMazzo();

		// TODO: aggiustare (scatena IllegalArgumentException)
		// inizializzaScomunica();

		inizializzaGiocatori();
		// prossimo giocatore ad eseguire un azione
		this.giocatoreDiTurno = giocatori.get(0);

		// Per ora non mi viene in mente altro che si potrebbe fare per
		// inizializzare la partita. C'� ancora da implementare la parte
		// della inizializzazione del mazzo perch� dipende dal file (per farlo
		// funzionare anche solo temporaneamente dovrei fare un enum). Stesso
		// discorso vale per le scomuniche.

	}

	/**
	 * Metodo per verificare la possibilit� di eseguire un azione da parte di un
	 * determinato giocatore
	 * 
	 * @param g
	 *            giocatore su cui verificare la validit� dell'azione da
	 *            eseguire
	 * @param e
	 *            (nel caso di invalidit� dell'azione che il giocatore sta
	 *            tentando di compiere) conterr� il codice associato all'errore
	 * @return true se giocatore pu� eseguire l'azione, false altrimenti
	 */

	protected boolean isElegible(Giocatore g, GameError e) {
		boolean elegibility = true;
		if (!isPartitaIniziata()) {
			e.setError(Errors.GAME_NOT_STARTED);
			elegibility = false;
		} else if (!isGiocatoreDiTurno(g)) {
			e.setError(Errors.NOT_YOUR_TURN);
			elegibility = false;
		} else if (isPartitaFinita()) {
			e.setError(Errors.GAME_ENDED);
			elegibility = false;
		}
		return elegibility;
	}

	protected boolean isPartitaIniziata() {
		return this.periodo > 0;
	}

	protected boolean isPartitaFinita() {
		return partitaTerminata;
	}

	protected void terminaPartita() {
		this.partitaTerminata = true;
	}

	protected boolean isGiocatoreDiTurno(Giocatore g) {
		return this.giocatoreDiTurno.equals(g);
	}

	public void inizializzaMazzo() {
		// TODO: implementare
	}

	/**
	 * Metodo che mischia il mazzo senza distinguere le carte per periodo e per
	 * tipo. Il riconoscere le carte da prendere per il relativo periodo e per
	 * la relativa torre vengono lasciate al metodo posizionaCartaSuTorre
	 * 
	 * @return
	 */
	public void mescolaMazzo() {
		Collections.shuffle(mazzo);
	}

	/**
	 * Metodo per riempire l'array delle tessere scomunica
	 * 
	 * @return
	 */
	public void inizializzaScomunica() {
		for (int i = 0; i < 3; i++) {
			int periodo = i + 1;
			int indice;
			ArrayList<Scomunica> temporaneo = new ArrayList<Scomunica>();
			Random random = new Random();
			// TODO: da file o database si prendono le carte scomunica del
			// periodo
			// corrispondente (indicato dalla variabile periodo). Con tali carte
			// riempio un ArrayList temporaneo. Genero un numero casuale che sia
			// compreso tra 0 e il numero di elementi nell'ArrayList tramite il
			// metodo random.nextInt(). Si va poi a prendere la scomunica
			// corrispondente all'indice generato casualmente.
			// NB:0<=indice<temporaneo.size(), non ci deovrebbero essere
			// problemi
			indice = random.nextInt(temporaneo.size());
			this.scomuniche[i] = temporaneo.get(indice);
		}
	}

	/**
	 * @return
	 */
	public void inizializzaGiocatori() {
		int contatoreMonete = 4;
		Collections.shuffle(giocatori);
		// generazione casuale dell'ordine dei turni di gioco

		for (int i = 0; i < this.giocatori.size(); i++) {
			this.giocatori.get(i).getRisorse().cambiaLegno(2);
			this.giocatori.get(i).getRisorse().cambiaPietre(2);
			this.giocatori.get(i).getRisorse().cambiaServitori(3);
			this.giocatori.get(i).getRisorse().cambiaMonete(contatoreMonete + 1);
			this.giocatori.get(i).setSpazioAzione(this.spazioAzione);
			if (i == 0)
				this.giocatori.get(i).setColore(PlayerColors.BLUE);
			if (i == 1)
				this.giocatori.get(i).setColore(PlayerColors.GREEN);
			if (i == 2)
				this.giocatori.get(i).setColore(PlayerColors.RED);
			if (i == 3)
				this.giocatori.get(i).setColore(PlayerColors.YELLOW);
		}

	}

	/**
	 * @return
	 */
	public void scegliOrdine() {

		// elimino le ricorrenze nell'arraylist del Palazzo del consiglio e
		// dall'arraylist dei giocatori, poi concateno
	}

	/**
	 * Metodo che posiziona le carte sulle torri all'inizio del turno
	 * 
	 * @return
	 */
	public void posizionaCarteSuTorre() {
		for (int j = 0; j < 16; j++) {
			for (int i = 0; i < this.mazzo.size(); i++) {
				if ((0 <= j) && j < 4) {
					if ((this.mazzo.get(i).getPeriodoCarta() == this.periodo)
							&& (this.mazzo.get(i) instanceof Territorio)) {
						this.spazioAzione.setCartaTorre(this.mazzo.get(i), j);
						this.mazzo.remove(i);
						break;
					}
				}
				if ((4 <= j) && j < 8) {
					if ((this.mazzo.get(i).getPeriodoCarta() == this.periodo)
							&& (this.mazzo.get(i) instanceof Personaggio)) {
						this.spazioAzione.setCartaTorre(this.mazzo.get(i), j);
						this.mazzo.remove(i);
						break;
					}
				}
				if ((8 <= j) && j < 12) {
					if ((this.mazzo.get(i).getPeriodoCarta() == this.periodo)
							&& (this.mazzo.get(i) instanceof Edificio)) {
						this.spazioAzione.setCartaTorre(this.mazzo.get(i), j);
						this.mazzo.remove(i);
						break;
					}
				}
				if ((12 <= j) && j < 16) {
					if ((this.mazzo.get(i).getPeriodoCarta() == this.periodo)
							&& (this.mazzo.get(i) instanceof Impresa)) {
						this.spazioAzione.setCartaTorre(this.mazzo.get(i), j);
						this.mazzo.remove(i);
						break;
					}
				}
			}
		}
	}

	/**
	 * Metodo che restituisce il giocatore che deve giocatore al turno
	 * successivo. Restituisce null se sono finiti i famigliari da muovere e
	 * restituisce il giocatore in ingresso nel caso non sia presente
	 * all'interno dell'elenco dei giocatori nella partita
	 * 
	 * @param
	 * @return
	 */
	public Giocatore giocatoreDelTurnoSuccessivo(Giocatore giocatoreDiTurno) {
		for (int i = 0; i < this.giocatori.size(); i++) {
			if (this.giocatori.get(i) == giocatoreDiTurno) {
				if ((i == (this.giocatori.size() - 1) && (!(this.giocatori.get(i).checkPosizionato()))))
					return this.giocatori.get(0);
				else if ((i == (this.giocatori.size() - 1)) && (this.giocatori.get(i).checkPosizionato()))
					return null;
				else
					return this.giocatori.get(i + 1);
			}
		}
		return giocatoreDiTurno;
	}

	/**
	 * Metodo che esegue il rapporto del vaticano per un giocatore. In ingresso
	 * sono il giocatore stesso e un boolean che indica se il giocatore vuole
	 * supportare (true) o no (false) la Chiesa.
	 * 
	 * @param
	 * @return
	 */
	public void eseguiRapportoVaticano(Giocatore giocatore, boolean esegui) {
		int puntiFede = 0;
		int incremento = 0;
		if (esegui == true) {
			puntiFede = giocatore.getPunti().getPuntiFede();
			for (int i = 0; i < puntiFede; i++) {
				if (i > 4)
					incremento++;
			}
			giocatore.getPunti().setPuntiVittoria(puntiFede + incremento);
			giocatore.getPunti().setPuntiFede(0);
		} else {
			// gli array delle scomuniche corrispondono al numero del periodo-1
			giocatore.setScomunica(periodo - 1, this.scomuniche[periodo - 1]);
		}
	}

	/**
	 * @return
	 */
	public void resetPerNuovoTurno() {
		// TODO implement here
		return;
	}

	/**
	 * @return
	 */
	public ArrayList<Giocatore> calcolaClassificaFinale() {
		// TODO implement here
		return null;
	}

	/**
	 * Metodo che lancia i dadi ed assegna i valori alle variabili legate ai
	 * dadi in SpazioAzione ed ai famigliari dei giocatori. Per convenzione
	 * lancio sempre prima il dado nero, poi l'arancione, poi il bianco.
	 * 
	 * @return
	 */
	public void lanciaDadi() {
		int valoreDado;
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			valoreDado = random.nextInt(6) + 1;
			for (int j = 0; j < this.giocatori.size(); j++) {
				this.giocatori.get(j).setValore(i, valoreDado);
				this.spazioAzione.setValoreDadi(valoreDado, i);
			}
		}
	}
}