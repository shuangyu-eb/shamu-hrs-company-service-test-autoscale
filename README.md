# shamu-hrs company service

## Local Setup

### preparation

Before running it locally, you should run the consul in local.
Here is the [document](https://github.com/tardisone/shamu-hrs/blob/master/consul-setup-in-local.md).

**Note**:

If you want run the project shamu-hrs, you should also run the shamu-hrs-gateway.

### Using maven to serve the application

The following packages are being required if you want to deploy the code to an environment.

   - Install Java jdk-8(open jdk or oracle jdk) depending on the system. If you install openjdk on mac, you can take a look at [this](https://apple.stackexchange.com/questions/334384/how-can-i-install-java-openjdk-8-on-high-sierra).
   - Install maven, here's the [documentation](https://maven.apache.org/install.html)

3. Go to your project root, run the following command to build images for the project.

   ```bash
   $ mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

    **Note:**
    You can also use the java IDE(e.g. IntelliJ IDEA) to set up it locally.

4. Visit [http://localhost:8500](http://localhost:8500), then you'll find the company service in **Services**.

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
