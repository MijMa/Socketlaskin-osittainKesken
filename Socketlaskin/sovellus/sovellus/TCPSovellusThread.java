package sovellus;

import java.io.IOException;

//Luo TCP-yhteyden + vastaanottaa ja lahettaa tietoa serverille

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
	
public class TCPSovellusThread extends Thread {
    private boolean onYhteys = false;
    private int luku;
    ServerSocket serveriSocket; /*Tama muutos on sita varten,
    etta tcp portia voi kayttaa muuallakin, kuin run() metodissa */
    private OutputStream output;
    private InputStream input;
	private ObjectOutputStream oOut;   
	private ObjectInputStream oIn;
    public static ArrayList<Summaaja> summaajat;
    public static ArrayList<Thread> threadit;
    public static ArrayList<ArrayList<Integer>> tulokset;
    
    public void run() {
        serveriSocket = null;
        try {
            System.out.println("[TCP SERVERI]");
            summaajat = new ArrayList<>();
            threadit = new ArrayList<>();
            tulokset = new ArrayList<>();
            
            //Luo sovelluspuolen serverin
            //TCPPortti = 4500
            Socket clientSocket=null;
            serveriSocket = new ServerSocket(TCPSovellus.TCPPORTTI);
            
            System.out.println(" TCP serveri luotu porttiin: "+ TCPSovellus.TCPPORTTI);	
            
            //aloittaa yhteyspyyntöjen kuuntelun
            clientSocket = serveriSocket.accept();
            onYhteys = true;
            System.out.println("[Sovellus] Yhteys paalla");
            
            //Alla olevat streamit toimivat tiedon vastaanottajina ja lahettajina
            output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();
			oOut = new ObjectOutputStream(output);
			oIn = new ObjectInputStream(input);
			
            kasitteleYhteys(clientSocket);
            
			//luo annetun määrän summaajapalvelimia
            for (int i=0; i<luku+1; i++){
    			int portti = TCPSovellus.portit[i];
    			Summaaja s = new Summaaja(portti, i);
    			summaajat.add(s);
    			Thread t = new Thread(s);
    			threadit.add(t);
    			t.start();
    			System.out.println("Summaajanluoja, kierros " + i);
    		}
            kuunteleKaskyja(clientSocket);
				
				
        }
        catch (Exception ex) {
            System.out.print("[TCPSovellusThread] Virhe:" + ex + " " + ex.getStackTrace()[0].getLineNumber());
        }

    }
    /*Tama metodi hoitaa nyt UDP lahetyksen. Tata pitaa kutsua erikseen Thredin ulkopuolelta,
     *  jotta toimii oikein*/
    public void lahetaPortti() throws Exception{
    	int yritysKerta=1;
    	System.out.println("[Sovellus] Yrityskerta: "+yritysKerta);
    	/*Ensimmainen yrityskerta ei tarvitse alkuodotusta, koska serveri pitaisi
    	 * olla taustalla tassa vaiheessa*/
    	new UDPSender("127.0.0.1", "3126", String.valueOf(TCPSovellus.TCPPORTTI));
    	
    	//System.out.println(String.valueOf(onYhteys));
    	/*Odotetaan vahan aikaa, etta yhteydentila on varmasti muuttunut, 
    	 * jos sai yhteyden, niin While silmukkaan ei menna toteuttamaan 4 muuta toistoa*/
    	Thread.sleep(5000);
    	yhteysMuodostettu();
    	while(yritysKerta<5 && yhteysMuodostettu() == false){
    		/*try catch hoitaa loput lahetykset ja vaihtaa While silmukan parametrin ehtoa,
    		 *  jos yhteys muodostuu jollakin yritys kerralla.*/
    		try{
    			yritysKerta++;
    			System.out.println("[Sovellus] Yrityskerta: "+yritysKerta);
    			Thread.sleep(2500);
    			new UDPSender("127.0.0.1", "3126", String.valueOf(TCPSovellus.TCPPORTTI));
    			Thread.sleep(5000);
    		}catch(Exception e){
    		}finally{
    			yhteysMuodostettu();
    			if(yritysKerta==5 && yhteysMuodostettu() == false){
    				/*jos yhteys ei muodostu viidennella kerralla, niin socket suljetaan*/
    				serveriSocket.close();
    			}
    		}
    	}
    }

