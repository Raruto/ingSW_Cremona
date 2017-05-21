package main.game.players;

/**
 * Classe per la gestione delle Risorse Giocatore
 */
public class RisorseGiocatore {
	private int legni;
	private int monete;
	private int pietre;
	private int servitori;

	public RisorseGiocatore(int legni, int monete, int pietre, int servitori) {
		this.legni = legni;
		this.monete = monete;
		this.pietre = pietre;
		this.servitori = servitori;
	}

	public int getLegni() {
		return legni;
	}

	public int getMonete() {
		return monete;
	}

	public int getPietre() {
		return pietre;
	}

	public int getServitori() {
		return servitori;
	}
	
	
}