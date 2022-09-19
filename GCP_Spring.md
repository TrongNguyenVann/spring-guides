# GOOGLE CLOUD WITH SPRING COURSE

## LAB 1: Introduction to Google Cloud Platform

**Cloud Console**: Web app to mangage cloud infrastructure  
**Cloud Shell**: like a terminal with gcloud command, support Code Editor, Web Preview

List active account name: `gcloud auth list`  
List project id: `gcloud config list project`  
Send GET request with curl: `curl http://localhost:8081/guestbookMessages`  
Send POST request with curl: `curl -XPOST -H "content-type: application/json"-d '{"name": "Ray", "message": "Hello"}' http://localhost:8081/guestbookMessages`

## LAB 2: CONNECT WITH CLOUD SQL

**Step 1. Enable the Cloud SQL Administration API:**


- Enable Cloud SQL Admin API: `gcloud services enable sqladmin.googleapis.com`
- Confirm that Cloud SQL Administration API is enabled: `gcloud services list | grep sqladmin`
- List the Cloud SQL instances: `gcloud sql instances list`

**Step 2. Create Cloud SQL instance**


- Create a Cloud SQL instance: `gcloud sql instances create guestbook --region=us-central1`
- Create a messages database in the MySQL instance: `gcloud sql databases create messages --instance guestbook`

**Step 3. Connect to Cloud SQL and create database**


By default, Cloud SQL is not accessible through public IP addresses. You can connect to Cloud SQL in the following ways:
- Use a local Cloud SQL proxy.
- Use gcloud to connect through a CLI client: `gcloud sql connect guestbook`
- From the Java application, use the MySQL JDBC driver with an SSL socket factory for secured connection.

**Step 4. Find the instance connection name**


- Find the instance connection name: `gcloud sql instances describe guestbook --format='value(connectionName)'`
- Create `guestbook-service/src/main/resources/application-cloud.properties` and add the following properties:
```
spring.cloud.gcp.sql.enabled=true
spring.cloud.gcp.sql.database-name=messages
spring.cloud.gcp.sql.instance-connection-name=YOUR_INSTANCE_CONNECTION_NAME
spring.datasource.hikari.maximum-pool-size=5
```

**Step 5. Run application**

- Run a test with the default profile and make sure there are no failures: `./mvnw test`
- Start the Guestbook Service with the cloud profile: `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=cloud"`

## LAB 3: CLOUD TRACE

Cloud Trace is a distributed tracing system that collect latency data and display it in Google Cloud Console
It tracks how request propagate though application, and generate report for assess performance of a system.

**Step 1. Enable Cloud Trace API:** `gcloud services enable cloudtrace.googleapis.com`

**Step 2. Add Spring Cloud GCP Starter to Spring pom file**

- Add the following dependency to pom.xml file:
```xml
        <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-gcp-starter-trace</artifactId>
        </dependency>
```

**Step 3. Enable trace sampling in Spring configuration**

- Add the following configuration in src/main/resources/application-cloud.properties file:
```
spring.cloud.gcp.trace.enabled=true
spring.sleuth.sampler.probability=1.0
spring.sleuth.scheduled.enabled=false
```

**Step 4. Set up a service account**

- Create service account: `gcloud iam service-accounts create guestbook`
- Add editor role for your project using this service account
```
export PROJECT_ID=$(gcloud config list --format 'value(core.project)')
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member serviceAccount:guestbook@${PROJECT_ID}.iam.gserviceaccount.com \
  --role roles/editor
```
- Generate JSON key file to be used by the application
```
gcloud iam service-accounts keys create \
    ~/service-account.json \
    --iam-account guestbook@${PROJECT_ID}.iam.gserviceaccount.com
```

**Step 5. Run application**
```
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=cloud \
  -Dspring.cloud.gcp.credentials.location=file:///$HOME/service-account.json"
```

## LAB 4: MESSAGING WITH PUB/SUB

Pub/Sub is a messaging service that allow send and receive message between independent application.
It is a middleware.

**Step 1. Enable Pub/Sub API**

- Run command: `gcloud services enable pubsub.googleapis.com`

**Step 2. Create Pub/Sub topic**

