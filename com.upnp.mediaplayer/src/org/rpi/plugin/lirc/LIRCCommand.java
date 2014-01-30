package org.rpi.plugin.lirc;

public class LIRCCommand {
	
	private String command = "";
	private String name = "";
	
	public LIRCCommand(String command, String name)
	{
		this.command = command;
		this.name = name;
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
