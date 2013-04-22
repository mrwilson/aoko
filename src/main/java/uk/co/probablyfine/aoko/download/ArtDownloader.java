package uk.co.probablyfine.aoko.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

@Service
public class ArtDownloader {

	private static final String YOUTUBE_IMAGES = "http://img.youtube.com/vi/%d/1.jpg";
	private static final String AMAZON_IMAGES = "http://ec1.images-amazon.com/images/P/%s.jpg";
	@Value("${media.art}") private String downloadPath;
	@Autowired private ApiExtractor apiExtractor;
		
	private final Logger log = LoggerFactory.getLogger(ArtDownloader.class);
	
	public void getVimeoArt(String vimeoUrl, String vimeoId) throws IOException {
		final String path = new File(downloadPath,vimeoId+".jpg").getAbsolutePath();
		downloadFile(vimeoUrl, path);
	}
	
	public void getYoutubeArt(String youtubeId) throws IOException {
		final String url = String.format(YOUTUBE_IMAGES, youtubeId);
		final String path = new File(downloadPath,youtubeId+".jpg").getAbsolutePath();
		downloadFile(path, url);
	}
	
	public void getAlbumArt(Map<String,String> args, String filename) throws ParserConfigurationException, MalformedURLException, SAXException, IOException {
		
		String artUrl;
		
		if (args.containsKey("amazon_id")) {
			artUrl = String.format(AMAZON_IMAGES,args.get("amazon_id"));
		} else {
			String asin = apiExtractor.getAsinFromMusicbrainz(args);
			if (asin.equals("")) {
				throw new RuntimeException("GET request did not return asin");
			}
			artUrl = String.format(AMAZON_IMAGES,asin);
		}
				
		downloadFile(artUrl, downloadPath+filename+".jpg");

	}
	
	public void downloadFile(String url, String downloadLocation) throws IOException {
		log.debug("Downloading art from {} to {}", url, downloadLocation);
		
		final URL imageRequest = new URL(url);
		final ReadableByteChannel rbc = Channels.newChannel(imageRequest.openStream());
		final FileOutputStream fos = new FileOutputStream(downloadLocation);
		
		fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		fos.close();
	}
	
}