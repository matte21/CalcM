Modifiche dall'ultima volta
====================================

1. Nell'applicazione delle statistiche, i dati relativi ad ogni posto vengono mantenuti in memoria e non più recuperati tramite query ad ogni notifica di nuovo valore rilevato dai sensori
2. Sono state aggiunte 4 nuove statistiche:
    1. media tra i posti di un'aula del rapporto tra il tempo in cui sono stati occupati e quello in cui non sono stati liberi (ovvero occupati o con materiale lasciato sul tavolo)
    2. media tra i posti di un'aula del rapporto tra il tempo in cui sono stati liberi e quello in cui non sono stati occupati (ovvero liberi o con materiale lasciato sul tavolo)
    3. rapporto tra posti occupati e posti non liberi al momento
    4. rapporto tra posti liberi e posti non occupati al momento
3. Nei grafici che rappresentano delle percentuali, la scala dell'asse delle ordinate è stata fissata in modo tale che venga visualizzato sempre l'intervallo 0%-100%
4. Sono stati prodotti dei dati di prova per le statistiche, riferiti alle 2 aule studio virtuali predisposte per la simulazione
5. All'applicazione che simula le aule studio è stata aggiunta la funzionalità di creare nuove aule con un numero arbitrario di tavoli e di posti
6. Aggiunta di un web client e del corrispondente back end che permettono di:
    1. visualizzare ID, numero di posti liberi, capienza, stato corrente (chiusa/aperta) indirizzo, orario della prossima apertura/chiusura e università delle aule studio
    2. impostare dei filtri e visualizzare solo le aule studio che li soddisfano. Tali filtri sono: solo aule aperte, solo aule con certe features (es. WiFi, bagno disabili, etc...), solo aule con almeno X (specificato da utente) posti disponibili, solo aule con due posti liberi vicini, solo aule che sono aperte per almeno Y (specificato da utente) minuti a partire dall'istante corrente, solo aule nel raggio di Z (specificato da utente) km dalla posizione dell'utente, varie (non tutte) combinazioni dei filtri elencati
    3. per aclune combinazioni di filtri, l'utente può chiedere di ricevere delle notifiche ogni volta che le aule studio che li soddisfano cambiano. Per esempio, dopo avere visualizzato le aule aperte, si può essere notificati ogni volta che un'aula studio aperta chiude o viceversa (e le aule mostrate sulla pagina web vengono aggiornate in real-time ad ogni modifica). Le combinazioni di filtri per cui le notifiche sono disponibili è limitata
7. Aggiunta di un KP che monitora con una subscribe SPARQL lo stato (libero/occupato) dei posti a sedere nelle aule studio e aggiorna il numero di posti liberi. Per ogni aula, il KP monitora, sempre tramite subscribe, anche lo stato dell'aula stessa (aperta/chiusa) e reagisce terminando o avviando la subscribe SPARQL sullo stato dei posti a sedere. Il motivo è che mentre un'aula è chiusa i posti liberi non cambieranno, e la SIB può gestire una sub in meno, per maggiore efficienza
8. Aggiunta di un KP che a partire da un file di testo contenente l'orario di un'aula studio, la apre/chiude in modo appropriato (con uno SPARQL update)
9. Per quanto riguarda le KPI Scala:
    1. è stata testata e perfezionata l'operazione di sottoscrizione
    2. è stata realizzata la classe KPIPrimitiveUsage per mostrare un esempio di utilizzo delle funzionalità di base della libreria
