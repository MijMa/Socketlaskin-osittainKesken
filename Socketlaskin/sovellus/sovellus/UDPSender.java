package sovellus;
//Hoitaa TCP-portin l‰hetyksen (toimiva)
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPSender{
	private String ip;
	private String portti;
	private String viesti;
	
	public UDPSender(String ip, String portti, String viesti) throws Exception{
		if (ip==null || portti==null || viesti==null) {
			System.out.println("[Sovellus] V‰‰r‰t parametrit! (IP, portti, viesti");
			System.exit(0);
		}
		this.ip = ip;
		this.portti = portti;
		this.viesti = viesti;
		
		
		InetAddress vastIP = InetAddress.getByName(ip);
		int vastPortti = Integer.parseInt(portti);
		DatagramSocket soketti = new DatagramSocket();
		byte[] data = viesti.getBytes();
		DatagramPacket paketti = new DatagramPacket(data, data.length,
				vastIP, vastPortti);
		soketti.send(paketti);
		System.out.println("[Sovellus] UDP-paketti l‰hetetty");
	}
}
