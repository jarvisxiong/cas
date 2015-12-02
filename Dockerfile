# Base Image
FROM dockerhub.corp.inmobi.com/idp/docker-jvm-8
MAINTAINER dcp-engg@inmobi.com

# Using a newer version of supervisor for environment variable substitution support in configs
RUN pip install supervisor==3.1.3
# Use "supervisorctl -c /etc/supervisor/supervisord.conf" inside the container to access the supervisord cli

# Change /opt/inmobi/cas to /opt/mkhoj/cas in the below segment for backward compatibility
ENV CAS_BASE_DIRECTORY /opt/inmobi/cas
WORKDIR $CAS_BASE_DIRECTORY

COPY docker/build .build
COPY docker/bin/start.bash bin/start.bash
COPY docker/automation .
COPY docker/bin/scribe-emitter/emit-scribe-stats bin/scribe-emitter/emit-scribe-stats
RUN chmod +x bin/scribe-emitter/emit-scribe-stats

# IDP health check
COPY docker/bin/health_check.sh /opt/inmobi/usr/deployment/validate

# Scribe Setup
RUN apt-get update && apt-get -y install inmobi-scribe-ctrl python-scribe scribe-scripts scribe-server-orig
ENV RUBYLIB=/var/lib/gems/1.8/gems/fb303-0.4.0/lib/:/var/lib/gems/1.8/gems/lwes-0.8.2/lib/

# Supervisord Setup
RUN mkdir -p logs/process
COPY docker/supervisor /etc/supervisor/conf.d

# Collectd Setup
RUN rm /opt/inmobi/ops/collectd/etc/collectd.d/collectd_graphite.conf
COPY docker/collectd/build/templates/conf/collectd_graphite.conf.mustache /opt/inmobi/usr/collectd-setup/collectd_graphite.conf.mustache
COPY docker/collectd/bin/setup_collectd_graphite.sh /opt/inmobi/usr/collectd-setup/pre-proc.sh

# Cas Ports
EXPOSE 8800 8801
# JMX Port
EXPOSE 9004

# See https://golang.org/pkg/path/filepath/#Match for filePath regex syntax. Current support is very basic and does not support wildcards for character ranges.
COPY Server/target/server\\-*[0-9].jar Server/target/server\\-*[0-9]-SNAPSHOT.jar bin/cas.jar

# Default execution point
CMD ["/usr/local/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]

# Future Enhancements:
# DB Passwords using IDP Secrets
# AirBnB Nerve Watcher for Service Registry

# Assumptions
# 1) IDP_NODE must be of the form [a-zA-Z]{3}[0-9]{4}.* for now.
# 2) HOST_NAME is optional but it will break JMX reporting

#note: IDP_NODE is equivalent to the CONTAINER_NAME

# Useful Docker run Commands:
# 1) Build: docker built -t cas_image .
# 2) Run: docker run -p 8800:8800 -p 8801:8801 -p 9004:9004 --ulimit nofile=98304:98304 -e IDP_ENVIRONMENT=non_prod -e IDP_CLUSTER=corp -e HOST_NAME=localhost -e IDP_NODE=cas1234 --name=cas_container cas_image
# 3) All in one: docker build -t cas_image .; docker rm cas_container; docker run -p 8800:8800 -p 8801:8801 -p 9004:9004 --ulimit nofile=98304:98304 -e IDP_ENVIRONMENT=non_prod -e IDP_CLUSTER=corp -e HOST_NAME=localhost -e IDP_NODE=cas1234 --name=cas_container cas_image
# 4) ssh into running container: docker exec -it cas_container /bin/bash
# 5) To override the default supervisord setup add -it just before the image name and /bin/bash just after the image name

# sudo docker run -p 8800:8800 -p 8801:8801 -p 9004:9004 --ulimit nofile=98304:98304 -e IDP_ENVIRONMENT=prod -e IDP_CLUSTER=hkg1 -e IDP_NODE=cas1000.ads.hkg1.inmobi.com --name=cas dockerhub.corp.inmobi.com/channel_adserve/cas:1681a0affb94
