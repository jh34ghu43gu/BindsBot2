package discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.BindsBot;
import bot.Utils;

public class UpdateCommand extends Command {

	public UpdateCommand() {
		this.name = "update";
		this.cooldown = 7200;
		this.help = "Update the binds cache from the binds channel, global cooldown of 2 hours.";
		this.cooldownScope = CooldownScope.GLOBAL;
	}

	@Override
	protected void execute(CommandEvent event) {
		if(!BindsBot.isReady()) { return; }
		//Delete message if sent outside command channel
		if(!Utils.commandCheck(event)) {
			event.getMessage().delete().queue();
			return;
		}
		
		int oldSize = BindsBot.getBindsHelper().getBindsSize();
		BindsBot.getBindsHelper().updateBinds(Utils.getBindsChannel(event));
		int currentSize = BindsBot.getBindsHelper().getBindsSize();
		event.reply("Updated binds cache, located " + (oldSize - currentSize) + "new binds since last update.");
		
	}
	
}
