"use client";
import { AppShell } from "@/components/app-shell";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  getCampaign,
  updateCampaign,
  createCampaign,
  getCommunications,
  listSegments,
  Campaign,
  CampaignInput,
  CommunicationChannel,
  Communication,
  Segment,
} from "@/lib/api/crm";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import Link from "next/link";

export default function CampaignDetailPage() {
  const params = useParams();
  const router = useRouter();
  const campaignId = params.id as string;
  const isNew = campaignId === "new";

  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [segments, setSegments] = useState<Segment[]>([]);
  const [communications, setCommunications] = useState<Communication[]>([]);
  const [isLoading, setIsLoading] = useState(!isNew);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState<CampaignInput>({
    name: "",
    objective: "",
    segmentId: "",
    channel: "EMAIL",
    message: "",
    reasoning: "",
    status: "DRAFT",
  });

  useEffect(() => {
    const fetch = async () => {
      try {
        const segs = await listSegments({ page: 0, size: 100 });
        setSegments(segs.content);

        if (!isNew) {
          const camp = await getCampaign(campaignId);
          setCampaign(camp);
          setFormData({
            name: camp.name,
            objective: camp.objective,
            segmentId: camp.segmentId,
            channel: camp.channel,
            message: camp.message,
            reasoning: camp.reasoning || "",
            status: camp.status,
          });

          if (camp.status === "LAUNCHED" || camp.status === "ARCHIVED") {
            const comms = await getCommunications(campaignId);
            setCommunications(comms);
          }
        }
      } catch (err) {
        setError("Failed to load data");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetch();
  }, [campaignId, isNew]);

  const handleSave = async () => {
    setIsSaving(true);
    try {
      if (isNew) {
        const result = await createCampaign(formData);
        router.push(`/campaigns/${result.id}`);
      } else {
        await updateCampaign(campaignId, formData);
        const updated = await getCampaign(campaignId);
        setCampaign(updated);
      }
      setError(null);
    } catch (err) {
      setError("Failed to save campaign");
      console.error(err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleInputChange = (field: keyof CampaignInput, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  if (isLoading) {
  return (
    <AppShell title="Campaign" eyebrow="Campaign Management">
      <div className="p-8">Loading...</div>
    </AppShell>
  );
}

  return (
  <AppShell
    title={isNew ? "New Campaign" : campaign?.name ?? "Campaign"}
    eyebrow="Campaign Management"
  >
    <div className="p-8">
      <div className="max-w-4xl">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">{isNew ? "New Campaign" : campaign?.name}</h1>
          {campaign?.status === "ARCHIVED" && (
            <p className="mt-2 text-sm font-medium text-amber-600 dark:text-amber-400">
              Archived Campaign
            </p>
          )}
          {!isNew && (
            <Link href="/campaigns">
              <Button variant="outline">Back to Campaigns</Button>
            </Link>
          )}
        </div>

        {error && (
          <div className="rounded-lg border border-destructive/30 bg-destructive/10 text-destructive p-4 mb-4">
            {error}
          </div>
        )}

        <div className="rounded-xl border bg-card text-card-foreground p-6 mb-8 shadow-sm">
          <div className="space-y-4">
            <div>
              <Label>Campaign Name</Label>
              <Input
                value={formData.name}
                onChange={(e) => handleInputChange("name", e.target.value)}
                disabled={!isNew && campaign?.status !== "DRAFT"}
                placeholder="e.g., Summer Sale Campaign"
              />
            </div>

            <div>
              <Label>Objective</Label>
              <Textarea
                value={formData.objective}
                onChange={(e) => handleInputChange("objective", e.target.value)}
                disabled={!isNew && campaign?.status !== "DRAFT"}
                placeholder="What is the goal of this campaign?"
                rows={3}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label>Segment</Label>
                <select
                  value={formData.segmentId}
                  onChange={(e) => handleInputChange("segmentId", e.target.value)}
                  disabled={!isNew && campaign?.status !== "DRAFT"}
                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                >
                  <option value="">Select a segment</option>
                  {segments.map(seg => (
                    <option key={seg.id} value={seg.id}>{seg.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <Label>Channel</Label>
                <select
                  value={formData.channel}
                  onChange={(e) => handleInputChange("channel", e.target.value as CommunicationChannel)}
                  disabled={!isNew && campaign?.status !== "DRAFT"}
                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                >
                  <option value="EMAIL">Email</option>
                  <option value="SMS">SMS</option>
                  <option value="WHATSAPP">WhatsApp</option>
                  <option value="RCS">RCS</option>
                </select>
              </div>
            </div>

            <div>
              <Label>Message</Label>
              <Textarea
                value={formData.message}
                onChange={(e) => handleInputChange("message", e.target.value)}
                disabled={!isNew && campaign?.status !== "DRAFT"}
                placeholder="Campaign message content"
                rows={4}
              />
            </div>

            <div>
              <Label>Reasoning</Label>
              <Textarea
                value={formData.reasoning}
                onChange={(e) => handleInputChange("reasoning", e.target.value)}
                disabled={!isNew && campaign?.status !== "DRAFT"}
                placeholder="Why this approach?"
                rows={3}
              />
            </div>

            {campaign?.status === "ARCHIVED" && (
              <div className="rounded-lg border border-amber-500/30 bg-amber-500/10 p-4 text-amber-700 dark:text-amber-300">
                This campaign has been archived and can no longer be edited.
              </div>
            )}

            {(isNew || campaign?.status === "DRAFT") && (
  <Button
    onClick={handleSave}
    disabled={isSaving}
    className="w-full"
  >
    {isSaving
      ? "Saving..."
      : isNew
      ? "Create Campaign"
      : "Save Campaign"}
  </Button>
)}
          </div>
        </div>

        {(campaign?.status === "LAUNCHED" || campaign?.status === "ARCHIVED") && (
          <>
            <div className="rounded-xl border bg-card text-card-foreground p-6 mb-8 shadow-sm">
              <h2 className="text-2xl font-bold mb-4">Communications History</h2>
              {communications.length === 0 ? (
                <p className="text-muted-foreground">No communications yet</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b border-border">
                        <th className="px-4 py-2 text-left text-sm font-semibold">Customer</th>
                        <th className="px-4 py-2 text-left text-sm font-semibold">Status</th>
                        <th className="px-4 py-2 text-left text-sm font-semibold">Channel</th>
                        <th className="px-4 py-2 text-left text-sm font-semibold">Sent</th>
                      </tr>
                    </thead>
                    <tbody>
                      {communications.slice(0, 10).map(comm => (
                        <tr key={comm.id} className="border-b border-border">
                          <td className="px-4 py-2 text-sm">{comm.customerName}</td>
                          <td className="px-4 py-2 text-sm">
                            <span className="px-2 py-1 rounded text-xs bg-primary/15 text-primary">
                              {comm.status}
                            </span>
                          </td>
                          <td className="px-4 py-2 text-sm">{comm.channel}</td>
                          <td className="px-4 py-2 text-sm text-muted-foreground">
                            {new Date(comm.createdAt).toLocaleDateString()}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            <Link href={`/analytics?campaign=${campaignId}`}>
              <Button className="w-full">View Analytics & Insights</Button>
            </Link>
          </>
        )}
      </div>
    </div>
    </AppShell>
  );
}
