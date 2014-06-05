package org.rpi.web.longpolling;

import java.util.List;

import org.apache.log4j.Logger;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.Biography;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Image;
import com.echonest.api.v4.News;

public class JenUttils {

	private Logger log = Logger.getLogger(JenUttils.class);
	private EchoNestAPI en;
	private static boolean trace = true;
	private String API_KEY = "RUGHIYI3GGBRXPGQQ";

	public JenUttils() throws EchoNestException {
		en = new EchoNestAPI(API_KEY);
		//en.setTraceSends(trace);
		//en.setTraceRecvs(trace);
		en.setMinCommandTime(0);
	}


	/***
	 * Get the Artist Biography, News and an Image
	 * @param name
	 * @return
	 * @throws EchoNestException
	 */
	public ArtistInfo searchArtistByName(String name) throws EchoNestException {
		ArtistInfo info = new ArtistInfo();
		List<Artist> artists = en.searchArtists(name);
		Biography biggest_biog = null;
		if(artists.size()>0)
		{
			Artist artist = artists.get(0);
			List<Biography> bios = artist.getBiographies(0, 5,"cc-by-sa");
			for(Biography biog : bios)
			{
				if(biggest_biog ==null)
					biggest_biog = biog;
				
				if(biggest_biog.getText().length() < biog.getText().length())
					biggest_biog = biog;
			}
			if(biggest_biog !=null)
			{
				//biggest_biog.dump();
				info.setBiography(biggest_biog.getText());
			}
			
//			List<News> newss = artist.getNews(0,5,true);
//			StringBuilder sb = new StringBuilder();
//			String nl = "<p>";
//			sb.append("<html>");
//			for (News news : newss)
//			{
//				//news.dump();
//				sb.append("<b>");
//				sb.append(news.getName());
//				sb.append(nl);
//				sb.append(news.getDateFound());
//				sb.append(nl);
//				sb.append("</b>");
//				sb.append(nl);
//				String summary = news.getSummary();
//				sb.append(summary);
//				sb.append(nl);
//				sb.append(nl);
//				sb.append(nl);
//				
//			}
//			sb.append("</html>");
//			info.setNews(sb.toString());
//			
//			List<Image> images = artist.getImages(0, 1);
//			for (Image image: images)
//			{
//				info.setImageURL(image.getURL());
//				//log.debug(info.getImageURL());
//			}
		}
		return info;
	}

	public void stats() {
		en.showStats();
	}


	public String getAlbumInfo(String album) {
		//Album album = en.
		return "";
	}
}