"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Trash2, Upload } from "lucide-react";
import { AppShell } from "@/components/app-shell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuthGuard } from "@/hooks/use-auth-guard";
import { createOrder, deleteOrder, importOrders, listCustomers, listOrders, updateOrder, type Order, type OrderInput } from "@/lib/api/crm";
import { money, shortDate } from "@/lib/format";

const emptyOrder: OrderInput = { customerId: "", amount: 0, category: "", purchaseDate: new Date().toISOString().slice(0, 10) };

export default function OrdersPage() {
  useAuthGuard();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<Order | null>(null);
  const [form, setForm] = useState<OrderInput>(emptyOrder);
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<any>(null);
  const orders = useQuery({ queryKey: ["orders", page], queryFn: () => listOrders({ page, size: 10 }) });
  const customers = useQuery({ queryKey: ["customers", "order-select"], queryFn: () => listCustomers({ size: 100 }) });
  const save = useMutation({
    mutationFn: () => editing ? updateOrder(editing.id, form) : createOrder(form),
    onSuccess: () => {
      setEditing(null);
      setForm(emptyOrder);
      queryClient.invalidateQueries({ queryKey: ["orders"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard-stats"] });
      queryClient.invalidateQueries({ queryKey: ["customer-profile"] });
    }
  });
  const remove = useMutation({
    mutationFn: deleteOrder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["orders"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard-stats"] });
    }
  });
  const upload = useMutation({
    mutationFn: (commit: boolean) => importOrders(file!, !commit),
    onSuccess: (data, commit) => {
      setPreview(data);
      if (commit) {
        queryClient.invalidateQueries({ queryKey: ["orders"] });
        queryClient.invalidateQueries({ queryKey: ["dashboard-stats"] });
      }
    }
  });

  return (
    <AppShell title="Orders" eyebrow="Order Management">
      <section className="grid gap-6 xl:grid-cols-[360px_1fr]">
        <form className="rounded-lg border bg-card p-5" onSubmit={(event) => { event.preventDefault(); save.mutate(); }}>
          <h2 className="text-lg font-semibold">{editing ? "Update order" : "Create order"}</h2>
          <div className="mt-5 grid gap-4">
            <div className="space-y-2">
              <Label>Customer</Label>
              <select required className="h-10 w-full rounded-md border bg-background px-3 text-sm" value={form.customerId} onChange={(event) => setForm({ ...form, customerId: event.target.value })}>
                <option value="">Select customer</option>
                {(customers.data?.content ?? []).map((customer) => <option key={customer.id} value={customer.id}>{customer.firstName} {customer.lastName}</option>)}
              </select>
            </div>
            <Field label="Amount" type="number" value={String(form.amount || "")} onChange={(value) => setForm({ ...form, amount: Number(value) })} />
            <Field label="Category" value={form.category} onChange={(value) => setForm({ ...form, category: value })} />
            <Field label="Purchase Date" type="date" value={form.purchaseDate} onChange={(value) => setForm({ ...form, purchaseDate: value })} />
            {save.error && <p className="text-sm text-destructive">{save.error.message}</p>}
            <Button type="submit" disabled={save.isPending}>{save.isPending ? "Saving" : editing ? "Update order" : "Create order"}</Button>
            {editing && <Button type="button" variant="outline" onClick={() => { setEditing(null); setForm(emptyOrder); }}>Cancel edit</Button>}
          </div>
        </form>

        <div className="space-y-6">
          <section className="rounded-lg border bg-card p-5">
            <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
              <div>
                <h2 className="font-semibold">CSV Import</h2>
                <p className="text-sm text-muted-foreground">Columns: customerId, amount, category, purchaseDate</p>
              </div>
              <div className="flex items-center gap-2">
                <Input type="file" accept=".csv,text/csv" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />
                <Button type="button" variant="outline" disabled={!file || upload.isPending} onClick={() => upload.mutate(false)}><Upload className="mr-2 h-4 w-4" />Preview</Button>
                <Button type="button" disabled={!file || upload.isPending} onClick={() => upload.mutate(true)}>Import</Button>
              </div>
            </div>
            {preview && (
              <div className="mt-4 overflow-hidden rounded-md border">
                <div className="bg-secondary px-3 py-2 text-sm font-medium">CSV preview: {preview.validCount} valid, {preview.invalidCount} invalid</div>
                <div className="max-h-56 overflow-auto">
                  <table className="w-full text-xs">
                    <tbody>
                      {preview.records.slice(0, 8).map((record: any) => (
                        <tr key={record.rowNumber} className="border-t">
                          <td className="p-2">Row {record.rowNumber}</td>
                          <td className="p-2">{record.data?.customerId ?? "-"}</td>
                          <td className="p-2">{record.data?.amount ? money(record.data.amount) : "-"}</td>
                          <td className={record.valid ? "p-2 text-accent" : "p-2 text-destructive"}>{record.valid ? "Valid" : record.errors.join(", ")}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </section>

          <section className="overflow-hidden rounded-lg border bg-card">
            <table className="w-full text-sm">
              <thead className="bg-secondary text-left">
                <tr><th className="p-3">Customer</th><th className="p-3">Amount</th><th className="p-3">Category</th><th className="p-3">Purchase Date</th><th className="p-3 text-right">Actions</th></tr>
              </thead>
              <tbody>
                {(orders.data?.content ?? []).map((order) => (
                  <tr key={order.id} className="border-t">
                    <td className="p-3 font-medium">{order.customerName}</td>
                    <td className="p-3">{money(order.amount)}</td>
                    <td className="p-3">{order.category}</td>
                    <td className="p-3">{shortDate(order.purchaseDate)}</td>
                    <td className="p-3">
                      <div className="flex justify-end gap-2">
                        <Button variant="outline" size="sm" onClick={() => { setEditing(order); setForm({ customerId: order.customerId, amount: order.amount, category: order.category, purchaseDate: order.purchaseDate }); }}>Edit</Button>
                        <Button variant="destructive" size="sm" onClick={() => remove.mutate(order.id)}><Trash2 className="h-4 w-4" /></Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pager page={page} totalPages={orders.data?.totalPages ?? 1} setPage={setPage} />
          </section>
        </div>
      </section>
    </AppShell>
  );
}

function Field({ label, value, onChange, type = "text" }: { label: string; value: string; onChange: (value: string) => void; type?: string }) {
  return <div className="space-y-2"><Label>{label}</Label><Input type={type} value={value} onChange={(event) => onChange(event.target.value)} required /></div>;
}

function Pager({ page, totalPages, setPage }: { page: number; totalPages: number; setPage: (page: number) => void }) {
  return <div className="flex items-center justify-end gap-2 border-t p-3"><Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</Button><span className="text-sm text-muted-foreground">Page {page + 1} of {Math.max(totalPages, 1)}</span><Button variant="outline" size="sm" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Next</Button></div>;
}
