package uk.co.probablyfine.aoko.download;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.co.probablyfine.aoko.dao.AccountDao;
import uk.co.probablyfine.aoko.dao.MusicFileDao;
import uk.co.probablyfine.aoko.dao.YoutubeDao;
import uk.co.probablyfine.aoko.domain.YoutubeDownload;
import uk.co.probablyfine.aoko.service.QueueService;

@RunWith(MockitoJUnitRunner.class)
public class VideoQueueTest {

	@InjectMocks private VideoQueue videoQueue = new VideoQueue();
	@Mock YoutubeDao mockVideos;
	@Mock MusicFileDao mockMusicFiles;
	@Mock AccountDao mockUsers;
	@Mock QueueService mockQueue;
	@Mock ApiExtractor mockApi;
	@Mock ArtDownloader mockArtDownloader;
	@Mock YoutubeDownload mockDownload;
	
	@Before
	public void setUp() {
		when(mockVideos.next()).thenReturn(mockDownload);
	}
	
	@Test
	public void pass() {
		
	}
	
}
