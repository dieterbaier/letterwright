package dieterbaier.letterwright.adapter.in.api;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;

/**
 * A transaction manager without a database. Joining, opening, commit and rollback including rollback-only are handled
 * by the real logic of {@link AbstractPlatformTransactionManager}; the fake only counts. An independent transaction
 * ({@code REQUIRES_NEW}) would suspend the running one; the fake does not support that and fails.
 */
final class FakeTransactionManager extends AbstractPlatformTransactionManager {

    private static final long serialVersionUID = 1L;

    boolean running;
    boolean rollbackOnly;
    int opened;
    int committed;
    int rolledBack;
    /** Whether a transaction was running at the last call of the use case, and how many had been opened by then. */
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

    /** Reports the global rollback-only state so that a subsequent commit fails as it would for real. */
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
