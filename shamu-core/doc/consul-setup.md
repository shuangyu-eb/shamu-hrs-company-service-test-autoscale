#### Install Consul
```
cd ~ 
wget https://releases.hashicorp.com/consul/1.4.2/consul_1.4.2_linux_amd64.zip
mkdir consul
unzip consul_1.4.2_linux_amd64.zip -d consul
sudo mv consul/consul /usr/bin/

start the server
consul agent -dev -ui
```
#### Set up
setting up consul configuration in application.yml:
```
spring:
  application:
    name: shamu-core
  cloud:
    consul:
      host: localhost
      port: 8500
      config:
        enabled: true
        profileSeparator: ','
```

Setting up consul instance information in bootstrap.yml:
```
spring:
  cloud:
    consul:
      discovery:
        instance-id: shamu-core
        serviceName: shamu-core-service

```