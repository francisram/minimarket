package py.com.base.clases;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import py.com.base.utils.AppConfig;

@Configuration
public class JmsConfig {
    
    static String destination = "Consumer.RIO";


    @Bean
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(AppConfig.BROKER_URL);
        activeMQConnectionFactory.setUserName(AppConfig.AMQ_USER);
        activeMQConnectionFactory.setPassword(AppConfig.AMQ_PASSWORD);
        return activeMQConnectionFactory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(senderActiveMQConnectionFactory());
        return cachingConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
        jmsTemplate.setPubSubDomain(false); 
        return jmsTemplate;
    }
}
