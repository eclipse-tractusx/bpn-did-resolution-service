package com.cofinityx.bdrs.test.cucumber.stepdefs.management;


import com.cofinityx.bdrs.test.auth.KeycloakService;
import com.cofinityx.bdrs.test.config.Configuration;
import com.cofinityx.bdrs.test.utils.CommonUtils;
import com.cofinityx.bdrs.test.utils.StringPool;
import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;

/**
 * This class contains step definitions for managing BPN-DID mappings in a BPN DID resolution system.
 * It uses the Cucumber framework for BDD testing.
 * <p>
 * The class includes methods for setting up the testing environment, interacting with the API,
 * and verifying the results of the API calls.
 * <p>
 * The class uses the OkHttpClient for making HTTP requests to the API, and the Assert class for
 * making assertions about the results.
 * <p>
 * The class also uses the Lombok library for logging, and the Apache Commons Lang library for
 * generating random strings.
 */
@Slf4j
public class ManagementSteps {

    private OkHttpClient client;
    private String bdrsHost;
    private String keycloakHost;
    private String clientId;
    private String clientSecret;
    private String realm;
    private String invalidToken;
    private String accessToken;
    private String issuerDid;
    private String issuerBpn;
    private Map<String, String> bpnDidList;
    private String newDid;
    private String newBpn;
    private String randomBpn;
    private Response response;
    private String companyAccessToken;
    private String anotherBpn;
    private String anotherDid;


    @Before
    public void SetUp() {
        log.info("Setting up bdrs app test");
        client = new OkHttpClient.Builder()
                .build();
        bdrsHost = null;
        keycloakHost = null;
        clientId = null;
        clientSecret = null;
        realm = null;
        issuerBpn = null;
        issuerDid = null;
        bpnDidList = null;
        newDid = null;
        response = null;
        accessToken = null;
        companyAccessToken = null;
        anotherBpn = null;
        anotherDid = null;
    }

    /**
     * Sets up the necessary configuration parameters for BDRS and Keycloak.
     */
    @Given("BDRS host, keycloak host, client_id and client_secret")
    public void bdrsHostAndXRayKey() {
        // Retrieve BDRS and Keycloak configuration parameters
        bdrsHost = Configuration.getBDRSManagementHost();
        keycloakHost = Configuration.getKeycloakUrl();
        clientId = Configuration.getKeycloakClientId();
        clientSecret = Configuration.getKeycloakClientSecret();
        realm = Configuration.getKeycloakRealm();
    }


    /**
     * This function retrieves the base wallet details (issuer DID and BPN) from the configuration.
     * It sets the values of the issuerDid and issuerBpn variables based on the retrieved configuration.
     */
    @And("base wallet details are provided")
    public void baseWalletDetailsAreProvided() {
        issuerDid = Configuration.getIssuerDid();
        issuerBpn = Configuration.getIssuerBpn();
    }

    @And("generated new BPN-DID for CURD")
    public void generateNewBPNDIDForCURD() {
        newBpn = StringPool.NEW_BPN;
        newDid = "did:web:localhost" + newBpn;
    }

    @And("access_token is created using client_id and client_secret")
    public void createAccessToken() {
        accessToken = KeycloakService.getAccessToken(keycloakHost, realm, clientId, clientSecret);
    }


    @When("admin requests the BPN-DID mapping using the API")
    public void adminRequestsTheBPNDIDMappingUsingTheAPI() {
        Request request = getRequest(StringPool.BPN_DIRECTORY_API, StringPool.GET, null);
        try (Response r = client.newCall(request).execute()) {
            Assert.assertEquals(r.code(), HttpStatus.SC_OK);
            bpnDidList = CommonUtils.getObjectMapper().readValue(r.body().string(), Map.class);
        } catch (Exception e) {
            log.error("Error while get bpn-did mapping", e);
            Assert.fail("Error while get bpn-did mapping");
        }
    }

    @Then("admin should receive the mapping information")
    public void adminShouldReceiveTheMappingInformation() {
        Assert.assertNotNull(bpnDidList);
    }

    @And("the base wallet BPN-DID should present in the mapping")
    public void theBaseWalletBPNDIDShouldPresentInTheMapping() {
        Assert.assertTrue(bpnDidList.containsKey(issuerBpn));
        String did = bpnDidList.get(issuerBpn);
        Assert.assertEquals(did, issuerDid);
    }

    @NotNull
    private Request getRequest(String endpoint, String method, RequestBody body) {
        return getRequest(endpoint, method, body, accessToken);
    }

    private Request getRequest(String endpoint, String method, RequestBody body, String token) {
        return
                new Request.Builder()
                        .url(
                                Objects.requireNonNull(HttpUrl.parse(
                                                bdrsHost + StringPool.MANAGEMENT_API + endpoint))
                                        .url())
                        .method(method, body)
                        .addHeader(HttpHeaders.AUTHORIZATION, token)
                        .build();
    }

