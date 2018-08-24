package org.axonframework.cdi;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.inject.spi.WithAnnotations;

import org.axonframework.cdi.stereotype.Aggregate;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main CDI extension class responsible for collecting CDI beans and setting up
 * Axon configuration.
 */
public class AxonCdiExtension implements Extension {

    private static final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    private Configuration configuration;

    private final List<Class<?>> aggregates = new ArrayList<>();
    private final List<Bean<?>> eventHandlers = new ArrayList<>();

    private Producer<EventStorageEngine> eventStorageEngineProducer;
    private Producer<Serializer> serializerProducer;
    private Producer<EventBus> eventBusProducer;
    private Producer<CommandBus> commandBusProducer;
    // TODO this should be a list of producers
    private Producer<EventHandlingConfiguration> eventHandlingConfigurationProducer;
    private Producer<Configurer> configurerProducer;
    private Producer<TransactionManager> transactionManagerProducer;
    private Producer<EntityManagerProvider> entityManagerProviderProducer;
    private Producer<TokenStore> tokenStoreProducer;

    // Antoine: Many of the beans and producers I am processing may use
    // container resources such as entity managers, etc. I believe this means
    // I should be handling them as late as possible to avoid initialization
    // timing issues. Right now things are processed as they make
    // "semantic" sense. Do you think this could be improved to do the same
    // processing later?
    /**
     * Scans all annotated types with the {@link Aggregate} annotation and
     * collects them for registration.
     *
     * @param processAnnotatedType annotated type processing event.
     */
    <T> void processAggregate(@Observes @WithAnnotations({Aggregate.class})
            final ProcessAnnotatedType<T> processAnnotatedType) {
        final Class<?> clazz = processAnnotatedType.getAnnotatedType().getJavaClass();

        logger.debug("Found Aggregate: {}.", clazz);

        aggregates.add(clazz);
    }

    // Antoine: While processing the producers, I can detect configuration
    // errors from an Axon standpoint. These errors should result in the
    // deployment failing. Should I wait to throw these validation errors until
    // later or should I do it right now during annotation scanning? Is there a
    // specific type of exception that's better to throw or will any runtime
    // exception do?
    /**
     * Scans for an event storage engine producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processEventStorageEngineProducer(
            @Observes final ProcessProducer<T, EventStorageEngine> processProducer,
            final BeanManager beanManager) {
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for EventStorageEngine found: {}.",
                processProducer.getProducer());

        this.eventStorageEngineProducer = processProducer.getProducer();
    }

    /**
     * Scans for a configurer producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processConfigurerProducer(
            @Observes final ProcessProducer<T, Configurer> processProducer,
            final BeanManager beanManager) {
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for Configurer found: {}.", processProducer.getProducer());

        this.configurerProducer = processProducer.getProducer();
    }

    /**
     * Scans for a transaction manager producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processTransactionManagerProducer(
            @Observes final ProcessProducer<T, TransactionManager> processProducer,
            final BeanManager beanManager) {
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for TransactionManager found: {}.",
                processProducer.getProducer());

        this.transactionManagerProducer = processProducer.getProducer();
    }

    /**
     * Scans for a serializer producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processSerializerProducer(
            @Observes final ProcessProducer<T, Serializer> processProducer,
            final BeanManager beanManager) {
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for Serializer found: {}.", processProducer.getProducer());

        this.serializerProducer = processProducer.getProducer();
    }

    /**
     * Scans for an event handling configuration producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processEventHandlingConfigurationProducer(
            @Observes final ProcessProducer<T, EventHandlingConfiguration> processProducer,
            final BeanManager beanManager) {
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for EventHandlingConfiguration found: {}.",
                processProducer.getProducer());

        this.eventHandlingConfigurationProducer = processProducer.getProducer();
    }

    /**
     * Scans for an event bus producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processEventBusProducer(
            @Observes final ProcessProducer<T, EventBus> processProducer,
            final BeanManager beanManager) {
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for EventBus found: {}.", processProducer.getProducer());

        this.eventBusProducer = processProducer.getProducer();
    }

    /**
     * Scans for a command bus producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processCommandBusProducer(
            @Observes final ProcessProducer<T, CommandBus> processProducer,
            final BeanManager beanManager) {
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for CommandBus found: {}.",
                processProducer.getProducer());

        this.commandBusProducer = processProducer.getProducer();
    }

    /**
     * Scans for an entity manager provider producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processEntityManagerProviderProducer(
            @Observes final ProcessProducer<T, EntityManagerProvider> processProducer,
            final BeanManager beanManager) {
        // TODO Investigate if there is a way to look up the entity manager
        // from the environment. There likely isn't.
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for EntityManagerProvider found: {}.",
                processProducer.getProducer());

        this.entityManagerProviderProducer = processProducer.getProducer();
    }

    /**
     * Scans for a token store producer.
     *
     * @param processProducer process producer event.
     * @param beanManager bean manager.
     */
    <T> void processTokenStoreProducer(
            @Observes final ProcessProducer<T, TokenStore> processProducer,
            final BeanManager beanManager) {
        // TODO Handle multiple producer definitions.

        logger.debug("Producer for TokenStore: {}.", processProducer.getProducer());

        this.tokenStoreProducer = processProducer.getProducer();
    }

    /**
     * Scans all beans and collects beans with {@link EventHandler} annotated
     * methods.
     *
     * @param processBean bean processing event.
     */
    <T> void processBean(@Observes final ProcessBean<T> processBean) {
        final Bean<?> bean = processBean.getBean();

        if (CdiUtilities.hasAnnotatedMethod(bean, EventHandler.class)) {
            logger.debug("Found event handler {}.", bean.getBeanClass().getSimpleName());

            eventHandlers.add(bean);
        }
    }

