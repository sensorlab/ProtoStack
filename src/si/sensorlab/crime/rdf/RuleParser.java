package si.sensorlab.crime.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import si.sensorlab.crime.exceptions.CRimeException;

public class RuleParser {
	private static List<Prefix> prefixes;

	public RuleParser(String path, TripleStore trStore) {
		prefixes = new ArrayList<Prefix>();		
		File dir = new File(path);
				
		parsePrefixes(dir, trStore);
		parseTriples(dir, trStore);
	}
	
	public static void parsePrefixes (File dir, TripleStore trStore) {
		prefixes = new ArrayList<Prefix>();		
		String FileNm = dir + "/stack.h";
		trStore.getConnection();
		try {
			FileReader hFReader = new FileReader(FileNm);
			BufferedReader hBReader = new BufferedReader(hFReader);	
			String lnStr;		
			while ((lnStr = hBReader.readLine()) != null) {
				if (lnStr.startsWith("//@prefix")){
					lnStr = lnStr.substring(10);
					prefixes.add(new Prefix(lnStr));
				} else if (lnStr.startsWith("//turtle") && lnStr.endsWith(" .")){
					lnStr = lnStr.substring(9, lnStr.length() - 2);
					trStore.writeTriple(lnStr);
				}
			}
			trStore.closeConnection();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void parseTriples (File dir, TripleStore trStore) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith("c_");
			}
		};
		String[] children = dir.list(filter);
		trStore.getConnection();
		
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				String fileNm = children[i];
				if (fileNm.endsWith(".h")) {
					try {
						FileReader hFReader = new FileReader(dir + "/" + fileNm);
						BufferedReader hBReader = new BufferedReader(hFReader);					
						String lnStr;					
						while ((lnStr = hBReader.readLine()) != null) {
							if (lnStr.startsWith("//turtle") && lnStr.endsWith(" .")){
								lnStr = lnStr.substring(9, lnStr.length() - 2);
								trStore.writeTriple(lnStr);
							}
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
			}
		} else {
			new CRimeException(
					"No c_rime files in the directory: " + dir.getParent() + " ! Modules cannot be generated.");
		}
		trStore.closeConnection();
	}
	public static String getPrefix(String key){
		for (int i = 0; i < prefixes.size(); i++) {
			if (prefixes.get(i).getKey().equals(key)){
				return prefixes.get(i).getVal();
			}
		}
		return "";
	}
}
