package org.daisy.streamline.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides a default implementation of {@link TempFileWriter}.
 * @author Joel Håkansson
 */
public class DefaultTempFileWriter implements TempFileWriter {
	/**
	 * Defines a path to a temporary folder. 
	 */
	public static final String TEMP_DIR;
	static {
		String path = System.getProperty("java.io.tmpdir");
		if (path!=null && !"".equals(path) && new File(path).isDirectory()) {
			TEMP_DIR = path;
		} else {
			// user.home is guaranteed to be defined
			TEMP_DIR = System.getProperty("user.home");
		}
	}
	private final Logger logger;
	private final File tempFilesFolder;
	private final String prefix;
	private final List<File> tempFiles;
	
	/**
	 * Creates a default temp file writer builder.
	 */
	public static class Builder {
		private File tempFilesFolder = new File(TEMP_DIR);
		private String prefix = "";
		/**
		 * Creates a new empty builder.
		 */
		public Builder() {
			super();
		}
		/**
		 * Sets the prefix to use when writing temp files
		 * @param value the value
		 * @return returns this builder
		 */
		public Builder prefix(String value) {
			this.prefix = value;
			return this;
		}
		/**
		 * Sets the folder where to write files. Note that
		 * writing of temporary files is disabled by default.
		 * @param value the folder
		 * @return returns this builder
		 * @throws IllegalArgumentException if <tt>value</tt> is not an existing directory
		 */
		public Builder tempFilesFolder(File value) {
			if (!value.isDirectory()) {
				throw new IllegalArgumentException(value + " is not an existing directory.");
			}
			this.tempFilesFolder = value;
			return this;
		}
		/**
		 * Sets the folder where to write files. Note that
		 * writing of temporary files is disabled by default. If the string
		 * is empty or null, the previously set value is used.
		 * @param value a string representing a folder
		 * @return returns this builder
		 * @throws IllegalArgumentException if <tt>value</tt> is not an existing directory
		 */
		public Builder tempFilesFolder(String value) {
			if (value!=null && !"".equals(value)) {
				tempFilesFolder(new File(value));
			}
			return this;
		}
		/**
		 * Creates a new default temp file writer.
		 * @return returns a new default temp file writer
		 */
		public DefaultTempFileWriter build() {
			return new DefaultTempFileWriter(this);
		}
	}
	
	private DefaultTempFileWriter(Builder builder) {
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.tempFilesFolder = builder.tempFilesFolder;
		this.prefix = builder.prefix + "@" + Integer.toHexString((int)(System.currentTimeMillis()-1261440000000l));
		this.tempFiles = new ArrayList<>();
	}
	
	@Override
	public void writeTempFile(File source, String identifier) throws IOException {
		int i = tempFiles.size();
		String fileNumber = ""+(i+1);
		while (fileNumber.length()<3) {
			fileNumber = "0" + fileNumber;
		}
		String fileName = (prefix + "-" 
						+ fileNumber + "-" 
						+ truncate(identifier, 20)
					).replaceAll("[^a-zA-Z0-9@\\-]+", "_");
		fileName += ".tmp";
		File f = new File(tempFilesFolder, fileName);
		logger.fine("Writing debug file: " + f);
		Files.copy(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
		tempFiles.add(f);
	}
	
	private String truncate(String str, int pos) {
		if (str.length()>pos) {
			return str.substring(0, pos);
		} else {
			return str;
		}
	}
	
	@Override
	public void deleteTempFiles() {
		for (File f : tempFiles) {
			if (!f.delete()) {
				f.deleteOnExit();
			}
		}
		tempFiles.clear();
	}
}