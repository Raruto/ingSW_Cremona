package main.network.protocol.socket;

import main.network.protocol.rmi.RMIClientInterface;
import main.network.protocol.rmi.RMIServerInterface;

/**
 * Classe che contiente tutte le costanti utilizzate dal protocollo per la
 * comunicazione tramite socket. Basato su {@link RMIClientInterface} e
 * {@link RMIServerInterface} per i "dettagli" sul protocollo.
 */
public class SocketConstants {

	/**
	 * Intestazioni di richiesta (Client).
	 */
	public static final String LOGIN_REQUEST = "loginRequest";
	public static final String GAME_ACTION = "gameAction";
	public static final String CHAT_MESSAGE = "chatMessage";

	/**
	 * Intestazioni di risposta (Server).
	 */
	public static final String ACTION_NOT_VALID = "actionNotValid";

	/**
	 * Codici di risposta del server.
	 */
	public static final int RESPONSE_OK = 200;
	public static final int RESPONSE_PLAYER_ALREADY_EXISTS = 401;
	public static final int RESPONSE_NO_ROOM_AVAILABLE = 402;
	public static final int RESPONSE_PLAYER_FORCE_JOINED = 403;
	public static final int RESPONSE_CONFIGURATION_NOT_VALID = 418;

	/**
	 * Costruttore privato.
	 */
	private SocketConstants() {
		// Questa classe non � stata progettata per essere istanziata.
	}
}