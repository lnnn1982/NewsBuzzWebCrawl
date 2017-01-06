# NewsBuzzWebCrawl
A single web crawl program based on Tomcat

Structure
-------  
This project is composed of two parts: java web crawler and Tomcat web server. 
<br/><br/>
Web spider is implemented in Spider.java.
<br/>
it uses HttpClient to get webPage content, jsoup and regular expressions to parse the html content. Everytime when it parses a web page, the process extracts urls of the current webpage for next search. Breadth search is used in this web crawler, however to make the search faster, the crawler processes relevant websites first, whose urls include the searched information. The program puts relevant urls to a separate container to make sure the relevant urls are first to be processed. To extracted urls from one web page, this crawler parses html href attrubute and the url infomation in java scripts. At first, it only extracts urls of href attritutes and misses lots of meaningful urls in Js segment. After correcting this problem, the crawler could find the targeted infomation.  
