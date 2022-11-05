package discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import bot.BindsBot;

public class RandomBindCommand extends Command {
	
	
	public RandomBindCommand() {
		this.name = "bind";
		this.help = "Posts a random bind.";
	}

	@Override
	protected void execute(CommandEvent event) {
		if(!BindsBot.isReady()) { return; }
		
		event.reply(BindsBot.getBindsHelper().getRandomBind());
		
	}
	
	

}
