package main.model.enums;

import java.util.ArrayList;

public enum ECostiCarte {

	CAPPELLA("cappella", 0, 2, 0, 0, 0),
	ESATTORIA("esattoria",0,3,1,0,0),
	ZECCA("zecca",0,1,3,0,0),
	TEATRO("teatro",2,2,0,0,0),
	ARCO_DI_TRIONFO("arco di trionfo",2,0,2,0,0),
	TAGLIAPIETRE("tagliapietre",1,0,2,0,0),
	RESIDENZA( "residenza",0,0,2,0,0),
	FALEGNAMERIA("falegnameria",1,2,0,0,0),
	SOSTEGNO_AL_VESCOVO("sostegno al vescovo",2,1,1,0,2), 
	CAMPAGNA_MILITARE("campagna militare",0,0,0,0,2), 
//TODO:devo implementare il controllo che non siano sempre possibili i due tipi di pagamento e 
	//che i pm da pagare non sono quelli su cui si fa il controllo dei costi
	INNALZARE_UNA_STATUA("innalzare una statua",0,2,2,0,0),
	COSTRUIRE_LE_MURA("costruire le mura",0,0,3,0,0), 
	COMBATTERE_LE_ERESIE("combattere le eresie",0,0,0,0,3), 
	INGAGGIARE_RECLUTE("ingaggiare reclute",4,0,0,0,0), 
	OSPITARE_I_MENDICANTI("ospitare i mendicanti",0,3,0,0,0), 
	RIPARARE_LA_CHIESA("riparare la chiesa", 1,1,1,0,0), 
	CONTADINO("contadino",3,0,0,0,0), 
	CONDOTTIERO("condottiero",2,0,0,0,0), 
	ARTIGIANO("artigiano",3,0,0,0,0),
	DAMA("dama",4,0,0,0,0), 
	BADESSA("badessa",3,0,0,0,0), 
	CAVALIERE("cavaliere",2,0,0,0,0), 
	PREDICATORE( "predicatore",2,0,0,0,0), 
	COSTRUTTORE("costruttore",4,0,0,0,0),
	FORESTA("foresta",0,0,0,0,0), 
	BOSCO("bosco",0,0,0,0,0),
	ROCCA("rocca",0,0,0,0,0), 
	BORGO("borgo",0,0,0,0,0), 
	CAVA_DI_GHIAIA("cava di ghiaia",0,0,0,0,0), 
	MONASTERO("monastero",0,0,0,0,0), 
	CITTA("citta",0,0,0,0,0),
	AVAMPOSTO_COMMERCIALE("avamposto commerciale",0,0,0,0,0);



	private String nome;
	private int costoMonete;
	private int costoLegno;
	private int costoPietra;
	private int costoServitori;
	private int costoPM;

	private ECostiCarte(String nome, int monete, int legno, int pietra, int servitori, int PM) {
		this.nome = nome;
		this.costoMonete = monete;
		this.costoLegno = legno;
		this.costoPietra = pietra;
		this.costoServitori = servitori;
		this.costoPM = PM;
	}

	public String getNome() {
		return this.nome;
	}

	public ArrayList<Object[]> getCosti() {
		ArrayList<Object[]> costi = new ArrayList<Object[]>();
		costi.add(new Object[13]);
		for (int i = 0; i < 13; i++) {
			costi.get(0)[i] = new Object();
		}
		costi.get(0)[0] = 1;
		costi.get(0)[5] = this.costoMonete;
		costi.get(0)[6] = this.costoLegno;
		costi.get(0)[7] = this.costoPietra;
		costi.get(0)[8] = this.costoServitori;
		costi.add(new Object[13]);
		for (int i = 0; i < 13; i++) {
			costi.get(1)[i] = new Object();
		}
		costi.get(1)[0] = 2;
		costi.get(1)[10] = this.costoPM;
		return costi;
	}
}
