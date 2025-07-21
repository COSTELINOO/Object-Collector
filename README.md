# WEB_OCo_Arseni-Baltag

Echipa formata din:

Arseni Costel-Ionut
Baltag Bianca-Teodora

Link prezentare: https://www.youtube.com/playlist?list=PLm7amvSWlOloMAHGJspcujjO5i4rNgakT

Diagrame C4
![c4-level 1](https://github.com/user-attachments/assets/df189ae8-e3e8-44dc-8af1-55dc0aa29c8b)
![c4-level2](https://github.com/user-attachments/assets/43a81d0f-37b7-4306-a9df-f5fa860f4549)
![c4-level 3 (2)](https://github.com/user-attachments/assets/5004e314-a93d-4a92-9dd3-a55495cacf34)

# 📚 Documentatie Backend - Collect.me

> **Publicat de**: Echipa Collect.me  
> **Data**: 25 iunie 2025

---

## Cuprins

1. [Prezentare generala](#1-prezentare-generala)
2. [Arhitectura sistemului](#2-arhitectura-sistemului)
3. [Functionalitati principale](#3-functionalitati-principale)
4. [Securitate](#4-securitate)
5. [Tehnologii si librarii](#5-tehnologii-si-librarii)
6. [Detalii de implementare](#6-detalii-de-implementare)
7. [Consideratii finale](#7-consideratii-finale)

---

## 1. Prezentare generala

Collect.me este o aplicatie web pentru colectionari, care permite catalogarea, organizarea si partajarea colectiilor personale.

Tipuri suportate:

- Monede
- Tablouri
- Timbre
- Viniluri
- Colectii personalizate (cu atribute definite de utilizator)

Backend-ul se ocupa cu:

- Procesarea logicii aplicatiei
- Autentificare/autorizare
- Interactiunea cu baza de date
- Expunerea API-urilor
- Securitate si validare

---

## 2. Arhitectura sistemului

### 2.1 Structura stratificata

- **Prezentare (API)**: HttpServer, controllere, filtre
- **Servicii (Business Logic)**: UserService, DTO, validatori
- **Persistenta**: Repository-uri, entitati, DatabaseManager
- **Utilitare**: JSON, email, rapoarte, logging
- **Securitate**: JWT, CORS, XSS, criptare

### 2.2 Module cheie

- **Security**: `JwtUtil`, `JwtFilter`, `CorsFilter`, `XSSPreventionFilter`
- **Controllers**: `AuthController`, `CollectionController`, `ObjectController`, `StatisticsController`
- **Repositories**: `UserRepository`, `ObjectRepository`, etc.
- **Utilities**: `EmailUtil`, `JsonUtil`, `PdfGenerator`, `XmlGenerator`

### 2.3 Fluxul de date

1. HTTP Request
2. Filtrare: CORS → XSS → JWT
3. Controller → Service → Repository
4. DTO → Response
5. HTTP Response

---

## 3. Functionalitati principale

### 3.1 Gestiune utilizatori

- Înregistrare cont cu email
- Autentificare cu JWT
- Resetare parola prin email
- Gestionare profil
- Deconectare si stergere cont

### 3.2 Gestiune colectii

- Creare colectie (tip standard sau personalizata)
- Actualizare / stergere
- Vizualizare colectii publice / private
- Statistici colectie

### 3.3 Gestiune obiecte

- Adaugare obiect cu imagine
- Editare si stergere
- Vizualizare obiecte
- Vizualizari, aprecieri (likes)

### 3.4 Statistici & rapoarte

- Statistici personale si globale
- Export în PDF, CSV
- Feed RSS

---

## 4. Securitate

### 4.1 Autentificare JWT

- Token include: username, email, id, codeSession
- Expirare implicita: 8h
- Validare: semnatura + expirare + codeSession
- Invalidare sesiune la logout

### 4.2 Protectie XSS

- `XSSPreventionFilter` detecteaza cod malitios
- Regex, scanari JSON, analiza multipart
- Raspuns 400 + log MALICIOUS

### 4.3 CORS

- Filtru `CorsFilter`
- Configurabil din `application.properties`
- Suport pentru preflight (OPTIONS)

### 4.4 Validare date

- Controller: validare format, parametri
- Service: validare business logic
- Validatori: email, campuri personalizate

### 4.5 Securitatea bazei de date

- `PreparedStatement` în loc de string SQL direct
- Parametrare 100%
- Validare input înainte de DB
- Închidere conexiuni corect

---

## 5. Tehnologii si librarii

- **Java 11+**
- **HttpServer** (Java native)
- **JDBC** pentru acces DB
- **Gson** pentru JSON
- **jjwt** pentru JWT
- **iText 7** pentru PDF
- **Jakarta Mail** pentru email
- **JAXP** pentru XML/RSS

---

## 6. Detalii de implementare

### 6.1 Structura cod

- `controller/` – controllere HTTP
- `service/` – logica aplicatiei
- `repository/` – interfata cu baza de date
- `model/` – entitati persistente
- `util/` – email, JSON, logging, multipart
- `config/` – proprietati, JWT, DB

### 6.2 Configurare aplicatie

- `application.properties`:
  - DB: URL, user, parola
  - CORS: originile permise
  - Email: SMTP + parole
  - Path fisiere încarcate

### 6.3 Gestionare erori

- Ierarhie de exceptii: `CustomException`, `Exception400`, `Exception500`
- `Logger` cu nivele (ERROR, WARNING, INFO, MALICIOUS)
- Formatare JSON automata raspunsuri de eroare

### 6.4 Initializare sistem

1. Încarcare `application.properties`
2. Initializare conexiune DB
3. Verificare tabele
4. Start server HTTP + rute + filtre
5. Generare RSS si statistici

---

## 7. Consideratii finale

Backend-ul aplicatiei **Collect.me** ofera o platforma scalabila, modulara si sigura pentru gestionarea colectiilor. Documentatia serveste drept referinta pentru dezvoltatori, asigurand mentenanta si extensibilitatea sistemului în timp.

---

# 🎨 Documentatie Front-End - Collect.me

> **Ultima actualizare**: 25 iunie 2025  
> **Platforma**: Aplicatie web pentru colectionari

---

## Cuprins

1. [Introducere](#1-introducere)
2. [Prezentare generala a aplicatiei](#2-prezentare-generala-a-aplicatiei)
3. [Arhitectura tehnica](#3-arhitectura-tehnica)
4. [Componente Front-End](#4-componente-front-end)
5. [Design UI](#5-design-ui)
6. [Sistem de autentificare](#6-sistem-de-autentificare)
7. [Cazuri de utilizare](#7-cazuri-de-utilizare)
8. [Integrare Back-End](#8-integrare-back-end)
9. [Gestionarea erorilor](#9-gestionarea-erorilor)
10. [Design Responsive](#10-design-responsive)
11. [Accesibilitate](#11-accesibilitate)
12. [Securitate](#12-securitate)
13. [Concluzie](#13-concluzie)

---

## 1. Introducere

Aceasta documentatie ofera o prezentare completa a implementarii front-end a aplicatiei **Collect.me**, care permite utilizatorilor sa creeze, administreze si partajeze colectiile lor. Sunt acoperite structura, tehnologiile, componentele si interactiunea cu back-end-ul.

---

## 2. Prezentare generala a aplicatiei

Functionalitati principale:

- Autentificare (login/înregistrare)
- Gestionare colectii (CRUD)
- Gestionare obiecte (CRUD)
- Explorare colectii publice
- Statistici si vizualizare date
- Gestionare profil
- Export colectii/statistici în PDF/CSV

---

## 3. Arhitectura tehnica

### Tehnologii:

- **HTML5** – structura
- **CSS** – design responsive
- **JavaScript** – logica aplicatiei
- **Fetch API** – comunicare cu API

### Structura fisierelor:

```
front-end/
├── images/
├── scripts/
│   ├── auth-middleware.js
│   ├── global.js
│   └── index.js
├── styles/
│   ├── global1.css
│   ├── auth1.css
│   └── collections.css
└── *.html
```

---

## 4. Componente Front-End

### Structura HTML

Toate paginile respecta structura:

```html
<html>
  <head>
    ...
  </head>
  <body>
    <header>...</header>
    <nav>...</nav>
    <main>...</main>
    <script>
      ...
    </script>
  </body>
</html>
```

### Pagini principale

- Autentificare: `login.html`, `register.html`, `reset-password*.html`
- Functionale: `collections.html`, `add-object.html`, `statistics.html`, `explore.html`, `profile.html`

### CSS

- `global1.css` – stiluri generale
- Pagini dedicate: `collections.css`, `view-object.css` etc.

### JS

- `auth-middleware.js` – protejeaza rutele
- `global.js` – utilitare
- `fetch.js` – apeluri API

---

## 5. Design UI

### Componente UI

- Header, Sidebar, Main Area
- Carduri pentru colectii/obiecte
- Formulare validate
- Dialoguri (modale) pentru confirmari si alerte

### Stilizare

- Paleta mov + neutre
- Tipografie web-safe
- Spatiere consistenta
- Componente reutilizabile

---

## 6. Sistem de autentificare

### Fluxuri:

- **Login**: `/auth/login` → JWT în `localStorage`
- **Înregistrare**: validare + `/auth/register`
- **Middleware**: redirect daca token-ul lipseste
- **Resetare parola**: 2 pasi prin email
- **Logout**: elimina token, notifica backend

---

## 7. Cazuri de utilizare

### Autentificare:

- UC1: Înregistrare utilizator
- UC2: Login utilizator
- UC3: Resetare parola

### Colectii:

- UC4: Creare colectie
- UC5: Vizualizare
- UC6: Editare
- UC7: stergere

### Obiecte:

- UC8: Adaugare
- UC9: Vizualizare
- UC10: Editare
- UC11: stergere

### Explorare & statistici:

- UC12: Explorare publica
- UC13: Vizualizare obiect public
- UC14: Vizualizare statistici personale
- UC15: Export date

---

## 8. Integrare Back-End

### Endpoint-uri API

#### Autentificare:

- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/reset-password`

#### Colectii:

- `GET/POST /user-collection`
- `PUT/DELETE /user-collection/{id}`

#### Obiecte:

- `GET /objects`, `POST /my-collection/{id}`

#### Statistici:

- `GET /statistics/personal/pdf`
- `GET /statistics/public/csv`
- `GET /statistics/public/rss`

### Flux API:

1. Adauga token JWT în header
2. Trimite cerere
3. Primeste raspuns JSON
4. Proceseaza si gestioneaza erorile

---

## 9. Gestionarea erorilor

### Validare client-side

- Campuri obligatorii
- Format email / parola
- Prevenire XSS (sanitizare input)

### Erori API

- Status 400/401/403/404/500
- Feedback UI pentru utilizator

---

## 10. Design Responsive

### Breakpoints:

- Mobile: `<767px`
- Tableta: `768–999px`
- Desktop: `1000px+`
- Large desktop: `1200px+`

Tehnici: grile fluide, media queries, imagini flexibile.

---

## 11. Accesibilitate

- Etichete corecte pentru formulare
- Contrast bun pentru text
- Navigare completa cu tastatura

---

## 12. Securitate

### Client-side:

- Protectie XSS (sanitizare input/output)
- CSRF cu token si `SameSite`
- Gestionare expirare JWT

### Retea:

- API exclusiv prin HTTPS
- Rate limiting
- Endpoint-uri protejate

### Date sensibile:

- Minimizarea stocarii pe client
- Fara parole în clar
- stergere dupa utilizare

---

## 13. Concluzie

Front-end-ul **Collect.me** ofera o interfata moderna si sigura pentru gestionarea colectiilor. Este scalabil, modular si usor de extins. Respecta principiile UX/UI moderne, design responsive si accesibilitate, integrandu-se perfect cu backend-ul REST.

### 🔧 Posibile îmbunatatiri:

- Suport multilingv
- Accesibilitate avansata
- Cautare inteligenta
- Partajare colectii în retele sociale

---
