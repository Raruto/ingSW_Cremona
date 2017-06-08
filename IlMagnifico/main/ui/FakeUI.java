package main.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import main.model.Famigliare;
import main.model.SpazioAzione;
import main.model.enums.EAzioniGiocatore;
import main.model.enums.EColoriPedine;
import main.model.enums.ECostiCarte;
import main.model.enums.ESceltePrivilegioDelConsiglio;
import main.model.enums.ETipiCarte;
import main.network.client.Client;
import main.network.client.ClientException;
import main.network.protocol.ConnectionTypes;
import main.network.server.Server;
import main.network.server.ServerException;
import main.util.ANSI;
import main.util.Costants;
import main.util.StringAlign;

/**
 * Classe di comodo per simulare l'interazione da parte del CLIENT verso il
 * SERVER
 *
 */
public class FakeUI {
	public static Scanner scanner = new Scanner(System.in);
	public static String inText;

	static Client client;

	/**
	 * Get Singleton Client
	 * 
	 * @return {@link Client}
	 */
	public static Client getClient() {
		if (client == null) {
			try {
				client = new Client();
			} catch (ClientException e) {
				e.printStackTrace();
				System.err.println("Exiting...");
			}
		}
		return client;
	}

	/**
	 * Start as Client or Server.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String serverAddress = Costants.SERVER_ADDRESS;
		int socketPort = Costants.SOCKET_PORT, rmiPort = Costants.RMI_PORT;

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

		System.out.print("Start as [C]lient or [S]erver? (Default: [C]): ");
		inText = scanner.nextLine().toUpperCase();

		if (inText.equals("S")) {
			FakeUI.mainServer(socketPort, rmiPort);
		} else if (inText.equals("C")) {
			FakeUI.mainClient(serverAddress, socketPort, rmiPort);
		}
		// Default: Client
		else {
			System.out.println("Starting as Client..");
			FakeUI.mainClient(serverAddress, socketPort, rmiPort);
		}
	}

	/**
	 * Start Client (RMI or Socket).
	 * 
	 * @param serverAddress
	 * @param socketPort
	 * @param rmiPort
	 */
	public static void mainClient(String serverAddress, int socketPort, int rmiPort) {

		System.out.print("[R]MI or [S]ocket? (Default: [R]): ");
		inText = scanner.nextLine().toUpperCase();

		if (inText.equals("S")) {
			inText = ConnectionTypes.SOCKET.toString();
		} else if (inText.equals("R")) {
			inText = ConnectionTypes.RMI.toString();
		}
		// Default: RMI
		else {
			inText = ConnectionTypes.RMI.toString();
			System.out.println("Connecting with RMI..");
		}

		boolean success = false;
		int attempts = Costants.MAX_CONNECTION_ATTEMPTS;
		int sec = Costants.CONNECTION_RETRY_SECONDS * 1000;
		while (!success && attempts > 0) {
			try {
				attempts--;
				Client client = getClient();
				client.startClient(inText, serverAddress, socketPort, rmiPort);
				success = true;
			} catch (ClientException e) {
				if (attempts > 0) {
					System.err.println(e.getMessage() + " (" + "Retry in " + sec / 1000 + " seconds" + ", " + attempts
							+ " attemps left)");
					try {
						Thread.sleep(sec);
					} catch (InterruptedException ie) {
						// TODO Auto-generated catch block
					}
				}
			}
		}

		if (success) {
			FakeUI.login();
			FakeUI.sayHelloToPlayers();
			FakeUI.infiniteLoop();
		} else {
			System.err.println("\nCannot establish a connection to the server, the program will launch a local server");

			FakeUI.mainServer(socketPort, rmiPort);
		}
	}

	/**
	 * Start Server (Client and Server).
	 * 
	 * @param socketPort
	 * @param rmiPort
	 */
	private static void mainServer(int socketPort, int rmiPort) {
		try {
			Server server = new Server();
			server.startServer(socketPort, rmiPort);

			System.out.print("\nServer listening at: ");
			System.out.println("127.0.0.1" + " (rmi: " + rmiPort + ", socket: " + socketPort + ")");
			System.out.println();

		} catch (ServerException e) {
			System.err.println(e.getMessage());
			System.err.println("Exiting...");
			System.exit(0);
		}
	}

