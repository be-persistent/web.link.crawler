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
import java.util.Map;
import java.util.Map.Entry;

public class jsoupMethods {
	
	//initialUrl must be the exact scheme and different domain elements TODO: make final
	private static String initialUrl = "https://www.cengage.com/";
	
	private static HashMap<String, HashMap<String, String>> mapOfUrlsAndInvalidLinks = new HashMap<String, HashMap<String, String>>();
	
	public static void main(String[] args) throws IOException {

		manageLinks(initialUrl);

		if(mapOfUrlsAndInvalidLinks.size() > 0) {
			Iterator<Entry<String, HashMap<String, String>>> iter = mapOfUrlsAndInvalidLinks.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, HashMap<String, String>> urlPlusMap = iter.next();
				String primaryUrl = urlPlusMap.getKey();
				Iterator<String> invalidIter = urlPlusMap.getValue().keySet().iterator();
				while(invalidIter.hasNext()) {
					String key = invalidIter.next();
					String val = urlPlusMap.getValue().get(key);
					System.out.println("Failure on page [".concat(primaryUrl).concat("] ---> ")
							.concat(key).concat(" [").concat(val).concat("]"));
				}
			}
			System.out.println("End of issues");
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
			HashMap<String, String> invalidLinks = validateLinks(links);
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
    
    private static HashMap<String, String> validateLinks(ArrayList<String> listOfLinks) {
    	URL url = null;
    	HashMap<String, String> mapOfFailedLinks = new HashMap<String, String>();
    	Map<String, Integer> mapOfOccurrences = new HashMap<String, Integer>();
    	ArrayList<String> listOfLinksVerified = new ArrayList<String>();
    	int response = 0;
    	int count = 1;
    	
    	for(String str:listOfLinks) {
    		if(!listOfLinksVerified.contains(str)) { //No need to keep verifying the same link
    			listOfLinksVerified.add(str);
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
    			            isValidUrl = false;
    					} finally {
    			            if((response >= 400 && response < 999) && (!mapOfFailedLinks.containsKey(url.toString()))) {
    			           		mapOfFailedLinks.put(url.toString(), String.valueOf(response));
    			           		isValidUrl = false;
    			            }
    						if(httpURLConnection != null) {
    							httpURLConnection.disconnect();
    						}
    					}
    				}
        		}
        		
        		if(!isValidUrl) {
            		int cnt = getCountOfStringInList(listOfLinks, str);
            		String val = mapOfFailedLinks.get(str);
            		val = val.concat(" x").concat(String.valueOf(cnt));
            		mapOfFailedLinks.put(str, val);
        		}
    		}
    		
    		utilities.printPercentage(listOfLinks.size(), count++);
    	}
    	return mapOfFailedLinks;
    }

    private static int getCountOfStringInList(ArrayList<String> list, String url) {
    	int count = 0;	
    	if(!url.isEmpty()) {
        	for(String str:list) {
        		if(str.equals(url)) {
        			count++;
        		}
        	}		
    	}
    	return count;
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
