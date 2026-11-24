package dieterbaier.standardschreiben.adapter.in.api;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;

/**
 * Ein Transaktionsmanager ohne Datenbank. Beitreten, Eröffnen, Commit und Rollback samt rollback-only übernimmt die
 * echte Logik von {@link AbstractPlatformTransactionManager}; die Attrappe zählt nur mit. Eine unabhängige Transaktion
 * ({@code REQUIRES_NEW}) würde die laufende aussetzen; das unterstützt sie nicht und scheitert dann.
 */
final class Transaktionsattrappe extends AbstractPlatformTransactionManager {

    private static final long serialVersionUID = 1L;

    boolean laeuft;
    boolean nurNochRollback;
    int eroeffnet;
    int committet;
    int zurueckgerollt;
    /** Ob beim letzten Aufruf des Anwendungsfalls eine Transaktion lief, und wie viele bis dahin eröffnet waren. */
    boolean liefBeimAufruf;
    int eroeffnetBeimAufruf;

    @Override
    protected Object doGetTransaction() {
        return new Transaktion(laeuft);
    }

    @Override
    protected boolean isExistingTransaction(Object transaktion) {
        return ((Transaktion) transaktion).bestehend;
    }

    @Override
    protected void doBegin(Object transaktion, TransactionDefinition definition) {
        laeuft = true;
        nurNochRollback = false;
        eroeffnet++;
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        committet++;
        laeuft = false;
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        zurueckgerollt++;
        laeuft = false;
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        nurNochRollback = true;
    }

    void merkeAufruf() {
        liefBeimAufruf = laeuft;
        eroeffnetBeimAufruf = eroeffnet;
    }

    /** Meldet den globalen rollback-only-Zustand, damit ein Commit danach wie in echt scheitert. */
    private final class Transaktion implements SmartTransactionObject {

        private final boolean bestehend;

        Transaktion(boolean bestehend) {
            this.bestehend = bestehend;
        }

        @Override
        public boolean isRollbackOnly() {
            return nurNochRollback;
        }

        @Override
        public void flush() {
        }
    }
}
