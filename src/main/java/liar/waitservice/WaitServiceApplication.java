package liar.waitservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
public class WaitServiceApplication{

    public static void main(String[] args) {
        SpringApplication.run(WaitServiceApplication.class, args);
    }
}
