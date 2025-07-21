# Documentație Proiect Web OCo (Object Collector on Web)

## Contribuitori
- **Back-End:** [COSTELINOO](https://github.com/COSTELINOO)
- **Front-End:** [biancabaltag](https://github.com/biancabaltag)

## Cerința problemei

Să se dezvolte o aplicație Web destinată colecționarilor de obiecte de interes (e.g., dopuri de plută, mărci poștale, jucării, discuri de vinil, autografe, articole vestimentare, echipamente electronice,...). Pe baza facilităților de căutare multi-criterială implementate, utilizatorii autentificați vor putea crea, inventaria și partaja (sub)colecții de recipiente în funcție de diverse caracteristici (tip, imagine, valoare, țară, perioadă de utilizare, istoric, existența etichetei etc.), plus vor putea importa/exporta datele referitoare la obiectele deținute. Se vor genera statistici diverse ce pot fi exportate în formate deschise – minimal, CSV și PDF. Se va realiza, de asemenea, un clasament al celor mai populare (categorii de) obiecte/colecții, disponibil și ca flux de date RSS.

## Restricții și limitări

- Proiectele vor putea fi implementate pe partea de server folosind orice tehnologie, platformă şi limbaj de programare actuale, cu condiţia ca acestea să adopte o licenţă deschisă.
- Nu se permite utilizarea de framework-uri la nivel de client (front-end) şi/sau server (back-end) Web.
- Architectura aplicaţiei Web realizate va fi obligatoriu bazată pe servicii Web.
- Pentru partea de client, interfaţa aplicaţiei Web va fi marcată obligatoriu în HTML5 – codul trebuind să fie valid conform specificaţiilor Consorţiului Web. Se vor utiliza foi de stiluri CSS valide – pentru verificare, se poate recurge la instrumente dedicate precum Stylelint.
- Pentru stocarea şi managementul datelor, se vor putea utiliza servere de baze de date relaţionale, interogate via SQL – minimal, a se considera SQLite. Complementar, se poate recurge la servere de baze de date aliniate paradigmei NoSQL.
- Se vor folosi pe cât posibil machete (template-uri) de prezentare şi metode de configurare şi administrare a aplicaţiei.
- Adoptarea principiilor designului Web responsiv.
- Recurgerea la tehnici de prevenire a atacurilor (minimal, SQL injection şi Cross Site Scripting).
- Import/export de date folosind formate deschise – minim, CSV (Comma Separated Values) şi JSON (JavaScript Object Notation).
- Respectarea cerinţelor de bază ale ingineriei software – e.g., comentarea şi modularizarea codului-sursă, utilizarea unui sistem de stocare şi management online al codului-sursă (e.g., Bitbucket, GitLab, GitHub etc.).

# Sistem Backend - Aplicație "Collect.me"

## Cuprins
1. [Prezentare generală](#1-prezentare-generală)
2. [Arhitectura sistemului](#2-arhitectura-sistemului)
   - [2.1 Prezentare arhitecturală](#21-prezentare-arhitecturală)
   - [2.2 Diagrama componentelor](#22-diagrama-componentelor)
   - [2.3 Fluxul de date](#23-fluxul-de-date)
   - [2.4 Modelul de date](#24-modelul-de-date)
3. [Funcționalități principale](#3-funcționalități-principale)
   - [3.1 Gestiunea utilizatorilor](#31-gestiunea-utilizatorilor)
   - [3.2 Gestiunea colecțiilor](#32-gestiunea-colecțiilor)
   - [3.3 Gestiunea obiectelor](#33-gestiunea-obiectelor)
   - [3.4 Statistici și rapoarte](#34-statistici-și-rapoarte)
4. [Securitate](#4-securitate)
   - [4.1 Autentificare JWT](#41-autentificare-jwt)
   - [4.2 Prevenirea atacurilor XSS](#42-prevenirea-atacurilor-xss)
   - [4.3 Configurare CORS](#43-configurare-cors)
   - [4.4 Validarea datelor](#44-validarea-datelor)
   - [4.5 Securitatea bazei de date](#45-securitatea-bazei-de-date)
5. [Tehnologii și librării](#5-tehnologii-și-librării)
   - [5.1 Tehnologii de bază](#51-tehnologii-de-bază)
   - [5.2 Librării pentru autentificare și securitate](#52-librării-pentru-autentificare-și-securitate)
   - [5.3 Procesare și serializare date](#53-procesare-și-serializare-date)
   - [5.4 Generare rapoarte și exporturi](#54-generare-rapoarte-și-exporturi)
   - [5.5 Comunicare email](#55-comunicare-email)
6. [Detalii de implementare](#6-detalii-de-implementare)
   - [6.1 Structura codului](#61-structura-codului)
   - [6.2 Configurarea aplicației](#62-configurarea-aplicației)
   - [6.3 Gestionarea erorilor](#63-gestionarea-erorilor)
   - [6.4 Inițializarea și pornirea sistemului](#64-inițializarea-și-pornirea-sistemului)
7. [Considerații finale](#7-considerații-finale)

## 1. Prezentare generală

Collect.me (Object Collector) este o aplicație web complexă destinată colecționarilor, permițând utilizatorilor să catalogheze, organizeze și să împărtășească colecțiile lor personale de obiecte. Sistemul suportă native următoarele tipuri de colecții:

- Monede
- Tablouri
- Timbre
- Viniluri
- Colecții personalizate: Pentru orice alt tip de obiecte, cu atribute configurabile

Backend-ul aplicației este componentă server responsabilă cu procesarea logicii, gestiunea datelor, autentificarea și autorizarea utilizatorilor, interacțiunea cu baza de date și expunerea API-urilor necesare pentru funcționarea frontend-ului. Sistemul este proiectat pentru a fi scalabil, securizat și performant, oferind utilizatorilor o experiență fluidă și fiabilă.

Funcționalitățile principale ale backend-ului includ: gestiunea utilizatorilor și a sesiunilor, administrarea colecțiilor și a obiectelor, generarea de statistici și rapoarte, exportul datelor în diverse formate, precum și mecanisme robuste de securitate și protecție a datelor.

## 2. Arhitectura sistemului

### 2.1 Prezentare arhitecturală

Backend-ul este implementat folosind o arhitectură stratificată, care separă clar responsabilitățile și permite o mai bună organizare și extensibilitate a codului. Arhitectura include următoarele straturi principale:

#### 2.1.1 Stratul de prezentare (API Layer)

Responsabil pentru expunerea API-urilor și interacțiunea cu clienții externi (frontend-ul aplicației). Acest strat este implementat prin:

- **Server HTTP**: Bazat pe clasa HttpServer din Java, gestionează cererile HTTP și direcționează traficul către componentele corespunzătoare
- **Controllere**: Clase specializate care prelucrează cererile HTTP, validează datele de intrare și coordonează execuția operațiunilor
- **Filtre HTTP**: Implementează pre-procesarea și post-procesarea cererilor HTTP (CORS, JWT, XSS prevention)
- **Utilitare de răspuns**: Clase dedicate formatării răspunsurilor HTTP (Response200, Response400, Response500)

#### 2.1.2 Stratul de servicii

Implementează logica a aplicației, independent de protocolul de comunicare sau de mecanismul de persistență. Include:

- **Servicii**: Clase care încapsulează regulile de business și coordonează operațiunile complexe
- **DTO-uri** (Data Transfer Objects): Structuri pentru transferul datelor între componentele sistemului
- **Validatori**: Mecanisme de validare a datelor și a regulilor de business

#### 2.1.3 Stratul de persistență (Repository Layer)

Gestionează accesul la datele persistente și abstractizează detaliile specifice ale bazei de date:

- **Repository-uri**: Clase specializate pentru operațiuni CRUD pe entități specifice
- **Manager de bază de date**: Configurează și gestionează conexiunile la baza de date
- **Entități**: Modele care reprezintă structurile de date persistente

#### 2.1.4 Stratul de utilități (Utility Layer)

Oferă funcționalități transversale utilizate în întreaga aplicație:

- **Procesare JSON**: Utilitare pentru serializarea/deserializarea datelor JSON
- **Trimitere Email**: Servicii pentru comunicarea prin email
- **Generare rapoarte**: Componente pentru generarea rapoartelor în diverse formate
- **Logging**: Sistem de înregistrare a evenimentelor și erorilor
- **Gestionare excepții**: Mecanisme pentru tratarea și raportarea erorilor

#### 2.1.5 Stratul de securitate (Security Layer)

Asigură protecția sistemului și a datelor utilizatorilor:

- **Autentificare**: Mecanisme de verificare a identității utilizatorilor
- **Autorizare**: Controlul accesului la resurse
- **Protecție împotriva atacurilor**: XSS, CSRF, SQL Injection
- **Criptare**: Protecția datelor sensibile

### 2.2 Diagrama componentelor

Sistemul backend este organizat în următoarele componente principale, care interacționează pentru a asigura funcționalitatea completă a aplicației:

#### 2.2.1 Essential Backend Modules

- **Server**: Componenta centrală care gestionează server-ul HTTP și configurează contextele pentru endpoint-uri
- **Properties**: Gestionează configurația aplicației, încărcată din fișierul application.properties
- **DatabaseManager**: Administrează conexiunile la baza de date și inițializează schema
- **Logger**: Sistem centralizat de logging pentru întreaga aplicație

#### 2.2.2 Security Components

- **JwtUtil**: Utilitar pentru generarea și validarea token-urilor JWT
- **JwtFilter**: Filtru HTTP pentru autentificarea și autorizarea cererilor bazate pe JWT
- **CorsFilter**: Configurează și aplică politicile CORS pentru accesul cross-origin
- **XSSPreventionFilter**: Protejează împotriva atacurilor Cross-Site Scripting

#### 2.2.3 Controller Components

- **AuthController**: Gestionează operațiunile de autentificare și administrare a contului
- **CollectionController**: Administrează operațiunile pe colecții
- **ObjectController**: Gestionează operațiunile pe obiecte din colecții
- **StatisticsController**: Furnizează statistici și rapoarte
- **PagesController**: Servește fișiere statice și pagini pentru frontend

#### 2.2.4 Service Components

- **UserService**: Implementează logica de business pentru utilizatori și conturi
- **CollectionService**: Gestionează operațiunile complexe pe colecții
- **ObjectService**: Administrează procesarea obiectelor din colecții
- **StatisticsService**: Calculează și generează statistici și rapoarte

#### 2.2.5 Repository Components

- **UserRepository**: Gestionează persistența datelor utilizatorilor
- **CollectionRepository**: Administrează datele colecțiilor
- **ObjectRepository**: Gestionează persistența obiectelor
- **CustomCollectionRepository**: Administrează câmpurile personalizate pentru colecții
- **ObjectLikeRepository**: Gestionează aprecierile pentru obiecte
- **ObjectViewRepository**: Înregistrează vizualizările obiectelor

#### 2.2.6 Utility Components

- **JsonUtil**: Oferă funcționalități pentru serializare/deserializare JSON
- **EmailUtil**: Servicii pentru trimiterea de emailuri
- **MultipartParser**: Procesează încărcări de fișiere și formulare multipart
- **PdfGenerator**: Generează rapoarte în format PDF
- **CsvGenerator**: Generează exporturi în format CSV
- **XmlGenerator**: Creează feed-uri XML și RSS

### 2.3 Fluxul de date

Fluxul general de date în sistemul backend urmează un model tipic pentru aplicațiile web stratificate:

#### 2.3.1 Flux de cerere-răspuns

1. **Recepționare cerere HTTP**: Server-ul HTTP primește cererea de la client (frontend)
2. **Filtrare**: Cererea trece prin lanțul de filtre:
   - CorsFilter: Verifică și configurează headerele CORS
   - XSSPreventionFilter: Scanează cererea pentru potențiale atacuri XSS
   - JwtFilter: Validează token-ul JWT pentru rutele protejate
3. **Rutare**: Cererea este direcționată către controller-ul corespunzător
4. **Procesare în controller**: Controller-ul identifică operația solicitată și validează cererea
5. **Execuție business logic**: Controller-ul delegă procesarea către serviciul corespunzător
6. **Accesare date**: Serviciul utilizează repository-urile pentru a interacționa cu baza de date
7. **Transformare date**: Datele obținute sunt transformate în DTO-uri pentru răspuns
8. **Formatare răspuns**: Controller-ul pregătește răspunsul HTTP (status code, headers, body)
9. **Trimitere răspuns**: Răspunsul formatat este trimis înapoi clientului

#### 2.3.2 Exemplu de flux: Autentificare utilizator

1. Clientul trimite o cerere POST la /auth/login cu credențialele utilizatorului
2. CorsFilter verifică originea cererii și aplică headerele CORS
3. XSSPreventionFilter scanează cererea pentru potențiale atacuri
4. AuthController primește cererea și extrage credențialele
5. UserService validează credențialele și verifică utilizatorul în baza de date via UserRepository
6. JwtUtil generează un token JWT pentru sesiunea utilizatorului
7. AuthController formatează răspunsul cu token-ul JWT și informațiile utilizatorului
8. Răspunsul este trimis înapoi clientului

#### 2.3.3 Exemplu de flux: Creare obiect în colecție

1. Clientul trimite o cerere POST la /my-collection/{id}/objects cu datele obiectului
2. Filtrul CORS verifică originea cererii
3. XSSPreventionFilter scanează cererea pentru potențiale atacuri
4. JwtFilter validează token-ul JWT și extrage informațiile utilizatorului
5. ObjectController primește cererea și extrage datele obiectului
6. ObjectService validează datele și verifică dacă utilizatorul este proprietarul colecției
7. CollectionRepository verifică existența colecției și drepturile de acces
8. ObjectService procesează imaginea obiectului (dacă există) și pregătește datele pentru persistență
9. ObjectRepository salvează obiectul în baza de date
10. ObjectController formatează răspunsul cu informațiile obiectului creat
11. Răspunsul este trimis înapoi clientului

### 2.4 Modelul de date

Sistemul utilizează un model de date relațional pentru stocarea informațiilor, cu următoarele entități principale:

#### 2.4.1 Entități de bază

- **User**: Reprezentarea unui utilizator în sistem
  - id: Identificator unic
  - username: Numele de utilizator
  - password: Parola criptată
  - email: Adresa de email
  - codeSession: Cod pentru validarea sesiunii
  - codeReset: Cod pentru resetarea parolei
  - profilePicture: Calea către imaginea de profil
  - createdAt: Data creării contului
  - updatedAt: Data ultimei actualizări

- **Collection**: Reprezentarea unei colecții de obiecte
  - id: Identificator unic
  - idUser: Legătura cu utilizatorul proprietar
  - idTip: Tipul colecției (monede, tablouri, etc.)
  - nume: Numele colecției
  - visibility: Vizibilitatea colecției (publică/privată)
  - value: Valoarea totală a obiectelor din colecție
  - count: Numărul de obiecte din colecție
  - createdAt: Data creării colecției

- **Obiect**: Reprezentarea unui obiect din colecție
  - id: Identificator unic
  - idColectie: Legătura cu colecția părinte
  - name: Numele obiectului
  - descriere: Descrierea detaliată
  - Atribute specifice: material, valoare, greutate, numeArtist, etc.
  - visibility: Vizibilitatea obiectului
  - image: Calea către imaginea obiectului
  - createdAt: Data adăugării obiectului
  - updatedAt: Data ultimei actualizări

#### 2.4.2 Entități de relație și metadate

- **CustomCollection**: Configurația câmpurilor pentru colecții personalizate
  - id: Identificator unic
  - idColectie: Legătura cu colecția personalizată
  - Configurații de câmpuri: material, valoare, greutate, etc. (boolean)
  - createdAt: Data creării configurației

- **ObjectView**: Înregistrarea vizualizărilor obiectelor
  - id: Identificator unic
  - idObject: Legătura cu obiectul vizualizat
  - data: Data vizualizării
  - ora: Ora vizualizării
  - idUser: Utilizatorul care a vizualizat obiectul

- **ObjectLike**: Înregistrarea aprecierilor pentru obiecte
  - id: Identificator unic
  - idObject: Legătura cu obiectul apreciat
  - data: Data aprecierii
  - ora: Ora aprecierii
  - idUser: Utilizatorul care a apreciat obiectul

#### 2.4.3 Entități pentru statistici

- **StatisticsProfile**: Agregarea statisticilor la nivel de utilizator
- **StatisticsCollection**: Statistici pentru o colecție specifică
- **StatisticsObject**: Statistici pentru un obiect specific
- **GeneralStatistics**: Statistici globale la nivelul aplicației
- **Clasament**: Reprezentarea clasamentelor pentru diverse criterii

## 3. Funcționalități principale

### 3.1 Gestiunea utilizatorilor

Sistemul oferă un set complet de funcționalități pentru gestionarea conturilor de utilizator, autentificare și autorizare.

#### 3.1.1 Înregistrare utilizator

Permite crearea unui cont nou în sistem, cu validarea datelor de înregistrare și trimiterea unui email de confirmare.

**Proces de implementare:**
1. AuthController primește cererea de înregistrare și validează formatul datelor
2. UserService validează unicitatea username-ului și adresei de email
3. Parola este criptată folosind algoritmi securizați
4. UserRepository salvează utilizatorul în baza de date
5. EmailRegister trimite un email de confirmare utilizatorului
6. Se creează structura de directoare pentru fișierele utilizatorului

**Componente implicate:**
- AuthController: Procesarea cererii de înregistrare
- UserService: Validarea și procesarea datelor
- UserRepository: Persistența datelor utilizatorului
- EmailRegister: Trimiterea emailului de confirmare
- EmailValidator: Validarea formatului adresei de email

#### 3.1.2 Autentificare

Permite utilizatorilor să se autentifice în sistem și să primească un token JWT pentru accesul la resurse protejate.

**Proces de implementare:**
1. AuthController primește credențialele și le validează
2. UserService verifică credențialele în baza de date
3. Se generează un cod de sesiune unic pentru utilizator
4. JwtUtil generează un token JWT cu informațiile utilizatorului și codul de sesiune
5. Răspunsul include token-ul JWT și informații de bază despre utilizator

**Componente implicate:**
- AuthController: Procesarea cererii de autentificare
- UserService: Validarea credențialelor
- UserRepository: Accesarea datelor utilizatorului
- JwtUtil: Generarea token-ului JWT

#### 3.1.3 Resetare parolă

Permite utilizatorilor să-și reseteze parola în cazul în care au uitat-o, prin intermediul unui cod de verificare trimis pe email.

**Proces de implementare:**
1. AuthController primește cererea de resetare cu adresa de email
2. UserService verifică existența utilizatorului cu adresa respectivă
3. Se generează un cod de resetare unic și se salvează în contul utilizatorului
4. EmailResetPassword trimite un email cu codul de resetare
5. Utilizatorul trimite codul și noua parolă pentru a finaliza procesul
6. UserService verifică codul, actualizează parola și resetează codul de resetare

**Componente implicate:**
- AuthController: Procesarea cererilor de resetare
- UserService: Gestionarea procesului de resetare
- UserRepository: Actualizarea datelor utilizatorului
- EmailResetPassword: Trimiterea emailului cu codul de resetare

#### 3.1.4 Gestionare profil

Permite utilizatorilor să-și administreze profilul, incluzând schimbarea informațiilor personale, a parolei și a imaginii de profil.

**Proces de implementare:**
1. AuthController primește cererea de actualizare a profilului
2. JwtFilter validează autentificarea utilizatorului
3. UserService procesează modificările solicitate
4. În cazul schimbării username-ului, se actualizează și structura de directoare
5. Pentru imaginea de profil, se procesează și se stochează fișierul încărcat

**Componente implicate:**
- AuthController: Procesarea cererilor de gestionare a profilului
- UserService: Implementarea logicii de actualizare
- UserRepository: Persistența modificărilor
- MultipartParser: Procesarea încărcărilor de fișiere pentru imaginea de profil

#### 3.1.5 Deconectare și ștergere cont

Permite utilizatorilor să se deconecteze din sistem sau să-și șteargă definitiv contul.

**Proces de implementare:**
1. Pentru deconectare, se invalidează codul de sesiune al utilizatorului
2. Pentru ștergerea contului, se verifică parola și se confirmă intenția
3. La ștergere, se elimină toate colecțiile, obiectele și resursele asociate
4. Se șterge structura de directoare a utilizatorului

**Componente implicate:**
- AuthController: Procesarea cererilor de deconectare și ștergere
- UserService: Implementarea logicii de deconectare și ștergere
- CollectionRepository: Ștergerea colecțiilor utilizatorului
- ObjectRepository: Ștergerea obiectelor utilizatorului

### 3.2 Gestiunea colecțiilor

Sistemul permite utilizatorilor să creeze și să administreze colecții de obiecte, cu diferite tipuri predefinite sau personalizate.

#### 3.2.1 Creare colecție

Permite utilizatorilor să creeze o nouă colecție, specificând tipul și configurând atributele pentru colecțiile personalizate.

**Proces de implementare:**
1. CollectionController primește cererea de creare a colecției
2. JwtFilter validează autentificarea utilizatorului
3. CollectionService validează datele colecției
4. CollectionRepository salvează colecția în baza de date
5. Pentru colecțiile personalizate, se salvează configurația câmpurilor în CustomCollectionRepository

**Componente implicate:**
- CollectionController: Procesarea cererii de creare
- CollectionService: Validarea și procesarea datelor
- CollectionRepository: Persistența colecției
- CustomCollectionRepository: Salvarea configurației personalizate

#### 3.2.2 Actualizare colecție

Permite modificarea proprietăților unei colecții existente, inclusiv numele, vizibilitatea și configurația câmpurilor personalizate.

**Proces de implementare:**
1. CollectionController primește cererea de actualizare
2. CollectionService verifică drepturile de acces ale utilizatorului
3. Se actualizează proprietățile colecției
4. Pentru colecțiile personalizate, se actualizează și configurația câmpurilor

**Componente implicate:**
- CollectionController: Procesarea cererii de actualizare
- CollectionService: Validarea și procesarea modificărilor
- CollectionRepository: Actualizarea datelor colecției
- CustomCollectionRepository: Actualizarea configurației personalizate

#### 3.2.3 Ștergere colecție

Permite eliminarea unei colecții împreună cu toate obiectele asociate.

**Proces de implementare:**
1. CollectionController primește cererea de ștergere
2. CollectionService verifică drepturile de acces ale utilizatorului
3. Se șterg toate obiectele din colecție și resursele asociate (imagini)
4. Se șterge configurația personalizată (dacă există)
5. Se elimină colecția din baza de date

**Componente implicate:**
- CollectionController: Procesarea cererii de ștergere
- CollectionService: Coordonarea procesului de ștergere
- ObjectRepository: Ștergerea obiectelor asociate
- CustomCollectionRepository: Eliminarea configurației personalizate
- CollectionRepository: Eliminarea colecției

#### 3.2.4 Vizualizare colecții

Permite listarea colecțiilor proprii sau publice, cu informații detaliate și statistici.

**Proces de implementare:**
1. CollectionController primește cererea de listare
2. Pentru colecțiile proprii, se verifică autentificarea utilizatorului
3. CollectionService recuperează colecțiile corespunzătoare
4. Se calculează statisticile asociate (număr de obiecte, valoare totală, etc.)
5. Se formatează răspunsul cu datele colecțiilor

**Componente implicate:**
- CollectionController: Procesarea cererilor de listare
- CollectionService: Recuperarea și procesarea datelor
- CollectionRepository: Accesarea colecțiilor
- ObjectRepository: Calculul statisticilor

#### 3.2.5 Vizualizare colecție specifică

Permite accesarea informațiilor detaliate despre o colecție specifică, inclusiv statistici și metadate.

**Proces de implementare:**
1. CollectionController primește cererea cu ID-ul colecției
2. Se verifică existența colecției și drepturile de acces
3. CollectionService recuperează datele colecției și statisticile asociate
4. Pentru colecțiile personalizate, se încarcă și configurația câmpurilor

**Componente implicate:**
- CollectionController: Procesarea cererii de vizualizare
- CollectionService: Recuperarea și procesarea datelor
- CollectionRepository: Accesarea datelor colecției
- CustomCollectionRepository: Încărcarea configurației personalizate

### 3.3 Gestiunea obiectelor

Sistemul permite utilizatorilor să administreze obiectele din colecții, cu suport pentru diverse atribute în funcție de tipul colecției.

#### 3.3.1 Adăugare obiect

Permite adăugarea unui obiect nou într-o colecție, cu atributele specifice tipului de colecție și o imagine opțională.

**Proces de implementare:**
1. ObjectController primește cererea de adăugare a obiectului
2. Se verifică drepturile utilizatorului asupra colecției părinte
3. ObjectService validează datele obiectului conform tipului colecției
4. Pentru colecțiile personalizate, se validează conform configurației
5. Se procesează imaginea obiectului (dacă există) și se stochează
6. ObjectRepository salvează obiectul în baza de date

**Componente implicate:**
- ObjectController: Procesarea cererii de adăugare
- ObjectService: Validarea și procesarea datelor
- CollectionRepository: Verificarea colecției părinte
- MultipartParser: Procesarea imaginii obiectului
- ObjectRepository: Persistența obiectului

#### 3.3.2 Actualizare obiect

Permite modificarea proprietăților unui obiect existent, inclusiv atributele și imaginea.

**Proces de implementare:**
1. ObjectController primește cererea de actualizare
2. Se verifică existența obiectului și drepturile utilizatorului
3. ObjectService validează noile date conform tipului colecției
4. Dacă se actualizează imaginea, se procesează și se înlocuiește cea veche
5. ObjectRepository actualizează obiectul în baza de date

**Componente implicate:**
- ObjectController: Procesarea cererii de actualizare
- ObjectService: Validarea și procesarea modificărilor
- MultipartParser: Procesarea noii imagini (dacă există)
- ObjectRepository: Actualizarea datelor obiectului

#### 3.3.3 Ștergere obiect

Permite eliminarea unui obiect din colecție, inclusiv resursele asociate.

**Proces de implementare:**
1. ObjectController primește cererea de ștergere
2. Se verifică existența obiectului și drepturile utilizatorului
3. ObjectService coordonează ștergerea obiectului și a resurselor asociate
4. Se elimină statisticile asociate (like-uri, vizualizări)
5. Se șterge imaginea obiectului (dacă există)
6. ObjectRepository elimină obiectul din baza de date

**Componente implicate:**
- ObjectController: Procesarea cererii de ștergere
- ObjectService: Coordonarea procesului de ștergere
- ObjectLikeRepository: Eliminarea aprecierilor
- ObjectViewRepository: Eliminarea vizualizărilor
- ObjectRepository: Eliminarea obiectului

#### 3.3.4 Vizualizare obiecte

Permite listarea obiectelor din colecții, cu filtrare și ordonare.

**Proces de implementare:**
1. ObjectController primește cererea de listare
2. Se verifică drepturile de acces pentru colecția specificată
3. ObjectService recuperează obiectele conform criteriilor
4. Se încarcă statisticile asociate (vizualizări, aprecieri)
5. Se formatează răspunsul cu datele obiectelor

**Componente implicate:**
- ObjectController: Procesarea cererilor de listare
- ObjectService: Recuperarea și procesarea datelor
- ObjectRepository: Accesarea obiectelor
- ObjectLikeRepository: Încărcarea aprecierilor
- ObjectViewRepository: Încărcarea vizualizărilor

#### 3.3.5 Vizualizare obiect specific

Permite accesarea informațiilor detaliate despre un obiect, înregistrând vizualizarea.

**Proces de implementare:**
1. ObjectController primește cererea cu ID-ul obiectului
2. Se verifică existența obiectului și drepturile de acces
3. ObjectService înregistrează vizualizarea în ObjectViewRepository
4. Se încarcă datele complete ale obiectului, inclusiv imaginea
5. Se formatează răspunsul cu datele obiectului și statisticile asociate

**Componente implicate:**
- ObjectController: Procesarea cererii de vizualizare
- ObjectService: Recuperarea datelor și înregistrarea vizualizării
- ObjectRepository: Accesarea datelor obiectului
- ObjectViewRepository: Înregistrarea vizualizării

#### 3.3.6 Apreciere obiect

Permite utilizatorilor să aprecieze (like) un obiect, înregistrând interacțiunea.

**Proces de implementare:**
1. ObjectController primește cererea de apreciere
2. Se verifică autentificarea utilizatorului și accesul la obiect
3. ObjectService înregistrează aprecierea în ObjectLikeRepository
4. Se actualizează contoarele de aprecieri pentru obiect

**Componente implicate:**
- ObjectController: Procesarea cererii de apreciere
- ObjectService: Procesarea aprecierii
- ObjectLikeRepository: Înregistrarea aprecierii
- ObjectRepository: Actualizarea contorului de aprecieri

### 3.4 Statistici și rapoarte

Sistemul oferă funcționalități avansate pentru generarea de statistici și rapoarte despre colecții și obiecte.

#### 3.4.1 Statistici personale

Generează statistici detaliate despre colecțiile și obiectele unui utilizator.

**Proces de implementare:**
1. StatisticsController primește cererea de statistici personale
2. Se verifică autentificarea utilizatorului
3. StatisticsService colectează datele despre colecțiile și obiectele utilizatorului
4. Se calculează statistici agregate: număr total de obiecte, valoare totală, vizualizări, aprecieri
5. Se identifică obiectele și colecțiile cele mai populare
6. Se formatează răspunsul cu statisticile calculate

**Componente implicate:**
- StatisticsController: Procesarea cererii de statistici
- StatisticsService: Calculul și agregarea statisticilor
- CollectionRepository: Accesarea datelor colecțiilor
- ObjectRepository: Accesarea datelor obiectelor
- ObjectLikeRepository: Recuperarea aprecierilor
- ObjectViewRepository: Recuperarea vizualizărilor

#### 3.4.2 Statistici globale

Generează statistici la nivelul întregii platforme, cu topuri și clasamente.

**Proces de implementare:**
1. StatisticsController primește cererea de statistici globale
2. StatisticsService colectează și agregă datele din întreaga platformă
3. Se calculează distribuția colecțiilor pe tipuri
4. Se generează topuri: cele mai vizualizate/apreciate colecții și obiecte
5. Se calculează tendințele de creștere și activitatea recentă

**Componente implicate:**
- StatisticsController: Procesarea cererii de statistici
- StatisticsService: Calculul și agregarea statisticilor
- GeneralStatisticsRepository: Recuperarea datelor agregate

#### 3.4.3 Export PDF

Permite exportul statisticilor în format PDF, cu formatare profesională.

**Proces de implementare:**
1. StatisticsController primește cererea de export PDF
2. StatisticsService colectează datele necesare (personale sau globale)
3. PdfPersonalStatistics/PdfGeneralStatistics generează documentul PDF
4. Se formatează datele în tabele, grafice și secțiuni structurate
5. Se returnează documentul PDF generat

**Componente implicate:**
- StatisticsController: Procesarea cererii de export
- StatisticsService: Colectarea datelor pentru raport
- PdfPersonalStatistics: Generarea PDF-ului pentru statistici personale
- PdfGeneralStatistics: Generarea PDF-ului pentru statistici globale

#### 3.4.4 Export CSV

Permite exportul datelor în format CSV pentru analiză și prelucrare externă.

**Proces de implementare:**
1. StatisticsController primește cererea de export CSV
2. StatisticsService colectează datele necesare
3. CsvPersonalStatistics/CsvGeneralStatistics generează fișierul CSV
4. Se structurează datele în format tabelar, cu header corespunzător
5. Se returnează fișierul CSV generat

**Componente implicate:**
- StatisticsController: Procesarea cererii de export
- StatisticsService: Colectarea datelor pentru export
- CsvPersonalStatistics: Generarea CSV-ului pentru statistici personale
- CsvGeneralStatistics: Generarea CSV-ului pentru statistici globale

#### 3.4.5 Flux RSS

Generează un feed RSS cu statisticile globale actualizate periodic.

**Proces de implementare:**
1. StatisticsController primește cererea de acces la feed-ul RSS
2. XmlGeneralStatistics generează sau recuperează feed-ul XML în format RSS
3. Se includ statisticile recente și topurile actualizate
4. Feed-ul RSS este returnat clientului cu headers corespunzătoare

**Componente implicate:**
- StatisticsController: Procesarea cererii de feed RSS
- StatisticsService: Colectarea datelor pentru feed
- XmlGeneralStatistics: Generarea și caching-ul feed-ului RSS

## 4. Securitate

### 4.1 Autentificare JWT

Sistemul utilizează JSON Web Tokens (JWT) pentru autentificarea și autorizarea utilizatorilor, oferind o soluție scalabilă pentru gestionarea sesiunilor.

#### 4.1.1 Generare token

Procesul de generare a token-urilor JWT se realizează în clasa `JwtUtil` și include următoarele etape:
1. Validarea datelor utilizatorului
2. Crearea payload-ului JWT cu informațiile utilizatorului (username, email, id, codeSession)
3. Stabilirea perioadei de valabilitate (8 ore implicit)
4. Semnarea token-ului cu cheia secretă generată la pornirea aplicației

Token-ul JWT generat conține suficiente informații pentru identificarea utilizatorului, fără a necesita interogări suplimentare în baza de date pentru fiecare cerere.

#### 4.1.2 Validare token

`JwtFilter` interceptează toate cererile către rutele protejate și verifică token-ul JWT furnizat în header-ul Authorization:
1. Verificarea prezenței și formatului token-ului (Bearer [token])
2. Validarea semnăturii token-ului cu cheia secretă
3. Verificarea expirării token-ului
4. Extragerea informațiilor utilizatorului din token
5. Verificarea validității sesiunii (codeSession) prin interogarea bazei de date

Această abordare permite invalidarea imediată a sesiunilor în caz de deconectare sau compromitere a contului.

#### 4.1.3 Gestiunea sesiunilor

Sistemul folosește un mecanism de gestiune a sesiunilor bazat pe codul de sesiune (codeSession):
- La autentificare, se generează un cod de sesiune unic și se salvează în contul utilizatorului
- Codul de sesiune este inclus în token-ul JWT
- La fiecare cerere autentificată, se verifică dacă codul de sesiune din token corespunde cu cel din baza de date
- La deconectare, se modifică codul de sesiune, invalidând astfel toate token-urile anterioare

Acest mecanism oferă un control fin asupra sesiunilor active și permite deconectarea imediată de pe toate dispozitivele.

### 4.2 Prevenirea atacurilor XSS

Sistemul implementează un filtru complex (`XSSPreventionFilter`) pentru detectarea și prevenirea atacurilor Cross-Site Scripting (XSS).

#### 4.2.1 Metode de detecție

Filtrul utilizează multiple tehnici pentru detectarea potențialelor atacuri XSS:
- Pattern-uri regex pentru identificarea scripturilor malițioase în URL-uri, parametri și body
- Verificări separate pentru headere, cu reguli specifice pentru headerele standard
- Analiză specifică pentru conținut de tip JSON, cu extragerea și verificarea valorilor
- Procesare specială pentru conținut multipart/form-data, cu analiză separată pentru fiecare parte

Aceste verificări sunt aplicate tuturor componentelor cererii HTTP, inclusiv path, query parameters, headere și body.

#### 4.2.2 Răspuns la detecția atacurilor

Când se detectează un potențial atac XSS, sistemul:
1. Blochează cererea și nu o transmite mai departe în lanțul de procesare
2. Înregistrează tentativa în sistemul de logging, cu nivel MALICIOUS
3. Returnează un răspuns 400 Bad Request către client
4. Nu include detalii specifice despre motivul blocării, pentru a nu oferi informații potențial utile atacatorilor

Această abordare defensivă protejează sistemul și utilizatorii împotriva unei game largi de atacuri XSS.

### 4.3 Configurare CORS

Sistemul implementează un mecanism de Cross-Origin Resource Sharing (CORS) pentru a controla accesul la API din diferite origini.

#### 4.3.1 Implementare

`CorsFilter` interceptează toate cererile HTTP și aplică politica CORS configurată:
- Adaugă headerele Access-Control-Allow-Origin pentru a specifica originile permise
- Configurează headerele Access-Control-Allow-Headers pentru a specifica headerele permise în cereri
- Setează headerele Access-Control-Allow-Methods pentru a specifica metodele HTTP permise
- Procesează special cererile OPTIONS (preflight) pentru a permite verificarea permisiunilor CORS

Aceste configurări sunt citite din fișierul de proprietăți și pot fi adaptate pentru diferite medii de deployment.

#### 4.3.2 Configurare

Politica CORS poate fi configurată prin următoarele proprietăți:
- `cors.allow.origin`: Specifică originile permise (implicit "*" pentru dezvoltare)
- `cors.allow.headers`: Lista headerelor permise în cereri (implicit "Content-Type, Authorization")
- `cors.allow.methods`: Metodele HTTP permise (implicit "GET, POST, PUT, DELETE, OPTIONS")

### 4.4 Validarea datelor

Sistemul implementează multiple niveluri de validare a datelor pentru a preveni intrări malițioase sau invalide.

#### 4.4.1 Validare la nivel de controller

Controller-ele verifică formatul și prezența parametrilor obligatorii în cereri:
- Validarea path parameters pentru existență și format
- Verificarea metodei HTTP pentru endpoint-ul solicitat
- Validarea formatului datelor primite (JSON, multipart, etc.)

#### 4.4.2 Validare la nivel de serviciu

Serviciile implementează validări complexe ale datelor de business:
- Verificarea regulilor de business specifice entităților
- Validarea relațiilor între entități (ex: proprietatea asupra colecțiilor)
- Verificarea unicității și integrității datelor

#### 4.4.3 Validatori specializați

Sistemul include validatori specializați pentru anumite tipuri de date:
- `EmailValidator`: Verifică formatul adreselor de email
- Validatori pentru diferite tipuri de colecții, care verifică prezența și formatul atributelor obligatorii
- Validarea fișierelor încărcate

### 4.5 Securitatea bazei de date

Sistemul implementează practici de securitate pentru accesul și manipularea datelor în baza de date.

#### 4.5.1 Prevenirea SQL Injection

Pentru a preveni atacurile de tip SQL Injection, sistemul:
- Utilizează PreparedStatement pentru toate interogările SQL
- Parametrizează toate valorile incluse în interogări
- Evită construirea de interogări prin concatenare directă de string-uri
- Validează datele înainte de a le include în interogări

#### 4.5.2 Managementul conexiunilor

Conexiunile la baza de date sunt gestionate în mod securizat:
- Credențialele sunt stocate în fișierul de proprietăți, separat de cod
- Conexiunile sunt închise corect după utilizare, folosind blocuri try-with-resources
- Sistemul verifică drepturile de acces la nivel de aplicație înainte de a executa operațiuni pe date

## 5. Tehnologii și librării

### 5.1 Tehnologii de bază

- **Java 11+**: Limbajul de programare principal, oferind performanță, portabilitate și un ecosistem bogat de librării
- **HttpServer**: Server HTTP integrat în Java (com.sun.net.httpserver), utilizat pentru gestionarea cererilor web și expunerea API-urilor
- **JDBC (Java Database Connectivity)**: API standard pentru conectarea și interacțiunea cu baze de date relaționale
- **Properties API**: Utilizat pentru încărcarea și gestionarea configurațiilor din fișiere externe

### 5.2 Librării pentru autentificare și securitate

- **jjwt (Java JWT)**: Librărie pentru generarea și validarea token-urilor JWT pentru autentificare
  - Oferă funcționalități complete pentru crearea, semnarea și verificarea token-urilor JWT
  - Suportă algoritmi de semnare precum HMAC-SHA256
  - Permite includerea și extragerea de claims personalizate
- **Java Security API**: Utilizat pentru generarea cheilor de semnare și criptare
  - Generarea de chei criptografice pentru semnarea token-urilor JWT
  - Algoritmi de hash pentru stocarea securizată a parolelor

### 5.3 Procesare și serializare date

- **Gson**: Librărie Google pentru serializare și deserializare JSON
  - Conversie bidirectională între obiecte Java și JSON
  - Suport pentru tipuri generice și colecții
  - Adaptoare personalizate pentru tipuri specifice (LocalDate, LocalDateTime)
  - Formatare JSON pentru răspunsuri API
- **Java IO/NIO**: API-uri standard pentru operațiuni de fișiere și stream-uri
  - Procesarea fișierelor încărcate (imagini pentru obiecte și profile)
  - Manipularea stream-urilor pentru cereri și răspunsuri HTTP
  - Operațiuni de citire/scriere pentru generarea rapoartelor și exporturilor

### 5.4 Generare rapoarte și exporturi

- **iText 7**: Librărie pentru generarea documentelor PDF
  - Crearea rapoartelor structurate în format PDF pentru statistici personale și globale
  - Suport pentru tabele, fonturi, culori și formatare avansată
  - Generarea dinamică a conținutului pe baza datelor din sistem
- **JAXP (Java API for XML Processing)**: API standard pentru procesarea XML
  - Generarea feed-urilor RSS în format XML pentru statistici
  - Procesarea și validarea documentelor XML
  - Transformarea între diferite formate de date

### 5.5 Comunicare email

- **Jakarta Mail (fostul JavaMail)**: API pentru trimiterea și primirea de email-uri
  - Trimiterea email-urilor de confirmare la înregistrare
  - Comunicarea codurilor de resetare a parolei
  - Configurarea conexiunilor SMTP pentru trimiterea email-urilor
  - Suport pentru șabloane de email personalizate

## 6. Detalii de implementare

### 6.1 Structura codului

Codul backend-ului urmează un model de organizare pe pachete și subpachete, grupate pe funcționalități și responsabilități:

#### 6.1.1 Pachete principale

- **backend.api**: Pachetul rădăcină al aplicației
- **backend.api.config**: Configurații și setări pentru diverse componente ale sistemului
- **backend.api.controller**: Controller-e pentru gestionarea cererilor HTTP
- **backend.api.service**: Servicii pentru implementarea logicii de business
- **backend.api.repository**: Repository-uri pentru interacțiunea cu baza de date
- **backend.api.model**: Modele și entități ale sistemului
- **backend.api.exception**: Clase pentru gestionarea și raportarea excepțiilor
- **backend.api.util**: Utilitare și componente auxiliare
- **backend.api.dataTransferObject**: DTO-uri pentru transferul datelor între straturi

#### 6.1.2 Subpachete specializate

- **backend.api.config.applicationConfig**: Configurații generale ale aplicației
- **backend.api.config.controllerConfig**: Configurații pentru controller-e
- **backend.api.config.databaseConfig**: Configurații pentru baza de date
- **backend.api.config.jwtConfig**: Configurații pentru autentificarea JWT
- **backend.api.model.GlobalStatistics**: Modele pentru statistici globale
- **backend.api.model.PersonalStatistics**: Modele pentru statistici personale
- **backend.api.util.email**: Utilitare pentru comunicarea prin email
- **backend.api.util.files**: Utilitare pentru generarea de fișiere (PDF, CSV, XML)
- **backend.api.util.json**: Utilitare pentru procesarea JSON
- **backend.api.util.multipart**: Utilitare pentru procesarea cererilor multipart

### 6.2 Configurarea aplicației

Sistemul folosește un mecanism flexibil de configurare, bazat pe fișiere de proprietăți externe:

#### 6.2.1 Fișierul application.properties

Principalul fișier de configurare conține setări pentru:
- Adresa și portul serverului HTTP
- Configurația bazei de date (URL, utilizator, parolă, driver)
- Setările pentru CORS (origini, headere și metode permise)
- Credențialele pentru serviciul de email
- Căile pentru directoarele de stocare a fișierelor

#### 6.2.2 Clasa Properties

Clasa `Properties` din pachetul `backend.api.config.applicationConfig` încarcă și gestionează configurările:
- Încărcarea proprietăților din fișierul application.properties
- Metode specifice pentru accesarea diferitelor setări (getPort, getAddress, etc.)
- Valori implicite pentru configurațiile opționale
- Accesarea dinamică a proprietăților prin numele lor

### 6.3 Gestionarea erorilor

Sistemul implementează un mecanism robust pentru gestionarea, raportarea și tratarea erorilor:

#### 6.3.1 Ierarhia de excepții

O ierarhie structurată de excepții, bazată pe clasa `CustomException`:
- `CustomException`: Clasa de bază pentru toate excepțiile personalizate
- `Exception400`: Subclase pentru erori de client (BadRequest, Unauthorized, etc.)
- `Exception500`: Subclase pentru erori de server (InternalServerError, etc.)

Fiecare excepție include:
- Un tip (identificator tehnic al erorii)
- Un nume (descriere scurtă și clară)
- O descriere detaliată
- Opțional, excepția care a cauzat eroarea

#### 6.3.2 Logging centralizat

Clasa `Logger` din pachetul `backend.api.exception` oferă un sistem centralizat de logging:
- Niveluri de logging diferite (ERROR, WARNING, INFO, DEBUG, etc.)
- Formatare vizuală a mesajelor în consolă, cu coduri de culoare
- Timestamp pentru fiecare mesaj pentru urmărire temporală
- Metode specializate pentru diferite tipuri de evenimente
- Identificarea activităților malițioase cu nivel specific (MALICIOUS)

#### 6.3.3 Tratarea erorilor în API

Mecanismul de tratare a erorilor în API include:
- Convertirea excepțiilor în răspunsuri HTTP corespunzătoare
- Clasele `Response200`, `Response400` și `Response500` pentru formatarea răspunsurilor
- Metoda `handleException` din `ControllerInterface` pentru tratarea uniformă a excepțiilor
- Logging automat al erorilor pentru diagnosticare
- Răspunsuri de eroare formatate în JSON pentru consum facil de către client

### 6.4 Inițializarea și pornirea sistemului

Secvența de inițializare și pornire a sistemului include următorii pași:

#### 6.4.1 Încărcarea configurației

1. Încărcarea fișierului application.properties
2. Inițializarea proprietăților și setărilor sistemului
3. Verificarea și validarea configurațiilor esențiale

#### 6.4.2 Inițializarea bazei de date

1. Configurarea conexiunii la baza de date prin `DatabaseManager`
2. Verificarea existenței tabelelor necesare
3. Crearea tabelelor lipsă (dacă este necesar)
4. Verificarea structurii tabelelor și a constrângerilor

#### 6.4.3 Pornirea serverului HTTP

1. Crearea și configurarea instanței `HttpServer`
2. Înregistrarea filtrelor globale (CORS, XSS, JWT)
3. Configurarea contextelor pentru endpoint-uri prin metoda `setupContexts`
4. Înregistrarea controller-elor pentru fiecare endpoint
5. Pornirea serverului pe adresa și portul configurate

#### 6.4.4 Configurarea statisticilor

1. Inițializarea serviciului de statistici
2. Generarea statisticilor inițiale pentru sistemul global
3. Configurarea programării pentru actualizarea periodică a statisticilor
4. Generarea feed-ului RSS inițial

## 7. Considerații finale

Backend-ul aplicației "Collect.me" reprezintă o fundație solidă pentru un sistem de gestionare a colecțiilor, implementând cele mai bune practici de dezvoltare software și securitate. Prin designul său modular și extensibil, sistemul poate evolua pentru a satisface cerințe în continuă schimbare și pentru a integra noi tehnologii și funcționalități.

Documentația tehnică prezentă servește ca referință pentru înțelegerea arhitecturii și funcționalităților sistemului, facilitând mentenanța, extinderea și integrarea sa în ecosistemul mai larg al aplicației.