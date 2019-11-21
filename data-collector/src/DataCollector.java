import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

public class DataCollector {
	public static void main(String[] args) {
		
		// Database access
		
		try(Connection conn = MySQLJDBCUtil.getConnection()){
			 System.out.println(String.format("Connected to database %s "
	                    + "successfully.", conn.getCatalog()));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		String token = readToken();
		GitHubClient client = new GitHubClient().setOAuth2Token(token);
		RepositoryService service = new RepositoryService(client);
//		try {
//			for (Repository repo : service.getRepositories("yungene")) {
//				if(!repo.getOwner().getLogin().equals("yungene")) {
//					continue;
//				}
//				System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
//				//Repository repo = service.getRepository("yungene", "OnlineJudges");
//				CommitService commServ = new CommitService(client);
//				 PageIterator<RepositoryCommit> pageIter = commServ.pageCommits(repo);
//				 Collection<RepositoryCommit> coll = pageIter.next();
//				 for(RepositoryCommit commit: coll) {
//					 RepositoryCommit commitFull = new CommitService(client).getCommit(repo, commit.getSha());
//					 if(commitFull.getStats()!= null) {
//					 System.out.println("Total is " + commitFull.getStats().getTotal());
//					 }
//				 }
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	private static String readToken() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("config/config.txt")));
			String token = br.readLine();
			return token;
		} catch (IOException e) {
			//System.err.printf("Not able to open and/or read OAuth token from ../config/config.txt. Using empty token instead.");
			e.printStackTrace();
			return "";
		}
	}
}
