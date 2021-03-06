package main.network.client;

import java.awt.Frame;
import java.rmi.RemoteException;
import java.util.HashMap;

import main.model.Famigliare;
import main.model.Plancia;
import main.model.Punti;
import main.model.Risorsa;
import main.model.Scomunica;
import main.model.SpazioAzione;
import main.model.enums.EAzioniGiocatore;
import main.model.enums.ECarte;
import main.model.enums.EColoriGiocatori;
import main.model.enums.EColoriPedine;
import main.model.enums.ECostiCarte;
import main.model.enums.EEffettiPermanenti;
import main.model.enums.EFasiDiGioco;
import main.model.enums.ESceltePrivilegioDelConsiglio;
import main.model.errors.Errors;
import main.network.NetworkException;
import main.network.client.rmi.RMIClient;
import main.network.client.socket.SocketClient;
import main.network.exceptions.LoginException;
import main.network.protocol.ConnectionTypes;
import main.network.server.game.Game;
import main.network.server.game.UpdateStats;
import main.ui.cli.CLI;
import main.util.ANSI;
import main.util.Costants;

/**
 * 
 * Client del gioco "Lorenzo Il Magnifico" della "CranioCreations".
 *
 */
public class Client implements IClient {
	/**
	 * Indirizzo Server sui cui le comunicazioni sono aperte.
	 */
	private static final String SERVER_ADDRESS = Costants.SERVER_ADDRESS;

	/**
	 * Porta in cui e' aperta la comunicazione Socket.
	 */
	private static final int SERVER_SOCKET_PORT = Costants.SOCKET_PORT;

	/**
	 * Porta in cui e' aperta la comunicazione RMI.
	 */
	private static final int SERVER_RMI_PORT = Costants.RMI_PORT;

	/**
	 * Flag per determinare stato della connessione.
	 */
	private boolean isLogged;

	/**
	 * Classe astratta che rappresenta il client selezionato (RMI o Socket).
	 */
	private AbstractClient client;

	/**
	 * Interfaccia per la inviare eventi a {@link CLI} oppure {@link Frame} (a
	 * seconda del tipo di interfaccia utilizzata per fare partire il client).
	 */
	private IClient ui;

	/**
	 * Nome del giocatore corrente.
	 */
	private String nickname;

	/*
	 * Mappa di tutte le intestazioni dei metodi per la gestione delle risposte
	 * del server.
	 */
	private final HashMap<Object, ResponseHandler> responseMap;

	/**
	 * {@link SpazioAzione} aggiornato all'ultimo aggiornamento ricevuto dal
	 * Server (vedi {@link UpdateStats}).
	 */
	private SpazioAzione board;

	/**
	 * Plance dei giocatori ("Nome","Plancia") aggiornate all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link Plancia}).
	 */
	private HashMap<String, Plancia> playersDashboards;

	/**
	 * Famigliari dei giocatori ("Nome","Famigliare[]") aggiornate all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link Famigliare}).
	 */
	private HashMap<String, Famigliare[]> playersFamilies;

	/**
	 * Risorse dei giocatori ("Nome","Risorsa") aggiornate all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link Risorsa}).
	 */
	private HashMap<String, Risorsa> playersResources;

	/**
	 * Punti dei giocatori ("Nome","Punti") aggiornate all'ultimo aggiornamento
	 * ricevuto dal Server (vedi {@link Punti}).
	 */
	private HashMap<String, Punti> playersPoints;

	/**
	 * Scomuniche dei giocatori ("Nome","Scomunica[]") aggiornate all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link Scomunica}).
	 */
	private HashMap<String, Scomunica[]> playersExcommunications;

	/**
	 * Colori dei giocatori ("Nome","EColoriGiocatori") aggiornate all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link EColoriGiocatori}).
	 */
	private HashMap<String, EColoriGiocatori> playersColors;

