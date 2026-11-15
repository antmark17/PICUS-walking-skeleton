# Consegna operativa — Giorno 1

1. Verificare JDK 21, Maven, Git e accesso al repository.
2. Copiare i file dello starter nella root del repository **esistente** e verificare la build con `mvn clean verify`.
3. Integrare la PR di bootstrap in `main`; solo dopo creare i tre branch di lavoro.
4. Leggere e approvare i contratti in `README.md`. Le firme sono congelate per il primo ciclo.
5. Avviare il server e provare `http://localhost:8080/health`.

## G1 — Identity e Organizations
1. Implementare `CurrentActorProvider` con Spring Security; non ricavare la OPT dal form HTTP.
2. Predisporre utenti *sintetici* Banco, OPT A, OPT B con password codificate.
3. Proteggere `/banco/**` (BANCO), `/opt/**` (OPT); mantenere CSRF.
4. Implementare `OrganizationLookup` con le organizzazioni di prova.
5. Testare login, logout, sessioni, negazione operazioni Banco a OPT.
6. Rimuovere/sostituire `BootstrapSecurityConfig` evitando doppi `SecurityFilterChain`.

## G2 — Requests
1. Conservare i contratti pubblici esistenti e completare `ServiceRequest` con factory e transizione.
2. Scrivere `RequestService implements RequestUseCases`, usando `CurrentActorProvider` e `OrganizationLookup`.
3. Derivare `organizationId` dal principal: mai fidarsi di un input client.
4. Implementare adapter JPA + entity + mapper; persistere con transazioni.
5. Testare invio, consultazione per proprietario, cambio di stato, ripetizione non valida.

## G3 — Web e test integrati
1. Creare le due viste OPT e Banco in Thymeleaf; mantenere palette verde/arancione.
2. Realizzare controller che usano **solo** `RequestUseCases`, non JPA.
3. Integrare gli endpoint concordati con G1 e G2.
4. Inserire token CSRF nei form POST.
5. Automatizzare percorso OPT A → Banco → OPT A, isolamento OPT B e controlli dati.


