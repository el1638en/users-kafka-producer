#Apache Kafka
Kafka est un système de messagerie distribué développé par la fondation Apache depuis 2012.
Kafka fonctionne en mode publish-subscirbe et conserve les données qu'il reçoit dans des topic ou catégories de données.
Les systèmes qui publient des données dans Kafka sont appelés des `producers`.
Les systèmes qui lisent les données stockées dans les topics sont appelés `consumers`.
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

Spring Kafka est un framework de l'écosystème Spring. Il offre une brique logicielle permettant de produire et de lire des données dans un serveur Kafka. Dans l'exemple qui suit, nous allons utiliser 2 micro-services Spring Boot :
  - le micro-service `users-kafka-producer` qui va écrire dans un topic kafka des informations d'utilisateur,
  - le micro-service `users-kafka-consumer` qui va lire du topic kafka les informations d'utilisateur.

    1. Création du topic kafka `users`

    ```
    root@pl-debian:/opt/kafka_2.11-2.1.0# bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic users
    ```

    2. Micro-service `users-kafka-producer`

      Le code source de ce micro-service est disponible [ici](https://github.com/el1638en/users-kafka-producer).

      - Configuration de l'adresse du serveur kafka et du topic d'écriture des données

      ```java
          @Configuration
          public class KafkaTopicConfig {

            @Value(value = "${kafka.serverAddress}")
            private String kafkaServerAddress;

            @Value(value = "${user.topic.name}")
            private String userTopicName;

            @Bean
            public KafkaAdmin kafkaAdmin() {
              Map<String, Object> configs = new HashMap<>();
              configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerAddress);
              return new KafkaAdmin(configs);
            }

            @Bean
            public NewTopic userTopic() {
              return new NewTopic(userTopicName, 1, (short) 1);
            }

          }
      ```

      - Configuration du producer d'event kafka

      ```java
          @Configuration
          public class KafkaUserProducerConfig {

            @Value(value = "${kafka.serverAddress}")
            private String kafkaServerAddress;

            @Bean
            public ProducerFactory<String, UserEvent> userProducerFactory() {
              Map<String, Object> configProperties = new HashMap<>();
              configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerAddress);
              configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
              configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
              return new DefaultKafkaProducerFactory<>(configProperties);
            }

            @Bean
            public KafkaTemplate<String, UserEvent> userKafkaTemplate() {
              return new KafkaTemplate<>(userProducerFactory());
            }
          }
        ```

      - Component Spring producer d'utilisateur

      ```java
          @Component
          public class UserProducer {
            private final Logger logger = LoggerFactory.getLogger(UserProducer.class);

            @Autowired
            private KafkaTemplate<String, UserEvent> userKafkaTemplate;

            @Value(value = "${user.topic.name}")
            private String userTopicName;

            public void send(UserEvent userEvent) {
              logger.info("Envoi de l'event {} dans le topic kafka {}.", userEvent, userTopicName);
              userKafkaTemplate.send(userTopicName, userEvent);
            }
          }
      ```

      - Utilisateur du component dans le service métier des utilisateurs

      ```java
          @Service
          @Transactional(rollbackFor = Exception.class)
          public class UserServiceImpl implements UserService {
            private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

            @Autowired
            private UserDao userDao;

            @Autowired
            private UserEventMapper userEventMapper;

            @Autowired
            private UserProducer userProducer;

            @Override
            public void create(User user) throws BusinessException {
              logger.info("Creation d'un nouvel utilisateur {}", user);
              Assert.notNull(user, "User must not be null");
              List<String> errors = checkUserData(user);
              if (!errors.isEmpty()) {
                throw new BusinessException(StringUtils.join(errors, " "));
              }
              if (userDao.findByLogin(user.getLogin()) != null) {
                throw new BusinessException("Login already used.");
              }

              userDao.save(user);
              userProducer.send(userEventMapper.beanToEvent(user));
            }
          }
      ```

      Le service métier `UserService.create(User user)` appelle en fin de traitement le producer `userProducer.send(userEventMapper.beanToEvent(user));` pour pousser dans kafka l'utilisateur.

      - Vérification des logs du micro-service `users-kafka-producer`

      ```
        00:22:01 - Envoi de l'event UserEvent(name=LEGBA, firstName=Eric, login=EL1638EN) dans le topic kafka users.
        00:22:01 - ProducerConfig values:
        acks = 1
        batch.size = 16384
        bootstrap.servers = [localhost:9092]
        buffer.memory = 33554432
        client.id =
        compression.type = none
        connections.max.idle.ms = 540000
        00:22:01 - Kafka version : 2.0.1
        00:22:01 - Kafka commitId : fa14705e51bd2ce5
      ```

    3. Micro-service `users-kafka-consumer`

      Le code source de ce micro-service est disponible [ici](https://github.com/el1638en/users-kafka-consumer).

      - Configuration du consumer d'event kafka

      ```java
          @Configuration
          public class KafkaUserConsumerConfig {

          	@Value(value = "${kafka.serverAddress}")
          	private String kafkaServerAddress;

          	public ConsumerFactory<String, UserEvent> userConsumerFactory() {
          		Map<String, Object> properties = new HashMap<>();
          		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerAddress);
          		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "users");
          		return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(),
          				new JsonDeserializer<>(UserEvent.class));
          	}

          	@Bean
          	public ConcurrentKafkaListenerContainerFactory<String, UserEvent> userKafkaListenerContainerFactory() {
          		ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
          		factory.setConsumerFactory(userConsumerFactory());
          		return factory;
          	}
          }
        ```

      - Component Spring consumer d'utilisateur

        ```java
            @Component
            public class UserConsumer {

        	     private final Logger logger = LoggerFactory.getLogger(UserConsumer.class);

            	@Autowired
            	private UserEventMapper userEventMapper;

            	@Autowired
            	private UserService userService;

              @KafkaListener(topics = "${user.topic.name}", containerFactory = "userKafkaListenerContainerFactory")
              public void userListener(UserEvent userEvent) {
              	logger.info("Reception d'un event kafka utilisateur : {}", userEvent);
                  userService.create(userEventMapper.eventToBean(userEvent));
              }
          }
        ```
      Le consumer appelle le service métier `userService.create(userEventMapper.eventToBean(userEvent))` qui enregistre l'utilisateur en bdd.

      - Vérification des logs du micro-service `users-kafka-producer`

        ```
          00:20:55 - Kafka version : 2.0.1
          00:20:55 - Kafka commitId : fa14705e51bd2ce5
          00:20:55 - Initializing ExecutorService
          00:20:55 - Cluster ID: YDwAFMOJTUC7xHECEbZkYw
          00:20:58 - [Consumer clientId=consumer-2, groupId=users] Discovered group coordinator pl-debian:9092 (id: 2147483647 rack: null)
          00:20:58 - [Consumer clientId=consumer-2, groupId=users] Revoking previously assigned partitions []
          00:20:58 - partitions revoked: []
          00:20:58 - [Consumer clientId=consumer-2, groupId=users] (Re-)joining group
          00:20:58 - [Consumer clientId=consumer-2, groupId=users] Successfully joined group with generation 1
          00:20:58 - partitions assigned: [users-0]
          00:22:01 - Reception d'un event kafka utilisateur : UserEvent(name=LEGBA, firstName=Eric, login=EL1638EN)
          00:22:01 - Création de l'tilisateur User(id=null, name=LEGBA, firstName=Eric, login=EL1638EN)
          Hibernate: select nextval ('user_seq')
          00:22:01:665|3|statement|connection 0|url jdbc:p6spy:postgresql://localhost:5432/db_users_2|select nextval ('user_seq')|select nextval ('user_seq')
          Hibernate: insert into t_user (u_first_name, u_login, u_name, u_password, u_id) values (?, ?, ?, ?, ?)
          00:22:01:711|0|statement|connection 0|url jdbc:p6spy:postgresql://localhost:5432/db_users_2|insert into t_user (u_first_name, u_login, u_name, u_password, u_id) values (?, ?, ?, ?, ?)|insert into t_user (u_first_name, u_login, u_name, u_password, u_id) values ('Eric', 'EL1638EN', 'LEGBA', 'Mmdp-3366', 1)
        ```
