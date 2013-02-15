package si.sensorlab.crime.srcgenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import si.sensorlab.crime.config.Module;
import si.sensorlab.crime.config.Stack;

public class CodeSrc {
	protected List<Stack> stackV;		

	public CodeSrc(ArrayList<Stack> stackList){
		stackV = stackList;		
	}

	public void configureStack(String fileNm) {
		try {
			FileReader cFReader = new FileReader(fileNm + ".c");
			BufferedReader cBReader = new BufferedReader(cFReader);
			String lnStr;

			String outCFNm = fileNm.replaceFirst("_template", "");
			outCFNm = outCFNm + ".c";
			FileWriter cFWriter = null;
			try {
				cFWriter = new FileWriter(outCFNm);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			BufferedWriter cBWriter = new BufferedWriter(cFWriter);			
			
			try {
				String chSeq = "@defStack";
				while ((lnStr = cBReader.readLine()) != null) {
					cBWriter.write(lnStr);
					cBWriter.newLine();					
					if (lnStr.contains(chSeq)) {
						writeStackConfig(cBWriter);
					}
				}
				cBWriter.close();
				cFWriter.close();
				cBReader.close();
				cFReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
		FileReader hFReader = new FileReader(fileNm + ".h");
		
		BufferedReader hBReader = new BufferedReader(hFReader);
		String lnStr;
		
		String outHdrFNm = fileNm.replaceFirst("_template", "");
		outHdrFNm = outHdrFNm + ".h";
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter(outHdrFNm);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		BufferedWriter hBWriter = new BufferedWriter(fWriter);			
		try {
			while ((lnStr = hBReader.readLine()) != null) {
				hBWriter.write(lnStr);
				hBWriter.newLine();					 
				if (lnStr.contains("//defStackNo")) {					
					//hBWriter.write("static uint8_t STACKNO = " + String.valueOf(stackV.size()) + ";");
					hBWriter.write("#define STACKNO " + String.valueOf(stackV.size()));
					hBWriter.newLine();
				}				
			}
			hBWriter.close();
			fWriter.close();
			hBReader.close();
			hFReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	public void writeStackConfig(BufferedWriter bWriter) {
		String outStr = "";
		boolean triggerFlag = false;
		try {
			for (int stackIdx = 0; stackIdx < stackV.size(); stackIdx++) {
				Stack stack = stackV.get(stackIdx);
				writeInitialization(stackIdx, stack.getModuleNo(), 
						stack.getStackNm().toUpperCase() + "_ATTRIBUTES", 
						stack.getChannelNo(), bWriter);
				for (int modIdx = 0; modIdx < stack.getModuleNo(); modIdx++) {
					Module module = stack.getModule(modIdx);
					String ids = "	amodule_STACKIDX[_MODIDX].stack_id = _STACKIDX; \n" +
								 "	amodule_STACKIDX[_MODIDX].module_id = _MODIDX; \n";
					ids = ids.replaceAll("_STACKIDX", String.valueOf(stackIdx));
					ids = ids.replaceAll("_MODIDX", String.valueOf(modIdx));					
					bWriter.write(ids);		
					if (module.getParentStackId() >= 0 && module.getParentModuleId() >= 0) {
						outStr = "	amodule_STACKIDX[_MODIDX].parent = &amodule_PSTACKIDX[_PMODIDX];";
						outStr = outStr.replaceAll("_STACKIDX", String.valueOf(stackIdx));
						outStr = outStr.replaceAll("_MODIDX", String.valueOf(modIdx));	
						outStr = outStr.replaceAll("_PSTACKIDX", String.valueOf(module.getParentStackId()));
						outStr = outStr.replaceAll("_PMODIDX", String.valueOf(module.getParentModuleId()));							
					} else {
						outStr = "	amodule_STACKIDX[_MODIDX].parent = NULL;";
						outStr = outStr.replaceAll("_STACKIDX", String.valueOf(stackIdx));
						outStr = outStr.replaceAll("_MODIDX", String.valueOf(modIdx));	
					}
					bWriter.write(outStr);
					bWriter.newLine();
										
					for (int paramIdx = 0; paramIdx < module.getParameterNo(); paramIdx++) {
						if (!module.getParameterVal(paramIdx).isEmpty()) {
							String parameterKey = module.getParameterKey(paramIdx);		
							String parameterVal = module.getParameterVal(paramIdx);
							if (parameterKey.equalsIgnoreCase("sender") ||
									parameterKey.equalsIgnoreCase("receiver") ||
									parameterKey.equalsIgnoreCase("esender") ||
									parameterKey.equalsIgnoreCase("ereceiver")) {
								String left = parameterVal.substring(0, parameterVal.indexOf('.'));
								String right = parameterVal.substring(parameterVal.indexOf('.') + 1);
								outStr = "	addr.u8[0] = "  + left + "; addr.u8[1] = " + right + "; \n"
										+ "	set_node_addr(" + String.valueOf(stackIdx) + ", OUT, " 
										+ parameterKey.toUpperCase() + ", &addr);";								
							} else if (parameterKey.equalsIgnoreCase("time_trigger_flg") ||
									parameterKey.equalsIgnoreCase("trigger_no") ||
									parameterKey.equalsIgnoreCase("trigger_th") ||
									parameterKey.equalsIgnoreCase("trigger_interval")) {
								outStr = "	" 
										+ "amodule" + stackIdx + "[" + modIdx + "]."
										+ parameterKey 							
										+ " = "
										+ parameterVal
										+ ";";
								if (parameterKey.equalsIgnoreCase("time_trigger_flg") && 
										(parameterVal.equalsIgnoreCase("1"))) {	triggerFlag = true; }
							} else {
								outStr = "	" + "stack[" + stackIdx + "].pip->"
									+ parameterKey	+ " = " + parameterVal + ";";
							}
							bWriter.write(outStr);
							bWriter.newLine();
						}
					}
					if (triggerFlag) {
						outStr = "	stack[" + stackIdx + "].time_trigger_flg = 1; \n" +
								"	amodule" + stackIdx + "[" + modIdx + "].trigger_init_flg = 0;";										
						bWriter.write(outStr);
						bWriter.newLine();
						triggerFlag = false;
					}
					for (int primIdx = 0; primIdx < module.getPrimitiveNo(); primIdx++) {	
						if (!module.getPrimitiveVal(primIdx).isEmpty()) {
							outStr = "	" 
									+ "amodule" + stackIdx + "[" + modIdx + "]."
									+ module.getPrimitiveKey(primIdx)											
									+ " = "
									+ module.getPrimitiveVal(primIdx)
									+ ";";
							bWriter.write(outStr);
							bWriter.newLine();
						}
					}																					
					bWriter.newLine();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writeInitialization(int stackIdx, int modno, 
			String attrNm, int channelNo, BufferedWriter bWriter){
		String stackTemplate = 
			"	struct pipe *pi_IDX; \n" +
			"	pi_IDX = (struct pipe*) calloc(1, sizeof(struct pipe)); \n" +
			"	struct channel *ch_IDX; \n" +
			"	ch_IDX = (struct channel*) calloc(1, sizeof(struct channel)); \n" +
			"	stack[_IDX].pip = pi_IDX; \n" +
			"	stack[_IDX].pip->channel = ch_IDX; \n" +
			"	stack[_IDX].modno = _MODNO; \n" +
			"	struct stackmodule_i *amodule_IDX; \n" +
			"	amodule_IDX = (struct stackmodule_i*) calloc( \n" +
			"		stack[_IDX].modno, sizeof(struct stackmodule_i)); \n" +
			"	addr.u8[0] = 0; addr.u8[1] = 0; \n" +	
			"	set_node_addr(0, OUT, SENDER, &addr); \n\n" +
			"	static struct packetbuf_attrlist c_attributes_IDX[] = \n" +
			"	{ \n" +
			"	_ATTRNM PACKETBUF_ATTR_LAST \n" +
			"	}; \n\n" +
			"	stack[_IDX].pip->channel_no = _CHNO; \n" +
			"	stack[_IDX].pip->attrlist = c_attributes_IDX; \n" +
			"	stack[_IDX].pip->channel->channelno = stack[_IDX].pip->channel_no; \n" +
			"	stack[_IDX].pip->channel->attrlist = stack[_IDX].pip->attrlist; \n" +
			"	stack[_IDX].amodule = amodule_IDX; \n";
		
		stackTemplate = stackTemplate.replaceAll("_IDX", String.valueOf(stackIdx));
		stackTemplate = stackTemplate.replaceAll("_MODNO", String.valueOf(modno));
		stackTemplate = stackTemplate.replaceAll("_ATTRNM", attrNm);
		stackTemplate = stackTemplate.replaceAll("_CHNO", String.valueOf(channelNo));		
		
		try {			
			bWriter.write(stackTemplate);
			bWriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
