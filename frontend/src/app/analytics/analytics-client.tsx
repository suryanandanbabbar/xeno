"use client";
import { AppShell } from "@/components/app-shell";
import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import {
  listCampaigns,
  getCampaignAnalytics,
  getChannelAnalytics,
  getCampaignInsights,
  Campaign,
  CampaignAnalytics,
  ChannelAnalytics,
  Insights,
} from "@/lib/api/crm";
import { Button } from "@/components/ui/button";
import Link from "next/link";

export function AnalyticsClient() {
  const searchParams = useSearchParams();
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [selectedCampaignId, setSelectedCampaignId] = useState(searchParams.get("campaign") || "");
  const [analytics, setAnalytics] = useState<CampaignAnalytics | null>(null);
  const [channelAnalytics, setChannelAnalytics] = useState<ChannelAnalytics | null>(null);
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
      setAnalytics(null);
      setChannelAnalytics(null);
      setInsights(null);
      return;
    }

    const fetch = async () => {
      setIsLoading(true);
      try {
        const [analytics, channel, insights] = await Promise.all([
          getCampaignAnalytics(selectedCampaignId),
          getChannelAnalytics(selectedCampaignId),
          getCampaignInsights(selectedCampaignId),
        ]);
        setAnalytics(analytics);
        setChannelAnalytics(channel);
        setInsights(insights);
        setError(null);
      } catch (err) {
        setError("Failed to load analytics");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetch();
  }, [selectedCampaignId]);

  const renderFunnel = () => {
    if (!analytics) return null;

    const stages = [
      { label: "Sent", value: analytics.sent },
      { label: "Delivered", value: analytics.delivered },
      { label: "Read", value: analytics.read },
      { label: "Clicked", value: analytics.clicked },
      { label: "Converted", value: analytics.converted },
    ];

    const maxValue = Math.max(...stages.map(s => s.value));

    return (
      <div className="bg-card text-card-foreground rounded-lg border p-6 mb-8">
        <h2 className="text-2xl font-bold mb-6">Conversion Funnel</h2>
        <div className="space-y-4">
          {stages.map((stage, idx) => (
            <div key={idx}>
              <div className="flex justify-between mb-2">
                <span className="font-semibold text-sm">{stage.label}</span>
                <span className="text-sm text-muted-foreground">{stage.value}</span>
              </div>
              <div className="bg-muted rounded-full overflow-hidden h-8">
                <div
                  className="bg-blue-500 h-full transition-all"
                  style={{ width: `${(stage.value / maxValue) * 100}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  return (
  <AppShell
    title="Analytics"
    eyebrow="Campaign Performance"
  >
      <div className="w-full">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">Analytics Dashboard</h1>
          <Link href="/campaigns">
            <Button variant="outline">Back to Campaigns</Button>
          </Link>
        </div>

        <div className="bg-card text-card-foreground rounded-lg border p-6 mb-8">
          <Label className="block mb-2">Select Campaign</Label>
          <select
            value={selectedCampaignId}
            onChange={(e) => setSelectedCampaignId(e.target.value)}
            className="w-full px-4 py-2 border rounded-md bg-background text-foreground"
          >
            <option value="">Choose a campaign...</option>
            {campaigns.map(camp => (
              <option key={camp.id} value={camp.id}>{camp.name}</option>
            ))}
          </select>
        </div>

        {error && <div className="border border-destructive/30 bg-destructive/10 text-destructive p-4 rounded mb-4">{error}</div>}

        {isLoading && <div className="text-center py-8">Loading analytics...</div>}

        {analytics && !isLoading && (
          <>
            {renderFunnel()}

            <div className="grid grid-cols-2 gap-6 mb-8">
              <div className="bg-card text-card-foreground rounded-lg border p-6">
                <h3 className="text-lg font-semibold mb-4">Performance Metrics</h3>
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span>Delivery Rate</span>
                    <span className="font-bold">{analytics.deliveryRate.toFixed(1)}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Read Rate</span>
                    <span className="font-bold">{analytics.readRate.toFixed(1)}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Click Rate</span>
                    <span className="font-bold">{analytics.clickRate.toFixed(1)}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Conversion Rate</span>
                    <span className="font-bold">{analytics.conversionRate.toFixed(1)}%</span>
                  </div>
                </div>
              </div>

              <div className="bg-card text-card-foreground rounded-lg border p-6">
                <h3 className="text-lg font-semibold mb-4">Summary</h3>
                <div className="space-y-2 text-sm">
                  <div>Total Sent: <strong>{analytics.sent}</strong></div>
                  <div>Delivered: <strong>{analytics.delivered}</strong></div>
                  <div>Read: <strong>{analytics.read}</strong></div>
                  <div>Clicked: <strong>{analytics.clicked}</strong></div>
                  <div>Converted: <strong>{analytics.converted}</strong></div>
                  <div>Failed: <strong>{analytics.failed}</strong></div>
                </div>
              </div>
            </div>

            {channelAnalytics && (
              <div className="bg-card text-card-foreground rounded-lg border p-6 mb-8">
                <h2 className="text-2xl font-bold mb-6">Channel Performance</h2>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b">
                        <th className="px-4 py-2 text-left font-semibold">Channel</th>
                        <th className="px-4 py-2 text-right font-semibold">Sent</th>
                        <th className="px-4 py-2 text-right font-semibold">Delivered</th>
                        <th className="px-4 py-2 text-right font-semibold">Read</th>
                        <th className="px-4 py-2 text-right font-semibold">Clicked</th>
                        <th className="px-4 py-2 text-right font-semibold">Delivery %</th>
                        <th className="px-4 py-2 text-right font-semibold">Click %</th>
                      </tr>
                    </thead>
                    <tbody>
                      {channelAnalytics.channelMetrics.map(metric => (
                        <tr key={metric.channel} className="border-b">
                          <td className="px-4 py-2">{metric.channel}</td>
                          <td className="px-4 py-2 text-right">{metric.sent}</td>
                          <td className="px-4 py-2 text-right">{metric.delivered}</td>
                          <td className="px-4 py-2 text-right">{metric.read}</td>
                          <td className="px-4 py-2 text-right">{metric.clicked}</td>
                          <td className="px-4 py-2 text-right">{metric.deliveryRate.toFixed(1)}%</td>
                          <td className="px-4 py-2 text-right">{metric.clickRate.toFixed(1)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {insights && (
              <div className="bg-card text-card-foreground rounded-lg border p-6">
                <h2 className="text-2xl font-bold mb-6">AI Insights</h2>
                <div className="space-y-6">
                  <div>
                    <h3 className="font-semibold text-lg mb-2">Performance Summary</h3>
                    <p className="text-muted-foreground">{insights.performanceSummary}</p>
                  </div>

                  <div>
                    <h3 className="font-semibold text-lg mb-2 text-green-600 dark:text-green-400">What Worked</h3>
                    <p className="text-muted-foreground">{insights.reasonsForSuccess}</p>
                  </div>

                  <div>
                    <h3 className="font-semibold text-lg mb-2 text-red-600 dark:text-red-400">What Didn't Work</h3>
                    <p className="text-muted-foreground">{insights.reasonsForFailure}</p>
                  </div>

                  <div>
                    <h3 className="font-semibold text-lg mb-2">Recommendations</h3>
                    <p className="text-muted-foreground">{insights.recommendedImprovements}</p>
                  </div>

                  <div className="bg-primary/10 border border-primary/20 p-4 rounded">
                    <h3 className="font-semibold mb-2">Next Best Action</h3>
                    <p className="text-primary">{insights.nextBestAction}</p>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </AppShell>
  );
}

const Label = ({ children, className }: { children: React.ReactNode; className?: string }) => (
  <label className={`text-sm font-semibold ${className}`}>{children}</label>
);
