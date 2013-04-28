package uk.co.probablyfine.aoko.processes;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import uk.co.probablyfine.aoko.dao.AccountDao;
import uk.co.probablyfine.aoko.dao.MusicFileDao;
import uk.co.probablyfine.aoko.dao.YoutubeDao;
import uk.co.probablyfine.aoko.domain.Account;
import uk.co.probablyfine.aoko.domain.FileType;
import uk.co.probablyfine.aoko.domain.MusicFile;
import uk.co.probablyfine.aoko.domain.YoutubeDownload;
import uk.co.probablyfine.aoko.download.ApiExtractor;
import uk.co.probablyfine.aoko.download.ArtDownloader;
import uk.co.probablyfine.aoko.download.DownloadTaskFactory;
import uk.co.probablyfine.aoko.download.DownloadTaskFactory.DownloadTask;
import uk.co.probablyfine.aoko.service.FileUtils;
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
	@Mock private FileUtils mockUtils;
	@Mock private File mockDownloadedFile;
	
	private final String VIDEO_CODE = "foo";
	private final String USER_NAME = "a_user";
	private final String DOWNLOADED_FILE_NAME = "file.mp4";
	
	private Map<String,String> MOCK_VIMEO_DATA;
	
	@Before
	public void setUp() {
		MOCK_VIMEO_DATA = new HashMap<String,String>();

		when(mockVideos.next()).thenReturn(mockDownload);
		when(mockDownload.getVideoCode()).thenReturn(VIDEO_CODE);
		when(mockDownload.getQueuedBy()).thenReturn(USER_NAME);
		when(mockUsers.getFromUsername(USER_NAME)).thenReturn(mockUser);
		when(mockTaskFactory.getNewTask(mockDownload)).thenReturn(mockTask);
		when(mockExecutor.submit(mockTask)).thenReturn(mockFuture);
		when(mockTask.getDownloadedFile()).thenReturn(mockDownloadedFile);
		when(mockDownloadedFile.getName()).thenReturn(DOWNLOADED_FILE_NAME);
		when(mockApi.getVimeoData(VIDEO_CODE)).thenReturn(MOCK_VIMEO_DATA);
	}
	
	@Test
	public void shouldQueueTrack_ifPresentInDatabase() {
		when(mockMusicFiles.getFromUniqueId(VIDEO_CODE)).thenReturn(mockMusicFile);
		when(mockMusicFiles.containsFile(VIDEO_CODE)).thenReturn(true);
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles).getFromUniqueId(VIDEO_CODE);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockQueue).queueTrack(mockUser, mockMusicFile);
		verify(mockVideos).markSuccessful(mockDownload);
	}
	
	@Test
	public void shouldAbort_ifDownloadFails_fromInterruption() throws InterruptedException, ExecutionException, TimeoutException {
		when(mockMusicFiles.containsFile(VIDEO_CODE)).thenReturn(false);
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenThrow(new InterruptedException());
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles, never()).getFromUniqueId(VIDEO_CODE);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockVideos).markFailure(mockDownload);
	}
	
	@Test
	public void shouldAbort_ifDownloadFails_fromTimeout() throws InterruptedException, ExecutionException, TimeoutException {
		when(mockMusicFiles.containsFile(VIDEO_CODE)).thenReturn(false);
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenThrow(new TimeoutException());
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles, never()).getFromUniqueId(VIDEO_CODE);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockVideos).markFailure(mockDownload);
	}
	
	@Test
	public void shouldAbort_ifFileTransferFails() throws InterruptedException, ExecutionException, TimeoutException, IOException {
		when(mockMusicFiles.containsFile(VIDEO_CODE)).thenReturn(false);
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenReturn(0);
		when(mockUtils.moveFile(Mockito.any(File.class), Mockito.anyString())).thenThrow(new IOException());
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles, never()).getFromUniqueId(VIDEO_CODE);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockVideos).markFailure(mockDownload);
	}
	
	@Test
	public void shouldQueueTrack() throws InterruptedException, ExecutionException, TimeoutException, IOException {
		when(mockMusicFiles.containsFile(VIDEO_CODE)).thenReturn(false);
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenReturn(0);
		when(mockUtils.moveFile(Mockito.eq(mockDownloadedFile), Mockito.anyString())).thenReturn(mockDownloadedFile);
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles, never()).getFromUniqueId(VIDEO_CODE);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockQueue).queueTrack(eq(mockUser), Mockito.any(MusicFile.class));
		verify(mockVideos).markSuccessful(mockDownload);
	}
	
	@Test
	public void shouldGetVimeoMetadata_ifDownloadIsVimeo() throws InterruptedException, ExecutionException, TimeoutException, IOException {
		when(mockMusicFiles.containsFile(VIDEO_CODE)).thenReturn(false);
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenReturn(0);
		when(mockUtils.moveFile(Mockito.eq(mockDownloadedFile), Mockito.anyString())).thenReturn(mockDownloadedFile);
		when(mockDownload.getFileType()).thenReturn(FileType.VIMEO);
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles, never()).getFromUniqueId(VIDEO_CODE);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockQueue).queueTrack(eq(mockUser), Mockito.any(MusicFile.class));
		verify(mockVideos).markSuccessful(mockDownload);
		verify(mockApi).getVimeoData(VIDEO_CODE);
	}
	 
	@Test
	public void shouldGetYouTubeArtByDefault() throws InterruptedException, ExecutionException, TimeoutException, IOException {
		when(mockMusicFiles.containsFile(VIDEO_CODE)).thenReturn(false);
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenReturn(0);
		when(mockUtils.moveFile(Mockito.eq(mockDownloadedFile), Mockito.anyString())).thenReturn(mockDownloadedFile);
		when(mockDownload.getFileType()).thenReturn(FileType.YOUTUBE);
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles, never()).getFromUniqueId(VIDEO_CODE);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockQueue).queueTrack(eq(mockUser), Mockito.any(MusicFile.class));
		verify(mockVideos).markSuccessful(mockDownload);
		verify(mockApi, never()).getVimeoData(VIDEO_CODE);
		verify(mockArtDownloader).getYoutubeArt(VIDEO_CODE);
	}
	
	@Test
	public void shouldGetVimeoArt_ifLocationIsDefined() throws InterruptedException, ExecutionException, TimeoutException, IOException {
		when(mockMusicFiles.containsFile(VIDEO_CODE)).thenReturn(false);
		when(mockFuture.get(anyInt(),eq(TimeUnit.SECONDS))).thenReturn(0);
		when(mockUtils.moveFile(Mockito.eq(mockDownloadedFile), Mockito.anyString())).thenReturn(mockDownloadedFile);
		when(mockDownload.getFileType()).thenReturn(FileType.VIMEO);
		String artLocation = "location";
		
		MOCK_VIMEO_DATA.put("artlocation", artLocation);
		
		videoQueue.download(mockDownload);
		
		verify(mockMusicFiles, never()).getFromUniqueId(VIDEO_CODE);
		verify(mockVideos).markStartDownloading(mockDownload);
		verify(mockQueue).queueTrack(eq(mockUser), Mockito.any(MusicFile.class));
		verify(mockVideos).markSuccessful(mockDownload);
		verify(mockApi).getVimeoData(VIDEO_CODE);
		verify(mockArtDownloader).getVimeoArt(artLocation, VIDEO_CODE);
	}
}