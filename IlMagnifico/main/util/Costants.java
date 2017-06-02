package main.util;

/**
 * Classe che definisce alcune costanti del il gioco e parametri per la
 * comunicazione con il server.
 */
public class Costants {
	// Connessione
	public static final String SERVER_ADDRESS = "127.0.0.1";
	public static final int SOCKET_PORT = 1098;
	public static final int RMI_PORT = 1099;
	public static final int MAX_CONNECTION_ATTEMPTS = 2;
	public static final int CONNECTION_RETRY_SECONDS = 0;

	// Stanza
	public static final int ROOM_MIN_PLAYERS = 2;
	public static final int ROOM_MAX_PLAYERS = 4;
	public static final int ROOM_WAITING_TIME = 1;
	public static final boolean ROOM_ENABLE_LOG = true;

	// Gioco
	public static final String GAME_ID = ANSI.CYAN + "[" + "GAME" + "]" + ANSI.RESET;
	public static final String ROOM_ID = ANSI.YELLOW + "[" + "ROOM" + "]" + ANSI.RESET;

	/**
	 * Costruttore privato.
	 */
	private Costants() {
		// Questa classe non � stata progettata per essere istanziata.
	}
}
