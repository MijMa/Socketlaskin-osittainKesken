package sovellus;
import java.io.IOException;
//vastaanottaa serverin lahettamia lukuja ja tallentaa ne
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Summaaja implements Runnable{
	private int portti;
	ServerSocket serveriSocket;
	private int id;
	private ArrayList<Integer> tulos;
	
	//Konstruktori
	public Summaaja(int portti, int id){
		this.portti = portti;
		this.id = id;
		serveriSocket = null;
		tulos = new ArrayList<>();
    }
	
	public void run(){
		try {
            //Luo sovelluspuolen serverin
            Socket clientSocket=null;
            serveriSocket = new ServerSocket(portti);
            
            System.out.println("[Summaaja " +id+ "] TCP serveri luotu porttiin: "+ portti);	          
            //aloittaa yhteyspyyntajen kuuntelun
            clientSocket = serveriSocket.accept();
            
            vastaanota(clientSocket);
        } catch (Exception ex) {
            System.out.print("[Summaaja] Error...");
        }
	}
		
        
	//Sama periaate kuin kasitteleYhteys():ssa;
	public void vastaanota(Socket clientSocket) throws Exception{

		try{
			clientSocket.setSoTimeout(60000);
			//Alla olevat streamit toimivat tiedon vastaanottajina ja lahettajina
			OutputStream output = clientSocket.getOutputStream();
			InputStream input = clientSocket.getInputStream();
			ObjectOutputStream oOut = new ObjectOutputStream(output);
			ObjectInputStream oIn = new ObjectInputStream(input);
			while (true){
				if (oIn.available() != 0){
					int luku = oIn.readInt();
					
					//Jos serveri lahettaa luvun 0, suljetaan
					if(luku == 0){
						System.out.println("[Summaaja " +id+"] Suljetaan");
						oOut.close();
					}
					oOut.flush();
					System.out.println("[Summaaja " +id+"] luku " + luku + " vastaanotettu");
					
					//Tallennetaan saatu luku listaan
					tulos.add(new Integer(luku));
					System.out.println("[Summaaja " +id+"] Luku tallennettu");
				}
			}
			
		}
		catch (Exception ex){
			System.out.println("[Summaaja] Virhe: "+ ex + " " + ex.getStackTrace()[0].getLineNumber());
			System.out.println("[Sovellus] Virhe: "+ ex + " " + ex.getStackTrace()[0].getLineNumber());
			clientSocket.close();
			return;
		}
	}
	//Palauttaa listaan tallennettujen lukujen summan
	public int annaSumma(){
		int summa = 0;
		for(int i : tulos){
			summa += i;
		}
		return summa;
	}
	//Palauttaa listaan tallennettujen lukujen kokonaismaaran
	public int annaMaara(){
		int maara = 0;
		for(int i : tulos){
			maara += 1;
		}
		return maara;
	}
	//Palauttaa kaytossa olevan portin
	public int annaPortti(){
		return this.portti;
	}
}
