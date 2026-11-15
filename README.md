# PICUS — base comune del Walking Skeleton

## Stato del progetto
Questa consegna è lo **scheletro tecnico condiviso**, NON l'MVP completo.
Contiene struttura Maven/Spring Boot, moduli, contratti Java, endpoint `/health`, sicurezza iniziale **deny-all** sulle route applicative, database H2 su file e due test di avvio.
**Non contiene:** account di prova, login operativo, controller del flusso richieste, adapter JPA, salvataggio o modifica dello stato: sono attività dei tre gruppi.

## Prerequisiti
- JDK **21** (`java -version`)
- Apache **Maven 3.6.3+** (`mvn -version`)
- Git

> L'archivio NON include Maven Wrapper (`mvnw`/`mvnw.cmd`): non usare quei comandi fino all'eventuale generazione del wrapper da una macchina con Maven installato. Per avere il wrapper nel repository, dopo l'installazione di Maven un referente può eseguire `mvn -N wrapper:wrapper` e committare i file generati.

## Avvio del primo giorno (Windows PowerShell)
```powershell
java -version
mvn -version
mvn clean verify
mvn spring-boot:run
```
Da un secondo terminale:
```powershell
Invoke-WebRequest http://localhost:8080/health | Select-Object -ExpandProperty Content
```
Risultato atteso: `PICUS baseline avviata`.
Le altre rotte non sono attive e sono bloccate finche' G1 non implementa i controlli.

## Importazione nel repository GitHub già esistente
Non creare un secondo repository Git in questa cartella. Copiare il **contenuto** della cartella dentro la radice del repository locale del gruppo (es. GR-12-Repository). Prima di copiare, confrontare i file già presenti e non sovrascrivere `README.md`, `pom.xml` o configurazioni condivise senza revisione.

Da PowerShell, nella radice **effettiva del repository Git**:
```powershell
git status
git switch -c chore/bootstrap-walking-skeleton
git add .
git commit -m "chore: base comune walking skeleton PICUS"
git push -u origin chore/bootstrap-walking-skeleton
```
Aprire una Pull Request verso `main`. Dopo l'unione, tutti aggiornano `main` e creano i branch G1, G2, G3.

## Moduli e proprietari
| Area | Responsabile | Cosa implementare |
|---|---|---|
| `identity/`, `organizations/`, Security | G1 | Login/logout reale, OPT A, OPT B, Banco, attore autenticato, lookup organizzazione, autorizzazione per ruolo |
| `requests/{api,application,domain,infrastructure}` | G2 | ServiceRequest, regole di stato, repository e adapter JPA, RequestService, controlli di ownership |
| `requests/web`, `templates/`, `static/`, E2E | G3 | Controller, form, viste OPT/Banco, test funzionali e integrazione |

### Contratti condivisi (NON cambiare senza accordo dei tre gruppi)
- `identity.api.CurrentActorProvider.currentActor() : AuthenticatedActor`
- `identity.api.AuthenticatedActor`: username, role, `Optional<UUID> organizationId`
- `organizations.api.OrganizationLookup.existsById(UUID) : boolean`
- `requests.api.RequestUseCases`: `submitRequest`, `listMine`, `getMine`, `listForBanco`, `getForBanco`, `startProcessing`
- `requests.domain.ServiceRequestRepository`: `save`, `findById`, `findByIdAndOrganizationId`, `findByOrganizationId`, `findAll`

`RequestUseCases` NON ha ancora un'implementazione Spring; G2 la fornirà. G3 potrà creare schermate statiche o usare finti servizi **solo nei test**, ma non va introdotto un mock persistente nell'applicazione distribuita.

## Regole di autorizzazione da implementare
- `BANCO`: consultazione e avvio elaborazione.
- `OPT`: invio e consultazione delle sole richieste della propria organizzazione.
- L'organizzazione proprietaria si ricava **sempre dal principal autenticato lato server**, mai da un campo inviato dal browser.
- Le viste distinte NON sostituiscono i controlli backend.
- Security iniziale: accesso alle route applicative negato; G1 la sostituisce con Spring Security, mantenendo CSRF attivo sui form.

## Flusso e perimetro
Login OPT → invio misura richiesta (nessuna verifica obbligatoria di misura attiva) → login Banco → avvio elaborazione → consultazione OPT. Gli stati iniziali sono `DA_VALUTARE` e `IN_ELABORAZIONE`, ancora proposti e da validare. Nessuna registrazione di dati identificativi degli assistiti. Catalogo dinamico, operatori, erogatori, appuntamenti, report e notifiche sono fuori MVP.

## Endpoint HTTP concordati
| Metodo | Path | Attore |
|---|---|---|
| GET/POST | `/login` | Pubblico; gestito da G1 (pagina integrata da G3) |
| POST | `/logout` | Autenticato |
| GET | `/opt/requests` | OPT |
| GET | `/opt/requests/new` | OPT |
| POST | `/opt/requests` | OPT |
| GET | `/opt/requests/{id}` | OPT proprietaria |
| GET | `/banco/requests` | BANCO |
| GET | `/banco/requests/{id}` | BANCO |
| POST | `/banco/requests/{id}/start` | BANCO |

## Checklist avvio comune
- [ ] `mvn clean verify` eseguito su almeno una macchina del gruppo
- [ ] `/health` restituisce `PICUS baseline avviata`
- [ ] `main` integrato, tre branch creati dopo l'integrazione
- [ ] Prima di modificare le API pubbliche, i tre referenti concordano la firma
- [ ] G1 prende in carico la sostituzione di `BootstrapSecurityConfig`
- [ ] G2 prende in carico l'implementazione di `RequestUseCases` e l'adapter JPA
- [ ] G3 prende in carico controller, template e test end-to-end

## Sicurezza e dati di prova
- H2 su file locale `picus-dev.mv.db` (ignorato da Git); configurazione **solo locale**, non per la produzione.
- Password fittizie di prova: crearle nel lavoro di G1 tramite encoder, **non** includerle nel repository o in documenti di consegna.
- Non disattivare CSRF né inserire credenziali reali nel codice.
- Evitare campo note o descrizioni libere che possano contenere dati personali degli assistiti.

## Criteri di accettazione del Walking Skeleton
1. Banco e due OPT accedono con ruoli corretti.
2. La OPT A invia una misura, che è salvata con proprietario e stato iniziale.
3. Il Banco consulta e avvia l'elaborazione.
4. La OPT A rilegge la richiesta aggiornata dopo il riavvio.
5. La OPT B non può leggere la richiesta A: QS-SEC-01.
6. Form, payload e persistenza sono privi di identificativi dell'assistito: QS-PRI-01.
7. Risultati dei test automatici registrati, non solo descritti.
