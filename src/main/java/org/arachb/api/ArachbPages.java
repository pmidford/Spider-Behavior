/**
 *
 */
package org.arachb.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.arachb.api.provider.ArachbPage;
import org.arachb.api.provider.DefaultPage;
import org.arachb.api.provider.MetaDataSummary;
import org.arachb.api.provider.NarrativePage;
import org.arachb.api.provider.PublicationPage;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;


/**
 * @author pmidford
 *
 */
public class ArachbPages extends HttpServlet {

	enum PageType {
		PUBLICATION,
		NARRATIVE,
		ONTOLOGY,
		TAXON,
		INDIVIDUAL,
		UNKNOWN;
	}

	//needs to extract target uri from request,
	//figure out what class this is or what it is an instance of
	//for publication individuals, find an rdfs:comment with the json record, parse, return or format
	//for taxon individuals - find parent, maybe some new comments


	final static String ARACHBPREFIX = "http://arachb.org";
	final static String TOSTRIP = "/spider-behavior";

	private static final long serialVersionUID = 1L;

	private final static File BASEDIR = new File(Util.ADUNAHOME);

	private static Logger log = Logger.getLogger(ArachbPages.class);


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {


		final ServletOutputStream os = response.getOutputStream();

		boolean writeHTML;
		String target;
		String[] targetComponents = request.getRequestURI().substring(TOSTRIP.length()).split("\\.");
		if (targetComponents.length == 0 || targetComponents.length > 2){
			final PrintStream ps = new PrintStream(response.getOutputStream());
			response.setStatus(500);
			ps.printf("Dubious request %s", Arrays.toString(targetComponents));
			log.error(String.format("Dubious request %s", Arrays.toString(targetComponents)));
		}
		else{
			if (System.getProperty("user.name").equals("pmidford")){  //debugging hack
				target = ARACHBPREFIX + "/arachb/ARACHB_0" + targetComponents[0];
			}
			else{
				target = ARACHBPREFIX + targetComponents[0];
			}
			log.info("User is " + System.getProperty("user.name"));
			log.info("target = " + target);
			if (targetComponents.length == 2){
				if ("json".equalsIgnoreCase(targetComponents[1])){
					writeHTML = false;
				}
				else {
					writeHTML = true;
				}
			}
			else {
				writeHTML = true;
			}
			Repository repo = null;
			RepositoryConnection con = null;
			final LocalRepositoryManager manager = new LocalRepositoryManager(BASEDIR);
			try {
				manager.initialize();
				repo = manager.getRepository(Util.REPONAME);
				con = repo.getConnection();
				final StringBuilder msgBuffer = new StringBuilder();
				List<String> commentStrings = getComments(target, con, response);
				MetaDataSummary mds = null;
				for (String comment : commentStrings){
					if (jsonCheck(comment)){
						mds = new MetaDataSummary(comment);  //overwrites default
						break;
					}
				}
				if (mds == null){
					mds = new MetaDataSummary();
				}
				if (!mds.contains("localIdentifier")){
					mds.add("localIdentifier", target);
				}

				PageType thisType = identifyPage(target,con);
				ArachbPage thisPage = null;
				switch (thisType){
					case PUBLICATION: {
						thisPage = new PublicationPage(mds);
						break;
					}
					case NARRATIVE: {
						thisPage = new NarrativePage(mds,con,response);
						break;
					}
					case ONTOLOGY: {
						getOntology(os);
						break;
					}
					default:{
						thisPage = new DefaultPage(mds);
					}
				}
				if (thisPage != null){
					if (writeHTML){
						msgBuffer.append(thisPage.generateHTML());
					}
					else {
						msgBuffer.append(thisPage.generatejson());
					}
				}

				response.getOutputStream().write(msgBuffer.toString().getBytes("UTF-8"));

			} catch (RepositoryException e) {  //TODO - make these return meaningful JSON strings
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(500);
				response.getOutputStream().write(e.getMessage().getBytes("UTF-8"));
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
				cleanupResources(repo,con);
			}
		}
		os.flush();
		os.close();
	}


