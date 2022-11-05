package discord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import bot.Utils;
import ch.qos.logback.classic.Logger;
import model.Bind;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Class to hold and retrieve binds on startup and update via commands or daily
 * This is where the logic for what is an isn't a bind goes
 */
public class BindsHelper {

	private static final Logger log = (Logger) LoggerFactory.getLogger(BindsHelper.class);
			
	private ArrayList<Bind> binds;
	private Long mostRecentBind;
	private ArrayList<String> blacklist;
	private Date lastUpdate;
	
	
	public BindsHelper() {
		mostRecentBind = 0l;
		binds = new ArrayList<Bind>();
		blacklist = new ArrayList<String>();
		updateBlacklist();
	}
	
	/**
	 * Update the most recent bind if the id given is larger
	 * @param id
	 */
	private void updateMostRecent(String id) {
		if(Long.parseLong(id) > mostRecentBind) {
			mostRecentBind = Long.parseLong(id);
		}
	}
	
	private Long getMostRecent() {
		return mostRecentBind;
	}
	
	//Variables for await method in fetchAll
	private boolean firstFetch;
	private boolean endOfHistory;
	private boolean await;
	/**
	 * Retrieve all messages 
	 * @param TC
	 * @return Size of binds on success
	 */
	public int fetchAll(TextChannel TC) {
		firstFetch = true;
		endOfHistory = false;
		await = false;
		long time = 0;
		
		while(!endOfHistory) {
			if(firstFetch && !await) { //Start at beginning
				await = true;
				TC.getHistoryFromBeginning(100).queue(history -> 
				{
					log.debug("Entered first fetch await...");
					for(Message m : history.getRetrievedHistory()) {
						for(Bind b : parseMessageToBind(m)) {
							binds.add(b);
						}
					}
					log.debug("Exiting first fetch await. Binds size: " + binds.size());
					await = false;
					firstFetch = false;
				});
			}
			
			if(System.currentTimeMillis() > (time + 1000)) {
				time = System.currentTimeMillis();
				log.debug("Await: " + await + " firstFetch: " + firstFetch);
			}
			
			if(!await && !firstFetch) {
				await = true;
				String id = getMostRecent().toString();
				TC.getHistoryAfter(id, 100).queue(history ->
				{
					log.debug("Entered a secondary fetch await...");
					log.debug("Retrieving messages after: " + id);
					if(history.isEmpty()) {
						log.debug("No more history, setting endOfHistory true.");
						endOfHistory = true;
						await = false;
					} else {
						for(Message m : history.getRetrievedHistory()) {
							updateMostRecent(m.getId());
							for(Bind b : parseMessageToBind(m)) {
								binds.add(b);
							}
						}
						log.debug("Exiting secondary fetch await. Binds size: " + binds.size());
						await = false;
					}
				});
			}
		}
		lastUpdate = new Date(System.currentTimeMillis());
		return binds.size();
	}
	
	/**
	 * Update binds list based on latest message in the list
	 * @return
	 */
	public int updateBinds(TextChannel TC) {
		firstFetch = false;
		endOfHistory = false;
		await = false;
		long time = 0;
		
		while(!endOfHistory) {			
			if(System.currentTimeMillis() > (time + 1000)) {
				time = System.currentTimeMillis();
				log.debug("Await: " + await + " firstFetch: " + firstFetch);
			}
			
			if(!await && !firstFetch) {
				await = true;
				String id = getMostRecent().toString();
				TC.getHistoryAfter(id, 100).queue(history ->
				{
					log.debug("Entered a secondary fetch await...");
					log.debug("Retrieving messages after: " + id);
					if(history.isEmpty()) {
						log.debug("No more history, setting endOfHistory true.");
						endOfHistory = true;
						await = false;
					} else {
						for(Message m : history.getRetrievedHistory()) {
							for(Bind b : parseMessageToBind(m)) {
								binds.add(b);
							}
						}
						log.debug("Exiting secondary fetch await. Binds size: " + binds.size());
						await = false;
					}
				});
			}
		}
		lastUpdate = new Date(System.currentTimeMillis());
		return binds.size();
	}
	
