import java.io.IOException;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

public class Main {

	public static void main(String[] args) {
		try {
			RepositoryService service = new RepositoryService();
			for (Repository repo : service.getRepositories("yungene"))
				System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
