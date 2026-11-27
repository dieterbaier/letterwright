package dieterbaier.letterwright.usecases.out;

/**
 * Checks a PDF for PDF/A-2b and PDF/UA-1 (ADR-005). Implemented by {@code adapter-rendering}. If the checker cannot
 * check, it throws an exception; that never counts as conformant.
 */
public interface ConformanceChecker {

    Conformance check(byte[] pdf);
}
