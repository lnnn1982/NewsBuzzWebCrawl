package com.NewBuzz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.NewBuzz.Spider.*;

/**
 * Servlet implementation class SearchNews
 */
@WebServlet("")
public class SearchNews extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Spider webSpider = new Spider();
	
	private static final int searchPartnerWebpageNums = 25;
	private static final int searchOpponantWebpageNums = 25;
	private static final String searchWebSiteName = "http://www.cnn.com";
	private static final String partnerSearchTitle = "Clinton";
	private static final String opponantSearchTitle = "Trump";
    
	/**
     * @see HttpServlet#HttpServlet()
     */
    public SearchNews() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());

        getSearchInfo(request);
	
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		RequestDispatcher d = request.getRequestDispatcher("/TrumpVsClinton.jsp");
		d.forward(request, response);
	}
	
	private void getSearchInfo(HttpServletRequest request)
	{
		SearchPolicy partnerPolicy = new SearchPolicy();
		partnerPolicy.setSearchScope(searchPartnerWebpageNums);
		partnerPolicy.addSearchTitle(partnerSearchTitle);
		ArrayList<SearchPolicy> partnerPolicies = new ArrayList<>();
		partnerPolicies.add(partnerPolicy);
		ArrayList<String> partnerExcludeSite = new ArrayList<>();
		
		HashMap<SearchPolicy, ResultUrlCollection> partnerRstCollection =
				webSpider.work(searchWebSiteName, partnerPolicies, partnerExcludeSite);
        
        ArrayList<String> opponentExcludeSite = new ArrayList<>();
        ResultUrlCollection partnerRst = partnerRstCollection.get(partnerPolicy);
        if (partnerRst != null) {
            for (ResultUrlCollection.ResultUrl rstUrl : partnerRst.getResultUrls()) {
            	opponentExcludeSite.add(rstUrl.getFullUrl());
    		}
		}

        System.out.println("opponentExcludeSite size:" + opponentExcludeSite.size());

		SearchPolicy opponentPolicy = new SearchPolicy();
		opponentPolicy.setSearchScope(searchOpponantWebpageNums);
		opponentPolicy.addSearchTitle(opponantSearchTitle);
		
		ArrayList<SearchPolicy> opponentPolicies = new ArrayList<>();
		opponentPolicies.add(opponentPolicy);
		HashMap<SearchPolicy, ResultUrlCollection> opponentRstCollection = 
				webSpider.work(searchWebSiteName, opponentPolicies, opponentExcludeSite);
	
		setRequestAttribute(partnerRst, opponentRstCollection.get(opponentPolicy), request);
	}
	
	private void setRequestAttribute(ResultUrlCollection partnerRst,
			ResultUrlCollection opponentRst, HttpServletRequest request) 
	{
		request.setAttribute("clintonRst", partnerRst);
		request.setAttribute("trumpRst", opponentRst);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
