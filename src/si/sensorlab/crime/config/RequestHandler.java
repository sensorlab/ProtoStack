package si.sensorlab.crime.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

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
			doPost(request, response);			
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
			throws ServletException, IOException {
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

}