	/**
	 * Scomuniche della partita (vedi {@link Scomunica}).
	 */
	private Scomunica[] excommunications;

	/**
	 * Turno corrente della partita (da 1 a 6).
	 */
	private int turn;

	/**
	 * Nome del giocatore attualmente di turno, aggiornato all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link UpdateStats}).
	 */
	private String playerTurn;

	/**
	 * Copia di "Backup" dell'ultimo aggiornamento {@link UpdateStats} ricevuto
	 * dal Server.
	 */
	private UpdateStats latestUpdate;

	/**
	 * Flag usato per determinare se il giocatore è abilitato ad effettuare una
	 * richiesta di {@link EAzioniGiocatore#SostegnoChiesa} (aka. Rapporto con
	 * il Vaticano).
	 */
	private boolean churchSupportFase;

	/**
	 * Flag usato per determinare se la partita in cui è inserito il giocatore è
	 * iniziata o meno.
	 */
	private boolean isGameStarted;

	/**
	 * Flag usato per abilitare il Log sul Server.
	 */
	private final boolean LOG_ENABLED = Costants.CLIENT_ENABLE_LOG;

	/**
	 * Crea una nuova istanza della classe.
	 * 
	 * @throws ClientException
	 *             se si verifica un errore.
	 */
	public Client(IClient ui) throws ClientException {
		this.ui = ui;
		nickname = "anonymous";
		isLogged = false;

		playersDashboards = new HashMap<>();
		playersFamilies = new HashMap<>();
		playersResources = new HashMap<>();
		playersPoints = new HashMap<>();
		playersExcommunications = new HashMap<>();
		playersColors = new HashMap<>();
		churchSupportFase = false;
		isGameStarted = false;

		this.excommunications = new Scomunica[3];
		this.turn = 0;

		responseMap = new HashMap<>();
		loadResponses();
	}

	/**
	 * Inizializza "responseMap" caricando tutti i possibili metodi di risposta
	 * (chiamati da {@link ResponseHandler}).
	 */
	private void loadResponses() {
		responseMap.put(EFasiDiGioco.InizioPartita, this::onGameStarted);
		responseMap.put(EFasiDiGioco.InizioPeriodo, this::onPeriodStarted);
		responseMap.put(EFasiDiGioco.InizioTurno, this::onTurnStarted);
		responseMap.put(EFasiDiGioco.FineTurno, this::onTurnEnd);
		responseMap.put(EFasiDiGioco.FinePeriodo, this::onPeriodEnd);
		responseMap.put(EFasiDiGioco.FinePartita, this::onGameEnd);

		responseMap.put(EFasiDiGioco.MossaGiocatore, this::onPlayerMove);
		responseMap.put(EFasiDiGioco.SostegnoChiesa, this::onChurchSupport);

		responseMap.put(EAzioniGiocatore.Mercato, this::onMarket);
		responseMap.put(EAzioniGiocatore.PalazzoConsiglio, this::onCouncilPalace);
		responseMap.put(EAzioniGiocatore.Produzione, this::onProductionRound);
		responseMap.put(EAzioniGiocatore.Raccolto, this::onHarvestRound);
		responseMap.put(EAzioniGiocatore.Torre, this::onTower);
		responseMap.put(EAzioniGiocatore.RaccoltoOvale, this::onHarvestOval);
		responseMap.put(EAzioniGiocatore.ProduzioneOvale, this::onProductionOval);
		responseMap.put(EAzioniGiocatore.Famigliare, this::onPayServant);
	}

