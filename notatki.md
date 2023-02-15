# Spring Boot 3, Security 6, JWT

Wysyłając żądanie HTTP do zabezpieczonego serwisu najpierw zostanie ueuchomiony filtr odpowiadający za autoryzację tokena.
W przypadku niepoprawnych danych, braku tokena lub nieaktualnego tokena filtr zwróci odpowiednią odpowiedź do klienta. 
  
Sam filtr współpracuje z serwisem, odpytując go o dane klienta uzyskane z tokena. Serwis z kolei odpyta bazę danych lub inny serwis 
odpowiedzialny za przechowywanie danych o użytkownikach aplikacji.  
Jeżeli walidacja klienta się powiodła musimy zwalidować token w komunikacji z serwisem JWT sprawdzając poprawnośc i aktualność
tokena.  
W przypadku gdy i token nie zawiera błędów musimy ustawić w kontekście informacje o uwierzytelnionym użytkowniku.   
Następnie żądanie jest przesyłane do *Dispatcher Servlet* i dalej do odpowiedniego kontrolera. 

## User
Spring wykorzystuje interfejs *UserDetails* do identyfikowania użytkowników. Można stworzyć adapter pozwalający
na przekształcenie naszego biznesowego usera w ten rozumiany przez springa. Naeży więc stworzyć klasę implementującą
``UserDetails``. Implementując interfejs ``UserDetails`` należy zwrócić uwagę na wymagane metody domyślnie zwracające ``false``
Jeżeli nie zamierzmy z nich korzystać należy pamiętać o zmianie tych wartośći na ``true``, w przeciwnym przypadku napotakamy
na nieoczekiwane zachowania. Np: 
```
@Override
public boolean isAccountNonLocked() {
    return false;
}
```
Domyślna implementacja zwracająca ``false`` oznacza, że użytkownik jest zablokowany!  
  
Alternatywą jest rozszerzenie klasy ``org.springframework.security.core.userdetails.User`` czyli domyślnej impementacji
interfejsu ``UserDetails`` zawartej w Spring. Implementacja interfejsu daje jednak szersze możliwości.  
  
Pierwszą ważną metodą do nadpisania sią role użytkownika. Musimy tutaj dostarczyć kolekcję rozszerzającą ``GrantedAuthority``.
Możemy utworzyć listę obiektów ``SimpleGrantedAuthority`` które w konstruktorze wymagają stringa oznaczającego rolę.
```
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(appUser.getRole().name()));
}
```