# Spring Boot 3, Security 6, JWT

Wysyłając żądanie HTTP do zabezpieczonego serwisu najpierw zostanie ueuchomiony filtr odpowiadający za autoryzację tokena.
W przypadku niepoprawnych danych, braku tokena lub nieaktualnego tokena filtr zwróci odpowiednią odpowiedź do klienta. 
  
Sam filtr współpracuje z serwisem, odpytując go o dane klienta uzyskane z tokena. Serwis z kolei odpyta bazę danych lub inny serwis 
odpowiedzialny za przechowywanie danych o użytkownikach aplikacji.  
Jeżeli walidacja klienta się powiodła musimy zwalidować token w komunikacji z serwisem JWT sprawdzając poprawnośc i aktualność
tokena.  
W przypadku gdy i token nie zawiera błędów musimy ustawić w kontekście informacje o uwierzytelnionym użytkowniku.   
Następnie żądanie jest przesyłane do *Dispatcher Servlet* i dalej do odpowiedniego kontrolera. 