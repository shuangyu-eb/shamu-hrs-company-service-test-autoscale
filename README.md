# shamu-hrs company service

## Local Setup

### Prerequisites


#### Consul
Before running the company service, you should have [consul running locally](https://github.com/tardisone/shamu-hrs/blob/master/consul-setup-in-local.md).

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

#### Redis

Install redis with [Homebrew](https://brew.sh/):

```
brew update
brew install redis
```

If you run into issues with brew, you can compile redis from source by following the directions [here](https://redis.io/topics/quickstart).

Then, run `redis-server`.

### Configuration
Create `application-database.yml` in `/config` that contains configuration related to the company database. This file is gitignored. The file should be in the format:

```
spring:
  datasource:
    jdbc-url: jdbc:mysql://{DATABASE_HOST}/{DATABASE_NAME}
    username: {DATABASE_USERNAME}
    password: {DATABASE_PASSWORD}
  secret:
    datasource:
      jdbc-url: jdbc:mysql://{SECRET_DATABASE_HOST}/{SECRET_DATABASE_NAME}
      username: {SECRET_DATABASE_USERNAME}
      password: {SECRET_DATABASE_PASSWORD}
```
Note: These configs of secret is used to store secrets of companies, but Auth0 can't connect your local database and store the secret hash to your local database, so it is just used to help you start up your local environment.

### Using maven to serve the application


1. Install Java openjdk 8 depending on the system. If you install it on mac, you can take a look at [this](https://apple.stackexchange.com/questions/334384/how-can-i-install-java-openjdk-8-on-high-sierra).

2. Go to your project root, run the following command to build images for the project.

   ```bash
   $ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

    **Note:**
    You can also use the java IDE(e.g. IntelliJ IDEA) to set up it locally.

3. Visit [http://localhost:8500](http://localhost:8500), then you'll find the company service in **Services**.

4. This micro service runs on port 8081. You can also check port 8081 to verify it's running.

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
