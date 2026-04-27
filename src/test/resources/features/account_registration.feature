Feature: Account registration

  Scenario: Creating a guest account links it to a guest profile and hashes the password
    When I create a guest account with username "guest_pedro" and password "secret123"
    Then the guest account should be linked to its guest profile
    And the stored password for username "guest_pedro" should be hashed instead of "secret123"

  Scenario: Creating a receptionist account stores a hashed password without guest linkage
    When I create a receptionist account with username "frontdesk_ana" and password "deskpass1"
    Then the receptionist account should not be linked to a guest profile
    And the stored password for username "frontdesk_ana" should be hashed instead of "deskpass1"