    public boolean yhteysMuodostettu() {	
        return onYhteys;
    }
    
    //Kasittelee sisaantulevan tiedon ja lahettaa tietoa
    public void kasitteleYhteys(Socket clientSocket) throws IOException{
		System.out.println("[Sovellus] Odotetaan tulevaa pyyntoa. \n");

		try{
			clientSocket.setSoTimeout(5000);
			//Serverin lahettama luku
			luku = oIn.readInt();
			System.out.println("[Sovellus] luku " + luku + " vastaanotettu");
			//Lahetetaan serverille summaamiseen kayettavat portit
			//porttien lukumaara on asken saatu luku
			//portit loytyvat TCPSovelluksesta listana
			for(int i = 0; i<luku; i++){
				if(luku >= 2){
					clientSocket.setSoTimeout(500000);
				}
				int porttinum = TCPSovellus.portit[i];
				oOut.writeInt(porttinum);
				System.out.println("[Sovellus] portti " + porttinum + " lahetetty");
				oOut.flush();
			}				
			
		}
		catch (Exception ex){
			System.out.println("[Sovellus] Virhe: "+ ex + " " + ex.getStackTrace()[0].getLineNumber());
			luku = -1;
			oOut.writeInt(luku);
			oOut.close();
			clientSocket.close();
			return;
		}
	}
    
    //Reagoidaan serverin lahettamiin kaskyihin eli numeroihin 0-3
    public void kuunteleKaskyja(Socket socket){
		try{
	    	int maarays = oIn.readInt();

			switch(maarays) {
				//0 = lopeta palvelu
				case 0:
					System.out.println("[Sovellus] Serveri haluaa lopettaa yhteyden");
					oOut.close();
					for(Thread t: threadit){
						t.interrupt();
						System.exit(0);
					}
				//1 = palauta kokonaissumma
				case 1: 
					System.out.println("[Sovellus] Serveri pyytää kokonaissummaa");
					int kokonaissumma = 0;
					for (Summaaja s:summaajat){
						kokonaissumma += s.annaSumma();	
						}
					System.out.println("[Sovellus] Palautetaan kokonaissumma: " + kokonaissumma);
					oOut.writeInt(kokonaissumma);
					break;
				
				//2 = palauta suurimman summan palvelin
				case 2:
					System.out.println("[Sovellus] Serveri pyytää suurimman summan summaajaa");
					int suurinsumma = summaajat.get(0).annaSumma();
					Summaaja suurinsummaaja = summaajat.get(0);
					int summaajaportti = summaajat.get(0).annaPortti();
					for (Summaaja s:summaajat){
						if (s.annaSumma() > suurinsumma){
							suurinsumma = s.annaSumma();
							suurinsummaaja = s;
							summaajaportti = s.annaPortti();
						}
					}
					
					System.out.println("[Sovellus] Palautetaan suurin summaaja: " + summaajaportti);
					oOut.writeInt(summaajaportti);
					break;
					
				//3 = palauta kokonaismaara
				case 3:
					int kokonaismaara = 0;
					for (Summaaja s:summaajat){
						kokonaismaara += s.annaMaara();
					}
					System.out.println("[Sovellus] Palautetaan kokonaismaara: " + kokonaismaara);
					oOut.writeInt(kokonaismaara);
					break;
				}

		}catch (Exception ex){
				System.out.println("[kuunteleKaskyja] Virhe: "+ ex + " " + ex.getStackTrace()[0].getLineNumber());
			}
    }  
}


