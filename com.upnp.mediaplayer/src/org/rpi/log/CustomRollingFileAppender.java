
package org.rpi.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;



public class CustomRollingFileAppender extends RollingFileAppender {
	

	private String mExtension;
	private String mLogHeader;
	private int mZipCounter;
	private boolean mZipEnabled;

	public CustomRollingFileAppender(Layout _pattern, String _file, String _extension, boolean _append) throws FileNotFoundException, IOException {
		super(_pattern, _file + _extension, _append);
		mExtension = _extension;
		mLogHeader = null;
	}


	public void setZipEnabled(boolean _zip) {
		mZipEnabled = _zip;
	}

	public boolean isZipEnabled() {
		return mZipEnabled;
	}

	public void setHeaderParameter(String _header) {
		mLogHeader = _header;
	}

	public void setMaximumFileSize(String _max) {
		int length = _max.length();
		String unit = "MB";
		String value = _max;
		if (length > 2) {
			unit = _max.substring(length - 2);
			value = _max.substring(0, length - 2);
		}
		if ("kB".equalsIgnoreCase(unit))
			setMaximumFileSize(Integer.parseInt(value) * 1024);
		else if ("MB".equalsIgnoreCase(unit))
			setMaximumFileSize(Integer.parseInt(value) * 1024 * 1024);
		else if ("GB".equalsIgnoreCase(unit))
		{
			long mValue = Integer.parseInt(value);
			setMaximumFileSize(mValue * 1024 * 1024 * 1024);
		}
		else
			setMaximumFileSize(Integer.parseInt(_max) * 1024 * 1024);
	}

	public void rollOver() {
		String zipTargetName = "";
		String targetName = "";
		String fileRoot = fileName.substring(0, fileName.length() - mExtension.length());
		LogLog.debug("Rolling over count=" + ((CountingQuietWriter) qw).getCount());
		LogLog.debug("maxBackupIndex=" + maxBackupIndex);
		if (maxBackupIndex > 0) {
			File file = new File(fileRoot + '.' + maxBackupIndex + mExtension);
			if (file.exists())
				file.delete();
			file = new File(fileRoot + '.' + maxBackupIndex + mExtension + ".zip");
			if (file.exists())
				file.delete();
			File target;
			if (mZipEnabled) {
				for (int i = maxBackupIndex - 1; i >= 1; i--) {
					file = new File(fileRoot + "." + i + mExtension + ".zip");
					if (file.exists()) {
						target = new File(fileRoot + '.' + (i + 1) + mExtension + ".zip");
						LogLog.debug("Renaming file " + file + " to " + target);
						file.renameTo(target);
					}
				}

			} else {
				for (int i = maxBackupIndex - 1; i >= 1; i--) {
					file = new File(fileRoot + "." + i + mExtension);
					if (file.exists()) {
						target = new File(fileRoot + '.' + (i + 1) + mExtension);
						LogLog.debug("Renaming file " + file + " to " + target);
						file.renameTo(target);
					}
				}

			}
			zipTargetName = fileRoot + "-" + ++mZipCounter + mExtension;
			targetName = fileRoot + ".1" + mExtension;
			target = new File(mZipEnabled ? zipTargetName : targetName);
			closeWriter();
			file = new File(fileRoot + mExtension);
			LogLog.debug("Renaming file " + file + " to " + target);
			file.renameTo(target);
		}
		try {
			setFile(fileRoot + mExtension, false, bufferedIO, bufferSize);
		} catch (IOException e) {
			LogLog.error("setFile(" + fileRoot + mExtension + ", false) call failed.", e);
		}
		if (mZipEnabled) {
			final String zipName = targetName + ".zip";
			final String fileToZip = zipTargetName;
			(new Thread(new Runnable() {

				public void run() {
					try {
						File file = new File(fileToZip);
						FileInputStream fileIn = new FileInputStream(file);
						long len = file.length();
						byte tab[] = new byte[(int) len];
						fileIn.read(tab);
						fileIn.close();
						ZipEntry zipEntry = new ZipEntry(fileToZip);
						ZipEntry _tmp = zipEntry;
						zipEntry.setMethod(8);
						zipEntry.setSize(len);
						FileOutputStream fileOut = new FileOutputStream(zipName);
						ZipOutputStream zipOut = new ZipOutputStream(fileOut);
						zipOut.putNextEntry(zipEntry);
						zipOut.write(tab, 0, (int) len);
						zipOut.flush();
						zipOut.closeEntry();
						zipOut.close();
						file.delete();
					} catch (FileNotFoundException fe) {
						LogLog.warn("Error while trying to zip log file " + fileToZip + ": " + fe.getMessage());
					} catch (IOException ioe) {
						LogLog.warn("Error while trying to zip log file " + fileToZip + ": " + ioe.getMessage());
					}
				}

			})).start();
		}
	}

	public void activateOptions(boolean writeHeader) {
		if (fileName != null) {
			try {
				setFile(fileName, fileAppend, bufferedIO, bufferSize, writeHeader);
			} catch (IOException e) {
				super.errorHandler.error("setFile(" + fileName + "," + fileAppend + ") call failed.", e, 4);
			}
		} else {
			LogLog.warn("File option not set for appender [" + super.name + "].");
			LogLog.warn("Are you using FileAppender instead of ConsoleAppender?");
		}
	}


	public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize, boolean writeHeader) throws IOException {
		LogLog.debug("setFile called: " + fileName + ", " + append);
		if (bufferedIO)
			setImmediateFlush(false);
		reset();
		FileOutputStream ostream = null;
		try {
			ostream = new FileOutputStream(fileName, append);
		} catch (FileNotFoundException ex) {
			String parentName = (new File(fileName)).getParent();
			if (parentName != null) {
				File parentDir = new File(parentName);
				if (!parentDir.exists() && parentDir.mkdirs())
					ostream = new FileOutputStream(fileName, append);
				else
					throw ex;
			} else {
				throw ex;
			}
		}
		Writer fw = createWriter(ostream);
		if (bufferedIO)
			fw = new BufferedWriter(fw, bufferSize);
		setQWForFiles(fw);
		this.fileName = fileName;
		fileAppend = append;
		this.bufferedIO = bufferedIO;
		this.bufferSize = bufferSize;
		if (writeHeader) {
			writeHeader();
		}
		if (append) {
			File f = new File(fileName);
			((CountingQuietWriter) super.qw).setCount(f.length());
		}
		LogLog.debug("setFile ended");
	}

}
