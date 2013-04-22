package uk.co.probablyfine.aoko.processes;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.co.probablyfine.aoko.dao.QueueItemDao;
import uk.co.probablyfine.aoko.domain.QueueItem;
import uk.co.probablyfine.aoko.player.MusicPlayerTaskFactory;
import uk.co.probablyfine.aoko.player.MusicPlayerTaskFactory.MusicPlayerTask;

@Service
public class MusicPlayer {

	private final Logger log = LoggerFactory.getLogger(MusicPlayer.class);
	
	@Value("${player.timeout}") private long playerTimeout;
	
	@Autowired private QueueItemDao queue;
	@Autowired private MusicPlayerTaskFactory musicPlayerTaskFactory;
	@Autowired private ExecutorService singleThreadExecutor;

	private MusicPlayerTask currentTask = null;
	
	@PostConstruct
	public void play() throws InterruptedException {
		
		final Timer playerTimer = new Timer();
		
		final TimerTask playerTask = new TimerTask() {
			public void run() {
				playTrack(queue.nextTrack());
			}
		};
		
		playerTimer.schedule(playerTask, 0, 2000);
		
	}    

	public void playTrack(final QueueItem entry) {
		
		if (entry == null) return;
		
		currentTask = musicPlayerTaskFactory.getNewTask(entry);
		queue.startedPlaying(entry);
		try {
			singleThreadExecutor.submit(currentTask).get(playerTimeout, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			log.debug("Player timed out on {}",entry);
			stopTrack();
		} catch (Exception e) {
			log.error("Error playing track - {}",entry);
			log.error("{}",e);
		} finally {
			queue.finishedPlaying(entry);
			currentTask = null;
		}
	}
	
	public void stopTrack() {
		log.debug("Stopping track now.");
		currentTask.stopPlayer();
	}

	public void setPlayerTimeout(long playerTimeout) {
		this.playerTimeout = playerTimeout;
	}

	public void setSingleThreadExecutor(ExecutorService singleThreadExecutor) {
		this.singleThreadExecutor = singleThreadExecutor;
	}

}