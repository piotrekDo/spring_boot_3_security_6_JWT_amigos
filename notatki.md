# Spring Boot 3, Security 6, JWT

Wysyłając żądanie HTTP do zabezpieczonego serwisu najpierw zostanie ueuchomiony filtr odpowiadający za autoryzację
tokena.
W przypadku niepoprawnych danych, braku tokena lub nieaktualnego tokena filtr zwróci odpowiednią odpowiedź do klienta.

Sam filtr współpracuje z serwisem, odpytując go o dane klienta uzyskane z tokena. Serwis z kolei odpyta bazę danych lub
inny serwis
odpowiedzialny za przechowywanie danych o użytkownikach aplikacji.  
Jeżeli walidacja klienta się powiodła musimy zwalidować token w komunikacji z serwisem JWT sprawdzając poprawnośc i
aktualność
tokena.  
W przypadku gdy i token nie zawiera błędów musimy ustawić w kontekście informacje o uwierzytelnionym użytkowniku.   
Następnie żądanie jest przesyłane do *Dispatcher Servlet* i dalej do odpowiedniego kontrolera.

## User

Spring wykorzystuje interfejs *UserDetails* do identyfikowania użytkowników. Można stworzyć adapter pozwalający
na przekształcenie naszego biznesowego usera w ten rozumiany przez springa. Naeży więc stworzyć klasę implementującą
``UserDetails``. Implementując interfejs ``UserDetails`` należy zwrócić uwagę na wymagane metody domyślnie
zwracające ``false``
Jeżeli nie zamierzmy z nich korzystać należy pamiętać o zmianie tych wartośći na ``true``, w przeciwnym przypadku
napotakamy
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

Pierwszą ważną metodą do nadpisania sią role użytkownika. Musimy tutaj dostarczyć kolekcję
rozszerzającą ``GrantedAuthority``.
Możemy utworzyć listę obiektów ``SimpleGrantedAuthority`` które w konstruktorze wymagają stringa oznaczającego rolę.

```
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(appUser.getRole().name()));
}
```

## JWT Authentication Filter

Filar naszych zabezpieczeń, filtr sprawdzający użytkownika. Tworzymy klasę rozszerzającą ``OncePerRequestFilter``.
Musimy tutaj
nadpisać metodę Zawierającą logikę.

```
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    }
```

**Pierwszą rzeczą którą musimy zrobić jest sprawdzenie tokena.**  
Pobieramy go z obiektu ``request`` poprzez header *Authorization*, sprawdzamy czy wartość istnieje i zaczyna się od "
Bearer ".
Jeżeli nie, kończymy pracęfiltra poprzez wywołanie metody ``filterChain.doFilter``. Nie kończymy żądania, zostanie
presłane dalej
do kolejnych filtrów, ale bez uwierzytelnienia użytkownika. W przeciwnym przypadku z pomocą metody ``substring``
wyciągamy
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

Dalej sprawdzamy czy nazwa użytkownika nie jest nullem oraz czy uwieżytelnianie już się nie odbyło. Wówczas
sięgamy do serwisu i odpytujemy o użytkownika z nazwą podaną w tokenie. Mając *userDetails* możemy sprawdzić czy
otrzymany token jest poprawny i jeżeli tak to aktualizujemy kontekst Springa z pomocą obiektu ``UsernamePasswordAuthenticationToken``
opcjonalnie przekazujemy do niego ``details`` uzyskane z pomocą obiektu ``WebAuthenticationDetailsSource`` do którego
przekazujemy obiekt ``request``. Ostatecznie kończymy pracę filtra wywołaniem metody ``filterChain.doFilter(request, response);``.

## Konfiguracja Spring Security

Mając gotowy filtr musmimy jeszcze skonfigurować Spring aby z niego korzystał. Domyślną implementacją zabezpieczeń jest
*simple security*. Tworzymy więc klasę konfiguracyjną oznaczoną adnotacjami ``@Configuration``
oraz ``@EnableWebSecurity``.

Spring seecurity przy starcie aplikacji szuka bena ``SecurityFilterChain`` przyjmującyego w parametrach
obiekt ``HttpSecurity`` który powinniśmy utworzyć aby zastąpić domyślne ustawienia.  
Na początku konfigurujemy *csrf* oraz pamiętamy o ``http.sessionManagement().sessionCreationPolicy(STATELESS);``  
W treciej wersji springa zmieniła się implementacja konfiguracji. Nie stosujemy już metod ``authorizeRequests().antMatchers()``.
Zamiast tego używamy ``http.authorizeHttpRequests().requestMatchers("")``
