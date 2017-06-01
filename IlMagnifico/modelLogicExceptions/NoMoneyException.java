package modelLogicExceptions;

/**
 * Eccezione che si verifica quando un giocatore posiziona il famigliare in una
 * orre dove è presente un altro famigliare ma non ha abbastanza le tre monete
 * da pagare al banco
 */
public class NoMoneyException extends Exception {
	public NoMoneyException() {
		super();
	}
}