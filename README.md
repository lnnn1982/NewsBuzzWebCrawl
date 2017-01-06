# NewsBuzzWebCrawl
A single web crawl program based on Tomcat

This project is composed of two parts: java web crawler and Tomcat web server. 
Java Web Crawler
-------
Structure
<br/><br/>
This web crawler is implemented in Spider.java.
<br/><br/>
It uses HttpClient to get webPage content, jsoup and regular expressions to parse the html content. Everytime when it parses a web page, the process extracts urls of the current webpage for next search. Breadth search is used in this web crawler, however to make the search faster, the crawler processes relevant websites first, whose urls include the searched information. The program puts relevant urls to a separate container to make sure these urls are first to be processed.
<br/><br/>
To extracted urls from one web page, this crawler parses html href attrubute and the url infomation in java scripts. At first, it only extracts urls of href attritutes and misses lots of meaningful urls in Js segment. After correcting this problem, the crawler could find the targeted infomation in a few seconds.  
<br/><br/>
Performance
<br/><br/>
The original design is to do web crawling once with several serch targets. For example, it could search the website once to get both information of Trump and Clinton with less time. However this is not the case. Trump has much more relevant webpages than Clinton now, and the crawler will finish Trump soon, but still with a number of Trump relevant urls as first priority to search, which actually have no relation with Clinton. And this causes the search for Clinton much slower. So in the program, Trump and Clinton information are searched seperately.
<br/><br/>
Since Trump has more covered webpages, to get 25 relevant webpages, the crawler will visite nearly 30 different urls in about 10 seconds; while for Clinton, it usually visits more than 80 different urls to get 25 webpages with more than 20 seconds. If the program was executed several times, the later performance is much better than the first one. The performance is not satisfying and needs to be optimised. The webpage parsing could be optimised by removing jsoup and only using Java regular expression. For further optimization, more tests are needed to find which part caused the low performance.
<br/><br/>
Test
<br/><br/>
Because of less time to do unit test, I wrote test code in the main function and recored all the results and important steps in the log file(spider.log in the current directory). 
