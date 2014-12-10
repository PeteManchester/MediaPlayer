package org.rpi.sources;

public class Source {
	private String name;
	private String type;
	private String GPIO_PIN;
	private String start_script = null;
	private String stop_script = null;
	private boolean visible = true;
	public Source(String name, String type, String GPIO_PIN,boolean visible) {
		this.name = name;
		this.type = type;
		this.GPIO_PIN = GPIO_PIN;
		this.setVisible(visible);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getGPIO_PIN() {
		return GPIO_PIN;
	}
	public void setGPIO_PIN(String gPIO_PIN) {
		GPIO_PIN = gPIO_PIN;
	}
	
	public String toString()
	{
		return "Name: " + name;
	}
	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}
	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	/**
	 * @return the start_script
	 */
	public String getStartScript() {
		return start_script;
	}
	/**
	 * @param start_script the start_script to set
	 */
	public void setStartScript(String start_script) {
		this.start_script = start_script;
	}
	/**
	 * @return the stop_script
	 */
	public String getStopScript() {
		return stop_script;
	}
	/**
	 * @param stop_script the stop_script to set
	 */
	public void setStopScript(String stop_script) {
		this.stop_script = stop_script;
	}

}
