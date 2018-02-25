package sovellus;

//Main-metodi, luo TCP-sovellusthreadin


public class TCPSovellus {
	boolean onYhteys = false;
	public static final int TCPPORTTI = 4500;
	public static int[] portit = {5000, 5001, 5002, 5003, 5004, 5005, 5006, 5007, 5008, 5009, 5010};
	public static int kokonaisluku = 0;
	public static int kokonaismaara = 0;
	
	public static void main(String[] args) throws Exception{
		try {
			TCPSovellusThread saie = new TCPSovellusThread(); 
			saie.start();
			Thread.sleep(2000); //Pieni odotus aika, etta saie Thread ehtii menna odottamaan yhteytta
			saie.lahetaPortti(); /*kaynistetaan Threadista metodi lahetaPortti(), mika hoitaa 
			kaikki 5 kertaa UDP lahetyksen tietyssa ajassa, 
			ellei saa yhteytta nailla yrityksilla. Socket suljetaan*/
			
			/*Tahan ei tarvitse Thread.sleep(), koska lahetaPortti()
			 *  sisaltaa odotus ajat ja saie odottaa yhteytta.
			 *  Kun yhteys on muodostunut TCPSovellusThreadissa, Kaikki saie kutsut yhdistetaan
			 *  (tai tapetaan) kayttamalla saie.join, koska yhteysMuodostettu muuttuu true saikeessa,
			 *  kun yhteys on muodostunut.*/
			if (saie.yhteysMuodostettu()) {
				saie.join();

			}

		}catch (Exception e){
			System.out.println("[Sovellus] Virhe TCP-serverin luonnissa");
		}
	}

}