## Apache Kafka

Kafka est un système de messagerie distribué développé par la fondation Apache depuis 2012.
Kafka fonctionne en mode publish-subscirbe et conserve les données qu'il reçoit dans des topics ou catégories de données.
Les systèmes qui publient des données dans Kafka sont appelés des `producers` et ceux qui les lisent sont appelés `consumers`.
Pour plus d'informations, consulter cette [page officielle](https://kafka.apache.org/intro.html).

1. Installation

    1. Environnement

    ```
    root@pl-debian:~# uname -a
    Linux pl-debian 4.9.0-8-amd64 #1 SMP Debian 4.9.110-3+deb9u6 (2018-10-08) x86_64 GNU/Linux
    root@pl-debian:~# lsb_release -a
    No LSB modules are available.
    Distributor ID: Debian
    Description:    Debian GNU/Linux 9.5 (stretch)
    Release:        9.5
    Codename:       stretch
    ```

    2. Télécharger et dézipper Kafka

    ```
    root@pl-debian:~# cd /opt
    root@pl-debian:/opt# wget http://apache.mirrors.benatherton.com/kafka/2.1.0/kafka_2.11-2.1.0.tgz
    root@pl-debian:/opt# tar -zxf kafka_2.11-2.1.0.tgz
    root@pl-debian:/opt# cd kafka_2.11-2.1.0
    ```

    3. Démarrer zookeeper

    ```
    root@pl-debian:/opt/kafka_2.11-2.1.0# bin/zookeeper-server-start.sh config/zookeeper.properties
    ```

    4. Démarrer le serveur kafka

    ```
    root@pl-debian-1:/opt/kafka_2.11-2.1.0# bin/kafka-server-start.sh config/server.properties
    ```

