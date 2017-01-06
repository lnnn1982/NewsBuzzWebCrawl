package com.NewBuzz;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.*;

import java.net.URI;
import java.net.URL;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpStatus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

public class Spider {

	public static class SearchPolicy {

		private static class SearchWordPolicy {
			private ArrayList<String> searchTitles = new ArrayList<>();

			public void addSearchTitle(String word) {
				searchTitles.add(word);
			}

			public ArrayList<String> getSearchTiles() {
				return (ArrayList<String>) searchTitles.clone();
			}
		}

		private SearchWordPolicy wordPolicy = new SearchWordPolicy();
		private int searchScope = 0;

		public void addSearchTitle(String word) {
			wordPolicy.addSearchTitle(word.trim());
		}

		public ArrayList<String> getSearchTiles() {
			return wordPolicy.getSearchTiles();
		}

		public void setSearchScope(int scope) {
			searchScope = scope;
		}

		public int getSearchScope() {
			return searchScope;
		}
	}

	public static class ResultUrlCollection
	{
		public static class ResultUrl {
			private String fullUrl = "";
			private String title = "";

			public void setFullUrl(String url) {
				fullUrl = url;
			}

			public void setTitle(String t) {
				title = t;
			}

			public String getFullUrl() {
				return fullUrl;
			}

			public String getTitle() {
				return title;
			}
		}
		
		ArrayList<ResultUrl> urls = new ArrayList<ResultUrl>();
		
		public void addResultUrl(ResultUrl url) {
			urls.add(url);
		}
		
		public ArrayList<ResultUrl> getResultUrls() {
			return (ArrayList<ResultUrl>)urls.clone();
		}
		
		public int size() {
			return urls.size();
		}
	}

	private CloseableHttpClient httpclient = HttpClients.createDefault();
	
	//result
	private HashMap<SearchPolicy, ResultUrlCollection> rstUrlCollectMap = new HashMap<>();
	
	//querySearchQueue
	private HashSet<String> visitedUrls = new HashSet<>();
	private ArrayDeque<String> relevantUrls = new ArrayDeque<>();
	private ArrayDeque<String> nonRelevantUrls = new ArrayDeque<>();
	
	//search input
	private String baseUrl = "";
	private String hostSymbol = "";
	ArrayList<SearchPolicy> searchPolicies = null;
	ArrayList<String> excludedSites = null;

	//search level
	private int curSearchLevel = 0;
	private static final int maxSearchLevel = 200;

	//in case of website's block 
	private static final int searchDelay = 10;
	
	private static final Logger myLogger = Logger.getLogger("com.NewBuzz.Spider");
	
