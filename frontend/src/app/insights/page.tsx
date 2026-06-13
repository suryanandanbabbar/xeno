"use client";

import { useEffect, useState } from "react";
import {
  listCampaigns,
  getCampaignInsights,
  Campaign,
  Insights,
} from "@/lib/api/crm";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import { AppShell } from "@/components/app-shell";

export default function InsightsPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [selectedCampaignId, setSelectedCampaignId] = useState("");
  const [insights, setInsights] = useState<Insights | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        const response = await listCampaigns({ page: 0, size: 100 });
        const launched = response.content.filter(c => c.status === "LAUNCHED");
        setCampaigns(launched);
      } catch (err) {
        setError("Failed to load campaigns");
        console.error(err);
      }
    };

    fetch();
  }, []);

  useEffect(() => {
    if (!selectedCampaignId) {
      setInsights(null);
      return;
    }

    const fetch = async () => {
      setIsLoading(true);
      try {
        const result = await getCampaignInsights(selectedCampaignId);
        setInsights(result);
        setError(null);
      } catch (err) {
        setError("Failed to load insights");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetch();
  }, [selectedCampaignId]);

  return (
    <AppShell title="Insights" eyebrow="AI Campaign Insights">
      <div className="w-full">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">Campaign Insights</h1>
          <Link href="/campaigns">
            <Button variant="outline">Back</Button>
          </Link>
        </div>

        <div className="rounded-xl border bg-card text-card-foreground p-6 mb-8 shadow-sm">
          <label className="block text-sm font-semibold mb-2">Select Campaign</label>
          <select
            value={selectedCampaignId}
            onChange={(e) => setSelectedCampaignId(e.target.value)}
            className="w-full rounded-md border border-input bg-background px-4 py-2"
          >
            <option value="">Choose a campaign...</option>
            {campaigns.map(camp => (
              <option key={camp.id} value={camp.id}>{camp.name}</option>
            ))}
          </select>
        </div>

        {error && (
          <div className="rounded-lg border border-destructive/30 bg-destructive/10 text-destructive p-4 mb-4">
            {error}
          </div>
        )}

        {isLoading && <div className="text-center py-8">Generating insights...</div>}

        {insights && !isLoading && (
          <div className="space-y-6">
            <div className="rounded-xl border bg-card text-card-foreground p-6 shadow-sm">
              <h2 className="text-2xl font-bold mb-4">Performance Summary</h2>
              <p className="text-lg leading-relaxed text-muted-foreground">{insights.performanceSummary}</p>
            </div>

            <div className="rounded-xl border border-green-500/20 bg-green-500/10 p-6">
              <h2 className="text-2xl font-bold mb-4">✓ What Worked Well</h2>
              <p className="text-lg leading-relaxed">{insights.reasonsForSuccess}</p>
            </div>

            <div className="rounded-xl border border-orange-500/20 bg-orange-500/10 p-6">
              <h2 className="text-2xl font-bold mb-4">⚠ Areas for Improvement</h2>
              <p className="text-lg leading-relaxed">{insights.reasonsForFailure}</p>
            </div>

            <div className="rounded-xl border bg-card text-card-foreground p-6 shadow-sm">
              <h2 className="text-2xl font-bold mb-4">💡 Recommended Actions</h2>
              <p className="text-lg leading-relaxed text-muted-foreground">{insights.recommendedImprovements}</p>
            </div>

            <div className="rounded-xl border border-purple-500/20 bg-purple-500/10 p-6">
              <h2 className="text-2xl font-bold mb-4">🎯 Next Best Action</h2>
              <p className="text-lg leading-relaxed font-semibold">{insights.nextBestAction}</p>
              <Link href="/campaigns" className="mt-4 inline-block">
                <Button>Go to Campaigns</Button>
              </Link>
            </div>
          </div>
        )}

        {!selectedCampaignId && !insights && (
          <div className="text-center py-12 text-muted-foreground">
            <p className="mb-4">Select a campaign to view AI-generated insights</p>
            <p className="text-sm">Insights analyze campaign performance and provide recommendations for improvement</p>
          </div>
        )}
      </div>
    </AppShell>
  );
}
