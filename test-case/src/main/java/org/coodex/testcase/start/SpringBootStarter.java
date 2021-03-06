/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.testcase.start;


import org.coodex.concrete.spring.boot.EnableConcreteJAXRS;
import org.coodex.concrete.support.jsr339.ConcreteJSR339Application;
import org.coodex.concrete.support.websocket.CallerHackConfigurator;
import org.coodex.concrete.support.websocket.ConcreteWebSocketApplication;
import org.coodex.testcase.api.TestCase;
import org.coodex.testcase.api.TestCase2;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;

@SpringBootApplication()
@ImportResource("classpath:testcase.xml")
@EnableConcreteJAXRS(
        servicePackages = "org.coodex.**.api"
)
public class SpringBootStarter {

//    @Bean
//    public AMQPApplication getAMQPApplication() {
//        AMQPConnectionConfig config = new AMQPConnectionConfig();
//        Profile profile = Profile.getProfile("client.amqp");
//        config.setUri(profile.getString("location"));
//        config.setUsername(profile.getString("amqp.username"));
//        config.setPassword(profile.getString("amqp.password"));
//        AMQPApplication amqpApplication = new AMQPApplication(
//                config,
//                profile.getString("amqp.exchangeName"),
//                profile.getString("amqp.queueName"),
//                profile.getLong("amqp.ttl")
//        );
//        amqpApplication.registerClasses(TestCase.class,TestCase2.class);
//        return amqpApplication;
//    }


    @Bean
    public ServletRegistrationBean webSocketServlet() {
        ServletContainer container = new ServletContainer();
        //noinspection unchecked
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                container, "/WebSocket") {

            @Override
            protected ServletRegistration.Dynamic addRegistration(String description, ServletContext servletContext) {
                servletContext.addListener(new ServletContextListener() {

                    @Override
                    public void contextInitialized(ServletContextEvent sce) {
                        final ServerContainer serverContainer = (ServerContainer) sce.getServletContext()
                                .getAttribute("javax.websocket.server.ServerContainer");

                        try {
                            serverContainer.addEndpoint(WebsocketApplication.class);
                        } catch (DeploymentException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void contextDestroyed(ServletContextEvent sce) {

                    }
                });
                return super.addRegistration(description, servletContext);
            }

        };
        registrationBean.setName("demo");
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }



//    @Bean
//    public ConcreteInterceptor mocker() {
//        return new MockInterceptor();
//    }


    public static class JaxRSApplication extends ConcreteJSR339Application {
        public JaxRSApplication() {
            super();
            register(JacksonFeature.class);
            registerPackage();
        }
    }

    @ServerEndpoint(value = "/WebSocket", configurator = CallerHackConfigurator.class)
    public static class WebsocketApplication extends ConcreteWebSocketApplication {
        public WebsocketApplication() {
            super();
            register(TestCase.class,TestCase2.class);
        }
    }

    public static void main(String[] args) {
//        ServiceLoader<BeanProvider> beanProviderServiceLoader = new ServiceLoaderImpl<BeanProvider>() {
//        };
//        System.out.println(beanProviderServiceLoader.getAllInstances());
        SpringApplication.run(SpringBootStarter.class, args);
    }

}
