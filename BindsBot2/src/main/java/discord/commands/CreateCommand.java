package discord.commands;

import java.io.File;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.BindsBot;
import bot.Utils;
import ch.qos.logback.classic.Logger;
import files.BindsFileHelper;

public class CreateCommand extends Command {

	private static final Logger log = (Logger) LoggerFactory.getLogger(CreateCommand.class);
	private BindsFileHelper BFH;
	private String[] specialKeys = { 
		"mouse1", "mouse2", "mouse3", "mouse4", "mouse5", "mwheelup", "mwheeldown",
		"esc", "backspace",
		"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10", "f11", "f12",
		"scrolllock", "numlock", "ins", "home", "pgup", "del", "end", "pgdn",
		"kp_slash", "kp_multiply", "kp_minus", "kp_home", "kp_uparrow", "kp_pgup", "kp_plus", "kp_leftarrow",
		"kp_5", "kp_rightarrow", "kp_end", "kp_downarrow", "kp_pgdn", "kp_enter", "kp_ins", "kp_del", 
		"uparrow", "downarrow", "leftarrow", "rightarrow",
		"enter", "rshift", "rctrl", "ralt", "rwin",
		"alt", "lwin", "ctrl", "shift", "capslock", "tab", "space",
		"semicolon"
	};
	
	public CreateCommand() {
		this.name = "create";
		this.help = "Create a binds file based on your specifications.\n"
				+ "Usage: binds.create --f <filter> --t <type> --k <keys>\n"
				+ "All arguements are optional.\n"
				+ "Filter - S N R and/or D to filter swear words, n-words, retard & fag, and death threats (messages with KYS or die). "
					+ "These filters aren't too advanced, not my fault if they get you banned from somewhere. "
					+ "Example: 'binds.create --f s d' will filter everything, 'binds.create --f d' will only filter death threats.\n"
				+ "Type - (Not implimented) 'SK' for single key cycles through all binds; more types possibly coming.\n"
				+ "Keys - for SK type a single character you want the cycling key attached to. Defaults to 'i'. For special keys surround with '' such as 'space'.\n";
		this.cooldown = 60;
		BFH = new BindsFileHelper();
	}
	
	@Override
	protected void execute(CommandEvent event) {
		if(!BindsBot.isReady()) { return; }
		//Delete message if sent outside command channel
		if(!Utils.commandCheck(event)) {
			event.getMessage().delete().queue();
			return;
		}
		
		String filename = event.getAuthor().getName();
		filename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
		String key = "i";
		String type = "SK";
		String filter = "";
		String filterString = "None";
		
		String msg = event.getMessage().getContentDisplay();
		String[] args = msg.split("--");
		String badArgs = "";
		
		for(String s : args) {
			s = s.trim();
			if(s.equalsIgnoreCase("binds.create")) { continue; }
			//Filter arg
			if(s.startsWith("f ")) {
				filter = s.substring(2);
				filterString = "";
				if(filter.toLowerCase().contains("n")) {
					filterString += " N-words";
				}
				if(filter.toLowerCase().contains("s")) {
					filterString += " Swear words";
				}
				if(filter.toLowerCase().contains("d")) {
					filterString += " Death threats";
				}
				if(filter.toLowerCase().contains("r")) {
					filterString += " Retard related words";
				}
			//Type arg - SK = single key
			} else if(s.startsWith("t ")) {
				if(s.substring(2).equalsIgnoreCase("SK")) {
					type = "SK";
				} else { //TODO other types
					badArgs += "Could not recognize config type. Valid options are 'SK'.\n";
				}
			} else if(s.startsWith("k ")) {
				String keys = s.substring(2);
				if(keys.isEmpty()) {
					badArgs += "Must provide at least 1 key for the key arguement.";
				} else if(type.equalsIgnoreCase("SK")) {
					if(keys.length() == 1) {
						if(keys.matches("[a-zA-Z0-9`\\-=\\[\\]\\\\',./]")) {
							keys = keys.toLowerCase();
							key = keys;
						} else {
							badArgs += "Invalid key for key arguement.\n";
						}
					} else {
						boolean valid = false;
						for(String k : specialKeys) {
							if(s.toLowerCase().contains(k)) {
								key = "'" + k + "'";
								valid = true;
							}
						}
						if(!valid) {
							badArgs += "Invalid key for key arguement.\n";
						}
					}
				} //TODO for other types
			}
		}
		if(!badArgs.isEmpty()) {
			event.reply("You had the following problems with your arguements: \n" + badArgs);
		}
		
		event.reply("Creating binds file with options: ```"
						+ "FileName: " + filename + ".cfg\n"
						+ "Filters applied: " + filterString + "\n"
						+ "Type: " + type + "\n"
						+ "Bound to key(s): " + key + "\n"
						+ "``` Your file will be ready in a few seconds...");

		File f = BFH.createSingleKeyFile(filename, BindsBot.getBindsHelper().getStringBinds(filter), "bb", key);
		log.debug("File created, uploading...");
		event.reply(f, f.getName());
		event.reply("Here is your config!");
		log.debug("Finished file creation command.");
		
	}

}
