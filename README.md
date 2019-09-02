# shamu-hrs company service

## Local Setup

### preparation  
#### Consul
Before running it locally, you should run the consul in local.
Here is the [document](https://github.com/tardisone/shamu-hrs/blob/master/consul-setup-in-local.md).

#### AWS Credentials
Make sure you have configured your aws credentials in folder `~/.aws` on macOS, Linux, or `C:\Users\<YOUR_USERNAME>\.aws\` on windows. There are two configuration files in that folder - `config` and `credentials`.
The content of file `config` shown as follow:
```
[default]
region=ap-northeast-1
```
<small>In develop environment, we create all resources in this specified region.</small>

The content of file `credentials` shown as follow:
```
aws_access_key_id=YOU_ACCESS_KEY_ID(replace with your own)
aws_secret_access_key=YOUR_SECRET_ACCESS_KEY(replace with your own)
```

You can also automatically generate it by AWS Cli command `aws configure`.

**Note**:  
Before start this micro service, make sure service gateway service is already running.

### Configuration
We split developer database on backend as well. Before you can start the application, you need to create a file called `application-database.yml` \
under folder `config`. This file is already ignore from git. This file contains configuration related to backend database and Auth0 validation configuration. The sample content shown as follow:
```
spring:
  datasource:
    url: DATABASE URL
    username: DATABASE USERNAME
    password: DATABASE PASSWORD

auth0:
  jwks: https://shamu-hrs-qa.auth0.com/.well-known/jwks.json(replace this with your own, in the format of <your auth0 domain>/.well-known/jwks.json>)
  domain: shamu-hrs-qa.auth0.com(replace this with your own)
  customNamespace: https://interviewed.com/
  clientId: XXXXXXXXXXXXXXXXXXXXXXXXX (Machine-to-Machine application client id)
  clientSecret: XXXXXXXXXXXXXXXXXXXXXXXX (Machine-to-Machine application client secret)
  database: XXXXXXXXXXXXXXXXX (connection database name)
```
<small>`customNamespace` is used to extract custom claims like id from token. Currently is fixed as shown above.</small>  
<small>`clientId` is the client id of a machine-to-machine application. In order to manage the users, it's necessary to create
 a machine-to-machine application with management api permissions. The scope of the machine-to-machine application 
 should include all about users, roles and resource-servers. </small>   
<small>`clientSecret` is the client secret of the machine-to-machine application.</small>  
<small>`database` is the name of the Auth0 database. we currently switch to use Auth0 store instead of custom database.</small>

### Using maven to serve the application


1. Install Java jdk-8(open jdk or oracle jdk) depending on the system. If you install openjdk on mac, you can take a look at [this](https://apple.stackexchange.com/questions/334384/how-can-i-install-java-openjdk-8-on-high-sierra).
2. Install maven, here's the [documentation](https://maven.apache.org/install.html)

3. Go to your project root, run the following command to build images for the project.

   ```bash
   $ mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

    **Note:**
    You can also use the java IDE(e.g. IntelliJ IDEA) to set up it locally.

4. Visit [http://localhost:8500](http://localhost:8500), then you'll find the company service in **Services**.  

5. This micro service runs on port 8081. You can also check port 8081 to verify it's running.

### Using docker to serve the application

1. Download docker by going to [Docker CE Installation](https://docs.docker.com/engine/installation/), choose the right wizard for you depending on your os system.
    
    The project requires `docker-compose` command to run, make sure you have it before you go to next step.

2. Go to your project root, run the following command to build images for the project.
    
    ```bash
    $ docker-compose build
    ```
    
    Run the following command to start the project.
    
    ```bash
    $ docker-compose up
    ```
    
3. Visit [http://localhost:8500](http://localhost:8500), then you'll find the company service in **Services**.  

4. This micro service runs on port 8081. You can also check port 8081 to verify it's running.
