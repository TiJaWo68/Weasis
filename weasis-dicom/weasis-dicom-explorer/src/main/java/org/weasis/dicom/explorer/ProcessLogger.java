package org.weasis.dicom.explorer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * redirect stdout and stderr of a given process to a given {@link Logger}
 *
 * @author t68
 */
public class ProcessLogger {

	private static AtomicInteger counter = new AtomicInteger(0);

	protected abstract static class ProcessStreamReader extends InputStreamReader implements Runnable {

		public ProcessStreamReader(InputStream in) {
			super(in);
		}

		public void run() {
			String line = "";
			try (BufferedReader input = new BufferedReader(this)) {
				while ((line = input.readLine()) != null)
					logMessage(line);
			} catch (Exception ex) {
				LoggerFactory.getLogger(ProcessStreamReader.class).warn("", ex);
			}
		}

		public abstract void logMessage(String line);
	}

	Logger processLogger;

	public ProcessLogger(Logger log) {
		if (log == null)
			throw new IllegalArgumentException("given Logger must not be null");
		processLogger = log;
	}

	public void start(Process p) {
		int c = counter.incrementAndGet();
		new Thread(new ProcessStreamReader(p.getInputStream()) {

			@Override
			public void logMessage(String line) {
				processLogger.info(line);
			}
		}, "ProcessLogger-stdout-" + c).start();
		new Thread(new ProcessStreamReader(p.getErrorStream()) {

			@Override
			public void logMessage(String line) {
				processLogger.warn(line);
			}
		}, "ProcessLogger-stderr-" + c).start();
	}

}
