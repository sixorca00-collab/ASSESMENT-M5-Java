Feature: Guest booking access

  Scenario: A guest only sees bookings linked to their own profile
    Given there is a guest account "guest_pedro" with password "secret123"
    And there is a guest account "guest_maria" with password "secret456"
    And guest "guest_pedro" has an occupied booking
    And guest "guest_maria" has an occupied booking
    When I log in as "guest_pedro" with password "secret123"
    And I load the bookings visible for the logged guest
    Then I should only see 1 booking for the logged guest
    And every visible booking should belong to username "guest_pedro"
