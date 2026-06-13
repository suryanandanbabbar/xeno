import { Suspense } from "react";
import { AnalyticsClient } from "./analytics-client";

function AnalyticsLoadingFallback() {
  return (
    <div className="p-8">
      <div className="w-full">
        <div className="h-10 bg-muted rounded mb-8 w-48"></div>
        <div className="h-20 bg-muted/60 rounded mb-8"></div>
      </div>
    </div>
  );
}

export default function AnalyticsPage() {
  return (
    <Suspense fallback={<AnalyticsLoadingFallback />}>
      <AnalyticsClient />
    </Suspense>
  );
}
