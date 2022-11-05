package discord.commands.owner;

import java.util.ArrayList;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import discord.BindsHelper;
import files.BindsFileHelper;
import model.Bind;
import net.dv8tion.jda.api.entities.Message;

public class DebugCommand extends Command {
	
	public DebugCommand() {
		this.name = "debug";
		this.ownerCommand = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		BindsHelper BH = new BindsHelper();
		BH.fetchAll(event.getGuild().getTextChannelById("567513480375042069"));
		
		ArrayList<String> binds = new ArrayList<String>();
		for(Bind b : BH.getBinds()) {
			for(String s : b.getBind()) {
				binds.add(s);
			}
		}
		
		BindsFileHelper BFH = new BindsFileHelper();
		BFH.createSingleKeyFile("jh34", binds, "bb", "i");
		
	}

}