	/**
	 *
	 * @param target
	 * @return
	 */
	private PageType identifyPage(String target, RepositoryConnection con)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		if (ontologyTest(target)){
			return PageType.ONTOLOGY;
		}
		String publicationTest = getURI2PublicationTest(target);
		if (checkType(publicationTest,con))
			return PageType.PUBLICATION;
		String narrativeTest = getURI2NarrativeTest(target);
		if (checkType(narrativeTest,con))
			return PageType.NARRATIVE;
		//String taxonTest = getURI2TaxonTest(target);
		return PageType.UNKNOWN;
	}

	private boolean checkType(String testQuery, RepositoryConnection con)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		log.info("Query String is: " + testQuery);
		boolean result = Util.askQuery(testQuery, con);
		log.info("Result is " + Boolean.toString(result));
		return result;
	}



	/**
	 * @param target
	 * @param con
	 * @param commentString
	 * @return
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	private List<String> getComments(String target, RepositoryConnection con, HttpServletResponse response)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		final String commentQueryString = getURI2CommentQuery(target);
		log.info("Query String is: " + commentQueryString);
		List<String> results = new ArrayList<>();
		ResultTable resultTable = new ResultTable(response);
		resultTable.tryAndAccumulateQueryResult(commentQueryString, con);
		if (!resultTable.isEmpty()){
			for (TupleQueryResult tqr : resultTable.getContents()){
				while (tqr.hasNext()){
					BindingSet bs = tqr.next();
					Binding b = bs.getBinding("comment");
					if (b != null){
						Value v = b.getValue();
						results.add(v.stringValue().trim());
					}
				}
			}
		}
		return results;
	}


	private boolean jsonCheck(String comment){
		return (comment != null) &&
				!comment.isEmpty() &&
				(comment.charAt(0) == '{') &&
				(comment.charAt(comment.length()-1) == '}');
	}


	String getURI2CommentQuery(String uriStr){
		SparqlBuilder b = SparqlBuilder.startSparql();
		b.addText("SELECT ?comment %n");
		String line2 = String.format("WHERE {<%s> rdfs:comment ?comment . } %n", uriStr);
		b.addText(line2);
		return b.finish();
	}

	//TODO: figure out where the .owl extension disappears
	final private String ontologyURI = "http://arachb.org/arachb/arachb";
	boolean ontologyTest(String target){
		return ontologyURI.equals(target);
	}

	/**
	 * the start of the service for http://arachb.org/arachb/arachb.owl
	 * @throws IOException
	 */
	void getOntology(ServletOutputStream os) throws IOException{
		BufferedReader inputReader = null;
		try {
			URL loadURL = ArachbPages.class.getClassLoader().getResource("arachb.owl");
			URLConnection loadConnection = loadURL.openConnection();
			InputStream ontStr = loadConnection.getInputStream();
			inputReader= new BufferedReader(new InputStreamReader(ontStr));
            String l;
            while ((l = inputReader.readLine()) != null) {
                os.println(l);
            }

		}
		finally {
			if (inputReader != null){
				inputReader.close();
			}
		}
	}

	String getURI2PublicationTest(String target){
		SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addText(String.format("ASK WHERE {<%s> rdf:type obo:IAO_0000311 . } %n", target));
		return b.finish();
	}

	String getURI2NarrativeTest(String target){
		SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addText(String.format("ASK WHERE {<%s> rdf:type obo:IAO_0000030 . %n", target));
		b.addText(String.format("<%s> obo:BFO_0000050 ?a . %n", target));
		b.addText(String.format("?b obo:BFO_0000050 <%s> . } %n", target));
		return b.finish();
	}

	String getURI2TaxonTest(String target){
		SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addText(String.format("ASK WHERE {<%s> rdf:type obo:NCBI_Taxon1 . } %n", target));
		return b.finish();
	}




	//cleanup methods

	private void cleanupResources(Repository repo, RepositoryConnection con){
		try{
			if (con != null){
				con.close();
			}
			if (repo != null){
				repo.shutDown();
			}
		}
		catch (NullPointerException e){
			System.out.println("Error trying to close null repository");
			e.printStackTrace();
		}
		catch (RepositoryException e){
			System.out.println("Error while trying to close repository");
			e.printStackTrace();
		}
	}


}

