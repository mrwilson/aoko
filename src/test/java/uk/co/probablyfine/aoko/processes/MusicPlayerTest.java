package uk.co.probablyfine.aoko.processes;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.co.probablyfine.aoko.dao.QueueItemDao;
import uk.co.probablyfine.aoko.domain.QueueItem;
import uk.co.probablyfine.aoko.player.MusicPlayerTaskFactory;
import uk.co.probablyfine.aoko.player.MusicPlayerTaskFactory.MusicPlayerTask;

@RunWith(MockitoJUnitRunner.class)
public class MusicPlayerTest {

	@InjectMocks private MusicPlayer player;
	@Mock private MusicPlayerTaskFactory mockTaskFactory;
	@Mock private QueueItemDao mockQueue;
	@Mock private QueueItem mockEntry;
	@Mock private MusicPlayerTask mockTask;
	@Mock private Future<Integer> mockFuture;
	@Mock private ExecutorService mockExecutorService;
	
	@Before
	public void setup() {
		when(mockQueue.nextTrack()).thenReturn(mockEntry);
		when(mockTaskFactory.getNewTask(mockEntry)).thenReturn(mockTask);
		when(mockExecutorService.submit(mockTask)).thenReturn(mockFuture);
	}
	
	@Test
	public void player_shouldStopTaskOnTimeout() throws InterruptedException, ExecutionException, TimeoutException {
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenThrow(new TimeoutException());
		player.playTrack(mockEntry);
		verify(mockTask,times(1)).stopPlayer();
		verify(mockQueue,times(1)).finishedPlaying(mockEntry);
	}
	
	@Test
	public void player_shouldMarkFileAsFinishedWhenPlaybackFinishes() throws InterruptedException, ExecutionException, TimeoutException {
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenReturn(1);
		player.playTrack(mockEntry);
		verify(mockTask,never()).stopPlayer();
		verify(mockQueue,times(1)).finishedPlaying(mockEntry);
	}
	
	@Test
	public void player_shouldDoNothingIfNullIsPassed() throws InterruptedException, ExecutionException, TimeoutException {
		player.playTrack(null);
		verifyNoMoreInteractions(mockQueue, mockTaskFactory, mockExecutorService);
	}

}
