package elcom.com.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Michael Hunger
 * @author Mark Angrish
 * @author Michael J. Simons
 */
//@EnableJpaRepositories(basePackages = {"elcom.com.neo4j.repositoryPostgre"})
//@EntityScan("com.elcom.itscore.recognition.flink.model")
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
public class LinkObjectApplication {

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("hadoop.home.dir","C:\\hadoop");
        SpringApplication.run(LinkObjectApplication.class, args);
    }
}