	/**
	 * Determine if and how a message should be turned into a bind(s)
	 * @param msg
	 * @return Empty if message can't be created into a bind, otherwise the list of binds we think needed to be made
	 */
	public ArrayList<Bind> parseMessageToBind(Message msg) {
		log.debug("Converting message into bind object. "
				+ "\nID: " + msg.getId()
				+ "\nTime: " + msg.getTimeCreated()
				+ "\nType: " + msg.getType());
		ArrayList<Bind> tempBinds = new ArrayList<Bind>();
		
		//Only take good types
		if(msg.getType() != MessageType.DEFAULT) {
			log.debug("Bad type!");
			return tempBinds;
		}
		//No blacklisted messages
		if(blacklist.contains(msg.getId())) {
			log.debug("Blacklisted message!");
			return tempBinds;
		}
		
		String[] split = msg.getContentRaw().split("\n");
		if(split.length == 1) { //Single line, check for valid length
			if(split[0].length() <= 127 && !split[0].startsWith("//")) {
				String b = split[0];
				if(b.startsWith("DEAD")) { //Attempt to correct formatting with *DEAD* -> DEAD
					String b2 = "*DEAD*" + b.substring("DEAD".length());
					if(b2.length() <= 127) {
						b = b2;
					}
				}
				tempBinds.add(new Bind(new String[] {b}, msg.getTimeCreated(), msg.getId()));
			}
		} else {
			//Multiple lines, first try to fit into one bind with spaces, no spaces, and finally split them if the previous both failed
			//TODO smart logic to determine if binds need to be on a wait timer?
			String b = "";
			String b1 = "";
			for(String s : split) { 
				b += s + " "; 
				b1 += s; //90% Chance we can't do combination so only do the loop once with 2 vars instead of twice on same var
			}
			b.trim();
			b1.trim();
			if(b.length() <= 127 && !b.startsWith("//")) { //All lines with spaces worked
				//Try DEAD fix
				String b2 = b.replaceAll("DEAD", "*DEAD*");
				if(b2.length() <= 127) {
					tempBinds.add(new Bind(new String[] {b2}, msg.getTimeCreated(), msg.getId()));
				} else {
					tempBinds.add(new Bind(new String[] {b}, msg.getTimeCreated(), msg.getId()));
				}
			} else if(b1.length() <= 127 && !b1.startsWith("//")) { //All lines w/o spaces worked
					//Try DEAD fix, unlikely due to spaces being too much but w/e
					String b2 = b1.replaceAll("DEAD", "*DEAD*");
					if(b2.length() <= 127) {
						tempBinds.add(new Bind(new String[] {b2}, msg.getTimeCreated(), msg.getId()));
					} else {
						tempBinds.add(new Bind(new String[] {b1}, msg.getTimeCreated(), msg.getId()));
					}
			} else { //put every line into its own bind 
				//TODO smart logic here
				for(String s : split) {
					if(s.length() <= 127 && !s.startsWith("//")) {
						//Try DEAD fix
						String s2 = s.replaceAll("DEAD", "*DEAD*");
						if(s2.length() <= 127) {
							tempBinds.add(new Bind(new String[] {s2}, msg.getTimeCreated(), msg.getId()));
						} else {
							tempBinds.add(new Bind(new String[] {s}, msg.getTimeCreated(), msg.getId()));
						}
					}
				}
			}
		}
		return tempBinds;
	}
	
	/**
	 * Update the internal blacklist from file
	 * @return -1 for error, 1 if updated successfully
	 */
	public int updateBlacklist() {
		log.debug("Updating blacklist array.");
		File file = new File("Message blacklist.json");
		if(!file.exists()) {
			JsonObject blObj = new JsonObject();
			blObj.add("Blacklist", new JsonArray());
			try {
				log.debug("Attempting to create empty blacklist file.");
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String s = gson.toJson(blObj);
				FileWriter writer = new FileWriter(file);
				writer.write(s);
				writer.flush();
				writer.close();
				log.debug("Succesfully created empty blacklist file.");
			} catch (Exception e) {
				log.error("Error occured writing blacklist file.\n" + e.toString());
				log.debug(e.getMessage(), e);
				return -1;
			}
		}
		//Clear the blacklist then read from file
		blacklist.clear();
		try {
			FileReader reader = new FileReader(file);
			Gson gson = new Gson();
			JsonArray bl = gson.fromJson(reader, JsonObject.class).getAsJsonArray("Blacklist");
			for(JsonElement el : bl) {
				blacklist.add(el.getAsString());
			}
			log.debug("Updated blacklist array.");
			return 1;
		} catch (Exception e) {
			log.error("Failed to read blacklist file.\n" + e.toString());
			log.debug(e.getMessage(), e);
			return -1;
		}		
	}	

	public ArrayList<String> getStringBinds(String filter) {
		ArrayList<String> stringBinds = new ArrayList<String>();
		for(Bind b : binds) {
			if(Utils.filterBind(b, filter)) {
				for(String s : b.getBind()) {
					stringBinds.add(s);
				}
			}
		}
		return stringBinds;
	}
	
	/**
	 * @return A random bind as a string
	 */
	public String getRandomBind() {
		Random rand = new Random();
		Bind b = binds.get(rand.nextInt(binds.size()));
		String out = "";
		for(String s : b.getBind()) {
			out += s;
		}
		return out;
	}
	
	public ArrayList<Bind> getBinds() {
		return binds;
	}
	
	public int getBindsSize() {
		return binds.size();
	}

	public void setBinds(ArrayList<Bind> binds) {
		this.binds = binds;
	}

	public ArrayList<String> getBlacklist() {
		return blacklist;
	}

	public void setBlacklist(ArrayList<String> blacklist) {
		this.blacklist = blacklist;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	
}
