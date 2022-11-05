package files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import bot.Utils;
import ch.qos.logback.classic.Logger;

//TODO Catch files bigger than one mebibyte (1.04858 megabytes) which is the current .cfg limit for tf2
public class BindsFileHelper {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(BindsFileHelper.class);
	
	//private HashMap<File, Date> filesCache;
	private static String path = "configs/";
	
	//public boolean hasCache() {
	//	return !filesCache.isEmpty();
	//}
	
	public BindsFileHelper() {
		//filesCache = new HashMap<File, Date>();
	}
	
	/**
	 * @param fileName Appends the .cfg when creating, do not include when passing
	 * @param binds Must be length of >2
	 * @param alias 
	 * @param key Key to bind to
	 * @return
	 */
	public File createSingleKeyFile(String fileName, ArrayList<String> binds, String alias, String key) {
		
		fileName = path + fileName;
		fileName += ".cfg";
		key.replaceAll("'", ""); //Special keys are passed in with '', make sure to remove them
		
		String top = "";
		String bottom = "";
		int bind = 1;
		//Concat startStrTop + bind + "\" \"say " + String + "\""; For the top strings
		String startStrTop = "alias \"" + alias; //Beginning string for first half of file
		//Concat startStrBottom + bind + middleStrBottom + bind + endStrBottom + "\"" for bottom strings
		String startStrBottom = "alias \"" + alias + "_roll_"; //Beginning string for bottom half of file
		String middleStrBottom = "\" \"alias " + alias + "_result " + alias; //Middle string for bottom half
		String endStrBottom = ";alias " + alias + "_cycle " + alias + "_roll_"; //Last string for bottom half
		
		//Put the binds into the halves of the cfg blocks
		for(String s : binds) {
			top += startStrTop + bind + "\" \"say " + s + "\"\n";
			if(bind < binds.size()) {
				bottom += startStrBottom + bind + middleStrBottom + bind + endStrBottom + (bind+1) + "\"\n";
				bind++;
			} else {
				//Last one we want to circle back
				bottom += startStrBottom + bind + middleStrBottom + bind + endStrBottom + "1\"\n";
			}
		}
		
		//Add the cycling on the bottom
		bottom += "\n"
				+ "alias " + alias + "_cycle " + alias + "_roll_2\n"
				+ "\n"
				+ "alias " + alias + "_result " + alias + "1\n"
				+ "\n"
				+ "bind " + key + " \"" + alias + "_result; " + alias + "_cycle\"";
		
		try {
			FileOutputStream FOS = new FileOutputStream(fileName);
			Writer writer = new OutputStreamWriter(FOS, StandardCharsets.UTF_8);
			writer.write(top + bottom);
			writer.flush();
			writer.close();
			FOS.close();
			binds.clear();
			return new File(fileName);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to create a single key file.");
			log.error(e.getMessage());
			return null;
		}
	}

}
