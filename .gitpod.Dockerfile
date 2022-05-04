FROM gitpod/workspace-postgres

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && sdk install java 17.0.3-tem"
