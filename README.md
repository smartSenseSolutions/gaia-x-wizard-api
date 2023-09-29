# Gaia-x onboarding and credential verification MVP by smartSense

This MVP showcases the capabilities of smartSense in contact with the Gaia-X economy.
This MVP covers below use cases:

1. On-boarding in Gaia-x
    1. Create a sub-domain for participant
    2. Create SSL key-value pair for participant
    3. Create web did
    4. Create participant credentials and sign in using Gaia-x API
    5. Host public key, did.json, and participant files under the .well-known path
2. Create resource and host resource file under the .well-known path
3. Create service offering and create service offering credential and host offer file under the .well-known path
4. List Catalogue

## Tools and Technologies

1. Spring boot with JPA
2. Keycloak for authentication through WebAuthn
3. K8S Java SDK
4. Certbot SDK acme4j
5. AWS Route53 SDK
6. AWS S3 SDK
7. NodeJS for signer tool
8. Hashicorp vault for managing user's certificates and secrets

## Onboarding flow

![onboarding.png](doc%2Fonboarding.png)

## WebAuthn through Keycloak

![webAuthn.png](doc%2FwebAuthn.png)

- During registration, the registering participant's user is created in the Keycloak. They will then receive a link in
  email which will allow them to register a device which supports webAuthn.
- After the device registration is successful, the user's legal participant generation will commence when they log in
  for the first time.
- The Keycloak realm configuration has been explained in the accompanying keycloak repository's Readme.

## Create service offer flow

![Service_Offer.png](doc%2FService_Offer.png)

**Creation of resources follows the same flow as Service Offer.**

## Known issues or improvements

1. Authentication and Authorization flow can be improved
2. Data exchange based on Gaia-x trust framework(Ocean protocol??)
3. Unit Test
4. K8S ingress and secret creation can be done using argoCD/argo workflow

## Run application

### Configuration

1. Create k8s user with access to ingress and secret creation.
2. Create AWS s3 bucket.
3. Create hosted zone in AWS with your base domain.
4. Create an AWS IAM user with access to the hosted zone and the S3 bucket.

### Running the application

This project contains a sample Env file - `wizard.env.example`, which contains all the properties used in this
application.

### Run in k8s

Please refer to the sample config files in `/k8s` folder.

## References

1. [Create SSL certificate using acme4j](https://github.com/shred/acme4j/blob/master/acme4j-example/src/main/java/org/shredzone/acme4j/example/ClientTest.java)
