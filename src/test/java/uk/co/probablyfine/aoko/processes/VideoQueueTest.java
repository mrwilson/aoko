package uk.co.probablyfine.aoko.processes;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import uk.co.probablyfine.aoko.dao.AccountDao;
import uk.co.probablyfine.aoko.dao.MusicFileDao;
import uk.co.probablyfine.aoko.dao.YoutubeDao;
import uk.co.probablyfine.aoko.domain.Account;
import uk.co.probablyfine.aoko.domain.MusicFile;
import uk.co.probablyfine.aoko.domain.YoutubeDownload;
import uk.co.probablyfine.aoko.download.ApiExtractor;
import uk.co.probablyfine.aoko.download.ArtDownloader;
import uk.co.probablyfine.aoko.download.DownloadTaskFactory;
import uk.co.probablyfine.aoko.download.DownloadTaskFactory.DownloadTask;
import uk.co.probablyfine.aoko.service.QueueService;

@RunWith(MockitoJUnitRunner.class)
public class VideoQueueTest {

	@InjectMocks private VideoQueue videoQueue = new VideoQueue();
	@Mock private DownloadTaskFactory mockTaskFactory;
	@Mock private YoutubeDao mockVideos;
	@Mock private MusicFileDao mockMusicFiles;
	@Mock private AccountDao mockUsers;
	@Mock private QueueService mockQueue;
	@Mock private ApiExtractor mockApi;
	@Mock private ArtDownloader mockArtDownloader;
	@Mock private YoutubeDownload mockDownload;
	@Mock private Account mockUser;
	@Mock private MusicFile mockMusicFile;
	@Mock private DownloadTask mockTask;
	@Mock private ExecutorService mockExecutor;
	@Mock private Future<Integer> mockFuture;
	
	private final String videoCode = "foo";
	private final String userName = "a_user";
	
	@Before
	public void setUp() {
		when(mockVideos.next()).thenReturn(mockDownload);
		when(mockDownload.getVideoCode()).thenReturn(videoCode);
		when(mockDownload.getQueuedBy()).thenReturn(userName);
		when(mockUsers.getFromUsername(userName)).thenReturn(mockUser);
		when(mockTaskFactory.getNewTask(mockDownload)).thenReturn(mockTask);
		when(mockExecutor.submit(mockTask)).thenReturn(mockFuture);
	}
	
	@Test
	public void shouldQueueTrack_ifPresentInDatabase() {
		when(mockMusicFiles.getFromUniqueId(videoCode)).thenReturn(mockMusicFile);
		when(mockMusicFiles.containsFile(videoCode)).thenReturn(true);
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles).getFromUniqueId(videoCode);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockQueue).queueTrack(mockUser, mockMusicFile);
		verify(mockVideos).markSuccessful(mockDownload);
	}
	
	@Test
	public void shouldAbort_ifDownloadFails() throws InterruptedException, ExecutionException, TimeoutException {
		when(mockMusicFiles.containsFile(videoCode)).thenReturn(false);
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenThrow(new InterruptedException());
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles, never()).getFromUniqueId(videoCode);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockVideos).markFailure(mockDownload);
	}
	
	@Test
	public void shouldAbort_ifFileTransferFails() {
		
	}
	
}