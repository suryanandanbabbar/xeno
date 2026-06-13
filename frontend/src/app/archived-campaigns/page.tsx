

"use client";

import { AppShell } from "@/components/app-shell";
import { useEffect, useState } from "react";
import { listCampaigns, Campaign } from "@/lib/api/crm";
import { Button } from "@/components/ui/button";
import Link from "next/link";

export default function ArchivedCampaignsPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCampaigns = async () => {
      try {
        const response = await listCampaigns({ page: 0, size: 100 });

        setCampaigns(
          response.content.filter(
            (campaign) => campaign.status === "ARCHIVED"
          )
        );
      } catch (err) {
        setError("Failed to load archived campaigns");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCampaigns();
  }, []);

  if (isLoading) {
    return (
      <AppShell
        title="Archived Campaigns"
        eyebrow="Campaign Archive"
      >
        <div className="p-8 text-muted-foreground">
          Loading...
        </div>
      </AppShell>
    );
  }

  return (
    <AppShell
      title="Archived Campaigns"
      eyebrow="Campaign Archive"
    >
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">
            Archived Campaigns
          </h1>

          <Link href="/campaigns">
            <Button variant="outline">
              Active Campaigns
            </Button>
          </Link>
        </div>

        {error && (
          <div className="mb-4 rounded border border-destructive/20 bg-destructive/10 p-4 text-destructive">
            {error}
          </div>
        )}

        {campaigns.length === 0 ? (
          <div className="py-12 text-center">
            <p className="text-muted-foreground">
              No archived campaigns found
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto rounded-lg border bg-card">
            <table className="w-full border-collapse">
              <thead>
                <tr className="border-b bg-muted/50">
                  <th className="px-6 py-3 text-left text-sm font-semibold">
                    Name
                  </th>
                  <th className="px-6 py-3 text-left text-sm font-semibold">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-sm font-semibold">
                    Channel
                  </th>
                  <th className="px-6 py-3 text-left text-sm font-semibold">
                    Segment
                  </th>
                  <th className="px-6 py-3 text-left text-sm font-semibold">
                    Created
                  </th>
                  <th className="px-6 py-3 text-right text-sm font-semibold">
                    Actions
                  </th>
                </tr>
              </thead>

              <tbody>
                {campaigns.map((campaign) => (
                  <tr
                    key={campaign.id}
                    className="border-b transition-colors hover:bg-muted/50"
                  >
                    <td className="px-6 py-4">
                      <Link
                        href={`/campaigns/${campaign.id}`}
                        className="text-primary hover:underline"
                      >
                        {campaign.name}
                      </Link>
                    </td>

                    <td className="px-6 py-4">
                      <span className="rounded px-3 py-1 text-sm font-medium bg-amber-500/15 text-amber-600 dark:text-amber-400">
                        ARCHIVED
                      </span>
                    </td>

                    <td className="px-6 py-4 text-sm">
                      {campaign.channel}
                    </td>

                    <td className="px-6 py-4 text-sm">
                      {campaign.segmentName}
                    </td>

                    <td className="px-6 py-4 text-sm text-muted-foreground">
                      {new Date(
                        campaign.createdAt
                      ).toLocaleDateString()}
                    </td>

                    <td className="px-6 py-4 text-right">
                      <Link
                        href={`/campaigns/${campaign.id}`}
                        className="text-sm text-primary hover:underline"
                      >
                        View
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </AppShell>
  );
}