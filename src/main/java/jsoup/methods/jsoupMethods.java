package jsoup.methods;

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
	
	private final String boundaryDomain = "cengage";
	private final static String urlToStart = "https://www.cengage.com/";
	private static List<String> links = new ArrayList<String>();

	public static void main(String[] args) {
		
		utilities Utilities = new utilities();

		try {
			findLinks();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> invalidLinks = validateLinks();
		Iterator<Entry<String, String>> iter = invalidLinks.entrySet().iterator();
		
		while(iter.hasNext()) {
			System.out.println("Failure -> ".concat(iter.next().toString().replace("="," ---> ")));
		}
		
		System.out.println("Done");
	}
	
    private static void findLinks() throws IOException {
        Document doc = Jsoup.connect(urlToStart).get();
        Elements elements = doc.select("[src]");
        elements.addAll(doc.select("[href]"));
        
        for (Element element : elements) {
            links.add(element.attr("href"));
            links.add(element.attr("src"));
        }
        
        System.out.println("Total links found [" .concat(String.valueOf(links.size()).concat("]")));
    }
    
    private static Map<String, String> validateLinks() {
    	URL url = null;
    	Map<String, String> mapOfFailedLinks = new HashMap<String, String>();
    	Map<String, Integer> mapOfOccurrences = new HashMap<String, Integer>();
    	int response = 0;
    	int count = 1;
    	
    	for(String str:links) {
    		boolean isValidUrl = true;
    		if(!str.isEmpty()) {
				try {
					url = new URL(str);
				} catch (MalformedURLException e) {
					if(urlToStart.endsWith("/") && str.startsWith("/")) {
						if(str.startsWith("//")) {
							str = urlToStart.concat(str.substring(2, str.length()));
						}else {
							str = urlToStart.concat(str.substring(1, str.length()));
						}
					}
					
					try {
						url = new URL(str);
					} catch (MalformedURLException e1) {
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
    		utilities.printPercentage(count++);
    	}
    	return mapOfFailedLinks;
    }

    private static class utilities {
    	
    	static int previousVal;
    	
    	private static void printPercentage(int count) {
    		
    		int iDiv = (count * 100 / links.size());
    		
    		if(((iDiv % 10) == 0) && (iDiv != previousVal)) {
    			System.out.println("Percent complete [".concat(String.valueOf(iDiv).concat("]")));
    			previousVal = iDiv;
    			
    		}
    	}
    	
    }
}
