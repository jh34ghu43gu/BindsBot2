package discord.events;

import bot.BindsBot;
import files.ConfigHelper;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Setup event to cache our binds
 */
public class BotReadyEvent extends ListenerAdapter {

	@Override
	public void onReady(ReadyEvent event) {
		//TODO maybe fix hardcode guild ID
		BindsBot.getBindsHelper().fetchAll(event.getJDA()
				.getGuildById("512197406616584217")
				.getTextChannelById(ConfigHelper.getOptionFromFile("BIND_CHANNEL")));
		BindsBot.ready = true;
		event.getJDA().getGuildById("512197406616584217")
					.getTextChannelById(ConfigHelper.getOptionFromFile("COMMAND_CHANNEL"))
					.sendMessage("Loaded binds cache with " + BindsBot.getBindsHelper().getBindsSize() + " binds, ready for commands.").queue();
	}

}
