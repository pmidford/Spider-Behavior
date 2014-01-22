package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;

public class Publication extends HttpServlet {
	
    String query = 
    		"prefix obo:<http://purl.obolibrary.org/obo/> select ?publication WHERE{?publication rdf:type obo:IAO_0000312 .}";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    		String foo = request.getQueryString();
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
      		File baseDir = new File("/Users/pmidford/temp/sesame/");
			String repositoryId = "test-db";
			Repository repo = null;
			RepositoryConnection con = null;
    		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
    		int pubCount = 0;
    		StringBuffer jsonResult = new StringBuffer(1000);
    		jsonResult.append("{head: [");
            jsonResult.append('"');
    		jsonResult.append("doi");
    		jsonResult.append('"');
    		jsonResult.append("]");
    		jsonResult.append(", results: {bindings: ["); 
    		try {
    			manager.initialize();
    			repo = manager.getRepository(repositoryId);
    		    con = repo.getConnection();
    			  TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);

    			  TupleQueryResult result = tupleQuery.evaluate();
    			  
    			  try {
    					BindingSet bindingSet = result.next();
    					while (result.hasNext()){
    						pubCount++;
    						Value pubId = bindingSet.getValue("publication");
    						if (pubCount > 1){
    							jsonResult.append(",");
    						}
    						jsonResult.append("[");
    						jsonResult.append('"');
    						jsonResult.append(URLEncoder.encode(pubId.stringValue(),"UTF-8"));
    						jsonResult.append('"');
    						jsonResult.append("]");
    						bindingSet = result.next();
    					}
    			  }
    			  finally{
    				  result.close();
    			  }
    		    con.close();
    			repo.shutDown();
    		} catch (RepositoryException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (RepositoryConfigException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		finally {
    			try{
    				con.close();
    				repo.shutDown();
    			}
    			catch (RepositoryException e){
    				System.out.println("Error while trying to close repository");
    				e.printStackTrace();
    			}
    		}
            out.println(jsonResult + " ]}}");
            out.flush();
            out.close();
        }

}
