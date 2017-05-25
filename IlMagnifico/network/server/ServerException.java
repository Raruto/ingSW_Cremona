package network.server;

import java.io.IOException;

/**
 * Eccezione scatenata quando si verifica un errore durante l'inizializzazione
 * del server.
 */
public class ServerException extends IOException {

	/**
	 * Costruttore.
	 * 
	 * @param message
	 *            messaggio di errore.
	 */
	public ServerException(String message) {
		super(message);
	}

	/**
	 * Costruttore.
	 * 
	 * @param message
	 *            messaggio di errore.
	 * @param cause
	 *            causa dell'errore.
	 */
	public ServerException(String message, Throwable cause) {
		super(message, cause);
	}
}