    /**
     * Registration of Axon components in CDI registry.
     *
     * @param afterBeanDiscovery after bean discovery event.
     * @param beanManager bean manager.
     */
    void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery,
            final BeanManager beanManager) {
        logger.info("Starting Axon Framework configuration.");

        // Configurer registration.
        final Configurer configurer;

        if (this.configurerProducer != null) {
            // Antoine: Is createCreationalContext(null) is correct here?
            // If not, what should I do instead? Again, many of these things
            // may be indirectly referencing container resources.
            configurer = this.configurerProducer.produce(
                    beanManager.createCreationalContext(null));
        } else {
            configurer = DefaultConfigurer.defaultConfiguration();
        }

        // Entity manager provider registration.
        if (this.entityManagerProviderProducer != null) {
            final EntityManagerProvider entityManagerProvider
                    = this.entityManagerProviderProducer.produce(
                            beanManager.createCreationalContext(null));

            logger.info("Registering entity manager provider {}.",
                    entityManagerProvider.getClass().getSimpleName());

            configurer.registerComponent(EntityManagerProvider.class,
                    c -> entityManagerProvider);
        }

        // Serializer registration.
        if (this.serializerProducer != null) {
            final Serializer serializer = this.serializerProducer.produce(
                    beanManager.createCreationalContext(null));

            logger.info("Registering serializer {}.",
                    serializer.getClass().getSimpleName());

            configurer.configureSerializer(c -> serializer);
        }

        // Transaction manager registration.
        if (this.transactionManagerProducer != null) {
            final TransactionManager transactionManager
                    = this.transactionManagerProducer.produce(
                            beanManager.createCreationalContext(null));

            logger.info("Registering transaction manager {}.",
                    transactionManager.getClass().getSimpleName());

            configurer.configureTransactionManager(c -> transactionManager);
        }

        // Command bus registration.
        if (this.commandBusProducer != null) {
            final CommandBus commandBus = this.commandBusProducer.produce(
                    beanManager.createCreationalContext(null));

            logger.info("Registering command bus {}.",
                    commandBus.getClass().getSimpleName());

            configurer.configureCommandBus(c -> commandBus);
        } else {
            logger.info("No command bus producer found, using default simple command bus.");
        }

        // Event handling configuration registration.
        final EventHandlingConfiguration eventHandlerConfiguration;

        // TODO Attach some logging here.
        if (this.eventHandlingConfigurationProducer != null) {
            eventHandlerConfiguration
                    = this.eventHandlingConfigurationProducer.produce(
                            beanManager.createCreationalContext(null));
        } else {
            eventHandlerConfiguration = new EventHandlingConfiguration();
        }

        // Register event handlers.
        eventHandlers.forEach(eventHandler -> {
            logger.info("Registering event handler {}.",
                    eventHandler.getBeanClass().getName());

            eventHandlerConfiguration.registerEventHandler(
                    c -> eventHandler.create(
                            beanManager.createCreationalContext(null)));
        });

        // Event handler configuration registration.
        configurer.registerModule(eventHandlerConfiguration);

        // Event bus registration.
        if (this.eventBusProducer != null) {
            final EventBus eventBus = this.eventBusProducer.produce(
                    beanManager.createCreationalContext(null));

            logger.info("Registering event bus {}.",
                    eventBus.getClass().getSimpleName());

            configurer.configureEventBus(c -> eventBus);
        }

        // Token store registration.
        if (this.tokenStoreProducer != null) {
            final TokenStore tokenStore = this.tokenStoreProducer.produce(
                    beanManager.createCreationalContext(null));

            logger.info("Registering token store {}.",
                    tokenStore.getClass().getSimpleName());

            configurer.registerComponent(TokenStore.class, c -> tokenStore);
        }

        // Event storage engine registration.
        if (this.eventStorageEngineProducer != null) {
            final EventStorageEngine eventStorageEngine
                    = this.eventStorageEngineProducer.produce(
                            beanManager.createCreationalContext(null));
            logger.info("Registering event storage {}.",
                    eventStorageEngine.getClass().getSimpleName());
            configurer.configureEmbeddedEventStore(c -> eventStorageEngine);
        }

        // Register aggregates.
        aggregates.forEach(aggregate -> {
            logger.info("Registering aggregate {}", aggregate.getSimpleName());
            configurer.configureAggregate(aggregate);
        });

        logger.info("Starting Axon configuration.");
        configuration = configurer.buildConfiguration();
        configuration.start();

        logger.info("Registering Axon APIs with CDI.");
        afterBeanDiscovery.addBean(
                new BeanWrapper<>(Configuration.class, () -> configuration));
        afterBeanDiscovery.addBean(
                new BeanWrapper<>(CommandBus.class, configuration::commandBus));
        afterBeanDiscovery.addBean(
                new BeanWrapper<>(CommandGateway.class, configuration::commandGateway));
        afterBeanDiscovery.addBean(
                new BeanWrapper<>(EventBus.class, configuration::eventBus));

        // If there was no serializer producer, register the serializer as a bean.
        if (this.serializerProducer == null) {
            afterBeanDiscovery.addBean(
                    new BeanWrapper<>(Serializer.class, configuration::serializer));
        }

        logger.info("Axon Framework configuration complete.");
    }

    void beforeShutdown(@Observes @Destroyed(ApplicationScoped.class) final Object event) {
        configuration.shutdown();
    }
}
