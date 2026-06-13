"use client";

import { AppShell } from "@/components/app-shell";
import { useEffect, useState } from "react";
import {
  listCampaigns,
  launchCampaign,
  archiveCampaign,
  Campaign,
} from "@/lib/api/crm";
import { Button } from "@/components/ui/button";
import Link from "next/link";

export default function CampaignsPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCampaigns = async () => {
      try {
        const response = await listCampaigns({ page: 0, size: 20 });
        setCampaigns(
          response.content.filter(
            (c) => c.status !== "ARCHIVED"
          )
        );
      } catch (err) {
        setError("Failed to load campaigns");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCampaigns();
  }, []);

  const handleLaunch = async (id: string) => {
    try {
      await launchCampaign(id);

      setCampaigns(
        campaigns.map((c) =>
          c.id === id
            ? { ...c, status: "LAUNCHED" as const }
            : c
        )
      );
    } catch (err) {
      setError("Failed to launch campaign");
      console.error(err);
    }
  };

  const handleArchive = async (id: string) => {
    if (!confirm("Archive this campaign?")) return;

    try {
      await archiveCampaign(id);

      setCampaigns(
        campaigns.filter((c) => c.id !== id)
      );
    } catch (err) {
      setError("Failed to archive campaign");
      console.error(err);
    }
  };

  const statusColor = (status: string) => {
    switch (status) {
      case "DRAFT":
        return "bg-muted text-foreground";

      case "LAUNCHED":
        return "bg-green-500/15 text-green-600 dark:text-green-400";

      case "ARCHIVED":
        return "bg-amber-500/15 text-amber-600 dark:text-amber-400";

      default:
        return "bg-muted text-foreground";
    }
  };

  if (isLoading) {
    return (
      <AppShell
        title="Campaigns"
        eyebrow="Campaign Management"
      >
        <div className="p-8 text-muted-foreground">
          Loading...
        </div>
      </AppShell>
    );
  }

  return (
    <AppShell
      title="Campaigns"
      eyebrow="Campaign Management"
    >
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">
            Campaigns
          </h1>

          <div className="space-x-4">
            <Link href="/campaigns/new">
              <Button>Create Campaign</Button>
            </Link>

            <Link href="/ai-audience">
              <Button variant="outline">
                AI Copilot
              </Button>
            </Link>
            <Link href="/archived-campaigns">
              <Button variant="outline">
                Archived
              </Button>
            </Link>
          </div>
        </div>

        {error && (
          <div className="mb-4 rounded border border-destructive/20 bg-destructive/10 p-4 text-destructive">
            {error}
          </div>
        )}

        {campaigns.length === 0 ? (
          <div className="py-12 text-center">
            <p className="mb-4 text-muted-foreground">
              No campaigns yet
            </p>

            <Link href="/campaigns/new">
              <Button>
                Create your first campaign
              </Button>
            </Link>
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
                      <span
                        className={`rounded px-3 py-1 text-sm font-medium ${statusColor(
                          campaign.status
                        )}`}
                      >
                        {campaign.status}
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

                    <td className="px-6 py-4 text-right space-x-2">
                      {campaign.status === "DRAFT" && (
                        <button
                          onClick={() =>
                            handleLaunch(campaign.id)
                          }
                          className="text-sm text-primary hover:underline"
                        >
                          Launch
                        </button>
                      )}

                      {campaign.status === "LAUNCHED" && (
                        <Link
                          href={`/analytics?campaign=${campaign.id}`}
                          className="text-sm text-primary hover:underline"
                        >
                          Analytics
                        </Link>
                      )}

                      <button
                        onClick={() =>
                          handleArchive(campaign.id)
                        }
                        className="ml-4 text-sm text-amber-600 hover:underline"
                      >
                        Archive
                      </button>
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