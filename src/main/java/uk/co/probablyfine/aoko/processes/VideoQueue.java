package uk.co.probablyfine.aoko.processes;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

import com.google.common.collect.Maps;

@Service
public class VideoQueue {

	private final Logger log = LoggerFactory.getLogger(VideoQueue.class);
	
	@Autowired private YoutubeDao videos;
	@Autowired private MusicFileDao musicFiles;
	@Autowired private AccountDao users;
	@Autowired private QueueService queue;
	@Autowired private ApiExtractor api;
	@Autowired private ArtDownloader artDownloader;
	@Autowired private DownloadTaskFactory taskFactory;
	@Autowired private ExecutorService singleThreadExecutor;
	@Autowired private FileUtils utils;

	@Value("${script.dltimeout}") int timeout;
	@Value("${media.repository}") String mediaPath;
	@Value("${media.art}") String artPath;
	
	private DownloadTask currentTask = null;
	
	@PostConstruct
	public void downloadVideos() {
		
		final Timer downloadTimer = new Timer();
		
		final TimerTask downloadTask = new TimerTask() {
			@Override
			public void run() {
				download(videos.next());
			}
		};
		
		downloadTimer.schedule(downloadTask, 0, 2000);
		
	}
	
	public void download(final YoutubeDownload download) {

		if (null == download) return;

		final FileType type = download.getFileType();
		final String videoCode = download.getVideoCode();
		final Account user = users.getFromUsername(download.getQueuedBy());
		
		log.debug("Attempting to download {}",download.getUrl());
		videos.markStartDownloading(download);
		currentTask = taskFactory.getNewTask(download);
		
		if (musicFiles.containsFile(videoCode)) {
			final MusicFile file = musicFiles.getFromUniqueId(videoCode);
			queue.queueTrack(user, file);
			videos.markSuccessful(download); 
			return;
		} 
		
		int code = -1;
		
		try {
			code = singleThreadExecutor.submit(currentTask).get(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			log.error("Failure to download {}, download interrupted",download.getUrl(),e1);
		} catch (ExecutionException e1) {
			log.error("Failure to download {}, error executing downloader",download.getUrl(),e1);
		} catch (TimeoutException e1) {
			log.debug("Download timed out for {}",download.getUrl());
		}

		if (code != 0) {
			log.debug("Download failure from {}",download.getUrl());
			videos.markFailure(download);
			return;
		}

		queueDownloadedFile(download, type, videoCode, user);
					
	}

	private void queueDownloadedFile(final YoutubeDownload download, final FileType type,
			final String videoCode, final Account user) {
		log.debug("{} has identifier {}",download.getUrl(),videoCode);

		final File downloadedFile = currentTask.getDownloadedFile();
		final File newFile;
		
		try {
			newFile = moveDownloadedFileToRepo(downloadedFile, videoCode);
		} catch (IOException e1) {
			log.error("Could not move file from {}", downloadedFile.getAbsolutePath());
			videos.markFailure(download);
			return;
		}
			
		final MusicFile file = new MusicFile();
			
		final Map<String,String> data;
			
		if (download.getFileType() == FileType.VIMEO) {
			data = api.getVimeoData(videoCode);
			if (data.containsKey("artlocation")) {
				file.setArtLocation(videoCode+".jpg");
			}
		} else {
			data = Maps.newHashMap();
			try {
				artDownloader.getYoutubeArt(videoCode);
				file.setArtLocation(videoCode+".jpg");
			} catch (Exception e) {
				log.error("Cannot download thumbnail for {} ",download.getUrl(),e);
			}
		}
		
		final String actualName = downloadedFile.getName().substring(0,downloadedFile.getName().lastIndexOf("."));
		
		data.put("name", actualName);
		data.put("originalname", downloadedFile.getName());
		
		file.setType(type);
		file.setLocation(newFile.getName());
		file.setMetaData(data);
		file.setUniqueId(videoCode);
						
		queue.queueTrack(user, file);
		videos.markSuccessful(download);
	}
	
	public File moveDownloadedFileToRepo(File downloadedFile, String path) throws IOException {
		final String name = downloadedFile.getName();
		final String extension = name.substring(name.lastIndexOf("."),name.length());
		final String newPath = mediaPath+File.separator+path+extension;

		return utils.moveFile(downloadedFile, newPath);
	}
	
	public void stopDownloader() {
		log.debug("Stopping the downloader.");
		currentTask.stopDownloader();
	}
	
}
