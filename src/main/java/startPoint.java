import com.example.demo.server.CcsClient;
import com.example.demo.util.Util;
import org.jivesoftware.smack.XMPPException;

import javax.annotation.PostConstruct;

public class startPoint {

    @PostConstruct
	public static void main(String[] args) {

        System.out.print("working");

		CcsClient ccsClient = CcsClient.prepareClient(Util.fcmProjectSenderId, Util.fcmServerKey, true);

		try {
			ccsClient.connect();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
    }
}
