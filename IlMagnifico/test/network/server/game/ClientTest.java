package test.network.server.game;

import static org.junit.Assert.*;

import org.junit.Test;

import main.model.enums.EAzioniGiocatore;
import main.model.enums.EColoriPedine;
import main.model.enums.EEffettiPermanenti;
import main.model.enums.ESceltePrivilegioDelConsiglio;
import main.network.client.Client;
import main.network.client.ClientException;
import main.network.server.Server;
import main.network.server.ServerException;
import main.util.Costants;

public class ClientTest {

	/**
	 * Test che verifica il funzionamento della comunicazione nel metodo
	 * relativo allo spostamento di un famigliare su una zona mercato
	 */
	@Test
	public void testMovePawnMarket() {
		try {
			Server server = new Server();
			server.startServer(Costants.SOCKET_PORT, Costants.RMI_PORT);
		} catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String nomeGiocatore = "";
		String nomeLastUpdate = "";
		try {
			Client client1 = new Client();
			client1.startClient("RMI", Costants.SERVER_ADDRESS, Costants.SOCKET_PORT, Costants.RMI_PORT);
			client1.loginPlayer("primo");
			Client client2 = new Client();
			client2.startClient("SOCKET", Costants.SERVER_ADDRESS, Costants.SOCKET_PORT, Costants.RMI_PORT);
			client2.loginPlayer("secondo");
			if (client1.getNickname().equals(client1.getPlayerTurn())) {
				nomeGiocatore = client1.getNickname();
				client1.movePawn(EAzioniGiocatore.Mercato, EColoriPedine.Nera, 1,
						new ESceltePrivilegioDelConsiglio[] { null, null });
				nomeLastUpdate = client1.getLatestUpdate().getNomeGiocatore();
			} else if (client2.getNickname().equals(client2.getPlayerTurn())) {
				nomeGiocatore = client2.getNickname();
				client2.movePawn(EAzioniGiocatore.Mercato, EColoriPedine.Nera, 1,
						new ESceltePrivilegioDelConsiglio[] { null, null });
				nomeLastUpdate = client2.getLatestUpdate().getNomeGiocatore();
			}
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(nomeGiocatore.equals(nomeLastUpdate));
	}

	@Test
	public void testMovePawnHarvest() {
		try {
			Server server = new Server();
			server.startServer(Costants.SOCKET_PORT, Costants.RMI_PORT);
		} catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String nomeGiocatore = "";
		String nomeLastUpdate = "";
		try {
			Client client1 = new Client();
			client1.startClient("RMI", Costants.SERVER_ADDRESS, Costants.SOCKET_PORT, Costants.RMI_PORT);
			client1.loginPlayer("primo");
			Client client2 = new Client();
			client2.startClient("SOCKET", Costants.SERVER_ADDRESS, Costants.SOCKET_PORT, Costants.RMI_PORT);
			client2.loginPlayer("secondo");
			if (client1.getNickname().equals(client1.getPlayerTurn())) {
				nomeGiocatore = client1.getNickname();
				client1.movePawn(EAzioniGiocatore.Raccolto, EColoriPedine.Nera, 1,
						new EEffettiPermanenti[] { null, null });
				nomeLastUpdate = client1.getLatestUpdate().getNomeGiocatore();
			} else if (client2.getNickname().equals(client2.getPlayerTurn())) {
				nomeGiocatore = client2.getNickname();
				client2.movePawn(EAzioniGiocatore.Raccolto, EColoriPedine.Nera, 1,
						new EEffettiPermanenti[] { null, null });
				nomeLastUpdate = client2.getLatestUpdate().getNomeGiocatore();
			}
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(nomeGiocatore.equals(nomeLastUpdate));
	}
	
}
