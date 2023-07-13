# Gaia-x onboarding and credential verification MVP by smartSense

This is MVP to showcase the capability of smartSense in contact with the Gaia-X economy.
This MVP covers below user case:

1. On-boarding in Gaia-x
    1. Create a sub-domain for enterprise
    2. Create SSL key-value pair for enterprise
    3. Create web did
    4. Create participant credentials and sign in using Gaia-x API
    5. Host public key, did.json, and participant files under the .well-known path
2. Create service offering and create service offering credential and host offer file under the .well-known path
3. List Catalogue
4. Create a verifiable presentation of Gaia-X participant credentials
5. validate VP and see masked(Secure) information bt verifying VP

## Tools and Technologies

1. Spring boot with JPA
2. K8S Java SDK
3. Certbot SDK acme4j
4. AWS Route53 SDK
5. AWS S3 SDK
6. [NodeJS for signer tool](https://github.com/smartSenseSolutions/smartsense-gaia-x-signer)

## Onboarding flow

![onboarding.png](doc%2Fonboarding.png)

## Create service offer flow

![Create service offer.png](doc%2FCreate%20service%20offer.png)

## Service offer flow

![Service offer flow.png](doc%2FService%20offer%20flow.png)

## Known issue or improvement

1. Authentication and Authorization flow can be improved
2. Data exchange based on Gaia-x trust framework(Ocean protocol??)
3. Unit Test
4. K8S ingress and secret creation can be done using argoCD/argo workflow
5. For login, we can use Openid4VP with the integration of keycloak

## Run application

### Configuration

1. Create k8s user with access to ingress and secret creation
2. Create AWS s3 bucket
3. Create hosted zone in AWS with your base domain
4. Create an AWS IAM user with the access to hosted zone and S3

### Run in IntelliJ Idea

1. Set values in the application.yaml
2. Run using the Intellij idea

### Run in k8s

Please refer to sample config files in ``/k8s`` folder

## References

1. [Create SSL certificate using acme4j](https://github.com/shred/acme4j/blob/master/acme4j-example/src/main/java/org/shredzone/acme4j/example/ClientTest.java)
