Example of Mutual Client Authorization in SpringBoot

**Quickstart**

Generate keystores and run server app followed by client test case.
```
cd mtls-springboot/bin
sh -x ./gen-non-prod-key.sh
cd ../server
mvn spring-boot:run
# In another shell
cd mtls-springboot/client
mvn test
```

**Summary**

This demo contains two SpringBoot applications that can be run to demonstrate mutual authorization. For mutual
authorization to occur the client must validate the server and the server must validate the client.

The first step, client validating the server, happens during the initial https request. In our example the client 
application's keystore `client-nonprod.jks` contains the server's public certificate. We use our keystore as a 
truststore so certificates returned from a server during a https request will be validated against our set of trusted
certificates. The full code for this can be seen in `ClientApplication.java`.
```
    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;
    @Value("${server.ssl.trust-store}")
    private Resource trustStore;

    ...
    
    // Load our trust store and key store containing certificates that we trust.
    SSLContext sslcontext =
            SSLContexts.custom().loadTrustMaterial(trustStore.getFile(), trustStorePassword.toCharArray())
                    .loadKeyMaterial(keyStore.getFile(), keyStorePassword.toCharArray(),
                            keyPassword.toCharArray()).build();
```


The second step, server validating the client, happens when the server validates the client's public certificate during
the TLSv1.2 Handshake (turn debugging on so you can see the following messages `-Djavax.net.debug=all`).

The client will send its public certificate
```
https-jsse-nio-8111-exec-4, READ: TLSv1.2 Handshake, length = 959
*** Certificate chain
chain [0] = [
[
  Version: V3
  Subject: CN=Client, OU=Client, O=PlumStep, L=San Francisco, ST=CA, C=U
  Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11

  Key:  Sun RSA public key, 2048 bits
  modulus: 23924815366191671527685604468707756921920367435263691229358087731529862243742791861749857441668937465882655232936400385161742990710489227264008217666996198481503102758954806108248423197429783435192038688889492757756275150038243065757169401424091252018478837180900174378000961676564908357097778694464840754820934412558396246978777685819277422908337420810360977522693088849841753891663744703592285824763759784328896409676461105804402578955544664436941232613361320667903301393140906588668044451841493172709143139866499595316834004329321057012333974423931211783507609409810080956508128654820187759105126624405395976512597
  public exponent: 65537
  Validity: [From: Tue Aug 30 20:46:33 PDT 2016,
               To: Mon Nov 28 19:46:33 PST 2016]
  Issuer: CN=Client, OU=Client, O=PlumStep, L=San Francisco, ST=CA, C=U
  SerialNumber: [    659d8227]
  ```
  
and then the server will validate that certificate against its truststore. Since the server's truststore contains
the client's public certificate it will find it and validate it.

```
***
 Found trusted certificate:
 [
 [
   Version: V3
   Subject: CN=Client, OU=Client, O=PlumStep, L=San Francisco, ST=CA, C=U
   Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11
 
   Key:  Sun RSA public key, 2048 bits
   modulus: 23924815366191671527685604468707756921920367435263691229358087731529862243742791861749857441668937465882655232936400385161742990710489227264008217666996198481503102758954806108248423197429783435192038688889492757756275150038243065757169401424091252018478837180900174378000961676564908357097778694464840754820934412558396246978777685819277422908337420810360977522693088849841753891663744703592285824763759784328896409676461105804402578955544664436941232613361320667903301393140906588668044451841493172709143139866499595316834004329321057012333974423931211783507609409810080956508128654820187759105126624405395976512597
   public exponent: 65537
   Validity: [From: Tue Aug 30 20:46:33 PDT 2016,
                To: Mon Nov 28 19:46:33 PST 2016]
   Issuer: CN=Client, OU=Client, O=PlumStep, L=San Francisco, ST=CA, C=U
   SerialNumber: [    659d8227]
```

The code for how the server loads its keystore and truststore is the same as in the client app and can be found in
`ServerApplication.java`.

The *default* profiles located at `src/main/resources/application.properties` under both the server and client
configures the SpringBoot applications to use SSL and to *need* client authorization via 
`server.ssl.client-auth=need`. You can also configure the server to `want` client authorization which will allow clients
to access the server without a certificate however this is dangerous if you do not have any other means of
authorization in your app.

**Notes**

1. The client application is a full SpringBoot app however we are just using the integration test support in
SpringBoot as an easy way to make a secure connection to the server.

2. To run this example you will first need to create a keystore with a public/private key for both the client and server
and import their public certificates in to the respective keystores.  A bash script is available `bin/gen-non-prod-key.sh` 
which will do this for you.  Re-running the script is non-destructive since keytool will not overwrite existing data. 
You must have the environment variable `JAVA_HOME` set and you **MUST** run the script from the bin directory. 

3. With logging turned on via `-Djavax.net.debug=all` you will be able to see that the server will validate the client 
certificate during a request

4. The password for the keystores is **changeme**

**Things to try**

1. Attempt to access the server's endpoint via a browser https://localhost:8111/server/ (**the trailing / is necessary**)
2. Remove the server public cert from the client's keystore and re-run.
3. Remove the client public cert from the server's keystore and re-run (**make sure to restore the server's public cert 
first if you did #2**).
