"use client";

import { useState } from "react";
import { generateAudience, AudienceBuilderResponse } from "@/lib/api/crm";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import Link from "next/link";
import { AppShell } from "@/components/app-shell";

export default function AiAudiencePage() {
  const [prompt, setPrompt] = useState("");
  const [result, setResult] = useState<AudienceBuilderResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [segmentName, setSegmentName] = useState("");
  const [shouldPersist, setShouldPersist] = useState(false);

  const handleGenerate = async () => {
    if (!prompt.trim()) {
      setError("Please enter a prompt");
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const response = await generateAudience(prompt, shouldPersist, shouldPersist ? segmentName : undefined);
      setResult(response);
    } catch (err) {
      setError("Failed to generate audience");
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const examplePrompts = [
    "Show me customers from Delhi who spent more than 5000",
    "Find customers who haven't purchased in the last 90 days",
    "Get high-value customers with more than 5 orders",
    "Show customers interested in beauty products",
    "Find fashion shoppers from Mumbai",
  ];

  return (
    <AppShell title="AI Audience" eyebrow="Audience Builder">
      <div className="w-full">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">AI Audience Builder</h1>
          <Link href="/campaigns">
            <Button variant="outline">Back</Button>
          </Link>
        </div>

        <div className="rounded-xl border bg-card text-card-foreground p-6 mb-8 shadow-sm">
          <div className="space-y-4">
            <div>
              <Label htmlFor="prompt">Describe Your Audience</Label>
              <Textarea
                id="prompt"
                value={prompt}
                onChange={(e) => setPrompt(e.target.value)}
                placeholder="E.g., 'Show me customers from Delhi who spent more than 5000'"
                rows={4}
                className="w-full"
              />
            </div>

            <div className="rounded-lg border bg-primary/10 p-4">
              <p className="text-sm font-semibold mb-2">Example prompts:</p>
              <div className="space-y-1">
                {examplePrompts.map((example, idx) => (
                  <button
                    key={idx}
                    onClick={() => setPrompt(example)}
                    className="block text-sm text-primary hover:underline text-left"
                  >
                    • {example}
                  </button>
                ))}
              </div>
            </div>

            <Button onClick={handleGenerate} disabled={isLoading} className="w-full">
              {isLoading ? "Generating..." : "Generate Audience"}
            </Button>
          </div>
        </div>

        {error && (
          <div className="rounded-lg border border-destructive/30 bg-destructive/10 text-destructive p-4 mb-4">
            {error}
          </div>
        )}

        {result && (
          <div className="space-y-6">
            <div className="rounded-xl border border-green-500/20 bg-green-500/10 p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h2 className="text-2xl font-bold">{result.audienceSize} Customers</h2>
                  <p className="mt-2 text-muted-foreground">{result.explanation}</p>
                </div>
              </div>
            </div>

            <div className="rounded-xl border bg-card text-card-foreground p-6 shadow-sm">
              <h3 className="text-xl font-bold mb-4">Save as Segment</h3>
              <div className="space-y-4">
                <div className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    id="persist"
                    checked={shouldPersist}
                    onChange={(e) => setShouldPersist(e.target.checked)}
                  />
                  <label htmlFor="persist" className="text-sm">Save this audience as a reusable segment</label>
                </div>

                {shouldPersist && (
                  <div>
                    <Label>Segment Name</Label>
                    <Input
                      value={segmentName}
                      onChange={(e) => setSegmentName(e.target.value)}
                      placeholder="e.g., High-Value Delhi Customers"
                    />
                  </div>
                )}

                {result.segmentId ? (
                  <div className="rounded-lg border border-primary/20 bg-primary/10 p-4">
                    <p className="text-sm">
                      ✓ Saved as segment: <strong>{result.segmentName}</strong>
                    </p>
                  </div>
                ) : (
                  <Button
                    onClick={async () => {
                      if (!segmentName.trim()) {
                        setError("Please enter a segment name");
                        return;
                      }
                      try {
                        const response = await generateAudience(prompt, true, segmentName);
                        setResult(response);
                      } catch (err) {
                        setError("Failed to save segment");
                        console.error(err);
                      }
                    }}
                    disabled={!shouldPersist || !segmentName.trim()}
                    className="w-full"
                  >
                    Save Segment
                  </Button>
                )}
              </div>
            </div>

            <div className="rounded-xl border bg-card text-card-foreground p-6 shadow-sm">
              <h3 className="text-xl font-bold mb-4">Preview ({result.customers.slice(0, 5).length} of {result.audienceSize})</h3>
              <div className="space-y-3">
                {result.customers.slice(0, 5).map((customer) => (
                  <div key={customer.id} className="flex items-center justify-between p-3 rounded-lg border bg-muted/40">
                    <div>
                      <p className="font-semibold text-sm">{customer.firstName} {customer.lastName}</p>
                      <p className="text-xs text-muted-foreground">{customer.email}</p>
                    </div>
                    {customer.city && <span className="text-xs bg-secondary px-2 py-1 rounded">{customer.city}</span>}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </AppShell>
  );
}
