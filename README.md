# Status Dashboard

## Environment

You need Maven to build.
You also have to install Angular CLI, if it's not done before. (npm install -g @angular/cli)

also you might have to execute 'node install' in src/main/frontend if  src/main/frontend/node_modules directory doesn't exist

## Build and test

for Unit testing:

*mvn test*

for creating a jar:

*mvn package*

the corn-status-dashboard-0.1.0.jar appears in *'target'* directory.

## HTTPS

The app uses HTTPS only and 8443 port by default. http and 8080 redirects to https and 8443.

Currently the application has a self-signed SSL certificate. You can replace it with a valid one. Just replace /src/main/resources/keystore.jks with your own and change *'server.ssl.key-store-password'* in *application-main.properties*. 

## Run

Use command:

*java -Dspring.profiles.active=main -jar obi-status-dashboard-0.1.0.jar*

After that just open *https://[yourhost]:8443/* in a browser.