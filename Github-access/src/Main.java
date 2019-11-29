import java.io.IOException;
import java.util.Scanner;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

public class Main {

	public static void main(String[] args) {
		try {
			GitHubClient client = new GitHubClient();
			Scanner reader = new Scanner(System.in);
			System.out.print("Input your OAuth token: ");
			String token = reader.next();
			client.setOAuth2Token(token);
			RepositoryService service = new RepositoryService(client);
			for (Repository repo : service.getRepositories())
				System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
