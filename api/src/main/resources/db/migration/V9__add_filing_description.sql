-- Expose EDGAR's primaryDocDescription for SEC filings so the company-research
-- view can present a human-readable label per filing. Free-form EDGAR text, so
-- TEXT (no length cap) and nullable, since EDGAR sometimes omits it.
ALTER TABLE sec_filings
    ADD COLUMN description TEXT;
