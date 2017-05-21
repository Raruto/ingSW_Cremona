package main.game.board.tower;

import main.game.cards.development.CartaSviluppo;

public class Torre {
	private SpazioTorre[] piani;

	public void setCartaPiano(CartaSviluppo carta, int piano) {

	}

	public CartaSviluppo prendiCarta(int piano) {
		return this.piani[piano].prendiCarta();
	}

	public CartaSviluppo visualizzaCarta(int piano) {
		return this.piani[piano].visualizzaCarta();
	}

}