- Run command: `gcloud pubsub topics create messages`

**Step 3. Add depencendy to Spring app**

- Add dependency to Pub/Sub starter to pom file
```
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-gcp-starter-pubsub</artifactId>
        </dependency>
```

**Step 4. Publish a message**

```
import org.springframework.cloud.gcp.pubsub.core.*;

// Declare template
@Autowired
private PubSubTemplate pubSubTemplate;

// Add this line to the caller
pubSubTemplate.publish("messages", name + ": " + message);
```

**Step 5. Create a subscription**

Before subscribing to a topic, you must create a subscription. Pub/Sub supports pull subscription and push subscription
- Pull subscription: client can pull message from the topic
- Push subscription: Pub/Sub can publish message to a target endpoint
- A topic can have many subscriptions, a subscription can have many subscribers

- Create Pub/Sub subscription: `gcloud pubsub subscriptions create messages-subscription-1 --topic=messages`
- Pull message from subscription: `gcloud pubsub subscriptions pull messages-subscription-1`

## LAB 5: INTERGRATING PUB/SUB WITH SPRING

**Step 1. Add spring intergation**

- Add dependency to pom file:
```
       <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-core</artifactId>
       </dependency>
```

**Step 2. Create outbound gateway**
```
package com.example.frontend;
import org.springframework.integration.annotation.MessagingGateway;
@MessagingGateway(defaultRequestChannel = "messagesOutputChannel")
public interface OutboundGateway {
        void publishMessage(String message);
}
```

**Step 3. Publish the message**
```
    @Autowired
    private OutboundGateway outboundGateway;

    outboundGateway.publishMessage(name + ": " + message);
```

**Step 4. Bind output channel to Pub/Sub topic**
```
import org.springframework.context.annotation.*;
import org.springframework.cloud.gcp.pubsub.core.*;
import org.springframework.cloud.gcp.pubsub.integration.outbound.*;
import org.springframework.integration.annotation.*;
import org.springframework.messaging.*;

    @Bean
    @ServiceActivator(inputChannel = "messagesOutputChannel")
    public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
        return new PubSubMessageHandler(pubsubTemplate, "messages");
    }
```

## LAB 6. UPLOADING AND STORING FILES


**Step 1. Add the cloud storage starter**


## LAB . WORKING WITH CLOUD SPANNER

**Step 1. Enable Spanner API:** `gcloud services enable spanner.googleapis.com`

**Step 2. Create new spanner instance**
- Create spanner instance:
```
gcloud spanner instances create guestbook --config=regional-us-central1 \
  --nodes=1 --description="Guestbook messages"
```
- Create message database in Spanner instance: `gcloud spanner databases create messages --instance=guestbook`
- Confirm database exist in Spanner instance: `gcloud spanner databases list --instance=guestbook`
- Create DDL file in db/ folder:
```
CREATE TABLE guestbook_message (
    id STRING(36) NOT NULL,
    name STRING(255) NOT NULL,
    image_uri STRING(255),
    message STRING(255)
) PRIMARY KEY (id)
```
- Run DDL command to create table:
```
gcloud spanner databases ddl update messages \
  --instance=guestbook --ddl-file=$HOME/guestbook-service/db/spanner.ddl
```

**Step 3. Add Spring Cloud GCP Spanner**
- Add dependency:
```
<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-gcp-starter-data-spanner</artifactId>
</dependency>
```
**Step 4. Update configuration**
- Open application.properties and update:
```
# Add Spanner configuration
spring.cloud.gcp.spanner.instance-id=guestbook
spring.cloud.gcp.spanner.database=messages
```

## LAB. DEPLOYING TO KUBERNETES ENGINE
Google Kubernetes Engine is a platform for managing containerized workloads and services. 
**Step 1. Create GKE Cluster**
- Enable Kubernetes Engine API: `gcloud services enable container.googleapis.com`
- Create GKE cluster:
```
gcloud container clusters create guestbook-cluster \
    --zone=us-central1-a \
    --num-nodes=2 \
    --machine-type=n1-standard-2 \
    --enable-autorepair \
    --enable-stackdriver-kubernetes
```
- Check GKE server version: `kubectl version`

