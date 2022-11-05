package bot;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandEvent;

import ch.qos.logback.classic.Logger;
import files.ConfigHelper;
import model.Bind;
import net.dv8tion.jda.api.entities.TextChannel;

public class Utils {

	private static final Logger log = (Logger) LoggerFactory.getLogger(Utils.class);
			
	/**
	 * Checks if command is being run in the command channel
	 * @param event
	 * @return
	 */
	public static boolean commandCheck(CommandEvent event) {
		String cID = event.getChannel().getId();
		if(cID.equals(ConfigHelper.getOptionFromFile("COMMAND_CHANNEL"))) {
			return true;
		}
		return false; //Catch is always false
	}
	
	/**
	 * Convince method for commands
	 * @param event
	 * @return
	 */
	public static TextChannel getBindsChannel(CommandEvent event) {
		return event.getGuild().getTextChannelById(ConfigHelper.getOptionFromFile("BIND_CHANNEL"));
	}
	
	
	private static String[] nCustomID = {"653837905491525632"};
	private static String[] nFilterWords = {"nigger", "nigs", "n*gger", "n*****", "negros", "negro", "kike"};
	private static String[] rCustomID = {"701727054302937181", "646046067866992664" };
	private static String[] rFilterWords = {"retard", "tards", "fag", "faggot"};
	private static String[] sFilterWords = {
			"fuck", "shit", "cock", "pussy", "ass", "dick", "penis", "vagina"
	};
	private static String[] dFilterWords = {"kys", "die", "kill you", "kill yourself", "end yourself", "end it tonight" };
	/**
	 * Determine if a bind meets a criteria of filter.
	 * Note: Filtering happens in the BindsHelper
	 * @param bind
	 * @param filter Filters divided by spaces. Valid filters are 'N' 'S' 'D' 'R'
	 * @return false if blocked by the filter (IE contains a filtered word)
	 */
	public static boolean filterBind(Bind bind, String filter) {
		boolean pass = true;
		if(filter.isEmpty()) { return pass; }
		
		
		String [] filters = filter.split(" ");
		boolean nFilter = false; //Nwords
		boolean sFilter = false; //Swear words
		boolean rFilter = false;
		boolean dFilter = false; //death threats 
		for(String s : filters) {
			if(s.equalsIgnoreCase("n")) { nFilter = true; }
			if(s.equalsIgnoreCase("s")) {
				nFilter = true;
				sFilter = true;
			}
			if(s.equalsIgnoreCase("d")) { dFilter = true; }
			if(s.equalsIgnoreCase("r")) { rFilter = true; }
		}
		
		if(sFilter || nFilter) {
			for(String s : nCustomID) {
				if(bind.getMessageID().equals(s)) {
					pass = false;
					break;
				}
			}
			if(pass) {
				for(String s : nFilterWords) {
					for(String b : bind.getBind()) {
						if(b.toLowerCase().contains(s)) {
							pass = false;
							break;
						}
					}
					if(!pass) { break; }
				}
			}
		}
		if(sFilter || rFilter) {
			for(String s : rCustomID) {
				if(bind.getMessageID().equals(s)) {
					pass = false;
					break;
				}
			}
			if(pass) {
				for(String s : rFilterWords) {
					for(String b : bind.getBind()) {
						if(b.toLowerCase().contains(s)) {
							pass = false;
							break;
						}
					}
					if(!pass) { break; }
				}
			}
		}
		if(sFilter) {
			for(String s : sFilterWords) {
				for(String b : bind.getBind()) {
					if(b.toLowerCase().contains(s)) {
						pass = false;
						break;
					}
				}
				if(!pass) { break; }
			}
		}
		if(dFilter) {
			for(String s : dFilterWords) {
				for(String b : bind.getBind()) {
					if(b.toLowerCase().contains(s)) {
						pass = false;
						break;
					}
				}
				if(!pass) { break; }
			}
		}
		return pass;
	}
}
