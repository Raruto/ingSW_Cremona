package network.server;

import network.exceptions.CreateRoomException;
import network.exceptions.JoinRoomException;
import network.exceptions.LoginException;

import network.model.exceptions.PlayerNotFound;

/**
 * Interfaccia usata come server controller in {@link AbstractServer} (invio di
 * richieste al server da parte del client).
 */
public interface IServer {

	/**
	 * Login del giocatore tramite nickname.
	 * 
	 * @param nickname
	 *            nome con cui il giocatore vorrebbe essere identificato sul
	 *            server.
	 * @param player
	 *            riferimento al giocatore che ha effettuato la richiesta (es.
	 *            {@link RMIPlayer}, {@link SocketPlayer}).
	 * @throws LoginException
	 *             se esiste gi� un altro giocatore con il nome fornito.
	 */
	void loginPlayer(String nickname, RemotePlayer remotePlayer) throws LoginException;

	/**
	 * Get the player associated to required nickname.
	 * 
	 * @param nickname
	 *            of the player to retrieve.
	 * @return the associated remote player if found.
	 */
	RemotePlayer getPlayer(String nickname);

	/**
	 * Join player to the first available room.
	 * 
	 * @param remotePlayer
	 *            that would join.
	 * @throws JoinRoomException
	 *             if no available room has been found.
	 */
	void joinFirstAvailableRoom(RemotePlayer remotePlayer) throws JoinRoomException;

	/**
	 * Create a new room on server.
	 * 
	 * @param remotePlayer
	 *            that made the request.
	 * @param maxPlayers
	 *            that player would like to add in the room.
	 * @throws CreateRoomException
	 *             if another player has created a new room in the meanwhile.
	 * @return configuration bundle that contains all default configurations.
	 */
	/* Configuration */ void createNewRoom(RemotePlayer remotePlayer, int maxPlayers) throws CreateRoomException;

	/**
	 * Check and apply the provided configuration into the player's room.
	 * 
	 * @param room
	 *            where should be applied the configuration.
	 * @param configuration
	 *            that should be validated and applied.
	 * @throws InvalidConfigurationException
	 *             if provided configuration is not valid.
	 */
	// void applyGameConfiguration(Room room, Configuration configuration)
	// throws InvalidConfigurationException;

	/**
	 * Invia un messaggio di chat a tutti i giocatori o un giocatore specifico.
	 * 
	 * @param player
	 *            MITTENTE del messaggio.
	 * @param receiver
	 *            nome del DESTINATARIO del messaggio. Se null il messaggio
	 *            verr� inviato a tutti i giocatori.
	 * @param message
	 *            messaggio da inviare.
	 * @throws PlayerNotFound
	 *             se il ricevitore non non corrisponde a nessun giocatore
	 *             presente sul server.
	 */
	public void sendChatMessage(RemotePlayer player, String receiver, String message) throws PlayerNotFound;
}