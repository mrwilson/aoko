package uk.co.probablyfine.aoko.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import uk.co.probablyfine.aoko.dao.QueueItemDao;
import uk.co.probablyfine.aoko.domain.Account;
import uk.co.probablyfine.aoko.domain.MusicFile;
import uk.co.probablyfine.aoko.domain.PlayerState;
import uk.co.probablyfine.aoko.domain.QueueItem;

@RunWith(MockitoJUnitRunner.class)
public class QueueServiceTest {

	@InjectMocks private QueueService queueService = new QueueService();
	@Mock QueueItemDao mockQueueItemDao;
	@Mock Account mockUser;
	@Mock MusicFile mockTrack;
	@Mock QueueItem mockQueueItem1;
	@Mock QueueItem mockQueueItem2;
	private List<QueueItem> answers;
	
	@Before
	public void setup() {
		answers = new ArrayList<QueueItem>();
		
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				answers.add((QueueItem) invocation.getArguments()[0]);
				return null;
			}
		}).when(mockQueueItemDao).merge(Mockito.any(QueueItem.class));
	}
	
	@Test
	public void queueTrack_shouldCreateNewBucketAndQueue_whenQueueIsEmpty() {
		Collection<QueueItem> emptyCollection = emptyList();
		when(mockQueueItemDao.getAll()).thenReturn(emptyCollection);
		
		queueService.queueTrack(mockUser, mockTrack);

		QueueItem mergedItem = answers.get(0);

		verify(mockQueueItemDao, times(1)).merge(Mockito.any(QueueItem.class));
		assertEquals(1, mergedItem.getBucket());
		assertEquals(1, mergedItem.getPosition());
	}

	@Test
	public void queueTrack_shouldCreateNewBucketAndQueue_whenHasQueuedInPreviousBucket() {

		String userName = "Foo";
		
		when(mockQueueItemDao.getAll()).thenReturn(singletonList(mockQueueItem1));
		when(mockQueueItem1.getBucket()).thenReturn(1);
		when(mockQueueItem1.getPosition()).thenReturn(1);
		
		when(mockUser.getUsername()).thenReturn(userName);
		when(mockQueueItem1.getUserName()).thenReturn(userName);
		
		queueService.queueTrack(mockUser, mockTrack);
		QueueItem queueItem = answers.get(0);

		verify(mockQueueItemDao, times(1)).merge(Mockito.any(QueueItem.class));
		assertEquals(2, queueItem.getBucket());
		assertEquals(1, queueItem.getPosition());
	}
	
	@Test public void queueTrack_shouldCreateNewBucketAndQueue_whenAllResultsHaveBeenPlayed() {

		when(mockQueueItemDao.getAll()).thenReturn(singletonList(mockQueueItem1));
		when(mockQueueItem1.getBucket()).thenReturn(1);
		when(mockQueueItem1.getPosition()).thenReturn(1);
		when(mockQueueItem1.getStatus()).thenReturn(PlayerState.PLAYED);
		
		queueService.queueTrack(mockUser, mockTrack);
		QueueItem queueItem = answers.get(0);

		verify(mockQueueItemDao, times(1)).merge(Mockito.any(QueueItem.class));
		assertEquals(2, queueItem.getBucket());
		assertEquals(1, queueItem.getPosition());
	}
	
	@Test
	public void queueTrack_shouldAppendToCurrentBucket_whenOtherItemsInBucket() {

		when(mockQueueItemDao.getAll()).thenReturn(singletonList(mockQueueItem1));
		when(mockQueueItem1.getBucket()).thenReturn(1);
		when(mockQueueItem1.getPosition()).thenReturn(1);
		
		when(mockUser.getUsername()).thenReturn("Foo");
		when(mockQueueItem1.getUserName()).thenReturn("Bar");
		
		queueService.queueTrack(mockUser, mockTrack);
		QueueItem queueItem = answers.get(0);

		verify(mockQueueItemDao, times(1)).merge(Mockito.any(QueueItem.class));
		assertEquals(1, queueItem.getBucket());
		assertEquals(2, queueItem.getPosition());
	}
	
	@Test
	public void getTrackLayout_shouldReturnEmptyListWithNothingInTheQueue() {
		when(mockQueueItemDao.getAllUnplayed()).thenReturn(new ArrayList<QueueItem>());
		
		Collection<Collection<QueueItem>> queueLayout = queueService.getQueueLayout();
		
		assertEquals(queueLayout.size(), 0);
	}
	
	@Test
	public void getTrackLayout_shouldReturnCurrentlyQueuedTracks() {
		when(mockQueueItemDao.getAllUnplayed()).thenReturn(Arrays.asList(mockQueueItem1,mockQueueItem2));
		when(mockQueueItem1.getBucket()).thenReturn(1);
		when(mockQueueItem1.getPosition()).thenReturn(1);
		when(mockQueueItem2.getBucket()).thenReturn(2);
		when(mockQueueItem2.getPosition()).thenReturn(1);
		
		Collection<Collection<QueueItem>> queueLayout = queueService.getQueueLayout();
		
		assertEquals(queueLayout, asList(singletonList(mockQueueItem1), singletonList(mockQueueItem2)));
		
	}
	
	
}