    @SneakyThrows
    @When("admin creates a new BPN-DID mapping using the API")
    public void adminCreatesANewBPNDIDMappingUsingTheAPI() {
        RequestBody body = getRequestBodyWithNewBpnAndDid(newBpn, newDid);
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API, StringPool.POST, body), "Error while post bpn-did mapping");
    }

    @Then("the new BPN-DID mapping should be successfully created")
    public void theNewBPNDIDMappingShouldBeSuccessfullyCreated() {
        CommonUtils.verifyHttpStatus(response, HttpStatus.SC_NO_CONTENT);
    }

    @SneakyThrows
    @Then("admin tries to create a record with the same the did but different BPN")
    public void adminTriesToCreateARecordWithTheSameTheDIDButDifferentBPN() {
        String differentBpn = "BPNLQVBEAELV3Y5L";
        RequestBody body = getRequestBodyWithNewBpnAndDid(differentBpn, newDid);
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API, StringPool.POST, body), "Error while post bpn-did mapping");
    }

    @Then("verify API should return http status 409")
    public void verifyHttpStatus409() {
        CommonUtils.verifyHttpStatus(response, HttpStatus.SC_CONFLICT);
    }

    @SneakyThrows
    @And("admin creates another BPN-DID mapping using the API")
    public void adminCreatesAnotherBPNDIDMappingUsingTheAPI() {
        anotherBpn = CommonUtils.getRandomBpmNumber();
        anotherDid = "did:web:localhost:"+anotherBpn;
        RequestBody body = getRequestBodyWithNewBpnAndDid(anotherBpn, anotherDid);
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API, StringPool.POST, body), "Error while post bpn-did mapping");
    }

    @SneakyThrows
    @Then("admin tries to update the second record with the doing of the first record")
    public void adminTriesToUpdateTheSecondRecordWithTheDoingOfTheFirstRecord() {
        RequestBody body = getRequestBodyWithNewBpnAndDid(anotherBpn, newDid);
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API, StringPool.PUT, body), "Error while put bpn-did mapping");
    }


    @Then("the new BPN-DID should present in the mapping")
    public void theNewBPNDIDShouldPresentInTheMapping() {
        Assert.assertNotNull(bpnDidList);
        String did = bpnDidList.get(newBpn);
        Assert.assertEquals(did, newDid);
    }

    @Then("delete the new BPN-DID if present mapping")
    public void deleteNewMappingIfExist() {
        if (bpnDidList.containsKey(newBpn)) {
            Request request = getRequest(StringPool.BPN_DIRECTORY_API + "/" + newBpn, StringPool.DELETE, null);
            try (Response r = client.newCall(request).execute()) {
                response = r;
                Assert.assertEquals(r.code(), HttpStatus.SC_NO_CONTENT);
            } catch (Exception e) {
                log.error("Error while put bpn-did mapping", e);
                Assert.fail("Error while put bpn-did mapping");
            }
        } else {
            Assert.assertNotNull(bpnDidList);
            Assert.assertFalse(bpnDidList.containsKey(newBpn));
            Assert.assertFalse(bpnDidList.containsValue(newDid));
        }
    }


    @When("admin has random BPN-DID mapping")
    public void adminHasRandomBPNDIDMapping() {
        randomBpn = CommonUtils.getRandomBpmNumber();
    }

    @SneakyThrows
    @When("admin updates the BPN-DID mapping using the API")
    public void adminUpdatesTheBPNDIDMappingUsingTheAPI() {
        RequestBody body = getRequestBodyWithNewBpnAndDid(newBpn, StringPool.UPDATE_DID);
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API, StringPool.PUT, body), "Error while put bpn-did mapping");
    }

    @SneakyThrows
    @Then("admin accesses PUT API with invalid access token")
    public void adminAccessPutAPIWithInvalidAccessToken() {
        RequestBody body = getRequestBodyWithNewBpnAndDid(newBpn, StringPool.UPDATE_DID);
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API, StringPool.PUT, body, invalidToken), "Error while put bpn-did mapping");
    }

    @SneakyThrows
    @When("admin updates the BPN-DID mapping with random BPN using the API")
    public void adminUpdatesTheBPNDIDWithRandomBpnMappingUsingTheAPI() {
        RequestBody body = getRequestBodyWithNewBpnAndDid(randomBpn, StringPool.UPDATE_DID);
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API, StringPool.PUT, body), "Error while put bpn-did mapping");
    }

    @Then("verify http status it should be 404")
    public void verifyHttpStatus404() {
        CommonUtils.verifyHttpStatus(response, HttpStatus.SC_NOT_FOUND);
    }


    @Then("the BPN-DID mapping should be successfully updated")
    public void theBPNDIDMappingShouldBeSuccessfullyUpdated() {
        CommonUtils.verifyHttpStatus(response, HttpStatus.SC_NO_CONTENT);
    }

    @Then("the updated BPN-DID should present in the mapping")
    public void theUpdatedBPNDIDShouldPresentInTheMapping() {
        Assert.assertNotNull(bpnDidList);
        String did = bpnDidList.get(newBpn);
        Assert.assertEquals(did, StringPool.UPDATE_DID);
    }

    @When("admin deletes the BPN-DID mapping using the API")
    public void adminDeletesTheBPNDIDMappingUsingTheAPI() {
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API + "/" + newBpn, StringPool.DELETE, null), "Error while delete bpn-did mapping");
    }

    @Then("admin accesses DELETE API with invalid access token")
    public void adminAccessDeleteAPIWithInvalidToken() {
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API + "/" + newBpn, StringPool.DELETE, null, invalidToken), "Error while delete bpn-did mapping");
    }

    @When("admin accesses POST API with invalid access token")
    public void adminDeletesTheBPNDIDMappingWithInvalidTokenUsingTheAPI() {
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API + "/" + newBpn, StringPool.DELETE, null, invalidToken), "Error while put bpn-did mapping");
    }

    @Then("verify API should return http status 401")
    public void verifyHttpStatus403() {
        CommonUtils.verifyHttpStatus(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @When("admin deletes the BPN-DID mapping with random BPN using the API")
    public void adminDeletesTheBPNDIDMappingWithRandomBpnUsingTheAPI() {
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API + "/" + randomBpn, StringPool.DELETE, null), "Error while deleting bpn-did mapping");
    }

    @When("invalid access token created")
    public void createInvalidAccessToken() {
        invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    @Then("the BPN-DID mapping should be successfully delete")
    public void theBPNDIDMappingShouldBeSuccessfullyDelete() {
        CommonUtils.verifyHttpStatus(response, HttpStatus.SC_NO_CONTENT);
    }

    @Then("the deleted BPN-DID should not present in the mapping")
    public void theDeletedBPNDIDShouldNotPresentInTheMapping() {
        Assert.assertNotNull(bpnDidList);
        Assert.assertFalse(bpnDidList.containsKey(newBpn));
        Assert.assertFalse(bpnDidList.containsValue(newDid));
        Assert.assertFalse(bpnDidList.containsValue(StringPool.UPDATE_DID));
    }

    @SneakyThrows
    @And("verify company BPN is added in allowlist")
    public void verifyCompanyBPNAllowlist() {
        RequestBody requestBody = RequestBody.create(CommonUtils.getObjectMapper().writeValueAsString(Map.of(StringPool.BPN, Configuration.getCompanyBpn())), MediaType.parse(StringPool.JSON_MEDIA_TYPE));
        sendApiRequest(getRequest(StringPool.ALLOW_LIST_API, StringPool.POST, requestBody, accessToken), "Error while adding allowlist");
    }

    @When("an access token is created using a company's client ID and client secret")
    public void getCompanyAccessToken() {
        companyAccessToken = KeycloakService.getAccessToken(keycloakHost, realm, Configuration.getKeycloakCompanyClientId(), Configuration.getKeycloakCompanyClientSecret());
    }

    @And("an attempt is made to update the created record using the company's access token")
    @SneakyThrows
    public void updatesTheBPNDIDWithRandomBpnMappingUsingTheCompanyAccessToken() {
        RequestBody body = getRequestBodyWithNewBpnAndDid(newBpn, StringPool.UPDATE_DID);
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API, StringPool.PUT, body, companyAccessToken), "Error while put bpn-did mapping");
    }

    @And("an attempt is made to delete the created record using the company's access token")
    public void deletedTheBPNDIDWithRandomBpnMappingUsingTheCompanyAccessToken() {
        sendApiRequest(getRequest(StringPool.BPN_DIRECTORY_API + "/" + newBpn, StringPool.DELETE, null, companyAccessToken), "Error while delete bpn-did mapping");
    }


    @Then("the HTTP status should be 403")
    public void verifyHttpStatus403ForCompanyAPI() {
        CommonUtils.verifyHttpStatus(response, HttpStatus.SC_FORBIDDEN);
    }

    private @NotNull RequestBody getRequestBodyWithNewBpnAndDid(String newBpn, String newDid) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put(StringPool.BPN, newBpn);
        map.put(StringPool.DID, newDid);
        String json = CommonUtils.getObjectMapper().writeValueAsString(map);
        return RequestBody.create(json, MediaType.parse(StringPool.JSON_MEDIA_TYPE));
    }

    private void sendApiRequest(Request request, String s) {
        try (Response r = client.newCall(request).execute()) {
            response = r;
        } catch (Exception e) {
            log.error(s, e);
            Assert.fail(s);
        }
    }
}