	/**
	 * Login Client to Server.
	 */
	public static void login() {
		Client client = getClient();
		while (!client.isLogged()) {
			System.out.print("Choose Player Name: ");
			inText = scanner.nextLine();
			client.loginPlayer(inText);

		}

		System.out.println();
	}

	/**
	 * Hello World! to other players.
	 */
	public static void sayHelloToPlayers() {
		Client client = getClient();
		client.sendChatMessage(null, "hello!");
	}

	/**
	 * Client commmand chooser.
	 */
	public static void infiniteLoop() {
		boolean quit = false;

		while (!quit) {
			System.out.println("'q' to exit\n");
			System.out.println("Available commands: [chat], [action], [board]");

			System.out.println(">");
			inText = scanner.nextLine();

			switch (inText.toLowerCase()) {
			case "q":
				quit = true;
				break;
			case "chat":
				FakeUI.sendChatMessages();
				break;

			case "action":
				FakeUI.performGameAction();
				break;

			case "board":
				FakeUI.printBoard();
				break;

			default:
				break;
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// Command: [chat]
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Client command: send chat messages.
	 */
	public static void sendChatMessages() {
		Client client = getClient();
		String receiver = null;

		System.out.println("'q' to quit\n");
		System.out.println("Send text messages: ");

		boolean quit = false;

		while (!quit) {
			System.out.println(">");
			inText = scanner.nextLine();
			if (inText.toLowerCase().equals("q")) {
				quit = true;
			} else {
				System.out.println("to [playerName]: ");
				receiver = scanner.nextLine().trim();
				if (receiver.length() == 0)
					receiver = null;
				client.sendChatMessage(receiver, inText);
			}
		}

	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// Command: [action]
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Client command: Game Action chooser.
	 */
	public static void performGameAction() {
		EAzioniGiocatore action;
		EAzioniGiocatore nestedAction;
		ESceltePrivilegioDelConsiglio[] privileges;
		Integer nestedPosition;

		boolean quit = false;
		boolean nestedQuit = false;

		while (!quit) {
			try {
				action = chooseGameAction();
				printDices(true, false);

				switch (action) {
				case Mercato:
					privileges = new ESceltePrivilegioDelConsiglio[] { null, null };
					while (!nestedQuit) {
						printMarketArea(true, true);
						nestedPosition = chooseMarketArea();
						if (nestedPosition != null) {
							try {
								// movePawn(action, nestedPosition, privilege);
								EColoriPedine color = choosePawnColor();

								if (color != null) {
									if (nestedPosition == 3) {
										privileges[0] = chooseCouncilPrivilege(
												new ArrayList<ESceltePrivilegioDelConsiglio>());
										privileges[1] = chooseCouncilPrivilege(Arrays.asList(privileges));
									}

									// if (privileges[0] != null &&
									// privileges[1] != null) {
									client.movePawn(action, color, nestedPosition, privileges);
									// }
								}

								quit = true;
								nestedQuit = true;
							} catch (QuitException e) {
								nestedQuit = false;
							}
						} else
							nestedQuit = true;
					}
					break;
				case Produzione:
					while (!nestedQuit) {
						printProductionArea(true, true);
						nestedAction = chooseProductionArea();
						if (nestedAction != null) {
							try {
								movePawn(nestedAction, 0);
								quit = true;
								nestedQuit = true;
							} catch (QuitException e) {
								nestedQuit = false;
							}
						} else
							nestedQuit = true;
					}
					break;
				case ProduzioneOvale:
					try {
						movePawn(action, 0);
						quit = true;
					} catch (QuitException e) {
					}
					break;
				case Raccolto:
					while (!nestedQuit) {
						printHarvestArea(true, true);
						nestedAction = chooseHarvestArea();
						if (nestedAction != null) {
							try {
								movePawn(nestedAction, 0);
								quit = true;
								nestedQuit = true;
							} catch (QuitException e) {
								nestedQuit = false;
							}
						} else
							nestedQuit = true;
					}
					break;
				case RaccoltoOvale:
					try {
						movePawn(action, 0);
						quit = true;
					} catch (QuitException e) {
					}
					break;
				case PalazzoConsiglio:
					printCouncilArea(true, true);
					try {
						movePawn(action, 0);
						quit = true;
					} catch (QuitException e) {
					}
					break;
				case Torre:
					while (!nestedQuit) {
						printTowerArea(true, true);
						nestedPosition = chooseTowerArea();
						if (nestedPosition != null) {
							try {
								ECostiCarte[] costs = chooseCardCost(nestedPosition);

								movePawn(action, nestedPosition);
								quit = true;
								nestedQuit = true;
							} catch (QuitException e) {
								nestedQuit = false;
							}
						} else
							nestedQuit = true;
					}
					break;
				case SostegnoChiesa:
					try {
						supportChurch();
						quit = true;
					} catch (QuitException e) {
					}
					break;

				default:
					System.out.println(ANSI.YELLOW + "Not yet implemented" + ANSI.RESET);
					break;
				}
			} catch (QuitException e) {
				quit = true;
			}
			nestedQuit = false;
		}

	}

	private static ECostiCarte[] chooseCardCost(int position) throws QuitException {
		Client client = getClient();
		SpazioAzione board = client.getBoard();
		List<ECostiCarte> costs;
		List<ECostiCarte> choosed = new ArrayList<ECostiCarte>();

		int number;
		
		if (board.getCartaTorre(position) != null) {
			int choices = board.getCartaTorre(position).getNumeroScelteCosti();
			System.out.println(choices);
			
			
			if (choices > 1) {
				costs = Arrays.asList(board.getCartaTorre(position).getCostiCarta());
				//System.out.println(costs.get(0).toString());
				boolean ok = false;

				while (!ok && choices > 0) {
					// System.out.println("'q' to quit\n");
					System.out.println("Select " + choices + " cost choice: ");
					System.out.println(costs.get(0).getNome());
					//System.out.println(ECostiCarte.stringify((ArrayList<ECostiCarte>) costs));
					inText = scanner.nextLine();
					if (inText.equals("q")) {
						throw new QuitException();
					} else {
						try {
							number = Integer.parseInt(inText);
							for (ECostiCarte cost : ECostiCarte.values()) {
								if (number == cost.ordinal()) {
									choosed.add(cost);
									choices--;
								}
							}
						} catch (NumberFormatException e) {
							for (ECostiCarte cost : ECostiCarte.values()) {
								if (inText.equalsIgnoreCase(cost.toString())) {
									choosed.add(cost);
									choices--;
								}
							}
						}
					}
				}
			}
		}
		return choosed.toArray(new ECostiCarte[choosed.size()]);
	}

	private static EAzioniGiocatore chooseGameAction() throws QuitException {
		ArrayList<EAzioniGiocatore> hidedElements = new ArrayList<EAzioniGiocatore>();
		hidedElements.add(EAzioniGiocatore.ProduzioneOvale);
		hidedElements.add(EAzioniGiocatore.RaccoltoOvale);

		boolean ok = false;

		while (!ok) {
			System.out.println("'q' to quit\n");
			System.out.println("Available actions: ");
			System.out.println(EAzioniGiocatore.stringify(hidedElements));
			inText = scanner.nextLine();

			if (inText.equals("q")) {
				throw new QuitException();
			} else {
				for (EAzioniGiocatore act : EAzioniGiocatore.values()) {
					if (inText.equalsIgnoreCase(act.toString())) {
						return act;
					}
				}
			}
		}
		return null;
	}

	private static EAzioniGiocatore chooseProductionArea() {
		boolean ok = false;
		int number;
		while (!ok) {
			// System.out.println("'q' to quit\n");
			System.out.println("Choose a Production area: [1] [2] ");

			inText = scanner.nextLine();

			if (inText.equalsIgnoreCase("q")) {
				ok = true;
			} else {
				try {
					number = Integer.parseInt(inText);
					if (number == 1) {
						return EAzioniGiocatore.Produzione;
					} else if (number == 2) {
						return EAzioniGiocatore.ProduzioneOvale;
					}
				} catch (NumberFormatException e) {
					// TODO: handle exception
				}
			}
		}
		return null;
	}

	private static EAzioniGiocatore chooseHarvestArea() {
		boolean ok = false;
		int number;
		while (!ok) {
			// System.out.println("'q' to quit\n");
			System.out.println("Choose a Harvest area: [1] [2] ");

			inText = scanner.nextLine();

			if (inText.equalsIgnoreCase("q")) {
				ok = true;
			} else {
				try {
					number = Integer.parseInt(inText);
					if (number == 1) {
						return EAzioniGiocatore.Raccolto;
					} else if (number == 2) {
						return EAzioniGiocatore.RaccoltoOvale;
					}
				} catch (NumberFormatException e) {
					// TODO: handle exception
				}
			}
		}
		return null;
	}

	private static Integer chooseMarketArea() {
		boolean ok = false;
		int number;
		while (!ok) {
			// System.out.println("'q' to quit\n");
			System.out.println("Choose a Market area: [1..4] ");

			inText = scanner.nextLine();

			if (inText.equalsIgnoreCase("q")) {
				ok = true;
			} else {
				try {
					number = Integer.parseInt(inText);
					return number - 1;
				} catch (NumberFormatException e) {
					// TODO: handle exception
				}
			}
		}
		return null;
	}

	private static ESceltePrivilegioDelConsiglio chooseCouncilPrivilege(List<ESceltePrivilegioDelConsiglio> hided)
			throws QuitException {
		boolean ok = false;
		int number;
		while (!ok) {
			// System.out.println("'q' to quit\n");
			System.out.println("Choose a privilege: ");
			System.out.println(ESceltePrivilegioDelConsiglio.stringify(hided));
			inText = scanner.nextLine();

			if (inText.equals("q")) {
				throw new QuitException();
			} else {
				try {
					number = Integer.parseInt(inText);
					for (ESceltePrivilegioDelConsiglio priv : ESceltePrivilegioDelConsiglio.values()) {
						if (number == priv.ordinal())
							return priv;
					}
				} catch (NumberFormatException e) {
					// TODO: handle exception
					for (ESceltePrivilegioDelConsiglio priv : ESceltePrivilegioDelConsiglio.values()) {
						if (inText.equalsIgnoreCase(priv.toString())) {
							return priv;
						}
					}
				}
			}
		}
		return null;
	}

	private static Integer chooseTowerArea() {
		Integer floor;
		int number;
		boolean ok = false;
		boolean nestedQuit = false;
		while (!ok) {
			// System.out.println("'q' to quit\n");
			System.out.print("Choose a Tower area: ");
			System.out.println(ETipiCarte.stringify());

			inText = scanner.nextLine();

			if (inText.equalsIgnoreCase("q")) {
				ok = true;
			} else {
				try {
					number = Integer.parseInt(inText);

					for (ETipiCarte car : ETipiCarte.values()) {
						if (number == car.ordinal()) {
							while (!nestedQuit) {
								try {
									floor = chooseTowerFloor(car);
									return floor;
								} catch (QuitException e) {
									nestedQuit = true;
								}
							}
							nestedQuit = false;

						}
					}
				} catch (NumberFormatException e) {
					// TODO: handle exception

					for (ETipiCarte car : ETipiCarte.values()) {
						if (inText.equalsIgnoreCase(car.toString())) {
							while (!nestedQuit) {
								try {
									floor = chooseTowerFloor(car);
									return floor;
								} catch (QuitException e1) {
									nestedQuit = true;
								}
							}
							nestedQuit = false;
						}
					}

				}

			}
		}
		return null;
	}

	private static Integer chooseTowerFloor(ETipiCarte tower) throws QuitException {
		boolean ok = false;
		int number;
		while (!ok) {
			// System.out.println("'q' to quit\n");
			System.out.println("Choose a floor: [1..4] ");

			inText = scanner.nextLine();

			if (inText.equalsIgnoreCase("q")) {
				// ok = true;
				throw new QuitException();
			} else {
				try {
					number = Integer.parseInt(inText);

					switch (tower) {
					case Territorio:
						// [0..3]
						return number - 1;
					case Personaggio:
						// [4..7]
						return (number + 4) - 1;
					case Edificio:
						// [8..11]
						return (number + 8) - 1;
					case Impresa:
						// [12..15]
						return (number + 12) - 1;
					}
					return number - 1;
				} catch (NumberFormatException e) {
					// TODO: handle exception
				}
			}
		}
		return null;
	}

	private static void supportChurch() throws QuitException {
		boolean ok = false;
		while (!ok) {
			System.out.println("'q' to quit\n");
			System.out.println("Support the Church? [y/n]: ");
			inText = scanner.nextLine();

			if (inText.equalsIgnoreCase("q")) {
				// ok = true;
				throw new QuitException();
			} else if (inText.equalsIgnoreCase("y")) {
				client.supportChurch(true);
				ok = true;
			} else if (inText.equalsIgnoreCase("n")) {
				client.supportChurch(false);
				ok = true;
			}
		}
	}

	/*
	 * private static void movePawn(EAzioniGiocatore action, Integer position,
	 * ESceltePrivilegioDelConsiglio[] privileges) throws QuitException {
	 * EColoriPedine color = choosePawnColor(); if (color != null) {
	 * client.movePawn(action, color, position, privileges); } }
	 */

	private static void movePawn(EAzioniGiocatore action, Integer position) throws QuitException {
		EColoriPedine color = choosePawnColor();
		if (color != null) {
			client.movePawn(action, color, position);
		}
	}

	private static EColoriPedine choosePawnColor() throws QuitException {
		int number;
		boolean ok = false;
		while (!ok) {
			// System.out.println("'q' to quit\n");
			System.out.print("Choose a Pawn Color: ");
			System.out.println(EColoriPedine.stringify());
			inText = scanner.nextLine();

			if (inText.equalsIgnoreCase("q")) {
				throw new QuitException();
			} else {
				try {
					number = Integer.parseInt(inText);
					for (EColoriPedine col : EColoriPedine.values()) {
						if (number == col.ordinal()) {
							return col;
						}
					}
				} catch (NumberFormatException e) {
					// TODO: handle exception
					for (EColoriPedine col : EColoriPedine.values()) {
						if (inText.equalsIgnoreCase(col.toString())) {
							return col;
						}
					}
				}
			}
		}
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// Command: [board]
	/////////////////////////////////////////////////////////////////////////////////////////

	private static void printBoard() {
		printDices(true, false);
		printTowerArea(true, false);
		printProductionArea(true, false);
		printHarvestArea(true, false);
		printMarketArea(true, false);
		printCouncilArea(true, true);
	}

	private static void printDices(boolean printSep1, boolean printSep2) {
		Client client = getClient();
		SpazioAzione board = client.getBoard();

		try {
			if (printSep1)
				System.out.println(Costants.ROW_SEPARATOR);

			int[] dadi = board.getValoreDadi();
			System.out.print(ANSI.YELLOW + "Dadi: " + ANSI.RESET);
			System.out.format("%40s%18s%15s\n", "Nero = " + dadi[0], "Arancione = " + dadi[1], "Bianco = " + dadi[2]);

			if (printSep2)
				System.out.println(Costants.ROW_SEPARATOR);
		} catch (NullPointerException e) {
			System.err.println("EXCPETION:" + e.getMessage());
		}
	}

	private static void printTowerArea(boolean printSep1, boolean printSep2) {
		String col1 = "", col2 = "", col3 = "", col4 = "";
		int pad = 30, cpad1 = pad, cpad2 = pad, cpad3 = pad, cpad4 = pad;
		String row;

		try {
			if (printSep1)
				System.out.println(Costants.ROW_SEPARATOR);

			System.out.format(
					ANSI.YELLOW + "     %-" + pad + "s%-" + pad + "s%-" + pad + "s%-" + pad + "s\n" + ANSI.RESET,
					"Territorio: ", "Personaggio: ", "Edificio: ", "Impresa: ");

			StringAlign util;
			for (int i = 3; i >= 0; i--) {
				row = ((i % 4) + 1) + ": ";

				// Territorio
				col1 = row + getTowerFloor(i);
				// cpad1 = pad - col1.length();
				// Personaggio
				col2 = row + getTowerFloor(i + 4);
				// cpad2 = pad - col2.length();
				// Edificio
				col3 = row + getTowerFloor(i + 8);
				// cpad3 = pad - col3.length();
				// Impresa
				col4 = row + getTowerFloor(i + 12);
				// cpad4 = pad - col4.length();

				// util = new StringAlign(30, Alignment.LEFT);
				// System.out.println(" "+ util.format(col1) + util.format(col2)
				// + util.format(col3) + util.format(col4));

				System.out.format(" %-" + cpad1 + "s%-" + cpad2 + "s%-" + cpad3 + "s%-" + cpad4 + "s\n", col1, col2,
						col3, col4);
			}

			if (printSep2)
				System.out.println(Costants.ROW_SEPARATOR);
		} catch (NullPointerException e) {
			System.err.println("EXCPETION:" + e.getMessage());
		}
	}

	private static String getTowerFloor(int floor) {
		Client client = getClient();
		SpazioAzione board = client.getBoard();

		String col;
		if (board.getCartaTorre(floor) != null)
			col = board.getCartaTorre(floor).getNome();
		else if (board.getFamigliareTorre(floor) != null) {
			col = getStringifiedPawn(board.getFamigliareTorre(floor));
		} else
			col = null;
		return col;
	}

	private static String getStringifiedPawn(Famigliare fam) {
		return fam.getGiocatore().getColore().getANSIBackground() + fam.getColoreFamigliare().getANSICode() + "♜  "
				+ fam.getGiocatore().getNome() + ANSI.RESET;
	}

	private static void printProductionArea(boolean printSep1, boolean printSep2) {
		Client client = getClient();
		SpazioAzione board = client.getBoard();
		Famigliare zona1;
		ArrayList<Famigliare> zona2;

		try {
			if (printSep1)
				System.out.println(Costants.ROW_SEPARATOR);

			System.out.println(ANSI.YELLOW + "Produzione: " + ANSI.RESET);

			System.out.print("Zona 1: ");
			zona1 = board.getZonaProduzioneRotonda();
			if (zona1 != null)
				System.out.println(getStringifiedPawn(zona1));
			else
				System.out.println(null + "");

			System.out.print("Zona 2: ");
			zona2 = board.getZonaProduzioneOvale();
			for (int i = 0; i < zona2.size(); i++) {
				System.out.print(getStringifiedPawn(zona2.get(i)) + ", ");
			}
			System.out.println();

			if (printSep2)
				System.out.println(Costants.ROW_SEPARATOR);
		} catch (NullPointerException e) {
			System.err.println("EXCPETION:" + e.getMessage());
		}
	}

	private static void printHarvestArea(boolean printSep1, boolean printSep2) {
		Client client = getClient();
		SpazioAzione board = client.getBoard();

		Famigliare zona1;
		ArrayList<Famigliare> zona2;

		try {
			if (printSep1)
				System.out.println(Costants.ROW_SEPARATOR);

			System.out.println(ANSI.YELLOW + "Raccolto: " + ANSI.RESET);

			System.out.print("Zona 1: ");
			zona1 = board.getZonaRaccoltoRotonda();
			if (zona1 != null)
				System.out.println(getStringifiedPawn(zona1));
			else
				System.out.println(null + "");

			System.out.print("Zona 2: ");
			zona2 = board.getZonaRaccoltoOvale();
			for (int i = 0; i < zona2.size(); i++) {
				System.out.print(getStringifiedPawn(zona2.get(i)) + ", ");
			}
			System.out.println();

			if (printSep2)
				System.out.println(Costants.ROW_SEPARATOR);
		} catch (NullPointerException e) {
			System.err.println("EXCPETION:" + e.getMessage());
		}
	}

	private static void printMarketArea(boolean printSep1, boolean printSep2) {
		Client client = getClient();
		SpazioAzione board = client.getBoard();

		Famigliare[] zona;

		try {
			if (printSep1)
				System.out.println(Costants.ROW_SEPARATOR);

			System.out.println(ANSI.YELLOW + "Mercato: " + ANSI.RESET);

			zona = board.getMercato();

			for (int i = 0; i < zona.length; i++) {
				if (zona[i] != null)
					System.out.print(getStringifiedPawn(zona[i]) + ", ");
				else
					System.out.print(null + ", ");
			}
			System.out.println();

			if (printSep2)
				System.out.println(Costants.ROW_SEPARATOR);
		} catch (NullPointerException e) {
			System.err.println("EXCPETION:" + e.getMessage());
		}
	}

	private static void printCouncilArea(boolean printSep1, boolean printSep2) {
		Client client = getClient();
		SpazioAzione board = client.getBoard();

		ArrayList<Famigliare> zona;

		try {
			if (printSep1)
				System.out.println(Costants.ROW_SEPARATOR);

			System.out.println(ANSI.YELLOW + "Palazzo del Consiglio: " + ANSI.RESET);

			zona = board.getPalazzoDelConsiglio();

			for (int i = 0; i < zona.size(); i++) {
				if (zona.get(i) != null)
					System.out.print(getStringifiedPawn(zona.get(i)) + ", ");
				else
					System.out.print(null + ", ");
			}
			System.out.println();

			if (printSep2)
				System.out.println(Costants.ROW_SEPARATOR);
		} catch (NullPointerException e) {
			System.err.println("EXCPETION:" + e.getMessage());
		}
	}
}
