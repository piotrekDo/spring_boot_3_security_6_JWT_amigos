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

## JWT Authentication Filter

Filar naszych zabezpieczeń, filtr sprawdzający użytkownika. Tworzymy klasę rozszerzającą ``OncePerRequestFilter``. Musimy tutaj 
nadpisać metodę Zawierającą logikę.
```
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    }
```
**Pierwszą rzeczą którą musimy zrobić jest sprawdzenie tokena.**  
Pobieramy go z obiektu ``request`` poprzez header *Authorization*, sprawdzamy czy wartość istnieje i zaczyna się od "Bearer ".
Jeżeli nie, kończymy pracęfiltra poprzez wywołanie metody ``filterChain.doFilter``. Nie kończymy żądania, zostanie presłane dalej
do kolejnych filtrów, ale bez uwierzytelnienia użytkownika. W przeciwnym przypadku z pomocą metody ``substring`` wyciągamy 
właściwy token z nagłówka. 
```
final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
final String jwt;
if (authHeader == null || !authHeader.startsWith("Bearer ")){
    filterChain.doFilter(request, response);
    return;
}
jwt = authHeader.substring(7);
```
  
Następnie musimy uzyskać dane użytkonwnika z tokena. Można do tego posłużyć się klasą pomocniczą. Do jej działania 
potrzebujemy dodatkowej zależnośći dla obsługi tokenów JWT np.
```
<dependency>
    <groupId>io.jsonwebtoken</groupId>-
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```


