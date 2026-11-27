package dieterbaier.letterwright.adapter.in.api;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;

/**
 * Ein Transaktionsmanager ohne Datenbank. Beitreten, Eröffnen, Commit und Rollback samt rollback-only übernimmt die
 * echte Logik von {@link AbstractPlatformTransactionManager}; die Attrappe zählt nur mit. Eine unabhängige Transaktion
 * ({@code REQUIRES_NEW}) würde die laufende aussetzen; das unterstützt sie nicht und scheitert dann.
 */
final class FakeTransactionManager extends AbstractPlatformTransactionManager {

    private static final long serialVersionUID = 1L;

    boolean running;
    boolean rollbackOnly;
    int opened;
    int committed;
    int rolledBack;
    /** Ob beim letzten Aufruf des Anwendungsfalls eine Transaktion lief, und wie viele bis dahin eröffnet waren. */
    boolean ranAtInvocation;
    int openedAtInvocation;

    @Override
    protected Object doGetTransaction() {
        return new Transaction(running);
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        return ((Transaction) transaction).existing;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        running = true;
        rollbackOnly = false;
        opened++;
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        committed++;
        running = false;
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        rolledBack++;
        running = false;
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        rollbackOnly = true;
    }

    void recordInvocation() {
        ranAtInvocation = running;
        openedAtInvocation = opened;
    }

    /** Meldet den globalen rollback-only-Zustand, damit ein Commit danach wie in echt scheitert. */
    private final class Transaction implements SmartTransactionObject {

        private final boolean existing;

        Transaction(boolean existing) {
            this.existing = existing;
        }

        @Override
        public boolean isRollbackOnly() {
            return rollbackOnly;
        }

        @Override
        public void flush() {
        }
    }
}
