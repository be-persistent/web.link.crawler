package jsoup.methods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class jsoupMethods {
	
	//private static Logger logger = LogManager.getLogger(jsoupMethods.class);
	
	//what string to look for in a url to keep searching links, also a safeguard to prevent runaway
	private static final String boundaryDomain = "cengage";
	
	//initialUrl must be the exact scheme and different domain elements TODO: make final
	private static String initialUrl = "https://www.google.com//";
	
	private static HashMap<String, HashMap<String, String>> mapOfUrlsAndInvalidLinks = new HashMap<String, HashMap<String, String>>();
	
	private static final String[] typesOfLinks = {"href", "src"};
	
	public static void main(String[] args) throws IOException {

		manageLinks(initialUrl);
		
		if(mapOfUrlsAndInvalidLinks.size()>0) {
			Iterator<Entry<String, HashMap<String, String>>> iter = mapOfUrlsAndInvalidLinks.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, HashMap<String, String>> urlPlusMap = iter.next();
				String primaryUrl = urlPlusMap.getKey();
				Iterator<Entry<String, String>> invalidIter = urlPlusMap.getValue().entrySet().iterator();
				while(invalidIter.hasNext()) {
					String strUrlAndCode = invalidIter.next().toString();
					System.out.println("Failure on page [".concat(primaryUrl).concat("] ---> ").concat(strUrlAndCode));					
				}
			}
			System.out.println("End of issues");
		}else {
			System.out.println("No issues found");
		}
		
	}
	
	private static void manageLinks(String url) throws IOException {
		
		while(url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		
		ArrayList<String> links = findLinks(url);
		
		if(links.isEmpty()) {
			System.out.println("No links found on page [".concat(url).concat("]"));
		}else {
			HashMap<String, String> invalidLinks = validateLinks(url, links);
			if(invalidLinks.isEmpty()) {
				System.out.println("No invalid links found on page [".concat(url).concat("]"));
			}else {
				mapOfUrlsAndInvalidLinks.put(url, invalidLinks);					
			}
		}
	}
	
    private static ArrayList<String> findLinks(String url) throws IOException {
    	ArrayList<String> links = new ArrayList<String>();
    	
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select("[src]");
        elements.addAll(doc.select("[href]"));
        
        for (Element element : elements) {
            links.add(element.attr("abs:href"));
            links.add(element.attr("abs:src"));
        }
        
        System.out.println("Total links found (".concat(String.valueOf(links.size()).concat(")")));
        return links;
    }
    
    private static HashMap<String, String> validateLinks(String urlOfPage, ArrayList<String> listOfLinks) {
    	URL url = null;
    	HashMap<String, String> mapOfFailedLinks = new HashMap<String, String>();
    	Map<String, Integer> mapOfOccurrences = new HashMap<String, Integer>();
    	int response = 0;
    	int count = 1;
    	
    	for(String str:listOfLinks) {
    		boolean isValidUrl = true;
    		if(!str.isEmpty()) {
				try {
					url = new URL(str);
				} catch (MalformedURLException e) {
					if(mapOfOccurrences.containsKey(str)) {
						int i = mapOfOccurrences.get(str);
						mapOfOccurrences.put(str, i++);
					}else {
						mapOfOccurrences.put(str, 1);
					}
					
					if(!mapOfFailedLinks.containsKey(str)) {
						mapOfFailedLinks.put(str, "MalformedURLException");
					}
					isValidUrl = false;
				}
				
				if(isValidUrl) {
		            HttpURLConnection.setFollowRedirects(false);
		            HttpURLConnection httpURLConnection = null;
					try {
						httpURLConnection = (HttpURLConnection) url.openConnection();
			            httpURLConnection.setRequestMethod("HEAD");
			            response = httpURLConnection.getResponseCode();
					} catch (IOException e) {
			            if(!mapOfFailedLinks.containsKey(url.toString())) {
			           		mapOfFailedLinks.put(url.toString(), e.toString());
			            }
					} finally {
			            if((response >= 400 && response < 999) && (!mapOfFailedLinks.containsKey(url.toString()))) {
			           		mapOfFailedLinks.put(url.toString(), String.valueOf(response));
			            }
						if(httpURLConnection != null) {
							httpURLConnection.disconnect();
						}
					}
				}
    		}else {
    			if(!mapOfFailedLinks.containsKey("empty link")) {
    				mapOfFailedLinks.put("empty link", "no value");
    			}
    		}
    		utilities.printPercentage(listOfLinks.size(), count++);
    	}
    	return mapOfFailedLinks;
    }

    private static class utilities {
    	
    	static int previousVal;
    	
    	private static void printPercentage(int size, int count) {
    		int iDiv = (count * 100 / size);
    		
    		if(((iDiv % 10) == 0) && (iDiv != previousVal)) {
    			System.out.println("Percent complete [".concat(String.valueOf(iDiv).concat("]")));
    			previousVal = iDiv;
    		}
    	}
    }
}