3. [Spring Kafka](https://spring.io/projects/spring-kafka)

[Spring Kafka](https://spring.io/projects/spring-kafka) est un framework de l'écosystème [Spring](https://spring.io/). Il offre une brique logicielle permettant de produire et de lire des données dans un serveur Kafka. Dans l'exemple qui suit, nous allons utiliser 2 applications Spring Boot :
  - le micro-service `users-kafka-producer` écrit dans un topic kafka les informations des
    utilisateurs (création et modification),
  - le micro-service `users-kafka-consumer` lit du topic kafka les informations des utilisateurs.

    1. Création du topic kafka `users-upsert`

    ```
    root@pl-debian:/opt/kafka_2.11-2.1.0# bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic users-upsert
    ```

    2. `users-kafka-producer`

      Le code source de ce micro-service est disponible [ici](https://github.com/el1638en/users-kafka-producer).

      - Configuration de l'adresse du serveur kafka et d'un ProducerFactory générique
        qui définit la stratégie de création des producers

      ```java
          @Configuration
          public class KafkaEventProducerConfig<T> {

        // Configuration de l'adresse du serveur Kafka.
		    @Value(value = "${spring.kafka.bootstrap-servers:localhost:9092}")
		    private String bootStrapServers;

		    @Bean
		    public ProducerFactory<String, T> eventProducerFactory() {
		        Map<String, Object> configProperties = new HashMap<>();
		        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
		        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializerWithJTM.class);
		        return new DefaultKafkaProducerFactory<>(configProperties);
		    }

		    @Bean
		    public KafkaTemplate<String, T> kafkaTemplate() {
		        return new KafkaTemplate<>(eventProducerFactory());
		    }
        }
      ```

      Les objets Java sont sérailisés au format JSON se fait avec `JsonSerializerWithJTM` qui étend de `JsonSerializer` auquel on ajoute le
      module `JavaTimeModule`.

      ```java
          public class JsonSerializerWithJTM<T> extends JsonSerializer<T> {
            public JsonSerializerWithJTM() {
                super();
                objectMapper.registerModule(new JavaTimeModule());
            }
         }
      ```     

      - Producer générique d'event kafka

      Nous allons utiliser un producer générique qui contient le code pour écrire dans
      un topic.

      ```java
          package com.syscom.producer;

		  import java.time.Duration;
		  import java.time.LocalDateTime;
		  import org.slf4j.Logger;
		  import org.slf4j.LoggerFactory;
		  import org.springframework.beans.factory.annotation.Autowired;
		  import org.springframework.kafka.core.KafkaTemplate;
		  import org.springframework.kafka.support.SendResult;
		  import org.springframework.util.concurrent.ListenableFuture;
		  import org.springframework.util.concurrent.ListenableFutureCallback;

          public abstract class AbstractEventProducer<T> {

            private final Logger logger = LoggerFactory.getLogger(AbstractEventProducer.class);

            private String topic;

		    @Autowired
		    private KafkaTemplate<String, T> kafkaTemplate;

		    public AbstractEventProducer(String topic) {
		        this.topic = topic;
		    }

            public void send(String key, T event) {
                doSend(key, event);
            }

            private void doSend(String key, T event) {
              LocalDateTime start = LocalDateTime.now();
              logger.info("Treatment of event {} {}, key {} in the kafka's topic {}.", event, event.getClass(), key, topic);
              ListenableFuture<SendResult<String, T>> future = kafkaTemplate.send(topic, key, event);
	          future.addCallback(new ListenableFutureCallback<SendResult<String, T>>() {
	            @Override
	            public void onSuccess(final SendResult<String, T> sendResultMessage) {
	                LocalDateTime end = LocalDateTime.now();
	                int duration = Duration.between(start, end).getNano() / 1000000;
	                logger.info("Sent Successffuly event {} {}, key {},  with offset {} (duration {} ms).", event,
	                        event.getClass(), key, sendResultMessage.getRecordMetadata().offset(), duration);
	            }

	            @Override
	            public void onFailure(final Throwable throwable) {
	                logger.error("unable to send event {}, key {}.", event, key, throwable);
	            }
	          });
            }

            public String getTopic() {
                return topic;
            }

            public void setTopic(String topic) {
                this.topic = topic;
            }

        }
      ```

      Tout producer d'event Kafka va étendre cette classe générique avec le nom du topic sur lequel il souhaite écrire des données. Le producer des utilisateurs se résume en quelques lignes :

      ```java
      @Component
      public class UserUpsertProducer extends AbstractEventProducer<UserUpsertEvent> {
          public UserUpsertProducer(@Value(value = "${spring.kafka.producer.topic.user.upsert:users-upsert}") String topic) {
                    super(topic);
          }
      }
      ```

      Lors de la création d'un utilisateur, le service `UserService` utilisera le producer `UserUpsertProducer` comme suit :

      ```java
      @Service
      @Transactional
      public class UserServiceImpl implements UserService {

          private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

          @Autowired
          private UserUpsertEventMapper userUpsertEventMapper;

          @Autowired
          private UserUpsertProducer userUpsertProducer;

          @Override
          public void create(User user) throws BusinessException {
            logger.info("Create new user {}", user);
            Assert.notNull(user, "User must not be null");
            List<String> errors = validateUser(user);
            if (!errors.isEmpty()) {
                throw new BusinessException(StringUtils.join(errors, ". "));
            }

            if (userRepository.findByLogin(user.getLogin()) != null) {
                throw new BusinessException("Login already used.");
            }
            userRepository.save(user);

            UserUpsertEvent userUpsertEvent = userUpsertEventMapper.beanToEvent(user);
            userUpsertProducer.send(userUpsertEvent.getKey(), userUpsertEvent);
          }

      }
      ```

      Le service métier `UserService.create(User user)` appelle en fin de traitement le producer `userUpsertProducer.send(userUpsertEvent.getKey(), userUpsertEvent);` pour écrire les données de l'utilisateur dans le topic kafka.

      - Vérification des logs du micro-service `users-kafka-producer`

      ```
        21:17:11 - Treatment of event UserUpsertEvent(name=LEGBA, firstName=Eric, login=EL1638EN@YAHOO.FR, birthDay=2000-07-12) class com.syscom.event.user.UserUpsertEvent, key UserUpsertEvent in the kafka's topic users-upsert.
        21:17:11 - ProducerConfig values:
        acks = 1
        batch.size = 16384
        bootstrap.servers = [localhost:9092]
        buffer.memory = 33554432
        key.serializer = class org.apache.kafka.common.serialization.StringSerializer
        receive.buffer.bytes = 32768
        reconnect.backoff.max.ms = 1000
        reconnect.backoff.ms = 50
        request.timeout.ms = 30000
        retries = 0
        value.serializer = class com.syscom.config.JsonSerializerWithJTM

        21:17:11 - Kafka version : 2.0.1
        21:17:11 - Kafka commitId : fa14705e51bd2ce5
        21:17:11 - Cluster ID: Mr_6_w35TA2Fu-hgRZlFOQ
        21:17:11 - Sent Successffuly event UserUpsertEvent(name=LEGBA, firstName=Eric, login=EL1638EN@YAHOO.FR, birthDay=2000-07-12) class com.syscom.event.user.UserUpsertEvent, key UserUpsertEvent,  with offset 0 (duration 346 ms).
      ```

    3. Micro-service `users-kafka-consumer`

      Le code source de ce micro-service est disponible [ici](https://github.com/el1638en/users-kafka-consumer).

      - Configuration du consumer d'event kafka

      ```java
          @Configuration
          public class EventHandlerConfig {

            @Value(value = "${spring.kafka.consumer.bootstrap-servers:localhost:9092}")
            private String consumerBootStrapServers;

            @Value(value = "${spring.kafka.consumer.group.user.upsert:users-upsert-group-id}")
            private String userUpsertGroupId;

            public Map<String, Object> getProperties() {
                Map<String, Object> properties = new HashMap<>();
                properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
                properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonSerializer.class);
                properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootStrapServers);
                return properties;
            }

            @Bean("userUpsertEventListenerContainerFactory")
            public ConcurrentKafkaListenerContainerFactory<String, UserUpsertEvent> userUpsertEventListenerContainerFactory() {
                Map<String, Object> properties = getProperties();
                properties.put(ConsumerConfig.GROUP_ID_CONFIG, userUpsertGroupId);
                ConsumerFactory<String, UserUpsertEvent> userCreatedEventConsumerFactory = new DefaultKafkaConsumerFactory<>(
                properties, new StringDeserializer(), new JsonDeserializerWithJTM<UserUpsertEvent>());

                ConcurrentKafkaListenerContainerFactory<String, UserUpsertEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
                factory.setConsumerFactory(userCreatedEventConsumerFactory);
                return factory;
            }

         }
      ```

      La désérialisarion des objets se fait avec `JsonDeserializerWithJTM` qui est une extension de `JsonDeserializer` auquel on ajoute le
      module `JavaTimeModule`

      ```java
      public class JsonDeserializerWithJTM<T> extends JsonDeserializer<T> {
          public JsonDeserializerWithJTM() {
                super();
                objectMapper.registerModule(new JavaTimeModule());
                setRemoveTypeHeaders(false);
                this.typeMapper.addTrustedPackages("*");
            }
         }
      ```

      - Consumer générique d'event kafka

      `AbstractEventHandler` contient le code générique pour consommer un event Kafka.

      ```java
          package com.syscom.handler;

          import java.time.Duration;
          import java.time.LocalDateTime;

          import org.slf4j.Logger;
          import org.slf4j.LoggerFactory;

          public abstract class AbstractEventHandler<T> {

            private final Logger logger = LoggerFactory.getLogger(AbstractEventHandler.class);

            public void handleEvent(T event) {
               LocalDateTime start = LocalDateTime.now();
               logger.info("Receive event kafka : {} {}", event, event.getClass());
               processEvent(event);
               LocalDateTime end = LocalDateTime.now();
               logger.info("End treatment of the event {} {} (duration {} ms) ", event, event.getClass(),
                Duration.between(start, end).getNano() / 1000000);

            }

            public abstract void processEvent(T event);

         }
      ```

      Un consumer d'event va étendre cette classe générique, implémenter la méthode abstraite `processEvent(T event)` et ajouter par annotation le nom du topic qu'il souhaite consommer.

      ```java
          package com.syscom.handler.user;

          import org.springframework.beans.factory.annotation.Autowired;
          import org.springframework.kafka.annotation.KafkaListener;
          import org.springframework.stereotype.Component;
          import com.syscom.event.user.UserUpsertEvent;
          import com.syscom.handler.AbstractEventHandler;
          import com.syscom.mapper.user.UserUpsertEventMapper;
          import com.syscom.service.UserService;

          @Component
          public class UserUpsertEventHandler extends AbstractEventHandler<UserUpsertEvent> {

            @Autowired
            private UserUpsertEventMapper userUpsertEventMapper;

            @Autowired
            private UserService userService;

            @Override
            @KafkaListener(topics = "${spring.kafka.consumer.topic.user.upsert:users-upsert}",
                groupId = "${spring.kafka.consumer.group.user.upsert:user-upsert-group-id}",
                containerFactory = "userUpsertEventListenerContainerFactory")
            public void handleEvent(UserUpsertEvent userEvent) {
                super.handleEvent(userEvent);
            }

            @Override
            public void processEvent(UserUpsertEvent userEvent) {
                userService.upsert(userUpsertEventMapper.eventToBean(userEvent));
            }
         }
      ```

      Le consumer appelle le service métier `userUpsertEventMapper.eventToBean(userEvent)` qui enregistre/modifie l'utilisateur en bdd.

      - Vérification des logs du micro-service `users-kafka-consumer`

      ```
        00:20:55 - Kafka version : 2.0.1
        00:20:55 - Kafka commitId : fa14705e51bd2ce5
        00:20:55 - Initializing ExecutorService
        00:20:55 - Cluster ID: YDwAFMOJTUC7xHECEbZkYw
        23:35:34 - [Consumer clientId=consumer-4, groupId=users-upsert-group-id] Discovered group coordinator pl-debian:9092 (id: 2147483647 rack: null)
        23:35:34 - Cluster ID: Mr_6_w35TA2Fu-hgRZlFOQ
        23:35:34 - [Consumer clientId=consumer-4, groupId=users-upsert-group-id] Revoking previously assigned partitions []
        23:35:34 - partitions revoked: []
        23:35:34 - [Consumer clientId=consumer-4, groupId=users-upsert-group-id] (Re-)joining group
        23:35:34 - [Consumer clientId=consumer-2, groupId=users-deleted-group-id] Resetting offset for partition users-deleted-0 to offset 0.
        23:35:34 - partitions assigned: [users-deleted-0]
        23:35:34 - [Consumer clientId=consumer-4, groupId=users-upsert-group-id] Successfully joined group with generation 1
        23:35:34 - [Consumer clientId=consumer-4, groupId=users-upsert-group-id] Setting newly assigned partitions [users-upsert-0]
        23:35:34 - [Consumer clientId=consumer-4, groupId=users-upsert-group-id] Resetting offset for partition users-upsert-0 to offset 2.
        23:35:34 - partitions assigned: [users-upsert-0]
        23:40:36 - Receive event kafka : UserUpsertEvent(name=LEGBA, firstName=Eric, login=EL1628EN@YAHOO.FR, birthDay=2000-07-12) class com.syscom.event.user.UserUpsertEvent
        23:40:36 - Création/Modification de l'utilisateur User(id=null, name=LEGBA, firstName=Eric, login=EL1628EN@YAHOO.FR, birthDay=2000-07-12)
        23:40:36:441|69|statement|connection 0|url jdbc:p6spy:postgresql://localhost:5432/db_users_2|select nextval ('user_seq')|select nextval ('user_seq')
        23:40:36 - Création réussie de l'utilisateur User(id=15, name=LEGBA, firstName=Eric, login=EL1628EN@YAHOO.FR, birthDay=2000-07-12)
        "Hibernate: insert into t_user (u_birth_day, u_first_name, u_login, u_name, u_password, u_id) values (?, ?, ?, ?, ?, ?)
        23:40:36:514|15|statement|connection 0|url jdbc:p6spy:postgresql://localhost:5432/db_users_2|insert into t_user (u_birth_day, u_first_name, u_login, u_name, u_password, u_id) values (?, ?, ?, ?, ?, ?)|insert into t_user (u_birth_day, u_first_name, u_login, u_name, u_password, u_id) values ('2000-07-12T00:00:00.000+0200', 'Eric', 'EL1628EN@YAHOO.FR', 'LEGBA', '********', 15)
        23:40:36 - End treatment of the event UserUpsertEvent(name=LEGBA, firstName=Eric, login=EL1628EN@YAHOO.FR, birthDay=2000-07-12) class com.syscom.event.user.UserUpsertEvent (duration 237 ms)

      ```