	static class MyLogHander extends Formatter { 
        @Override 
        public String format(LogRecord record) { 
                return record.getLevel() + ":" + record.getMessage()+"\n"; 
        } 
    }
	static
	{
		try {        
			FileHandler fileHandler = new FileHandler("spider.log"); 
	        fileHandler.setLevel(Level.INFO); 
	        fileHandler.setFormatter(new MyLogHander()); 
	        myLogger.addHandler(fileHandler); 
	        
	        ConsoleHandler consoleHandler = new ConsoleHandler();
	        consoleHandler.setLevel(Level.SEVERE);
	        myLogger.addHandler(consoleHandler);
	        			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	//work function
	public HashMap<SearchPolicy, ResultUrlCollection> work(String baseUrl, SearchPolicy policy,
			ArrayList<String> excludeUrls) {
		ArrayList<SearchPolicy> policies = new ArrayList<SearchPolicy>();
		policies.add(policy);

		return work(baseUrl, policies, excludeUrls);
	}

	//work function
	public HashMap<SearchPolicy, ResultUrlCollection> work(String bUrl, ArrayList<SearchPolicy> policies,
			ArrayList<String> excludeUrls) {
		clearData();
		baseUrl = trimUrlBackSlash(bUrl.trim());
		getHostSymbol();
		
		searchPolicies = policies;
		excludedSites = excludeUrls;
		
		initRstCollection();
		
		breadthSearchUrl();

		return rstUrlCollectMap;
	}
	
	public HashMap<SearchPolicy, ResultUrlCollection> continueWork() {
		clearRstData();
		
		initRstCollection();
		breadthSearchUrl();

		return rstUrlCollectMap;
	}
	
	private void getHostSymbol() {
		String[] splitStrs = baseUrl.split("\\.");
		if (splitStrs.length == 3 && splitStrs[2].toLowerCase().contains("com")) {
			hostSymbol = splitStrs[1];
		}
	}
	
	private void clearData() {
		myLogger.info("clear old info");
		
		rstUrlCollectMap.clear();
		
		visitedUrls.clear();
		relevantUrls.clear();
		nonRelevantUrls.clear();
		
		baseUrl = "";
		hostSymbol = "";
		searchPolicies = null;
		excludedSites = null;
		
		curSearchLevel = 0;
	}
	
	
	private void clearRstData() {
		rstUrlCollectMap.clear();	
		
		curSearchLevel = 0;
	}
	
	private void breadthSearchUrl() {
		addSearchUrl(baseUrl);

		while(true){
			System.out.println("breadthSearchUrl level:" + curSearchLevel);
			
			myLogger.info("*********************nonRelevantUrl size:" + nonRelevantUrls.size() + ", relevantUrl size:" + relevantUrls.size()
			+ ", visited url size:" + visitedUrls.size() + ", level:" + curSearchLevel);
			
			for(Map.Entry<SearchPolicy, ResultUrlCollection> entry : rstUrlCollectMap.entrySet()) {
				myLogger.info("search title:" + entry.getKey().getSearchTiles() + ", rst size:" + 
			    entry.getValue().size());
			}
				
			if (isResultsEnough() || curSearchLevel == maxSearchLevel) {
				break;
			}
			
			if (!relevantUrls.isEmpty()) {
				searchOneUrl(relevantUrls.pop());
			}
			else if (!nonRelevantUrls.isEmpty()) {
				searchOneUrl(nonRelevantUrls.pop());
			}
			else {
				myLogger.info("**************************no url for search");
				break;
			}
			
			curSearchLevel++;
			//sleep(searchDelay);
		}

		System.out.println("finish search. level:" + curSearchLevel + ", max level:" + maxSearchLevel);
		myLogger.info("finish search. level:" + curSearchLevel + ", max level:" + maxSearchLevel);
	}
	
	private void searchOneUrl(String curUrl) {
		String content = getWebpageFileContent(curUrl);

		Document curDoc = Jsoup.parse(content, curUrl);

		addRelatedUrl(curUrl, curDoc);
		collectUrls(curUrl, content, curDoc);
	}
	
	private String trimUrlBackSlash(String orgUrl) {
		if (orgUrl.endsWith("/")) {
			return orgUrl.substring(0, orgUrl.length()-1);
		}
		
		return orgUrl;
	}
	
	private String getAbsoluteURL(String baseURI, String relativePath){ 
	    String abUrl = null;  
	    try {  
	        URI base = new URI(baseURI);
	        URI abs = base.resolve(relativePath);
	        
	        URL absURL = abs.toURL();
	        abUrl = absURL.toString();  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    } 
	    
		myLogger.info("######relative url. baseUri:" + baseURI + ",relativePath"
				+ relativePath + ", abUrl:" + abUrl);
	    return abUrl;
	} 

	private String getWebpageFileContent(String curUrl) {
		String contentStr = "";

		try {
			HttpGet httpget = new HttpGet(curUrl);
			CloseableHttpResponse response = httpclient.execute(httpget);

			try {
				if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					contentStr = EntityUtils.toString(response.getEntity(), "utf-8");
				}
				else {
					myLogger.info("visit web url fail");
				}
			} finally {
				response.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//genFile(contentStr, "searchWeb.txt");
		myLogger.info("url:" + curUrl + ", webpage size:" + contentStr.length());
		
		return contentStr;
	}
	
	private void genFile(String content, String fileName) {
		try
		{
			FileWriter fw = new FileWriter(fileName);   
			fw.write(content);
			fw.close();
			
		}
		catch (Exception e) {   
            e.printStackTrace();   
        }   
	}

	private void collectUrls(String curUrl, String content, Document doc) {
		visitedUrls.add(curUrl);
		
		addUrfFromRegex(curUrl, content);
		addUrlFromHref(doc);
	}
	
	public void addUrfFromRegex(String curUrl, String content) {
		String regexStr = "\"uri\"\\s*:\\s*\"(.+?)\"";
		Pattern pattern = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		
		while(matcher.find()) {
            String uri = matcher.group(1).trim();
            
            if (uri.startsWith("/")) {
            	addSearchUrl(baseUrl + uri);
			} 
            else if (uri.startsWith("http")) {
            	addSearchUrl(uri);
            }
            else {
				addSearchUrl(getAbsoluteURL(curUrl, uri));
			}
		}
	}
	
	private void addUrlFromHref(Document doc) {
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			addSearchUrl(link.attr("abs:href").trim());
		}
	}
	
	private void addSearchUrl(String orgUrl) {
		if ((!orgUrl.isEmpty()) && (!visitedUrls.contains(orgUrl))) {	
			if (isNeedSearch(orgUrl)) {		
				if (isSearchRelevant(orgUrl)) {
					if ((!relevantUrls.contains(orgUrl)) && (!nonRelevantUrls.contains(orgUrl))) {
						myLogger.info("add one relevant url:" + orgUrl);
						relevantUrls.add(orgUrl);
					}
				} 
				else {
					if ((!relevantUrls.contains(orgUrl)) && (!nonRelevantUrls.contains(orgUrl))) {
						myLogger.info("add one nonrelevant url:" + orgUrl);
						nonRelevantUrls.add(orgUrl);
					}
					
				}
			}
		}
	}
	
	private Boolean isNeedSearch(String orgUrl) {
		if(!isTargetWebSite(orgUrl)) {
			return false;
		}
		
		if (orgUrl.endsWith(".jpg") || orgUrl.endsWith(".png")) {
			return false;
		}
		
		return true;
	}
	
	private Boolean isTargetWebSite(String orgUrl) {
		if (hostSymbol.isEmpty()) {
			return true;
		}

		return (orgUrl.toLowerCase().contains("/" + hostSymbol.toLowerCase() + "."))
				|| (orgUrl.toLowerCase().contains("." + hostSymbol.toLowerCase() + "."));
	}

	public Boolean isSearchRelevant(String orgUrl) {
		for (SearchPolicy searchPolicy : searchPolicies) {
			if (isResultsEnough(searchPolicy)) {
				continue;
			}
			
			ArrayList<String> titles = searchPolicy.getSearchTiles();
			for (String title : titles) {
				if (orgUrl.toLowerCase().contains(title.toLowerCase())) {
					return true;
				}
			}
		}

		return false;
	}
	
	Boolean isResultsEnough() {
		for (SearchPolicy policy : searchPolicies) {
			ResultUrlCollection resultUrlCollection = null;
			if (rstUrlCollectMap.containsKey(policy)) {
				resultUrlCollection = rstUrlCollectMap.get(policy);
			}
			else {
				return false;
			}
			
			if (policy.getSearchScope() > resultUrlCollection.size()) {
				return false;
			}
		}
		
		myLogger.info("find enough result");
		return true;
	}
	
	
	Boolean isResultsEnough(SearchPolicy inPolicy) {

		ResultUrlCollection resultUrlCollection = null;
		if (rstUrlCollectMap.containsKey(inPolicy)) {
			resultUrlCollection = rstUrlCollectMap.get(inPolicy);
		} else {
			return false;
		}
		
		return inPolicy.getSearchScope() == resultUrlCollection.size();
	}
	
	private void initRstCollection() {
		for (SearchPolicy policy : searchPolicies) {
			if (!rstUrlCollectMap.containsKey(policy)) {
				ResultUrlCollection resultUrlCollection = new ResultUrlCollection();
				rstUrlCollectMap.put(policy, resultUrlCollection);
			}
		}
	}
		
	private void addRelatedUrl(String curUrl, Document doc) {
		if (excludedSites.contains(curUrl)) {
			return;
		}
		
		Elements titles = doc.select("title");
		
		for (Element title : titles) {
			String titleTxt = title.text().trim();
			
			for (SearchPolicy policy : searchPolicies) {
				ResultUrlCollection resultUrlCollection = null;
				if (rstUrlCollectMap.containsKey(policy)) {
					resultUrlCollection = rstUrlCollectMap.get(policy);
				}
				else {
					resultUrlCollection = new ResultUrlCollection();
					rstUrlCollectMap.put(policy, resultUrlCollection);
				}

				if (resultUrlCollection.size() == policy.getSearchScope()) {
					continue;
				}

				ArrayList<String> searchTitles = policy.getSearchTiles();
				for (String searchTile : searchTitles) {
					if (isPageTitleRelated(titleTxt, searchTile)) {
						ResultUrlCollection.ResultUrl resultUrl = new ResultUrlCollection.ResultUrl();
						resultUrl.setFullUrl(curUrl);
						resultUrl.setTitle(titleTxt);

						resultUrlCollection.addResultUrl(resultUrl);
						myLogger.info("search title:" + searchTile + ", add one related url. url:"+curUrl+", title:"+titleTxt);
						return;
					}
				}
			}
		}
	}
	
    private Boolean isPageTitleRelated(String pageTitle, String searchTitle) { 
		String regexStr = "\\b" + searchTitle + "\\b";
		Pattern pattern = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageTitle); 	

    	return matcher.find();
    }

	private void testWork() {
		SearchPolicy clintonPolicy = new SearchPolicy();
		clintonPolicy.setSearchScope(25);
		clintonPolicy.addSearchTitle("Clinton");
		
		ArrayList<SearchPolicy> clintonPolicies = new ArrayList<>();
		clintonPolicies.add(clintonPolicy);
		ArrayList<String> excludeSite = new ArrayList<>();
		HashMap<SearchPolicy, ResultUrlCollection> rstClintonCollection =
				work("http://www.cnn.com", clintonPolicies, excludeSite);
        
        ArrayList<String> excludeSite1 = new ArrayList<>();
        ResultUrlCollection clintonRst = rstClintonCollection.get(clintonPolicy);
        for (ResultUrlCollection.ResultUrl rstUrl : clintonRst.getResultUrls()) {
        	excludeSite1.add(rstUrl.getFullUrl());
		}
        
        myLogger.info("excludeSite1 size:" + excludeSite1.size());
        System.out.println("excludeSite1 size:" + excludeSite1.size());

		SearchPolicy trumpPolicy = new SearchPolicy();
		trumpPolicy.setSearchScope(25);
		trumpPolicy.addSearchTitle("Trump");
		
		ArrayList<SearchPolicy> trumpPolicies = new ArrayList<>();
		trumpPolicies.add(trumpPolicy);
		work("http://www.cnn.com", trumpPolicies, excludeSite1);
	}

	static void sleep(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Spider s = new Spider();
		s.testWork();
		
		//s.addUrfFromRegex("", "{\"uri\":\"/ddd\",dfsdfdsdf,,,{\"uri\":\"/ddl\",");

	}

}
