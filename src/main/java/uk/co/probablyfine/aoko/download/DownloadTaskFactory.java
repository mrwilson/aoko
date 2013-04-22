package uk.co.probablyfine.aoko.download;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.probablyfine.aoko.domain.YoutubeDownload;

import com.google.common.io.Files;

@Component
public class DownloadTaskFactory {

	@Value("${script.youtubedl}") private String scriptPath;
	
	public DownloadTask getNewTask(final YoutubeDownload entry) {
		return new DownloadTask(entry);
	}
	
	public class DownloadTask implements Callable<Integer> {
	
		private final YoutubeDownload entry;
		private final DefaultExecutor executor;
		private final File tempDir;
		
		public DownloadTask(YoutubeDownload entry) {
			this.entry = entry;
			this.executor = new DefaultExecutor();
			this.tempDir = Files.createTempDir();
		}
		
		public void stopDownloader() {
			executor.getWatchdog().destroyProcess();
		}
		
		@Override
		public Integer call() throws Exception {
			final String outputFormat = new File(this.tempDir,"%(stitle)s.%(ext)s").getAbsolutePath();
			
			final CommandLine command = new CommandLine(scriptPath);
			command.addArgument("-o");
			command.addArgument(outputFormat);
			command.addArgument(entry.getUrl());
			return executor.execute(command);
			
		}

		public File getDownloadedFile() {
			return tempDir.listFiles()[0];
		}
		
		public void deleteTempDir() {
			this.tempDir.delete();
		}
	
	}
}