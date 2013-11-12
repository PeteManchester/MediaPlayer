package org.rpi.sources;

public class Source {
	private String name;
	private String type;
	private String GPIO_PIN;
	public Source(String name, String type, String GPIO_PIN) {
		this.name = name;
		this.type = type;
		this.GPIO_PIN = GPIO_PIN;
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

}
