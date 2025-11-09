package py.com.base.clases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

public class SpringProducer {


    private JmsTemplate template;
    private Destination destination;
    private int messageCount = 10;

    public void start() throws JMSException {
    	
        for (int i = 0; i < messageCount; i++) {
            final String text = "Text for message: " + i;
            template.send(destination, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {

                    TextMessage message = session.createTextMessage(text);
                    message.setStringProperty("next", "foo");
                    return message;
                }
            });
        }
        
    }

    public void stop() throws JMSException {
    }



    public JmsTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
    
}
