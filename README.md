# EmailStorage

EmailStorage è un'applicazione che automatizza la gestione delle email ricevute, salva i dettagli delle email in un database PostgreSQL, e gestisce gli allegati salvandoli su Amazon S3. L'applicazione esegue i seguenti passaggi:

- Recupera le email dal server di posta del giorno seguente.
- Salva i dettagli delle email nel database.
- Se un'email non ha allegati, il suo stato sarà "letto".
- Se un'email ha allegati, questi verranno caricati su Amazon S3 e l'email avrà lo stato "elaborato".

## Tecnologie Utilizzate

- **Spring Boot**
- **Java 21**
- **Maven 3.9.9**
- **Java Mail API**
- **Lombok**
- **Docker**
- **PostgreSQL**
- **Amazon S3**
- **JPA (Java Persistence API)**

## Configurazione

Nel file `src/main/resources/application.yml`, configura i dettagli del tuo database PostgreSQL, di aws s3 dopo aver creato un nuovo bucket. 
Configura anche username e password per l'accesso alla casella di posta. 
Completa la configurazione del db nel docker-compose. 

## Autore
Andrea Italiano