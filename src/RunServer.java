import java.io.File;
import org.eclipse.jetty.server.Server;

import si.sensorlab.crime.config.RequestHandler;
import si.sensorlab.crime.rdf.RuleParser;
import si.sensorlab.crime.rdf.TripleStore;

public class RunServer {
	public static void main(String[] args) {
		 try {					 			
			
			//c file turtle rules  to triple store
			String cSrcPath = "C:/Users/carolina/Documents/Work/Software/Contiki-2.5/core/net/rime/crime/";
			File dir = new File(cSrcPath);
			String ontologyFNm = "C:/Users/carolina/Documents/Work/Software/C-RIME Server/crime.owl";
			TripleStore trStore = new TripleStore(ontologyFNm);
			RuleParser.parsePrefixes(dir, trStore);
			RuleParser.parseTriples(dir, trStore);															
									
			String outSrcPath = "C:/Users/carolina/Documents/Work/Software/Contiki-2.5/core/net/rime/crime/";
			Server server = new Server(8080);
		    server.setHandler(new RequestHandler(outSrcPath, trStore));
		 
		     server.start();
		     server.join();		     					
										     
		 } catch (Exception e){
			 e.printStackTrace();
		 }
	}
}


//Very good tutorial
//http://answers.oreilly.com/topic/447-how-to-use-the-sesame-java-api-to-power-a-web-or-client-server-application/
