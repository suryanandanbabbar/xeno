"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Trash2 } from "lucide-react";
import { AppShell } from "@/components/app-shell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useAuthGuard } from "@/hooks/use-auth-guard";
import { createSegment, deleteSegment, listSegments, updateSegment, type Segment, type SegmentInput } from "@/lib/api/crm";
import { shortDate } from "@/lib/format";

const defaultConditions = {
  totalSpend: { operator: "gte", value: 0 },
  orderCount: { operator: "gte", value: 0 },
  lastPurchaseDate: { operator: "after", value: "" },
  city: "",
  category: ""
};

export default function SegmentsPage() {
  useAuthGuard();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<Segment | null>(null);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [conditions, setConditions] = useState(defaultConditions);
  const filterJson = useMemo(() => JSON.stringify(conditions, null, 2), [conditions]);
  const segments = useQuery({ queryKey: ["segments", page], queryFn: () => listSegments({ page, size: 10 }) });
  const save = useMutation({
    mutationFn: () => {
      const payload: SegmentInput = { name, description, filterJson };
      return editing ? updateSegment(editing.id, payload) : createSegment(payload);
    },
    onSuccess: () => {
      reset();
      queryClient.invalidateQueries({ queryKey: ["segments"] });
    }
  });
  const remove = useMutation({ mutationFn: deleteSegment, onSuccess: () => queryClient.invalidateQueries({ queryKey: ["segments"] }) });

  function reset() {
    setEditing(null);
    setName("");
    setDescription("");
    setConditions(defaultConditions);
  }

  return (
    <AppShell title="Segments" eyebrow="Audience Builder">
      <section className="grid gap-6 xl:grid-cols-[420px_1fr]">
        <form className="rounded-lg border bg-card p-5" onSubmit={(event) => { event.preventDefault(); save.mutate(); }}>
          <h2 className="text-lg font-semibold">{editing ? "Update segment" : "Create segment"}</h2>
          <div className="mt-5 grid gap-4">
            <Field label="Name" value={name} onChange={setName} />
            <div className="space-y-2"><Label>Description</Label><Textarea value={description} onChange={(event) => setDescription(event.target.value)} /></div>
            <div className="grid gap-3 sm:grid-cols-2">
              <NumberCondition label="Total spend at least" value={conditions.totalSpend.value} onChange={(value) => setConditions({ ...conditions, totalSpend: { operator: "gte", value } })} />
              <NumberCondition label="Order count at least" value={conditions.orderCount.value} onChange={(value) => setConditions({ ...conditions, orderCount: { operator: "gte", value } })} />
            </div>
            <Field label="Last purchase after" type="date" value={conditions.lastPurchaseDate.value} onChange={(value) => setConditions({ ...conditions, lastPurchaseDate: { operator: "after", value } })} required={false} />
            <Field label="City" value={conditions.city} onChange={(value) => setConditions({ ...conditions, city: value })} required={false} />
            <Field label="Category" value={conditions.category} onChange={(value) => setConditions({ ...conditions, category: value })} required={false} />
            {/* <div className="space-y-2"><Label>Stored filter JSON</Label><Textarea value={filterJson} readOnly className="font-mono text-xs" /></div> */}
            {save.error && <p className="text-sm text-destructive">{save.error.message}</p>}
            <Button type="submit" disabled={save.isPending}>{save.isPending ? "Saving" : editing ? "Update segment" : "Create segment"}</Button>
            {editing && <Button type="button" variant="outline" onClick={reset}>Cancel edit</Button>}
          </div>
        </form>

        <section className="overflow-hidden rounded-lg border bg-card">
          <table className="w-full text-sm">
            <thead className="bg-secondary text-left">
              <tr><th className="p-3">Name</th><th className="p-3">Description</th><th className="p-3">Created</th><th className="p-3 text-right">Actions</th></tr>
            </thead>
            <tbody>
              {(segments.data?.content ?? []).map((segment) => (
                <tr key={segment.id} className="border-t align-top">
                  <td className="p-3 font-medium">{segment.name}</td>
                  <td className="p-3 text-muted-foreground">{segment.description || "-"}</td>
                  <td className="p-3">{shortDate(segment.createdAt)}</td>
                  <td className="p-3">
                    <div className="flex justify-end gap-2">
                      <Button variant="outline" size="sm" onClick={() => {
                        setEditing(segment);
                        setName(segment.name);
                        setDescription(segment.description ?? "");
                        setConditions(JSON.parse(segment.filterJson));
                      }}>Edit</Button>
                      <Button variant="destructive" size="sm" onClick={() => remove.mutate(segment.id)}><Trash2 className="h-4 w-4" /></Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pager page={page} totalPages={segments.data?.totalPages ?? 1} setPage={setPage} />
        </section>
      </section>
    </AppShell>
  );
}

function Field({ label, value, onChange, type = "text", required = true }: { label: string; value: string; onChange: (value: string) => void; type?: string; required?: boolean }) {
  return <div className="space-y-2"><Label>{label}</Label><Input type={type} value={value} onChange={(event) => onChange(event.target.value)} required={required} /></div>;
}

function NumberCondition({ label, value, onChange }: { label: string; value: number; onChange: (value: number) => void }) {
  return <div className="space-y-2"><Label>{label}</Label><Input type="number" value={String(value)} min={0} onChange={(event) => onChange(Number(event.target.value))} /></div>;
}

function Pager({ page, totalPages, setPage }: { page: number; totalPages: number; setPage: (page: number) => void }) {
  return <div className="flex items-center justify-end gap-2 border-t p-3"><Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</Button><span className="text-sm text-muted-foreground">Page {page + 1} of {Math.max(totalPages, 1)}</span><Button variant="outline" size="sm" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Next</Button></div>;
}
