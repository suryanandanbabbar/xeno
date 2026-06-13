package ai.xenopilot.customer;

import java.util.List;

public record CsvImportPreviewResponse<T>(List<CsvRecordPreview<T>> records, long validCount, long invalidCount) {
}