**Step 3. Containerize application**
- Enable Container Registry API: `gcloud services enable containerregistry.googleapis.com`
- Add the following plugin into pom file:
```
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>2.4.0</version>
    <configuration>
        <to>
        <!-- Replace [PROJECT_ID]! -->
        <image>gcr.io/[PROJECT_ID]/guestbook-frontend</image>
        </to>
    </configuration>
</plugin>
```
- Using maven to build container using Jib plugin: `./mvnw clean compile jib:build`

**Step 4. Setup a service account**
- Create service account: `gcloud iam service-accounts create guestbook`
- Add editor role for your project to this service account:
```
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member serviceAccount:guestbook@${PROJECT_ID}.iam.gserviceaccount.com \
  --role roles/editor
```
- Generate JSON key for this account:
```
gcloud iam service-accounts keys create \
    ~/service-account.json \
    --iam-account guestbook@${PROJECT_ID}.iam.gserviceaccount.com
```
- Create secrete using service account credential file
```
kubectl create secret generic guestbook-service-account \
  --from-file=$HOME/service-account.json
```
- Verify service account is stored: `kubectl describe secret guestbook-service-account`

**Step 5. Deploy the containers**
- Edit `~/kubernetes/guestbook-frontend-deployment.yaml`, set `image: gcr.io/[PROJECT_ID]/guestbook-frontend:latest`
- Edit `~/kubernetes/guestbook-service-deployment.yaml`, set `image: gcr.io/[PROJECT_ID]/guestbook-service:latest`
- Deploy the updated GKE deployment: `kubectl apply -f ~/kubernetes/`
- Check status of all services running on GKE clusters: `kubectl get svc`

## WORKING WITH KUBERNETES ENGINE MONITORING
Kubernetes engine monitoring contains logs, events and metrics from GKE environmnent to help you understand application's behavior in production.
**Promethus** is an optional monitoring tool often used with GKE.

**Step 1. Enable Monitoring and view Kubernetes dashboard**
- Open Monitoring -> Choose Dashboard

**Step 2. Expose Prometheus metrics from Spring Boot app**
- Insert dependency to actuator and micrometer
```
<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
     <groupId>io.micrometer</groupId>
     <artifactId>micrometer-registry-prometheus</artifactId>
     <scope>runtime</scope>
</dependency>
```
- Add properties to configure Spring Boot Actuator expose metrics on port 9000:
```
management.server.port=9000
management.endpoints.web.exposure.include=*
```

**Step 3. Rebuild container**
- Rebuild application container: `./mvnw clean compile jib:build`
- Update GKE manifest file `~/kustomize/base/guestbook-frontend-deployment.yaml`, to declare metrics ports:
```
- name: metrics
  containerPort: 9000
```
- Redeploy manifest:
```
mkdir -p ~/bin
cd ~/bin
curl -s "https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh" | bash
export PATH=$PATH:$HOME/bin
cd ~/kustomize/base
cp ~/service-account.json ~/kustomize/base
kustomize build
gcloud container clusters get-credentials guestbook-cluster --zone=us-central1-a
kustomize edit set namespace default
kustomize build | kubectl apply -f -
```
- Find pod name: `kubectl get pods -l app=guestbook-frontend`
- Port forward  to a pod: `kubectl port-forward guestbook-frontend-[podnumber] 9000:9000`

**Step 4. Install Prometheus and Sidecar**
- Install Prometheus operator:
```
export PROMETHEUS_VERSION=v0.58.0
gcloud container clusters get-credentials guestbook-cluster --zone=us-central1-a
kubectl apply -f https://raw.githubusercontent.com/coreos/prometheus-operator/${PROMETHEUS_VERSION}/bundle.yaml --force-conflicts=true --server-side
```
- Provision Prometheus using above operator:
```
cd ~/prometheus
export PROJECT_ID=$(gcloud config list --format 'value(core.project)')
# Make sure the project ID is set
echo $PROJECT_ID
cat prometheus.yaml | envsubst | kubectl apply -f -
kubectl apply -f pod-monitors.yaml
```
- Port forward:
```
pkill java
kubectl port-forward svc/prometheus 9090:9090
```