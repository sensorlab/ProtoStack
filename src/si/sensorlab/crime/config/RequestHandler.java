package si.sensorlab.crime.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import si.sensorlab.crime.exceptions.CRimeException;
import si.sensorlab.crime.rdf.TripleStore;
import si.sensorlab.crime.srcgenerator.CodeSrc;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class RequestHandler extends AbstractHandler {
	
	//private Configuration configuration;
	private Settings settings;
	private String outSrcPath;
	private TripleStore trStore;

	public RequestHandler(String outSrcPath, TripleStore trStore){
		//this.configuration = configuration;
		this.outSrcPath = outSrcPath;
		this.trStore = trStore;
	}
	
	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		if (request.getMethod().equalsIgnoreCase("post")) {
			try {
				doPost(request, response);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} else if (request.getMethod().equalsIgnoreCase("get")) {
			doGet(request, response);
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
						
		PrintWriter out = resp.getWriter();
		out.print(trStore.getCrimeLayersLang().toString());
		out.close();
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException, InterruptedException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					req.getInputStream()));
			String queryStr = reader.readLine();							
			System.out.println(queryStr);
			JSONObject recJSon = new JSONObject(queryStr);
			String methodNm = recJSon.getString("method");
			PrintWriter out = resp.getWriter();
			if (methodNm.equalsIgnoreCase("listWirings")){
				//listWirings();			
			} else if (methodNm.equalsIgnoreCase("saveWiring")){
				try {					
					FileWriter hFWriter = new FileWriter("C:/Users/carolina/Documents/Work/Software/C-RIME Server/CRIMESrv/stacks/saved.json");
					hFWriter.write(recJSon.toString());
					hFWriter.close();						
					settings = new Settings(recJSon, trStore);
					CodeSrc outSrc = new CodeSrc((ArrayList<Stack>) settings.getStackList());
					outSrc.configureStack(outSrcPath + "stack_template");
					//program the node	
					compileAndProgram("WINDOWS", out);
				} catch (CRimeException e) {
					out.print(e.getMessage());
					out.close();
				};
			} 			
			out.print("{\"method\":\"" + methodNm + "\"}");
			out.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void compileAndProgram(String HostOSNm, PrintWriter out) throws CRimeException {
		try {	
			if (HostOSNm.matches("WINDOWS")) {
				List<String> command = new ArrayList<String>();
				command.add("cs-make");
				command.add("example-crime.load");
				ProcessBuilder builder = new ProcessBuilder(command);
				Map<String, String> environ = builder.environment();
				environ.put("PATH", "C:/VesnaIDE/codesourcery/bin;/windows;/windows/system32;/winnt;C:/VesnaIDE/cygwin/bin;C:/VesnaIDE/openocd-x64-0.5.0/bin;");				   
				builder.directory(
						new File("C:\\Users\\carolina\\Documents\\Work\\Software\\Contiki-2.5\\examples\\rime_cmp"));

				Process proc = builder.start();
				StreamGobbler errorGobbler = new 
		                StreamGobbler(proc.getErrorStream(), "-");     
				StreamGobbler outputGobbler = new 
		                StreamGobbler(proc.getInputStream(), "-");
				 errorGobbler.start();
		         outputGobbler.start();
		         int exitVal = proc.waitFor();
		         System.out.println("ExitValue: " + exitVal);        				
			} else { 
				new CRimeException("Unsupported host operating systemm!");
			}
		} catch (IOException e) {
			out.print(e.getMessage());
			out.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
}

class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    
    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println(type + "> " + line);    
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
}
