package bot;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

import ch.qos.logback.classic.Logger;
import discord.BindsHelper;
import discord.commands.*;
import discord.commands.owner.*;
import discord.events.*;
import files.ConfigHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BindsBot {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(BindsBot.class);
	
	private static JDA jda;
	
	private static BindsHelper BindsHelper;
	public static boolean ready;

	public static void main(String[] args) {
		ready = false;
		
		//Setup config
		ConfigHelper.buildEmptyConfig();
		ConfigHelper.setOptionToFile("DISCORD_TOKEN", "", false);
		ConfigHelper.setOptionToFile("DISCORD_PREFIX", "binds.", false);
		ConfigHelper.setOptionToFile("DISCORD_OWNER", "", false);
		ConfigHelper.setOptionToFile("BIND_CHANNEL", "", false);
		ConfigHelper.setOptionToFile("COMMAND_CHANNEL", "", false);
		
		BindsHelper = new BindsHelper();
		
		//Setup commands
		CommandClientBuilder commandBuilder = new CommandClientBuilder();
		commandBuilder.setPrefix(ConfigHelper.getOptionFromFile("DISCORD_PREFIX"));
		commandBuilder.setOwnerId(ConfigHelper.getOptionFromFile("DISCORD_OWNER"));
		//commandBuilder.addCommand(new DebugCommand());
		commandBuilder.addCommand(new UpdateCommand());
		commandBuilder.addCommand(new CreateCommand());
		commandBuilder.addCommand(new RandomBindCommand());
		CommandClient commandClient = commandBuilder.build();
		
		//Launch discord bot
		JDABuilder builder = JDABuilder.createDefault(ConfigHelper.getOptionFromFile("DISCORD_TOKEN"));
		builder.enableIntents(GatewayIntent.GUILD_MESSAGES);
		builder.addEventListeners(commandClient);
		builder.addEventListeners(new BotReadyEvent());
		
		try {
			jda = builder.build();
		} catch (Exception e) {
			log.error("Error logging in.");
			e.printStackTrace();
		} 

	}
	
	public static BindsHelper getBindsHelper() {
		return BindsHelper;
	}
	
	public static boolean isReady() {
		return ready;
	}

}