	/**
	 * Metodo statico per eseguire il client.
	 * 
	 * @param args
	 *            parametri per la connessione.
	 */
	public static void main(String[] args) {
		String serverAddress = SERVER_ADDRESS;
		int socketPort = SERVER_SOCKET_PORT, rmiPort = SERVER_RMI_PORT;

		// Check if arguments were passed in
		if (args.length != 0) {
			try {
				serverAddress = args[0];
				socketPort = Integer.parseInt(args[1]);
				rmiPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Proper usage is: [\"serverAddress\" socketPort rmiPort]");
				System.exit(0);
			}
		}

		// Debugging purpose
		try {
			CLI.mainClient(serverAddress, socketPort, rmiPort);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * "True" se il giocatore ha portato a termine correttamente la fase di
	 * login presso il server.
	 * 
	 * @return boolean isLogged
	 */
	public boolean isLogged() {
		return this.isLogged;
	}

	/**
	 * Nome scelto dal giocatore durante la fase di login e approvato dal
	 * server.
	 * 
	 * @return String nickname
	 */
	public String getNickname() {
		return this.nickname;
	}

	/**
	 * Avvia connessioni client.
	 * 
	 * @param connectionType
	 *            nome del tipo di connessione scelta
	 * @param serverAddress
	 *            indirizzo Server sui cui le comunicazioni sono aperte
	 * @param socketPort
	 *            porta in cui e' aperta la comunicazione Socket.
	 * @param rmiPort
	 *            porta in cui e' aperta la comunicazione RMI.
	 * @throws ClientException
	 *             se si verifica un errore.
	 */
	public void startClient(String connectionType, String serverAddress, int socketPort, int rmiPort)
			throws ClientException {
		if (connectionType.equals(ConnectionTypes.RMI.toString())) {
			startRMIClient(serverAddress, rmiPort);
		} else if (connectionType.equals(ConnectionTypes.SOCKET.toString())) {
			startSocketClient(serverAddress, socketPort);
		} else {
			throw new ClientException(new Throwable("Uknown Connection Type"));
		}
	}

	/**
	 * Avvia la connessione RMI.
	 *
	 * @param serverAddress
	 *            indirizzo del Server su cui avviare la connessione.
	 * @param rmiPort
	 *            porta in cui e' aperta la comunicazione RMI.
	 * 
	 * @throws ClientException
	 *             se si verifica un errore.
	 */
	private void startRMIClient(String serverAddress, int rmiPort) throws ClientException {
		System.out.println("Starting RMI Connection...");
		client = new RMIClient(this, serverAddress, rmiPort);
		client.connect();

		System.out.println();
	}

	/**
	 * Avvia la connessione Socket.
	 * 
	 * @param serverAddress
	 *            indirizzo del Server su cui avviare la connessione.
	 * @param sockePort
	 *            porta dove avviare la connessione.
	 * @throws ClientException
	 *             se si verifica un errore.
	 */
	private void startSocketClient(String serverAddress, int socketPort) throws ClientException {
		System.out.println("Starting Socket Connection...");
		client = new SocketClient(this, serverAddress, socketPort);
		client.connect();

		System.out.println();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// "Getters" (per verificare lo stato del Client, in Locale).
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Ritorna lo {@link SpazioAzione} aggiornato all'ultimo aggiornamento
	 * ricevuto dal Server (vedi {@link UpdateStats}).
	 */
	public SpazioAzione getGameBoard() {
		return this.board;
	}

	/**
	 * Ritorna le Plance dei giocatori ("Nome","Plancia") aggiornate all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link Plancia}).
	 */
	public HashMap<String, Plancia> getPlayersDashboards() {
		return this.playersDashboards;
	}

	/**
	 * Ritorna i Famigliari dei giocatori ("Nome","Famigliare[]") aggiornate
	 * all'ultimo aggiornamento ricevuto dal Server (vedi {@link Famigliare}).
	 */
	public HashMap<String, Famigliare[]> getPlayersFamilies() {
		return this.playersFamilies;
	}

	/**
	 * Ritorna le Risorse dei giocatori ("Nome","Risorsa") aggiornate all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link Risorsa}).
	 */
	public HashMap<String, Risorsa> getPlayersResources() {
		return this.playersResources;
	}

	/**
	 * Ritorna le Risorse dei giocatori ("Nome","Punti") aggiornate all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link Punti}).
	 */
	public HashMap<String, Punti> getPlayersPoints() {
		return this.playersPoints;
	}

	/**
	 * Ritorna le Scomuniche dei giocatori ("Nome","Scomunica[]") aggiornate
	 * all'ultimo aggiornamento ricevuto dal Server (vedi {@link Scomunica}).
	 */
	public HashMap<String, Scomunica[]> getPlayersExcommunications() {
		return this.playersExcommunications;
	}

	/**
	 * Ritorna i colori dei giocatori ("Nome","EColoriGiocatori") aggiornate
	 * all'ultimo aggiornamento ricevuto dal Server (vedi
	 * {@link EColoriGiocatori}).
	 */
	public HashMap<String, EColoriGiocatori> getPlayersColors() {
		return this.playersColors;
	}

	/**
	 * Ritorna le Scomuniche della partita (vedi {@link Scomunica}).
	 */
	public Scomunica[] getExcommunications() {
		return this.excommunications;
	}

	/**
	 * Ritorna il numero del turno corrente (da 1 a 6).
	 * 
	 * @return turn
	 */
	public int getTurnNumber() {
		return this.turn;
	}

	/**
	 * Ritorna il Nome del giocatore attualmente di turno, aggiornato all'ultimo
	 * aggiornamento ricevuto dal Server (vedi {@link UpdateStats}).
	 */
	public String getPlayerTurn() {
		return playerTurn;
	}

	/**
	 * Ritorna l'ultimo oggetto aggiornamento ricevuto dal Server (vedi
	 * {@link UpdateStats}).
	 */
	public UpdateStats getLatestUpdate() {
		return this.latestUpdate;
	}

	/**
	 * Ritorna True se è attualmente attivo {@link EFasiDiGioco#SostegnoChiesa}
	 * (aka. Rapporto con il Vaticano).
	 * 
	 * @return churchSupportFase
	 */
	public boolean isChurchSupportFase() {
		return this.churchSupportFase;
	}

	/**
	 * Ritorna True se la partita è iniziata.
	 * 
	 * @return isGameStarted
	 */
	public boolean isGameStarted() {
		return this.isGameStarted;
	}

	/**
	 * Ritorna True se il giocatore puo' effettuare un {@link EAzioniGiocatore}
	 * (diversa da {@link EAzioniGiocatore#SostegnoChiesa}).
	 * 
	 * @return boolean
	 */
	public boolean isGameActionAvailable() {
		return this.isGameStarted() && !isChurchSupportFase();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// "Senders" (per l'invio di informazioni verso il Server, in Remoto).
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Metodo per effettuare il login presso il Server.
	 * 
	 * @param nickname
	 *            nickname da usare per il login presso il Server.
	 */
	public void loginPlayer(String nickname) {
		boolean success = false;
		try {
			System.out.println("Try to login user with nickname: " + nickname);
			client.sendLoginRequest(nickname);
			success = true;
		} catch (LoginException e) {
			System.out.println("Nickname is already in use on server");
		} catch (NetworkException e) {
			System.err.println(e.getMessage());
		}

		if (success) {
			this.nickname = nickname;
			this.isLogged = true;
			System.out.println("Logged in as: " + nickname);
		}
	}

	/**
	 * Callback per inviare un messaggio sulla chat.
	 * 
	 * @param nickname
	 *            del destinatario se e' un messaggio privato, altrimenti null.
	 * @param messaggio
	 *            da inviare.
	 */
	public void sendChatMessage(String receiver, String message) {
		try {
			client.sendChatMessage(receiver, message);
		} catch (NetworkException e) {
			System.err.println("Cannot send chat message request");
		}
	}

	/**
	 * Callback per inviare una richiesta da parte del giocatore per svolgere
	 * una "azione giocatore" (vedi {@link EAzioniGiocatore}).
	 * 
	 * @param requestedAction
	 */
	public void performGameAction(UpdateStats requestedAction) {
		try {
			client.sendGameActionRequest(requestedAction);
		} catch (NetworkException e) {
			System.err.println("Cannot perform action request");
		}
	}

	/**
	 * Invia una richiesta di spostamento di una pedina sopra una zona Mercato
	 * che richiede la scelta di privilegi del consiglio (Area 4).
	 * 
	 * @param action
	 *            tipicamente {@link EAzioniGiocatore#Mercato}.
	 * @param color
	 *            colore della pedina da spostare (vedi {@link EColoriPedine}).
	 * @param position
	 *            posizione all'interno della zona selezionata (per l'Area 4 del
	 *            mercato position == 3).
	 * @param choosedPrivileges
	 *            array di {@link ESceltePrivilegioDelConsiglio} contenenti i
	 *            privilegi del consiglio scelti.
	 */
	public void movePawn(EAzioniGiocatore action, EColoriPedine color, Integer position,
			ESceltePrivilegioDelConsiglio[] choosedPrivileges) {
		UpdateStats requestedAction = new UpdateStats(action);
		requestedAction.spostaPedina(color, position);
		requestedAction.setSceltePrivilegiConsiglio(choosedPrivileges);
		performGameAction(requestedAction);
	}

	/**
	 * Invia una richiesta di spostamento di una pedina sopra una zona Torre che
	 * richiede la scelta di un costo opzionale (es.
	 * {@link ECarte#SOSTEGNO_AL_VESCOVO}).
	 * 
	 * @param action
	 *            tipicamente {@link EAzioniGiocatore#Torre}.
	 * @param color
	 *            colore della pedina da spostare (vedi {@link EColoriPedine}).
	 * @param position
	 *            posizione all'interno della zona selezionata (le torri vanno
	 *            da 0 a 15, [0..3] = Territorio, [4..7] = Personaggio, [8..11]
	 *            = Edificio, [12..15] = Impresa), per esempio: (position == 1)
	 *            è il Secondo Piano della Torre Territorio.
	 * @param choosedCosts
	 *            array di {@link ECostiCarte} contenenti i costi scelti (nel
	 *            qual caso ci siano, per esempio
	 *            {@link ECarte#SOSTEGNO_AL_VESCOVO} ha due costi possibili:
	 *            {@link ECostiCarte#SOSTEGNO_AL_VESCOVO1},
	 *            {@link ECostiCarte#SOSTEGNO_AL_VESCOVO2}).
	 */
	public void movePawn(EAzioniGiocatore action, EColoriPedine color, Integer position, ECostiCarte[] choosedCosts) {
		UpdateStats requestedAction = new UpdateStats(action);
		requestedAction.spostaPedina(color, position);
		requestedAction.setScelteCosti(choosedCosts);
		performGameAction(requestedAction);
	}

	/**
	 * Invia una richiesta di spostamento di una pedina sopra una zona
	 * Produzione o Raccolto che richiede la scelta di attivazione degli effetti
	 * permanenti (es. {@link ECarte#TAGLIAPIETRE},
	 * {@link ECarte#FALEGNAMERIA}).
	 * 
	 * @param action
	 *            tipicamente {@link EAzioniGiocatore#Mercato} oppure
	 *            {@link EAzioniGiocatore#Produzione}.
	 * @param color
	 *            colore della pedina da spostare (vedi {@link EColoriPedine}).
	 * @param position
	 *            posizione all'interno della zona selezionata (tipicamente è
	 *            position==0 per la prima posizione disponibile).
	 * @param choosedEffects
	 *            array di {@link EEffettiPermanenti} contenente tutti gli
	 *            effetti permanenti delle carte presenti nella plancia
	 *            giocatore che il giocatore ha scelto di attivare.
	 */
	public void movePawn(EAzioniGiocatore action, EColoriPedine color, Integer position,
			EEffettiPermanenti[] choosedEffects) {
		UpdateStats requestedAction = new UpdateStats(action);
		requestedAction.spostaPedina(color, position);
		requestedAction.setScelteEffettiPermanenti(choosedEffects);
		performGameAction(requestedAction);
	}

	/**
	 * Invia una richiesta di spostamento di una pedina sopra una zona Torre che
	 * NON richiede la scelta di un costo opzionale (es. {@link ECarte#BOSCO}).
	 * 
	 * @param action
	 *            tipicamente {@link EAzioniGiocatore#Torre}.
	 * @param color
	 *            colore della pedina da spostare (vedi {@link EColoriPedine}).
	 * @param position
	 *            posizione all'interno della zona selezionata (le torri vanno
	 *            da 0 a 15, [0..3] = Territorio, [4..7] = Personaggio, [8..11]
	 *            = Edificio, [12..15] = Impresa), per esempio: (position == 1)
	 *            è il Secondo Piano della Torre Territorio.
	 */
	public void movePawn(EAzioniGiocatore action, EColoriPedine color, int position) {
		UpdateStats requestedAction = new UpdateStats(action);
		requestedAction.spostaPedina(color, position);
		performGameAction(requestedAction);
	}

	/**
	 * Invia una richiesta di supportare o meno il Vaticano ().
	 * 
	 * @param isSupported
	 *            se True il Vaticano sara' supportato dal giocatore, False
	 *            altrimenti.
	 */
	public void supportChurch(boolean isSupported) {
		UpdateStats requestedAction = new UpdateStats(EAzioniGiocatore.SostegnoChiesa);
		requestedAction.supportaChiesa(isSupported);
		performGameAction(requestedAction);
	}

	/**
	 * Invia una richiesta di aumentare il valore di una Pedina pagando con i
	 * servitori posseduti.
	 * 
	 * @param color
	 *            colore della pedina selezionata (vedi {@link EColoriPedine}).
	 * @param servants
	 *            numero di servitori spesi per aumentare il valore della pedina
	 *            (1 Servitore = +1).
	 */
	public void incrementPawnValue(EColoriPedine color, int servants) {
		UpdateStats requestedAction = new UpdateStats(EAzioniGiocatore.Famigliare);
		requestedAction.aumentaValorePedina(color, servants);
		performGameAction(requestedAction);
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// Metodi invocati sul Client Controller (vedi RMIClient, SocketClient)
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Notifica che e' arrivato un nuovo messaggio dalla chat.
	 * 
	 * @param author
	 *            autore del messaggio.
	 * @param message
	 *            corpo del messaggio.
	 */
	@Override
	public void onChatMessage(String author, String message) {
		ui.onChatMessage(author, message);
	}

	/**
	 * Metodo invocato dal Server ogni qualvolta si presenta un errore (es.
	 * azione illegale) a seguito di una richiesta del giocatore (vedi
	 * {@link Client#performGameAction(UpdateStats)})
	 * 
	 * @param errorCode
	 *            stringa contenenti informazioni sull'errore (vedi
	 *            {@link Errors}).
	 */
	@Override
	public void onActionNotValid(String errorCode) {
		ui.onActionNotValid(errorCode);
	}

	/**
	 * Metodo invocato dal Server ogni qualvolta l'azione richiesta dal
	 * giocatore è stata accettata (vedi
	 * {@link Client#performGameAction(UpdateStats)}) oppure si è verificato
	 * un'avanzamento nello stato della logica della partita (vedi
	 * {@link Game}).
	 * 
	 * @param update
	 *            oggetto aggiornamento contenente tutte le informazioni
	 *            relative all'avanzamento della partita (vedi
	 *            {@link UpdateStats}).
	 */
	@Override
	public void onGameUpdate(UpdateStats update) {
		String playerName = update.getNomeGiocatore();

		// update local game copy
		this.latestUpdate = update;
		this.board = update.getSpazioAzione();

		if (update.getPlanciaGiocatore() != null)
			this.playersDashboards.put(playerName, update.getPlanciaGiocatore());
		if (update.getFamigliaGiocatore() != null)
			this.playersFamilies.put(playerName, update.getFamigliaGiocatore());
		if (update.getRisorseGiocatore() != null)
			this.playersResources.put(playerName, update.getRisorseGiocatore());
		if (update.getPuntiGiocatore() != null)
			this.playersPoints.put(playerName, update.getPuntiGiocatore());
		if (update.getScomunicheGiocatore() != null)
			this.playersExcommunications.put(playerName, update.getScomunicheGiocatore());
		if (update.getColoreGiocatore() != null)
			this.playersColors.put(playerName, update.getColoreGiocatore());

		// handle server response
		if (update.getAzioneGiocatore() != null) {
			log(playerName, "ACTION: " + ANSI.YELLOW + update.getAzioneGiocatore().toString() + ANSI.RESET);
		} else if (update.getAzioneServer() != null) {
			if (update.getAzioneServer() != EFasiDiGioco.MossaGiocatore)
				log(Costants.GAME_ID, "UPDATE: " + ANSI.CYAN + update.getAzioneServer().toString() + ANSI.RESET);
		}
		handleResponse(update);

		// "fallback" (please don't use it..)
		ui.onGameUpdate(update);
	}

	/**
	 * Scatenato quando il server notifica {@link EFasiDiGioco#SostegnoChiesa}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onChurchSupport(UpdateStats update) {
		this.churchSupportFase = true;
		ui.onChurchSupport(update);
	}

	/**
	 * Scatenato quando il server ha autorizzato un giocatore ad eseguire:
	 * {@link EAzioniGiocatore#Mercato}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onMarket(UpdateStats update) {
		ui.onMarket(update);
	}

	/**
	 * Scatenato quando il server ha autorizzato un giocatore ad eseguire:
	 * {@link EAzioniGiocatore#Famigliare}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onPayServant(UpdateStats update) {
		ui.onPayServant(update);
	}

	/**
	 * Scatenato quando il server ha autorizzato un giocatore ad eseguire:
	 * {@link EAzioniGiocatore#Torre}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onTower(UpdateStats update) {
		ui.onTower(update);
	}

	/**
	 * Scatenato quando il server ha autorizzato un giocatore ad eseguire:
	 * {@link EAzioniGiocatore#PalazzoConsiglio}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onCouncilPalace(UpdateStats update) {
		ui.onCouncilPalace(update);
	}

	/**
	 * Scatenato quando il server ha autorizzato un giocatore ad eseguire:
	 * {@link EAzioniGiocatore#Raccolto}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onHarvestRound(UpdateStats update) {
		ui.onHarvestRound(update);
	}

	/**
	 * Scatenato quando il server ha autorizzato un giocatore ad eseguire:
	 * {@link EAzioniGiocatore#Produzione}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onProductionRound(UpdateStats update) {
		ui.onProductionRound(update);
	}

	/**
	 * Scatenato quando il server ha autorizzato un giocatore ad eseguire:
	 * {@link EAzioniGiocatore#RaccoltoOvale}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onHarvestOval(UpdateStats update) {
		ui.onHarvestOval(update);
	}

	/**
	 * Scatenato quando il server ha autorizzato un giocatore ad eseguire:
	 * {@link EAzioniGiocatore#ProduzioneOvale}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onProductionOval(UpdateStats update) {
		ui.onProductionOval(update);
	}

	/**
	 * Scatenato quando il server notifica {@link EFasiDiGioco#FineTurno}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onTurnEnd(UpdateStats update) {
		ui.onTurnEnd(update);
	}

	/**
	 * Scatenato quando il server notifica {@link EFasiDiGioco#FinePeriodo}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onPeriodEnd(UpdateStats update) {
		this.churchSupportFase = false;
		ui.onPeriodEnd(update);
	}

	/**
	 * Scatenato quando il server notifica {@link EFasiDiGioco#FinePartita}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onGameEnd(UpdateStats update) {
		isGameStarted = false;

		ui.onGameEnd(update);
	}

	/**
	 * Scatenato quando il server notifica {@link EFasiDiGioco#MossaGiocatore}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onPlayerMove(UpdateStats update) {
		this.playerTurn = update.getNomeGiocatore();

		ui.onPlayerMove(update);
	}

	/**
	 * Scatenato quando il server notifica {@link EFasiDiGioco#InizioTurno}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onTurnStarted(UpdateStats update) {
		this.turn++;
		if (update.getNomeGiocatore() != null)
			this.playerTurn = update.getNomeGiocatore();

		if (update.getFamiglieGiocatori() != null)
			this.playersFamilies = update.getFamiglieGiocatori();

		ui.onTurnStarted(update);
	}

	/**
	 * Scatenato quando il server notifica {@link EFasiDiGioco#InizioPeriodo}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onPeriodStarted(UpdateStats update) {
		ui.onPeriodStarted(update);
	}

	/**
	 * Scatenato quando il server notifica {@link EFasiDiGioco#InizioPartita}.
	 * 
	 * @param update
	 *            in ingresso un oggetto aggiornamento {@link UpdateStats}
	 *            contenente lo stato della partita con tutte le modifiche
	 *            apportate allo stato della partita.
	 */
	@Override
	public void onGameStarted(UpdateStats update) {
		isGameStarted = true;

		if (update.getRisorseGiocatori() != null)
			this.playersResources = update.getRisorseGiocatori();
		if (update.getPuntiGiocatori() != null)
			this.playersPoints = update.getPuntiGiocatori();
		if (update.getFamiglieGiocatori() != null)
			this.playersFamilies = update.getFamiglieGiocatori();
		if (update.getPlanceGiocatori() != null)
			this.playersDashboards = update.getPlanceGiocatori();
		if (update.getScomunicheGiocatori() != null)
			this.playersExcommunications = update.getScomunicheGiocatori();
		if (update.getColoriGiocatori() != null)
			this.playersColors = update.getColoriGiocatori();
		if (update.getScomuniche() != null)
			this.excommunications = update.getScomuniche();

		ui.onGameStarted(update);
	}

	/**
	 * Metodo per il "debug"
	 */
	@Override
	public void onNotify(Object object) throws RemoteException {
		ui.onNotify(object);
	}

	/*
	 * Metodo interno usato per il Log sul Client (abilitato da: LOG_ENABLED)
	 * 
	 * @param message
	 */
	public void log(String author, String message) {
		if (LOG_ENABLED) {
			String id;
			if (author.contains("[") && author.contains("]"))
				id = author;
			else
				id = ANSI.YELLOW + "[" + author.toUpperCase() + "]" + ANSI.RESET;
			System.out.println(id + " " + message);
		}
	}

	/**
	 * Gestisce la risposta ricevuta dal Server ed invoca il metodo associatogli
	 * nella "responseMap".
	 * 
	 * @param update
	 *            risposta ricevuta dal server (es. {@link UpdateStats}).
	 */
	public void handleResponse(UpdateStats update) {
		ResponseHandler handler = null;
		EAzioniGiocatore azione = update.getAzioneGiocatore();
		EFasiDiGioco fase = update.getAzioneServer();

		if (azione != null)
			handler = responseMap.get(azione);
		else if (fase != null)
			handler = responseMap.get(fase);

		if (handler != null) {
			handler.handle(update);
		}
	}

	/**
	 * Interfaccia utilizzata "come" l'interfaccia {@link Runnable}.
	 */
	@FunctionalInterface
	private interface ResponseHandler {

		/**
		 * Gestisce la risposta del Server.
		 * 
		 * @param update
		 *            (vedi {@link UpdateStats}).
		 */
		void handle(UpdateStats update);
	}

}