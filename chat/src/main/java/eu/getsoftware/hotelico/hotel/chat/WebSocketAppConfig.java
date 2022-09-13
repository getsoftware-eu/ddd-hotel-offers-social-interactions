//package eu.getsoftware.hotelico.chat;
//
//import java.util.List;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.converter.MessageConverter;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.joda.JodaModule;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketAppConfig extends AbstractWebSocketMessageBrokerConfigurer {
//    
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        //CHAT BROKER (/topic/app)
//        
//        String[] brokers = {};
//        config.enableSimpleBroker("/walltopic", "/chattopic", "/activitytopic", "/notificationtopic");
//        
//        config.setApplicationDestinationPrefixes("/app");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        
//		//TODO Eugen: maybe a simple pseudo-path 'notify' benutzen?
//		registry.addEndpoint("/notify").withSockJS();
//        
//		//Eugen: /notification is a pseudo-controller, only for sending info. Not for receiving!!!!
//		
//        //Eugen: Path to controller methode (SOCKET URL)
//        registry.addEndpoint("/wall/wall").withSockJS();
////        registry.addEndpoint("/activity/activity").withSockJS();
//        registry.addEndpoint("/notification/notification").withSockJS();
//        registry.addEndpoint("/notification/activity").withSockJS();
//        registry.addEndpoint("/chat/chat").withSockJS();
//
//    }
//
//    @Override
//    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
//        MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
//        messageConverters.add(mappingJackson2MessageConverter);
//
//        ObjectMapper objectMapper = mappingJackson2MessageConverter.getObjectMapper();
//        objectMapper.registerModule(new JodaModule());
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//
//        return super.configureMessageConverters(messageConverters);
//    }
//}