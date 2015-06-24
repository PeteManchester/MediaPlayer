package org.rpi.credentials;

import java.util.Arrays;


public class CredentialInfo {
	
	private String userName = "";
	private byte[] password = new byte[]{};
	private boolean enabled = true;
	private String status = "";
	private String data = "";
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the password
	 */
	public byte[] getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(byte[] password) {
		this.password = password;
	}
	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "CredentialInfo [userName=" + userName + ", password Length=" + password.length + ", enabled=" + enabled + ", status=" + status + ", data=" + data + "]";
	}

}
