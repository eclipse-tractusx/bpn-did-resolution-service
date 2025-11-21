Feature: BPN-DID Resolution Service(BDRS) Management CURD Test

  Background:
    Given BDRS host, keycloak host, client_id and client_secret
    And base wallet details are provided
    And generated new BPN-DID for CURD
    And access_token is created using client_id and client_secret

  @TEST_CS-2821
  Scenario: Validate base wallet BPN-DID mapping
    When admin requests the BPN-DID mapping using the API
    Then admin should receive the mapping information
    And the base wallet BPN-DID should present in the mapping

  @TEST_CS-2822
  Scenario: New BPN-DID must not present in the mapping
    When admin requests the BPN-DID mapping using the API
    Then delete the new BPN-DID if present mapping

  @TEST_CS-2823
  Scenario: Create a new BPN-DID mapping
    When admin creates a new BPN-DID mapping using the API
    Then the new BPN-DID mapping should be successfully created
    When admin requests the BPN-DID mapping using the API
    Then the new BPN-DID should present in the mapping

  @TEST_CS-2824
  Scenario: Update an existing BPN-DID mapping
    When admin updates the BPN-DID mapping using the API
    Then the BPN-DID mapping should be successfully updated
    When admin requests the BPN-DID mapping using the API
    Then the updated BPN-DID should present in the mapping

  @TEST_CS-2825
  Scenario: Update a non-existing BPN-DID mapping
    When admin has random BPN-DID mapping
    And admin updates the BPN-DID mapping with random BPN using the API
    Then verify http status it should be 404

  @TEST_CS-2826
  Scenario: Delete a non-existing BPN-DID mapping
    When admin has random BPN-DID mapping
    And admin deletes the BPN-DID mapping with random BPN using the API
    Then verify http status it should be 404

  @TEST_CS-2827
  Scenario: Delete an existing BPN-DID mapping
    When admin deletes the BPN-DID mapping using the API
    Then the BPN-DID mapping should be successfully delete
    When admin requests the BPN-DID mapping using the API
    Then the deleted BPN-DID should not present in the mapping

  @TEST_CS-4514
  Scenario: Try to add BPN-DID mapping with existing did
    When admin creates a new BPN-DID mapping using the API
    Then the new BPN-DID mapping should be successfully created
    Then admin tries to create a record with the same the did but different BPN
    Then verify API should return http status 409
    Then admin deletes the BPN-DID mapping using the API

  @TEST_CS-4515
  Scenario: Try to update BPN-DID mapping with existing did
    When admin creates a new BPN-DID mapping using the API
    Then the new BPN-DID mapping should be successfully created
    And admin creates another BPN-DID mapping using the API
    Then admin tries to update the second record with the doing of the first record
    Then verify API should return http status 409
    Then admin deletes the BPN-DID mapping using the API


  @TEST_CS-2828
  Scenario: Try to add BPN-DID mapping with invalid access_token
    When invalid access token created
    Then admin accesses POST API with invalid access token
    Then verify API should return http status 401

  @TEST_CS-2829
  Scenario: Try to update BPN-DID mapping with invalid access_token
    When invalid access token created
    Then admin accesses PUT API with invalid access token
    Then verify API should return http status 401

  @TEST_CS-2830
  Scenario: Try to delete BPN-DID mapping with invalid access_token
    When invalid access token created
    Then admin accesses DELETE API with invalid access token
    Then verify API should return http status 401

  @TEST_CS-2831
  Scenario: Attempt to update and delete mapping with an unauthorized data owner's token
    And admin creates a new BPN-DID mapping using the API
    And verify company BPN is added in allowlist
    When an access token is created using a company's client ID and client secret
    And an attempt is made to update the created record using the company's access token
    Then the HTTP status should be 403
    When an attempt is made to delete the created record using the company's access token
    Then the HTTP status should be 403