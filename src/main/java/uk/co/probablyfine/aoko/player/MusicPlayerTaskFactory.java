package uk.co.probablyfine.aoko.player;

import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.probablyfine.aoko.domain.QueueItem;

@Component
public class MusicPlayerTaskFactory {

	@Value("${player.path}") private String playerPath;
	@Value("${media.repository}") private String downloadPath;
	
	public MusicPlayerTask getNewTask(final QueueItem entry) {
		return new MusicPlayerTask(entry);
	}
	
	public class MusicPlayerTask implements Callable<Integer> {
	
		private final QueueItem entry;
		private DefaultExecutor executor;
		
		public MusicPlayerTask(QueueItem entry) {
			this.entry = entry;
			this.executor = new DefaultExecutor();
		}
		
		public void stopPlayer() {
			executor.getWatchdog().destroyProcess();
		}
		
		@Override
		public Integer call() throws Exception {
			final CommandLine command = new CommandLine(playerPath);
			command.addArgument(downloadPath+entry.getFile().getLocation());
			return executor.execute(command);
		}
	
	}
}