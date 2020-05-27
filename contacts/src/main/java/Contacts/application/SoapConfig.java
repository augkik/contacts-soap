package Contacts.application;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import javax.xml.bind.annotation.XmlSchema;

@EnableWs
@Configuration
public class SoapConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext context){
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean(servlet, "/SoapContacts/*");
    }

    @Bean
    public XsdSchema contactSchema(){
        return new SimpleXsdSchema(new ClassPathResource("contacts.xsd"));
    }

    @Bean(name = "contacts")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema contactsSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("ContactsPort");
        wsdl11Definition.setLocationUri("/SoapContacts");
        wsdl11Definition.setTargetNamespace("http://www.example.com/contacts");
        wsdl11Definition.setSchema(contactsSchema);
        return wsdl11Definition;
    }
}
