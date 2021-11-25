package io.qiot.manufacturing.edge.machinery.util.producer;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import io.qiot.manufacturing.edge.machinery.service.machinery.MachineryService;

/**
 * @author andreabattaglia
 *
 */
@Singleton
public class AMQConnectionFactoryProducer {

    @Inject
    Logger LOGGER;

    @ConfigProperty(name = "qiot.artemis.sslEnabled", defaultValue = "false")
    boolean sslEnabled;

    @ConfigProperty(name = "qiot.artemis.url")
    String url;

    @ConfigProperty(name = "qiot.artemis.username")
    String username;

    @ConfigProperty(name = "qiot.artemis.password")
    String password;

    @ConfigProperty(name = "qiot.artemis.protocol")
    String protocol;

    @ConfigProperty(name = "qiot.artemis.host")
    String host;

    @ConfigProperty(name = "qiot.artemis.port")
    int port;

    @ConfigProperty(name = "qiot.artemis.verifyHost")
    boolean verifyHost;

    @ConfigProperty(name = "qiot.artemis.trustAll")
    boolean trustAll;

    @ConfigProperty(name = "qiot.artemis.keyStoreProvider")
    String keyStoreProvider;

    @ConfigProperty(name = "qiot.artemis.keyStorePath")
    String keyStorePath;

    @ConfigProperty(name = "qiot.artemis.keyStorePassword")
    String keyStorePassword;

    @ConfigProperty(name = "qiot.artemis.trustStoreProvider")
    String trustStoreProvider;

    @ConfigProperty(name = "qiot.artemis.trustStorePath")
    String trustStorePath;

    @ConfigProperty(name = "qiot.artemis.trustStorePassword")
    String trustStorePassword;

    private ActiveMQConnectionFactory connectionFactory;

    @ConfigProperty(name = "qiot.artemis.jndi")
    String jndi;
    
    InitialContext initialContext = null;
    
    @Inject
    MachineryService machineryService;

    private ReadWriteLock readWriteLock;
    private final Lock readLock;
    private final Lock writeLock;

    public AMQConnectionFactoryProducer() {
        this.readWriteLock = new ReentrantReadWriteLock();
        this.readLock = this.readWriteLock.readLock();
        this.writeLock = this.readWriteLock.writeLock();
    }

    @PostConstruct
    void init() {
        writeLock.lock();
        try {
            if (sslEnabled) {
                LOGGER.info("SSL ENABLED");
                // connectionFactory = initSSLConnectionFactory();
                Hashtable<Object, Object> env = new Hashtable<>();

                env.put("java.naming.factory.initial",
                        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
//                env.put("connectionFactory.ConnectionFactory", //
//                        protocol + "://" + host + ":" + port//
//                                + "?ha=false"//
//                                + "&retryInterval=1000"//
//                                + "&retryIntervalMultiplier=1.0"//
//                                + "&reconnectAttempts=-1"//
//                                + "&sslEnabled=true"//
//                                + "&useTopologyForLoadBalancing=false"//
//                                + "&verifyHost=false"//
//                                + "&trustAll=" + Boolean.toString(trustAll)//
//                                + "&keyStoreProvider=" + keyStoreProvider//
//                                + "&keyStorePath=" + keyStorePath//
//                                + "&keyStorePassword=" + keyStorePassword//
//                                + "&trustStoreProvider=" + trustStoreProvider//
//                                + "&trustStorePath=" + trustStorePath//
//                                + "&trustStorePassword=" + trustStorePassword);//

              env.put("connectionFactory.ConnectionFactory", jndi);
                
                initialContext = new InitialContext(env);

                // Step 3. Look-up a JMS Connection Factory object from JNDI on
                // server 0
                connectionFactory = (ActiveMQConnectionFactory) initialContext
                        .lookup("ConnectionFactory");
            } else {
                LOGGER.info("SSL DISABLED");
                connectionFactory = new ActiveMQConnectionFactory(url, username,
                        password);
            }
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @PreDestroy
    void destroy() {
        writeLock.lock();
        try {
            initialContext.close();
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

//    private ActiveMQConnectionFactory initSSLConnectionFactory() {
//
//        HashMap<String, Object> map = new HashMap<>();
//
//        // ./artemis producer --destination foo --user amq --password amq
//        // --message-count 10
//        // --url="tcp://broker-service-edge-0-svc-rte-factory.apps.manufacturingfacility.qiot.io:443?sslEnabled=true;keyStorePath=./client.ks;keyStorePassword=password;trustStorePath=./client.ts;trustStorePassword=password"
//
//        // ./artemis consumer --destination foo --user amq --password amq
//        // --url="tcp://broker-service-edge-0-svc-rte-factory.apps.manufacturingfacility.qiot.io:443?sslEnabled=true;keyStorePath=./client.ks;keyStorePassword=password;trustStorePath=./client.ts;trustStorePassword=password"
//
//        // map.put(TransportConstants.PROTOCOLS_PROP_NAME, "CORE");
//        // host
//        map.put(TransportConstants.HOST_PROP_NAME, host);
//        // port
//        map.put(TransportConstants.PORT_PROP_NAME, port);
//        // sslEnabled
//        map.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
//        // verifyHost
//        map.put(TransportConstants.VERIFY_HOST_PROP_NAME, false);
//        // trustAll
//        map.put(TransportConstants.TRUST_ALL_PROP_NAME, true);
//        // keyStoreProvider
//        map.put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME,
//                keyStoreProvider);
//        // keyStorePath
//        map.put(TransportConstants.KEYSTORE_PATH_PROP_NAME, keyStorePath);
//        // keyStorePassword
//        map.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME,
//                keyStorePassword);
//        // trustStoreProvider
//        map.put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME,
//                trustStoreProvider);
//        // trustStorePath
//        map.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, trustStorePath);
//        // useTopologyForLoadBalancing
//        map.put("useTopologyForLoadBalancing", false);
//        map.put("retryInterval", 1000);
//        map.put("retryIntervalMultiplier", 1.0d);
//        map.put("reconnectAttempts", -1);
//
//        TransportConfiguration tc = new TransportConfiguration(
//                NettyConnectorFactory.class.getName(), map);
//
//        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(false, tc);
//        return cf;
//    }

    @Produces
    public ActiveMQConnectionFactory produce() {
        readLock.lock();
        try {
            return connectionFactory;
        } finally {
            readLock.unlock();
        }
    }
}
