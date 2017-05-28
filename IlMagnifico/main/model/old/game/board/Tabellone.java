package main.model.old.game.board;

import java.util.*;

import main.model.old.game.board.council.PalazzoConsiglio;
import main.model.old.game.board.harvest.AreaRaccolta;
import main.model.old.game.board.market.Mercato;
import main.model.old.game.board.production.AreaProduzione;
import main.model.old.game.board.tower.AreaTorre;

/**
 * 
 */
public class Tabellone {

	/**
	 * Default constructor
	 */
	public Tabellone() {
	}

	/**
	 * 
	 */
	private AreaTorre torri;

	/**
	 * 
	 */
	private AreaRaccolta areaRaccolta;

	/**
	 * 
	 */
	private AreaProduzione areaProduzione;

	/**
	 * 
	 */
	private Mercato mercato;

	/**
	 * 
	 */
	private PalazzoConsiglio palazzoDelConsiglio;

	/**
	 * 
	 */
	public AreaTorre getTorri() {
		return this.torri;
	}

	/**
	 * 
	 */
	public PalazzoConsiglio getPalazzoDelConsiglio() {
		return this.palazzoDelConsiglio;
	}

}